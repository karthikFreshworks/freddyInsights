package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIDialogueCentralResponsePayload extends AISprinklerCentralResponsePayload {
    private String dialogueId;
    private String handlerType;
    private String intentHandlerId;
    private String intentHandlerStartTime;
    private String intentHandlerEndTime;
    private String intentHandlerDuration;
    private String intentHandlerResponseCode;
    private String intentHandlerErrorMessage;
    private String intentHandlerStatusCode;
    private String intentHandlerResponseChronology;
    private String intenthandlerSelector;
    private String dialogueResponseCode;
    private String dialogueSelectedIntentHandlerIds;
    private String dialogueResponse;
    private final int intentHandlersCount;
    private final boolean isFallbackIntentHandler;

    public AIDialogueCentralResponsePayload() {
        super();
        this.dialogueId = MDC.get(ObservabilityConstant.X_FW_DIALOGUE_ID);
        this.intentHandlerId = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ID);
        this.intentHandlerStartTime = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_START_TIME);
        this.intentHandlerEndTime = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_END_TIME);
        this.intentHandlerDuration = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_DURATION);
        this.intentHandlerResponseCode = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CODE);
        this.intentHandlerErrorMessage = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ERROR);
        this.intentHandlerStatusCode = MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_STATUS_CODE);
        this.dialogueResponseCode = MDC.get(ObservabilityConstant.DIALOGUE_RESPONSE_CODE);
        this.handlerType = MDC.get(ObservabilityConstant.DIALOGUE_HANDLER_TYPE);
        this.intentHandlerResponseChronology =
                MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CHRONOLOGY);
        this.intenthandlerSelector = MDC.get(ObservabilityConstant.INTENT_HANDLER_SELECTOR);
        this.dialogueSelectedIntentHandlerIds = MDC.get(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS);
        this.intentHandlersCount = calculateNumberOfIntentHandlers(this.dialogueSelectedIntentHandlerIds);
        this.isFallbackIntentHandler = isFallbackIntentHandler(this.dialogueSelectedIntentHandlerIds);
    }

    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId == null ? MDC.get(ObservabilityConstant.X_FW_DIALOGUE_ID) : dialogueId;
    }

    public void setIntentHandlerId(String intentHandlerId) {
        this.intentHandlerId = intentHandlerId == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ID) : intentHandlerId;
    }

    public void setIntentHandlerStartTime(String intentHandlerStartTime) {
        this.intentHandlerStartTime = intentHandlerStartTime == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_START_TIME) : intentHandlerStartTime;
    }

    public void setIntentHandlerEndTime(String intentHandlerEndTime) {
        this.intentHandlerEndTime = intentHandlerEndTime == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_END_TIME) : intentHandlerEndTime;
    }

    public void setIntentHandlerDuration(String intentHandlerDuration) {
        this.intentHandlerDuration = intentHandlerDuration == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_DURATION) :
                intentHandlerDuration;
    }

    public void setIntentHandlerResponseCode(String intentHandlerResponseCode) {
        this.intentHandlerResponseCode = intentHandlerResponseCode == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CODE) : intentHandlerResponseCode;
    }

    public void setIntentHandlerErrorMessage(String intentHandlerErrorMessage) {
        this.intentHandlerErrorMessage = intentHandlerErrorMessage == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ERROR) : intentHandlerErrorMessage;
    }

    public void setIntentHandlerStatusCode(String intentHandlerStatusCode) {
        this.intentHandlerStatusCode = intentHandlerStatusCode == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_STATUS_CODE) : intentHandlerStatusCode;
    }

    public void setDialogueResponseCode(String dialogueResponseCode) {
        this.dialogueResponseCode = dialogueResponseCode == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_RESPONSE_CODE) : dialogueResponseCode;
    }

    public void setHandlerType(String handlerType) {
        this.handlerType = handlerType == null ? MDC.get(ObservabilityConstant.DIALOGUE_HANDLER_TYPE) : handlerType;
    }

    public void setIntentHandlerResponseChronology(String intentHandlerResponseChronology) {
        this.intentHandlerResponseChronology = intentHandlerResponseChronology == null
                ? MDC.get(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CHRONOLOGY) :
                intentHandlerResponseChronology;
    }

    public void setIntenthandlerSelector(String intenthandlerSelector) {
        this.intenthandlerSelector = intenthandlerSelector == null
                ? MDC.get(ObservabilityConstant.INTENT_HANDLER_SELECTOR) : intenthandlerSelector;
    }

    public void setDialogueSelectedIntentHandlerIds(String dialogueSelectedIntentHandlerIds) {
        this.dialogueSelectedIntentHandlerIds = dialogueSelectedIntentHandlerIds == null
                ? MDC.get(ObservabilityConstant.SELECTED_INTENT_HANDLER_IDS) : dialogueSelectedIntentHandlerIds;
    }

    public void setResponse(String dialogueResponse) {
        this.dialogueResponse = dialogueResponse;
    }

    private int calculateNumberOfIntentHandlers(String dialogueSelectedIntentHandlerIds) {
        if (dialogueSelectedIntentHandlerIds == null || dialogueSelectedIntentHandlerIds.isEmpty()) {
            return 0;
        }
        String[] intentHandlers = dialogueSelectedIntentHandlerIds.split("->");
        return intentHandlers.length;
    }

    private boolean isFallbackIntentHandler(String dialogueSelectedIntentHandlerIds) {
        if (dialogueSelectedIntentHandlerIds == null || dialogueSelectedIntentHandlerIds.isEmpty()) {
            return false;
        }
        String[] intentHandlers = dialogueSelectedIntentHandlerIds.split("->");
        return intentHandlers[intentHandlers.length - 1].contains("fallback");
    }
}
