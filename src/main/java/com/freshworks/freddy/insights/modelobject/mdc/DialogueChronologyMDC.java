package com.freshworks.freddy.insights.modelobject.mdc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * DialogueChronologyMDC is a class that encapsulates information related to the chronology of a dialogue
 * for logging purposes. It retrieves values from the Mapped Diagnostic Context (MDC) and provides a JSON
 * representation of the encapsulated data.
 *
 * <p>The class includes information such as dialogue type, intent handler selector, selected intent handler IDs,
 * intent handler responses, and dialogue response code.
 */
@Slf4j
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DialogueChronologyMDC extends AbstractChronologyMDC {
    /**
     * The type of the dialogue.
     */
    private final String dialogueTypeFlow;

    /**
     * The intent handler selector associated with the dialogue.
     */
    private final String intentHandlerSelectorFlow;

    /**
     * The selected intent handler IDs for the dialogue.
     */
    private final String selectedIntentHandlerIdsFlow;
    /**
     * The response code of the dialogue.
     */
    private final String dialogueResponseCode;
    /**
     * The map containing intent handler responses associated with the dialogue.
     */
    private Map<String, Object> intentHandlerResponses;

    /**
     * Constructs a DialogueChronologyMDC object by retrieving values from the MDC.
     * Initializes dialogueType, intentHandlerSelector, selectedIntentHandlerIds, intentHandlerResponses,
     * and dialogueResponseCode based on MDC values.
     */
    public DialogueChronologyMDC() {
        dialogueTypeFlow = MDC.get(ObservabilityConstant.DIALOGUE_HANDLER_TYPE);
        intentHandlerSelectorFlow = MDC.get(ObservabilityConstant.INTENT_HANDLER_SELECTOR);
        selectedIntentHandlerIdsFlow = MDC.get(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS);

        try {
            String json = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CHRONOLOGY);
            if (json != null && !json.isEmpty()) {
                intentHandlerResponses = ObjectMapperHelper.readMapOfStrings(json);
            }
        } catch (Exception e) {
            log.error("Error processing JSON of dialogue intent handler response chronology: {}, CAUSE: {}",
                    e.getMessage(), ExceptionHelper.stackTrace(e));
        }
        dialogueResponseCode = MDC.get(ObservabilityConstant.DIALOGUE_RESPONSE_CODE);
    }

    /**
     * Converts the DialogueChronologyMDC object to its JSON representation.
     *
     * @return JSON representation of the object.
     */
    @Override
    public String toJson() {
        return ObjectMapperHelper.toJson(this, false, "Error parsing DialogueChronologyMDC");
    }
}
