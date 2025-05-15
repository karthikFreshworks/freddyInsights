package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIDialogueCentralRequestPayload extends AISprinklerCentralRequestPayload {
    private String crmCloudType;
    private String method;
    private String request;
    private String dialogueId;
    private String apiUrl;
    private String requestTime;

    public AIDialogueCentralRequestPayload() {
        super();
        this.crmCloudType = MDC.get(ObservabilityConstant.X_FW_CLOUD_TYPE);
        this.method = MDC.get(ObservabilityConstant.METHOD);
        this.request = MDC.get(ObservabilityConstant.DIALOGUE_REQUEST);
        this.dialogueId = MDC.get(ObservabilityConstant.X_FW_DIALOGUE_ID);
        this.apiUrl = MDC.get(ObservabilityConstant.URI);
        this.requestTime = requestTime == null ? MDC.get(ObservabilityConstant.START_TIME) : requestTime;
    }

    public void setCrmCloudType(String crmCloudType) {
        this.crmCloudType = crmCloudType == null ? MDC.get(ObservabilityConstant.X_FW_CLOUD_TYPE) : crmCloudType;
    }

    public void setMethod(String method) {
        this.method = method == null ? MDC.get(ObservabilityConstant.METHOD) : method;
    }

    public void setRequest(String request) {
        this.request = request == null ? MDC.get(ObservabilityConstant.LLM_REQUEST) : request;
    }

    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId == null ? MDC.get(ObservabilityConstant.X_FW_DIALOGUE_ID) : dialogueId;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl == null ? MDC.get(ObservabilityConstant.URI) : apiUrl;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime == null ? MDC.get(ObservabilityConstant.START_TIME) : requestTime;
    }
}
