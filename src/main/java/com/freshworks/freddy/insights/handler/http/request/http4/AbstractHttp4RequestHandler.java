package com.freshworks.freddy.insights.handler.http.request.http4;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.http.request.RequestHandler;
import com.freshworks.freddy.insights.helper.AwsHttpRequestHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractHttp4RequestHandler<A, R> implements RequestHandler<A, R> {
    protected AwsHttpRequestHelper awsHttpRequestHelper;

    @Autowired
    public void setAwsHttpRequestHelper(AwsHttpRequestHelper awsHttpRequestHelper) {
        this.awsHttpRequestHelper = awsHttpRequestHelper;
    }

    public HttpEntity buildApacheHttp4MultipartEntity(AIServiceMO aiServiceMO) {
        List<MultipartFile> files = aiServiceMO.getMultipartFiles();
        Map<String, Object> formData = aiServiceMO.getTemplatedRuleBodyMap();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        Optional.ofNullable(formData).ifPresent(map ->
                map.forEach((key, value) -> Optional.ofNullable(value)
                        .filter(v -> !String.valueOf(v).isEmpty())
                        .ifPresent(v -> builder.addTextBody(key, String.valueOf(v), ContentType.TEXT_PLAIN))));

        try {
            Optional.ofNullable(files).ifPresent(list ->
                    list.forEach(file -> Optional.ofNullable(file)
                            .filter(f -> !f.isEmpty())
                            .ifPresent(f -> {
                                String contentType = f.getContentType() != null
                                        ? f.getContentType() : "application/octet-stream";
                                try {
                                    builder.addBinaryBody("file", f.getBytes(), ContentType.parse(contentType),
                                            f.getOriginalFilename());
                                } catch (IOException e) {
                                    handleIOException(e);
                                }
                            })));
        } catch (Exception e) {
            handleException(e);
        }
        return builder.build();
    }

    private void handleIOException(IOException e) {
        String message = String.format("Error building multipart body: %s", e.getMessage());
        log.error(message + " CAUSE : ", ExceptionHelper.stackTrace(e));
        throw new AIResponseStatusException(
                message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void handleException(Exception e) {
        String message = String.format("Error building multipart body: %s", e.getMessage());
        log.error(message + " CAUSE : ", ExceptionHelper.stackTrace(e));
        throw new AIResponseStatusException(
                message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    protected org.apache.http.Header[] apacheHttp4Headers(String[] headers) {
        if (headers.length % 2 != 0) {
            return new org.apache.http.Header[0];
        }
        org.apache.http.Header[] apacheHeaders = new org.apache.http.Header[headers.length / 2];

        for (int i = 0; i < headers.length; i += 2) {
            String key = headers[i].trim();
            String value = headers[i + 1].trim();
            apacheHeaders[i / 2] = new org.apache.http.message.BasicHeader(key, value);
        }
        return apacheHeaders;
    }

    protected org.apache.http.client.methods.HttpUriRequest createHtt4Request(
            AIServiceMO aiServiceMO, String httpMethod, HttpEntity entity) {
        if (aiServiceMO.getPlatform() == PlatformEnum.amazon) {
            return awsHttpRequestHelper.signAwsRequest(aiServiceMO, httpMethod, entity);
        }

        RequestBuilder requestBuilder = RequestBuilder.create(httpMethod).setUri(aiServiceMO.getUrl());
        if (entity != null) {
            requestBuilder.setEntity(entity);
        }
        Arrays.stream(apacheHttp4Headers(aiServiceMO.getRemoteRequestHeaders())).forEach(requestBuilder::addHeader);
        return requestBuilder.build();
    }

    public static class CustomHttp4Entity extends HttpEntityWrapper {
        private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("Content-Length: \\d+",
                Pattern.CASE_INSENSITIVE);

        public CustomHttp4Entity(HttpEntity httpEntity) {
            super(httpEntity);
        }

        @Override
        public long getContentLength() {
            return -1;
        }

        @Override
        public void writeTo(java.io.OutputStream outstream) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            wrappedEntity.writeTo(output);

            Charset charset = StandardCharsets.ISO_8859_1;
            ContentType contentType = ContentType.getOrDefault(wrappedEntity);
            if (contentType.getCharset() != null) {
                charset = contentType.getCharset();
            }

            String bodyWithoutContentLengthHeaders = CONTENT_LENGTH_PATTERN.matcher(
                    output.toString(charset)
            ).replaceAll("");

            outstream.write(bodyWithoutContentLengthHeaders.getBytes(charset));
        }
    }
}
