package com.freshworks.freddy.insights.handler.observability;

import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component(ObservabilityConstant.SERVICE_RUN_OBSERVABILITY)
public class AIServiceRunObservabilityHandlerImpl extends AbstractObservabilityHandler {
    private static final String LLM_RESPONSE_TIME_METRIC = "llm.response.time";
    private static final String COMPLETION_TOKEN_COUNTER = "completion.token";
    private static final String PROMPT_TOKEN_COUNTER = "prompt.token";
    private static final String TOTAL_TOKEN_COUNTER = "total.token";
    private final AICommonObservabilityHandlerImpl aiCommonObservabilityHandler;

    @Autowired
    public AIServiceRunObservabilityHandlerImpl(AICommonObservabilityHandlerImpl aiCommonObservabilityHandler) {
        this.aiCommonObservabilityHandler = aiCommonObservabilityHandler;
    }

    @Override
    public StringBuilder getLogBuilder() {
        StringBuilder sb = aiCommonObservabilityHandler.getLogBuilder();
        sb.append(DELIMITER)
                .append(ObservabilityConstant.SRVC)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.SERVICE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.PTFRM)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.PLATFORM, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.MDL)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MODEL, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.MDL_URL)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MODEL_URL, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_RES_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_RESPONSE_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_RES_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_RESPONSE_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.PMT_TKN)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.PROMPT_TOKENS, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.CMPL_TKN)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.COMPLETION_TOKENS, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.TTL_TKN)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.TOTAL_TOKENS, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_RES_CRNLGY)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_ERR_MSG)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_ERROR_MESSAGE, EMPTY_STRING));
        return sb;
    }

    @Override
    public void recordMetrics() {
        aiCommonObservabilityHandler.recordMetrics();

        String tenantName = getOrDefault(ObservabilityConstant.TENANT, NA_STRING);
        String modelName = getOrDefault(ObservabilityConstant.MODEL, NA_STRING);
        String modelUrl = getOrDefault(ObservabilityConstant.MODEL_URL, NA_STRING);
        String platform = getOrDefault(ObservabilityConstant.PLATFORM, NA_STRING);
        String llmStatusCode = getOrDefault(ObservabilityConstant.LLM_RESPONSE_CODE, NA_STRING);
        String llmResponseTime = getOrDefault(ObservabilityConstant.LLM_RESPONSE_TIME, ZERO_STRING);
        String service = getOrDefault(ObservabilityConstant.SERVICE, NA_STRING);

        MetricBuilder builder = new MetricBuilder(LLM_RESPONSE_TIME_METRIC)
                .withTag(ObservabilityConstant.TENANT, tenantName)
                .withTag(ObservabilityConstant.MODEL, modelName)
                // .withTag(ObservabilityConstant.MODEL_URL, modelUrl) commented for scalability issue
                .withTag(ObservabilityConstant.PLATFORM, platform)
                .withTag(ObservabilityConstant.LLM_RESPONSE_CODE, llmStatusCode)
                .withTag(ObservabilityConstant.SERVICE, service);

        builder.buildTimer().record(Duration.ofMillis(Long.parseLong(llmResponseTime)));

        try {
            String completionToken = getOrDefault(ObservabilityConstant.COMPLETION_TOKENS, ZERO_STRING);
            int completionTokenNum = Integer.parseInt(completionToken);
            if (completionTokenNum > 0) {
                recordCounter(COMPLETION_TOKEN_COUNTER, tenantName, modelName, modelUrl, platform, service,
                        completionToken);
            }
        } catch (NumberFormatException ignored) {
            // Ignoring NumberFormatException as completionToken is not critical for further processing
        }

        recordCounter(PROMPT_TOKEN_COUNTER, tenantName, modelName, modelUrl, platform, service,
                getOrDefault(ObservabilityConstant.PROMPT_TOKENS, ZERO_STRING));
        recordCounter(TOTAL_TOKEN_COUNTER, tenantName, modelName, modelUrl, platform, service,
                getOrDefault(ObservabilityConstant.TOTAL_TOKENS, ZERO_STRING));
    }

    private void recordCounter(String counterName, String tenantName, String modelName, String modelUrl,
                               String platform, String service, String value) {
        MetricBuilder builder = new MetricBuilder(counterName)
                .withTag(ObservabilityConstant.TENANT, tenantName)
                .withTag(ObservabilityConstant.MODEL, modelName)
                // .withTag(ObservabilityConstant.MODEL_URL, modelUrl) commented for scalability issue
                .withTag(ObservabilityConstant.PLATFORM, platform)
                .withTag(ObservabilityConstant.SERVICE, service);
        builder.buildCounter().increment(Double.parseDouble(value));
    }
}
