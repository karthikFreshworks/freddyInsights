package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AISprinklerCentralRequestPayload extends AISprinklerCentralBasePayload {
    private String language;
    private String llmRequest;

    public AISprinklerCentralRequestPayload() {
        super();
        this.language = LanguageCodeEnum.en.name();
        this.llmRequest = MDC.get(ObservabilityConstant.LLM_REQUEST);
    }

    public void setLanguage(String language) {
        this.language = language == null ? LanguageCodeEnum.en.name() : language;
    }

    public void setLlmRequest(String llmRequest) {
        this.llmRequest = llmRequest == null ? MDC.get(ObservabilityConstant.LLM_REQUEST) : llmRequest;
    }
}
