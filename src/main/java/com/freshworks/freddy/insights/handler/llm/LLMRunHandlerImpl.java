package com.freshworks.freddy.insights.handler.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.handler.AbstractAIHandler;
import com.freshworks.freddy.insights.handler.ErrorHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.google.common.base.Strings;
import com.octomix.josson.Josson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Slf4j
@RequiredArgsConstructor
@Component(AIHandlerConstant.AI_SERVICE_RUN_STRATEGY)
public class LLMRunHandlerImpl extends AbstractAIHandler<AIServiceMO, AIServiceMO> {
    private final ErrorHandler errorHandler;

    public AIServiceMO executeStrategy(AIServiceMO aiServiceMO) throws AIResponseStatusException {
        AtomicReference<Instant> startTime = new AtomicReference<>(Instant.now());
        Josson jossonJson = null;
        return executeRunService(aiServiceMO, startTime, jossonJson);
    }

    private AIServiceMO executeRunService(AIServiceMO aiServiceMO, AtomicReference<Instant> startTime,
                                                          Josson jossonJson) {
        try {
            this.setMdcContext(aiServiceMO);
            String httpRequest = isAmazonSingInRequests(aiServiceMO.getPlatform(), aiServiceMO.getMethod())
                    ? AIHandlerConstant.APACHE_HTTP4_SYNC_REQUEST : AIHandlerConstant.APACHE_SYNC_REQUEST;
            CustomHttpResponse<String> response =
                    super.syncRequestHandler.get(httpRequest).execute(aiServiceMO, aiServiceMO.getMethod());
            Map<String, String> runServiceResponseHeaders = response.getHeaders();
            var requiredResponseHeaders = getRequiredResponseHeaders(runServiceResponseHeaders,
                    aiServiceMO.getExternalResponseHeaders());
            aiServiceMO.setRunServiceResponseHeader(requiredResponseHeaders);
            int statusCode = response.getStatusCode();
            String llmResponseBody = response.getBody();

            this.updateMdcWithResponseInfo(startTime.get(), statusCode, llmResponseBody, aiServiceMO.isCot());

            if (Strings.isNullOrEmpty(llmResponseBody)) {
                log.warn(String.format("AIServiceRunHandlerImpl: Empty LLM response received=%s with status code=%s",
                        llmResponseBody, statusCode));
                aiServiceMO.setLlmResponse(llmResponseBody);
            } else {
                JsonNode llmJsonNodeResponse;
                try {
                    jossonJson = Josson.fromJsonString(llmResponseBody);
                    llmJsonNodeResponse = AIServiceHelper.parseJson(jossonJson, aiServiceMO.getResponseParser());
                } catch (Exception e) {
                    llmJsonNodeResponse = null;
                }

                if (llmJsonNodeResponse == null) {
                    throw new AIResponseStatusException(String.format(
                            "Failed to parse the llm response using the specified response parser (%s). Please "
                                    + "review the provided response parser or the response received from LLM: %s",
                            aiServiceMO.getResponseParser(), llmResponseBody), HttpStatus.NOT_ACCEPTABLE);
                }

                if (llmJsonNodeResponse.elements().hasNext()) {
                    aiServiceMO.setLlmResponse(llmJsonNodeResponse);
                } else {
                    aiServiceMO.setLlmResponse(llmJsonNodeResponse.asText());
                }
            }
            return aiServiceMO;
        } catch (Throwable ex) {
            log.error("There was error executing run-service. CAUSE: {}", ExceptionHelper.stackTrace(ex));
            throw this.handleException(ex, startTime.get(), aiServiceMO);
        } finally {
            mdcHelper.updateMDCWithLLMUsage(jossonJson, aiServiceMO);
            mdcHelper.sprinklerResponseChronology(aiServiceMO);
        }
    }

    private boolean isAmazonSingInRequests(PlatformEnum platformEnum, ApiMethodEnum methodEnum) {
        List<String> http4Methods = List.of("http4_put",
                "http4_post",
                "http4_get",
                "http4_delete",
                "htt4_multipart_put",
                "http4_multipart_post");
        return http4Methods.contains(methodEnum.name()) && PlatformEnum.amazon.name().equals(platformEnum.name());
    }

    private Map<String, String> getRequiredResponseHeaders(Map<String, String> runServiceResponseHeaders,
                                                           List<AIServiceBaseDTO.ExternalResponseHeader>
                                                                   externalResponseHeaders) {
        Map<String, String> headersMap = new HashMap<>();
        if (externalResponseHeaders != null) {
            for (AIServiceBaseDTO.ExternalResponseHeader header : externalResponseHeaders) {
                var requiredHeader = header.getKey();
                if (requiredHeader != null && runServiceResponseHeaders.containsKey(requiredHeader)) {
                    var responseHeader = defaultIfNull(header.getRename(), requiredHeader);
                    var value = runServiceResponseHeaders.get(requiredHeader);
                    var responseHeaderValue = header.getTransformationParser() != null
                            ? getParsedValue(header.getTransformationParser(), value) : value;
                    headersMap.put(responseHeader, responseHeaderValue);
                }
            }
        }
        return headersMap;
    }

    private String getParsedValue(String parser, String value) throws AIResponseStatusException {
        try {
            Josson josson = Josson.from(value);
            return josson.getNode(parser).toString();
        } catch (Exception e) {
            throw new AIResponseStatusException(String.format(
                    "Failed to parse the value (%s) using the josson parser (%s)",
                    parser, value), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private void setMdcContext(AIServiceMO aiServiceMO) {
        MDC.put(ObservabilityConstant.MODEL, aiServiceMO.getModel());
        MDC.put(ObservabilityConstant.MODEL_URL, aiServiceMO.getUrl());
        MDC.put(ObservabilityConstant.SERVICE, aiServiceMO.getService());
        MDC.put(ObservabilityConstant.PLATFORM, aiServiceMO.getPlatform().name());
        if (MediaType.APPLICATION_JSON_VALUE.equals(aiServiceMO.getAiServiceMediaType())) {
            MDC.put(AIServiceHelper.getPromptKey(aiServiceMO.isCot()), aiServiceMO.getRemoteRequestBody().toString());
        }
    }

    private void updateMdcWithResponseInfo(Instant startTime, int statusCode, String responseBody, boolean isCot) {
        MDC.put(AIServiceHelper.getResponseTimeKey(isCot),
                String.valueOf(Duration.between(startTime, Instant.now()).toMillis()));
        MDC.put(AIServiceHelper.getResponseCodeKey(isCot), String.valueOf(statusCode));
        String responseContextKey = statusCode > 299
                ? AIServiceHelper.getErrorMessageKey(isCot) : AIServiceHelper.getResponseKey(isCot);
        MDC.put(responseContextKey, responseBody);
    }

    private AIResponseStatusException handleException(Throwable ex, Instant startTime, AIServiceMO aiService) {
        AIResponseStatusException aiResponseStatusException;
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AIResponseStatusException) {
            aiResponseStatusException = errorHandler.resolveErrors((AIResponseStatusException) cause, aiService);
        } else {
            aiResponseStatusException = new AIResponseStatusException(
                    ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        this.updateMdcWithResponseInfo(
                startTime, aiResponseStatusException.getStatusCode().value(), aiResponseStatusException.getReason(),
                aiService.isCot());
        throw aiResponseStatusException;
    }
}
