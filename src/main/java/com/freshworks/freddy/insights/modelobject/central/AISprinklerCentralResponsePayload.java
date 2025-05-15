package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AISprinklerCentralResponsePayload extends AISprinklerCentralBasePayload {
    private String status;
    private String language;
    private String errorMessage;
    private String startTime;
    private String endTime;
    private String duration;
    private String llmService;
    private String llmPlatform;
    private String llmPrompt;
    private String llmResponse;
    private String llmModel;
    private String llmModelUrl;
    private String llmResponseDuration;
    private String llmResponseCode;
    private String llmErrorMessage;
    private String llmPromptToken;
    private String llmCompletionToken;
    private String llmTotalToken;
    private String documentdbResponseTime;
    private String redisResponseTime;
    private String llmCotPrompt;
    private String llmCotResponse;
    private String llmCotResponseDuration;
    private String llmCotResponseCode;
    private String llmCotErrorMessage;
    private String llmResponseChronology;

    public AISprinklerCentralResponsePayload() {
        super();
        this.status = MDC.get(ObservabilityConstant.STATUS);
        //this will be updated later
        this.language = language == null ? LanguageCodeEnum.en.toString() : language;
        this.errorMessage = MDC.get(ObservabilityConstant.ERROR);
        this.startTime = MDC.get(ObservabilityConstant.START_TIME);
        this.endTime = MDC.get(ObservabilityConstant.END_TIME);
        this.duration = MDC.get(ObservabilityConstant.DURATION);
        this.llmService = MDC.get(ObservabilityConstant.SERVICE);
        this.llmPlatform = MDC.get(ObservabilityConstant.PLATFORM);
        this.llmPrompt = MDC.get(ObservabilityConstant.LLM_PROMPT);
        this.llmResponse = MDC.get(ObservabilityConstant.LLM_RESPONSE);
        this.llmModel = MDC.get(ObservabilityConstant.MODEL);
        this.llmModelUrl = MDC.get(ObservabilityConstant.MODEL_URL);
        this.llmResponseDuration = MDC.get(ObservabilityConstant.LLM_RESPONSE_TIME);
        this.llmResponseCode = MDC.get(ObservabilityConstant.LLM_RESPONSE_CODE);
        this.llmErrorMessage = MDC.get(ObservabilityConstant.LLM_ERROR_MESSAGE);
        this.llmPromptToken = MDC.get(ObservabilityConstant.PROMPT_TOKENS);
        this.llmCompletionToken = MDC.get(ObservabilityConstant.COMPLETION_TOKENS);
        this.llmTotalToken = MDC.get(ObservabilityConstant.TOTAL_TOKENS);
        this.documentdbResponseTime = MDC.get(ObservabilityConstant.MONGODB_DURATION);
        this.redisResponseTime = MDC.get(ObservabilityConstant.REDIS_DURATION);
        this.llmCotPrompt = MDC.get(ObservabilityConstant.LLM_COT_PROMPT);
        this.llmCotResponse = MDC.get(ObservabilityConstant.LLM_COT_RESPONSE);
        this.llmCotResponseDuration = MDC.get(ObservabilityConstant.LLM_COT_RESPONSE_TIME);
        this.llmCotResponseCode = MDC.get(ObservabilityConstant.LLM_COT_RESPONSE_CODE);
        this.llmCotErrorMessage = MDC.get(ObservabilityConstant.LLM_COT_ERROR_MESSAGE);
        this.llmResponseChronology = MDC.get(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY);
    }

    public void setStatus(String status) {
        this.status = status == null ? MDC.get(ObservabilityConstant.STATUS) : status;
    }

    public void setLanguage(String language) {
        this.language = language == null ? LanguageCodeEnum.en.toString() : language;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? MDC.get(ObservabilityConstant.ERROR) : errorMessage;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? MDC.get(ObservabilityConstant.START_TIME) : startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? MDC.get(ObservabilityConstant.END_TIME) : endTime;
    }

    public void setDuration(String duration) {
        this.duration = duration == null ? MDC.get(ObservabilityConstant.DURATION) : duration;
    }

    public void setLlmService(String llmService) {
        this.llmService = llmService == null ? MDC.get(ObservabilityConstant.SERVICE) : llmService;
    }

    public void setLlmPlatform(String llmPlatform) {
        this.llmPlatform = llmPlatform == null ? MDC.get(ObservabilityConstant.PLATFORM) : llmPlatform;
    }

    public void setLlmPrompt(String llmPrompt) {
        this.llmPrompt = llmPrompt == null ? MDC.get(ObservabilityConstant.LLM_PROMPT) : llmPrompt;
    }

    public void setLlmResponse(String llmResponse) {
        this.llmResponse = llmResponse == null ? MDC.get(ObservabilityConstant.LLM_RESPONSE) :
                llmResponse;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel == null ? MDC.get(ObservabilityConstant.MODEL) : llmModel;
    }

    public void setLlmModelUrl(String llmModelUrl) {
        this.llmModelUrl = llmModelUrl == null ? MDC.get(ObservabilityConstant.MODEL_URL) : llmModelUrl;
    }

    public void setLlmResponseDuration(String llmResponseDuration) {
        this.llmResponseDuration = llmResponseDuration == null
                ? MDC.get(ObservabilityConstant.LLM_RESPONSE_TIME) : llmResponseDuration;
    }

    public void setLlmResponseCode(String llmResponseCode) {
        this.llmResponseCode = llmResponseCode == null
                ? MDC.get(ObservabilityConstant.LLM_RESPONSE_CODE) : llmResponseCode;
    }

    public void setLlmErrorMessage(String llmErrorMessage) {
        this.llmErrorMessage = llmErrorMessage == null ? MDC.get(ObservabilityConstant.LLM_ERROR_MESSAGE) :
                llmErrorMessage;
    }

    public void setLlmPromptToken(String llmPromptToken) {
        this.llmPromptToken = llmPromptToken == null ? MDC.get(ObservabilityConstant.PROMPT_TOKENS) :
                llmPromptToken;
    }

    public void setLlmCompletionToken(String llmCompletionToken) {
        this.llmCompletionToken = llmCompletionToken == null
                ? MDC.get(ObservabilityConstant.COMPLETION_TOKENS) : llmCompletionToken;
    }

    public void setLlmTotalToken(String llmTotalToken) {
        this.llmTotalToken = llmTotalToken == null ? MDC.get(ObservabilityConstant.TOTAL_TOKENS) : llmTotalToken;
    }

    public void setDocumentdbResponseTime(String documentdbResponseTime) {
        this.documentdbResponseTime = documentdbResponseTime == null
                ? MDC.get(ObservabilityConstant.MONGODB_DURATION) : documentdbResponseTime;
    }

    public void setRedisResponseTime(String redisResponseTime) {
        this.redisResponseTime = redisResponseTime == null
                ? MDC.get(ObservabilityConstant.REDIS_DURATION) : redisResponseTime;
    }

    public void setLlmCotPrompt(String llmCotPrompt) {
        this.llmCotPrompt = llmCotPrompt == null
                ? MDC.get(ObservabilityConstant.LLM_COT_PROMPT) : llmCotPrompt;
    }

    public void setLlmCotResponse(String llmCotResponse) {
        this.llmCotResponse = llmCotResponse == null
                ? MDC.get(ObservabilityConstant.LLM_COT_RESPONSE) : llmCotResponse;
    }

    public void setLlmCotResponseDuration(String llmCotResponseDuration) {
        this.llmCotResponseDuration = llmCotResponseDuration == null
                ? MDC.get(ObservabilityConstant.LLM_COT_RESPONSE_TIME) : llmCotResponseDuration;
    }

    public void setLlmCotResponseCode(String llmCotResponseCode) {
        this.llmCotResponseCode = llmCotResponseCode == null
                ? MDC.get(ObservabilityConstant.LLM_COT_RESPONSE_CODE) : llmCotResponseCode;
    }

    public void setLlmCotErrorMessage(String llmCotErrorMessage) {
        this.llmCotErrorMessage = llmCotErrorMessage == null
                ? MDC.get(ObservabilityConstant.LLM_COT_ERROR_MESSAGE) : llmCotErrorMessage;
    }

    public void setLlmResponseChronology(String llmResponseChronology) {
        this.llmResponseChronology = llmResponseChronology == null
                ? MDC.get(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY) : llmResponseChronology;
    }
}
