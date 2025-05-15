package com.freshworks.freddy.insights.helper;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.*;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.apache.request.impl.ApacheHttpRequestFactory;
import com.amazonaws.http.settings.HttpClientSettings;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.util.StringUtils;
import com.freshworks.freddy.insights.constant.AIBaseConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AwsHttpRequestHelper {
    public static final String REGION = "region";
    public static final String ROLE = "role";
    public static final String SERVICE = "service";
    public static final String SESSION_PREFIX = "Session";
    public static final String X_AMZ_CONTENT_SHA256 = "X-Amz-Content-Sha256";

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AwsHttpRequestHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public HttpUriRequest signAwsRequest(AIServiceMO aiServiceMO, String method, HttpEntity httpEntity) {
        try {
            validateAIServiceMO(aiServiceMO);
            Credentials credentials = getCachedAwsCredentials(aiServiceMO.getHeader());
            return signRequest(aiServiceMO, method, credentials, httpEntity);
        } catch (AIResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error signing AWS request: {}", ExceptionHelper.stackTrace(e));
            throw new AIResponseStatusException("Failed to sign AWS HTTP request: " + e.getMessage());
        }
    }

    private void validateAIServiceMO(AIServiceMO aiServiceMO) {
        Map<String, String> headers = aiServiceMO.getHeader();
        if (headers == null || headers.isEmpty()) {
            throw new AIResponseStatusException(
                    "Headers are not properly set for AWS service. "
                            + "Please ensure that the region, service, and role are correctly configured.",
                    HttpStatus.BAD_REQUEST);
        }

        Map<String, String> normalizedHeaders = headers.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));

        aiServiceMO.setHeader(normalizedHeaders);

        validateRequiredField(normalizedHeaders, SERVICE);
        validateRequiredField(normalizedHeaders, ROLE);
        validateRegion(normalizedHeaders.get(REGION));
    }

    private void validateRequiredField(Map<String, String> headers, String fieldName) {
        String value = headers.get(fieldName.toLowerCase());
        if (StringUtils.isNullOrEmpty(value)) {
            throw new AIResponseStatusException(
                    "Missing or empty " + fieldName + " in AWS service headers", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRegion(String regionName) {
        if (StringUtils.isNullOrEmpty(regionName)) {
            throw new AIResponseStatusException("Missing or empty AWS region in headers", HttpStatus.BAD_REQUEST);
        }

        try {
            Regions.fromName(regionName);
        } catch (IllegalArgumentException e) {
            throw new AIResponseStatusException("Invalid AWS region: " + regionName, HttpStatus.BAD_REQUEST);
        }
    }

    private HttpUriRequest signRequest(
            AIServiceMO aiServiceMO, String method, Credentials credentials, HttpEntity httpEntity) {
        try {
            final Map<String, String> headers = aiServiceMO.getHeader();
            final URI uri = new URI(aiServiceMO.getUrl());
            final String contentType = headers.getOrDefault(
                    HttpHeaders.CONTENT_TYPE.toLowerCase(), AIBaseConstant.APPLICATION_JSON);

            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName(headers.get(SERVICE));
            signer.setRegionName(headers.get(REGION));

            Request<Void> awsRequest = new DefaultRequest<>(headers.get(SERVICE));
            awsRequest.setHttpMethod(HttpMethodName.fromValue(method));
            awsRequest.setEndpoint(uri);
            awsRequest.addHeader(HttpHeaders.HOST, uri.getHost());
            awsRequest.addHeader(HttpHeaders.CONTENT_TYPE, contentType);

            if (httpEntity != null && httpEntity.getContent() != null) {
                awsRequest.addHeader(X_AMZ_CONTENT_SHA256, DigestUtils.sha256Hex(httpEntity.getContent()));
                awsRequest.setContent(httpEntity.getContent());
            }

            AWSCredentialsProvider credentialsProvider = getCredentialsProvider(credentials);
            signer.sign(awsRequest, credentialsProvider.getCredentials());

            log.debug("AWS Request Headers After Sign: AccessKey: {} , SecretKey: {} , SessionToken: {} , {}",
                    credentials.getAccessKeyId(), credentials.getSecretAccessKey(), credentials.getSessionToken(),
                    awsRequest.getHeaders().entrySet().stream()
                            .map(entry -> entry.getKey() + ": " + entry.getValue())
                            .collect(Collectors.joining(" , ")));

            ClientConfiguration clientConfiguration = new ClientConfiguration().withUseExpectContinue(true);
            return new ApacheHttpRequestFactory().create(awsRequest, HttpClientSettings.adapt(clientConfiguration));
        } catch (Exception e) {
            log.error("Error signing AWS HTTP request sign: {}", ExceptionHelper.stackTrace(e));
            throw new AIResponseStatusException("Failed to sign AWS HTTP request sign: " + e.getMessage());
        }
    }

    private AWSCredentialsProvider getCredentialsProvider(Credentials credentials) {
        String accessKeyId = credentials.getAccessKeyId();
        String secretAccessKey = credentials.getSecretAccessKey();
        String sessionToken = credentials.getSessionToken();

        AWSCredentials awsCredentials = (sessionToken != null)
                ? new BasicSessionCredentials(accessKeyId, secretAccessKey, sessionToken)
                : new BasicAWSCredentials(accessKeyId, secretAccessKey);

        return new AWSStaticCredentialsProvider(awsCredentials);
    }

    private Credentials getCachedAwsCredentials(Map<String, String> headers) {
        String role = headers.get(ROLE);
        Credentials credentials = (Credentials) redisTemplate.opsForValue().get(role);
        Long expire = redisTemplate.getExpire(role);

        if (credentials == null || (expire != null && expire < 0)) {
            credentials = getAwsCredentials(headers);
            redisTemplate.opsForValue().set(role, credentials, 1, TimeUnit.HOURS);
        }
        return credentials;
    }

    private Credentials getAwsCredentials(Map<String, String> headers) {
        try {
            AWSSecurityTokenService sts = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                    .withRegion(headers.get(REGION))
                    .build();
            AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                    .withRoleArn(headers.get(ROLE))
                    .withRoleSessionName(SESSION_PREFIX + System.currentTimeMillis());
            AssumeRoleResult assumeRoleResult = sts.assumeRole(assumeRoleRequest);
            return assumeRoleResult.getCredentials();
        } catch (Exception e) {
            log.error("Error while obtaining AWS session credentials: {}", ExceptionHelper.stackTrace(e));
            throw new AIResponseStatusException("Error while obtaining AWS session credentials: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
