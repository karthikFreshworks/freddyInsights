package com.freshworks.freddy.insights.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.freshworks.freddy.insights.modelobject.mdc.IntentHandlerChronologyMDC;
import com.freshworks.freddy.insights.modelobject.mdc.SprinklerResponseChronologyMDC;
import com.octomix.josson.Josson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MDCHelper extends AbstractAIBaseHelper {
    private static final String USAGE = "usage";
    private static final String INTERMEDIATE = "intermediate";

    /**
     * Updates the Mapped Diagnostic Context (MDC) with token counts for a specific token type based on the provided
     * * usage JSON.
     *
     * @param usage     The JSON node containing token usage information.
     * @param tokenType The type of token for which counts are to be updated in the MDC.
     */
    private void updateTokenCounts(JsonNode usage, String tokenType) {
        Long existingCount = getLongFromMDC(tokenType);
        Long newCount = usage.has(tokenType) ? usage.get(tokenType).asLong() : 0;
        MDC.put(String.format("%s_%s", INTERMEDIATE, tokenType), String.valueOf(newCount));
        MDC.put(tokenType, String.valueOf(existingCount + newCount));
    }

    /**
     * Updates MDC with usage token counts from Josson JSON.
     *
     * @param jossonJson  The Josson JSON object.
     * @param aiServiceMO The AI Service Metadata Object.
     */
    public void updateMDCWithLLMUsage(Josson jossonJson, AIServiceMO aiServiceMO) {
        if (jossonJson != null && isSupportedPlatform(aiServiceMO.getPlatform())) {
            JsonNode usage = jossonJson.getNode(USAGE);
            if (usage != null && !usage.isEmpty()) {
                updateTokenCounts(usage, ObservabilityConstant.PROMPT_TOKENS);
                updateTokenCounts(usage, ObservabilityConstant.COMPLETION_TOKENS);
                updateTokenCounts(usage, ObservabilityConstant.TOTAL_TOKENS);
            }
        }
    }

    /**
     * Retrieves a long value associated with the given key from MDC.
     *
     * @param key The key to retrieve the value from MDC.
     * @return The long value associated with the given key. Returns 0 if key is not found or value is not a valid long.
     */
    public Long getLongFromMDC(String key) {
        String value = MDC.get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * Updates MDC with dialogue type chronology.
     *
     * @param handlerType The handler type.
     */
    public void handlerDialogueTypeChronology(String handlerType) {
        String selectedDialogueType = MDC.get(ObservabilityConstant.DIALOGUE_HANDLER_TYPE);
        if (selectedDialogueType == null) {
            MDC.put(ObservabilityConstant.DIALOGUE_HANDLER_TYPE, handlerType);
        } else {
            MDC.put(ObservabilityConstant.DIALOGUE_HANDLER_TYPE, String.format("%s->%s", selectedDialogueType,
                    handlerType));
        }
    }

    /**
     * Updates MDC with handler selector chronology.
     *
     * @param selector The selector.
     */
    public void handlerSelectorChronology(String selector) {
        String selectors = MDC.get(ObservabilityConstant.INTENT_HANDLER_SELECTOR);
        if (selectors == null) {
            MDC.put(ObservabilityConstant.INTENT_HANDLER_SELECTOR, selector);
        } else {
            MDC.put(ObservabilityConstant.INTENT_HANDLER_SELECTOR, String.format("%s->%s", selectors,
                    selector));
        }
    }

    /**
     * Updates MDC with handler selector IDs chronology.
     *
     * @param selectorId The selector ID.
     */
    public void handlerSelectorIdsChronology(String selectorId) {
        String selectorIds = MDC.get(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS);
        if (selectorIds == null) {
            MDC.put(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS, selectorId);
        } else {
            MDC.put(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS, String.format("%s->%s", selectorIds,
                    selectorId));
        }
    }

    /**
     * Updates MDC with the response chronology of sprinkler.
     *
     * @param aiServiceMO The AI Service Metadata Object.
     */
    public void sprinklerResponseChronology(AIServiceMO aiServiceMO) {
        SprinklerResponseChronologyMDC sprinklerResponse = SprinklerResponseChronologyMDC.builder()
                .isLlmCot(aiServiceMO.isCot())
                .duration(MDC.get(AIServiceHelper.getResponseTimeKey(aiServiceMO.isCot())))
                .statusCode(MDC.get(AIServiceHelper.getResponseCodeKey(aiServiceMO.isCot())))
                .promptTokens(MDC.get(String.format("%s_%s", INTERMEDIATE, ObservabilityConstant.PROMPT_TOKENS)))
                .completionTokens(MDC.get(String.format("%s_%s", INTERMEDIATE,
                        ObservabilityConstant.COMPLETION_TOKENS)))
                .totalTokens(MDC.get(String.format("%s_%s", INTERMEDIATE, ObservabilityConstant.TOTAL_TOKENS)))
                .build();

        String chronology = MDC.get(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY);

        try {
            List<SprinklerResponseChronologyMDC> responses = new ArrayList<>();
            if (chronology != null) {
                responses = ObjectMapperHelper.readValueWithGenericType(chronology);
            }
            responses.add(sprinklerResponse);
            chronology = ObjectMapperHelper.objectToJsonString(responses);
        } catch (Exception e) {
            log.error("Error while converting sprinkler response chronology to JSON: {}, CAUSE: {}", e.getMessage(),
                    ExceptionHelper.stackTrace(e));
            chronology = "{\"error\": \"Error while building sprinkler response chronology\"}";
        }
        MDC.put(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY, chronology);
    }
}
