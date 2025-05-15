package com.freshworks.freddy.insights.handler.llm.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.anonymize.AnonymizeRequestDTO;
import com.freshworks.freddy.insights.dto.anonymize.AnonymizeResponseDTO;
import com.freshworks.freddy.insights.dto.anonymize.DeAnonymizeRequestDTO;
import com.freshworks.freddy.insights.dto.anonymize.DeAnonymizeResponseDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.http.AbstractRequestHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.handler.llm.LLMRunHandlerImpl;
import com.freshworks.freddy.insights.handler.llm.ServiceStreamHandlerImpl;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.freshworks.freddy.insights.constant.AIBaseConstant.TENANT;
import static com.freshworks.freddy.insights.constant.AIRequestConstant.CONTENT_TYPE;
import static com.freshworks.freddy.insights.constant.AIRequestConstant.X_REQUEST_ID;
import static com.freshworks.freddy.insights.constant.AIServiceConstant.ANONYMIZE;
import static com.freshworks.freddy.insights.constant.AIServiceConstant.DEANONYMIZE;

/**
 * A step to anonymize data in a request body.
 * This should be the final step before calling the LLM
 */
@Slf4j
@Component
public class AnonymizeRequestDataStepImpl extends AbstractProcessStep {
    private static final String ANONYMIZE_API_KEY_NAME = "X-Api-Key";
    protected Map<String, AbstractRequestHandler<CustomHttpResponse<String>>> syncRequestHandler;

    @Autowired
    public void setSyncRequestHandler(
            Map<String, AbstractRequestHandler<CustomHttpResponse<String>>> syncRequestHandler) {
        this.syncRequestHandler = syncRequestHandler;
    }

    /**
     * This should be the final step before calling the LLM, Don't change it
     */
    private final LLMRunHandlerImpl llmRunHandler;
    private final ServiceStreamHandlerImpl serviceStreamHandler;

    /**
     * Constructs a new AnonymizeRequestDataStepImpl instance.
     */
    @Autowired
    public AnonymizeRequestDataStepImpl(LLMRunHandlerImpl llmRunHandler,
                                        ServiceStreamHandlerImpl serviceStreamHandler) {
        super(AnonymizeRequestDataStepImpl.class.getSimpleName());
        this.llmRunHandler = llmRunHandler;
        this.serviceStreamHandler = serviceStreamHandler;
    }

    /**
     * Executes the step to anonymize the data in the request body.
     *
     * @param input The input for the step.
     * @throws AIResponseStatusException If an error occurs during step execution.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            getAnonymizedData(input);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while running anonymize service Step: %s, Error Message: %s",
                    stepName, e.getMessage());
        }
    }

    /**
     * Executes the next step in the process.
     *
     * @param input The input for the step.
     * @return Always returns null as there's no next step.
     * @throws AIResponseStatusException If an error occurs during step execution.
     */
    @Override
    protected AIServiceMO executeNextStep(AIServiceMO input) throws AIResponseStatusException {
        var handler = input.getSseEmitter() != null
                ? serviceStreamHandler : llmRunHandler;
        log.debug("Executing next LLM step {}", handler.getClass().getName());
        AIServiceMO aiServiceMOResult = handler.executeStrategy(input);
        // write the logic to call the deAnonymize service and set the data in the resultBody
        try {
            return getDeAnonymizedData(aiServiceMOResult);
        } catch (Exception e) {
            log.info("Exception from the de-anonymize service hence, going with default flow with original data {}",
                    input.getOriginalRequestBody());
            input.setRemoteRequestBody(input.getOriginalRequestBody());
            return handler.executeStrategy(input);
        }
    }

    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        boolean shouldSkip = !isAnonymizeEnabled(input);
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }

    private boolean isAnonymizeEnabled(AIServiceMO input) {
        if (input.getFeatures() != null) {
            var isAnonymizeEnabled = this.appConfigHelper.isAnonymizeEnabled()
                    && input.getEnabledFeature().contains(ANONYMIZE) && input.getFeatures().contains(ANONYMIZE);
            log.info("Anonymize is set to {}", isAnonymizeEnabled);
            return isAnonymizeEnabled;
        }
        return false;
    }

    // logic to call the anonymize service and set the data in the requestBody
    private AIServiceMO getAnonymizedData(AIServiceMO input) {
        input.setOriginalRequestBody(input.getRemoteRequestBody());
        var textData = input.getRemoteRequestBody();
        AnonymizeRequestDTO requestDto =
                AnonymizeRequestDTO.builder().text(textData.toString()).build();
        var payload = objectMapper.valueToTree(requestDto);
        log.info("The request input is {}", textData);
        var url = this.appConfigHelper.getAnonymizeUrl();
        try {
            AnonymizeResponseDTO responseData =
                    callAnonymizeServiceViaFreddyRunServiceApi(payload, url,
                            input.getTenant(), AnonymizeResponseDTO.class, ANONYMIZE);
            if (!responseData.getMeta().getEntities().isEmpty()) {
                input.setRemoteRequestBody(responseData.getAnonymizedText());
                log.info("The anonymized input is {}", responseData.getAnonymizedText());
                input.setAnonymizationId(responseData.getMeta().getAnonymizationId());
                log.info("The request input after anonymizing the data is {}", input.getRemoteRequestBody());
            }
        } catch (Exception e) {
            log.info("Exception from the anonymize service hence, going with default flow with data {}",
                    input.getRemoteRequestBody());
            return input;
        }
        return input;
    }

    // call the deAnonymize service and set the result
    private AIServiceMO getDeAnonymizedData(AIServiceMO aiServiceMOResult) {
        var llmResponse = aiServiceMOResult.getLlmResponse();
        log.info("The request output is {}", llmResponse);
        if (aiServiceMOResult.getAnonymizationId() != null) {
            DeAnonymizeRequestDTO requestDto =
                    DeAnonymizeRequestDTO.builder()
                            .anonymizationId(aiServiceMOResult.getAnonymizationId())
                            .text(llmResponse.toString())
                            .build();
            var payload = objectMapper.valueToTree(requestDto);
            var url = this.appConfigHelper.getDeanonymizeUrl();
            DeAnonymizeResponseDTO responseData =
                    callAnonymizeServiceViaFreddyRunServiceApi(payload, url, aiServiceMOResult.getTenant(),
                            DeAnonymizeResponseDTO.class, DEANONYMIZE);
            log.info("The deanonymized output is {}", responseData.getDeanonymizedText());
            JsonNode responseObject;
            try {
                responseObject = objectMapper.readTree(responseData.getDeanonymizedText());
            } catch (JsonProcessingException e) {
                log.error("AnonymizeServiceParser: unable to parse anonymize service response {}"
                        + ". Cause: {}", responseData.getDeanonymizedText(), ExceptionHelper.stackTrace(e));
                throw new JsonParseException(e);
            }
            aiServiceMOResult.setLlmResponse(responseObject);
            log.info("the request output after deanonymize is {}", aiServiceMOResult);
            return aiServiceMOResult;
        }
        return aiServiceMOResult;
    }

    private AIServiceMO getAiServiceMO(List<String> headers, String jsonString, String url) {
        return AIServiceMO.builder()
                .url(url)
                .remoteRequestHeaders(headers.toArray(String[]::new))
                .remoteRequestBody(jsonString)
                .build();
    }

    public <T> T mapToDTO(String jsonString, Class<T> dtoClass, String service) {
        try {
            return objectMapper.readValue(jsonString, dtoClass);
        } catch (JsonProcessingException e) {
            log.error("AnonymizeServiceParser: unable to parse {} service response {}"
                    + ". Cause: {}", service, jsonString, ExceptionHelper.stackTrace(e));
            throw new JsonParseException(e);
        }
    }

    public <T> T callAnonymizeServiceViaFreddyRunServiceApi(JsonNode payload, String url, TenantEnum requestedTenant,
                                                            Class<T> dtoClass, String service) {
        try {
            List<String> headers = getAnonymizeServiceHeaders(requestedTenant);
            AIServiceMO anonymizeAiServiceMO = getAiServiceMO(headers, payload.toString(), url);
            log.info("The {} request with payload: {} and url: {}", service, payload, url);
            CustomHttpResponse<String> result =
                    syncRequestHandler.get(AIHandlerConstant.APACHE_SYNC_REQUEST)
                            .execute(anonymizeAiServiceMO, ApiMethodEnum.post);
            log.info("Response from service {} is with status code {}", result.getBody(), result.getStatusCode());
            String responseBody = result.getBody();
            // Map the response body to the DTO
            return mapToDTO(responseBody, dtoClass, service);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while running service %s and  Step: %s, Error Message: %s",
                    ANONYMIZE, stepName, e.getMessage());
        }
        return null;
    }

    public List<String> getAnonymizeServiceHeaders(TenantEnum requestedTenant) {
        var anonymizeApiKeyName = this.appConfigHelper.getAnonymizeApiKeyName();
        var anonymizeApiKey = this.appConfigHelper.getFreddyAIPlatformLLMSecrets().get(anonymizeApiKeyName);
        List<String> headers = new ArrayList<>();
        headers.add(CONTENT_TYPE);
        headers.add(MediaType.APPLICATION_JSON_VALUE);
        headers.add(ANONYMIZE_API_KEY_NAME);
        headers.add(anonymizeApiKey != null ? anonymizeApiKey.toString() : "");
        headers.add(TENANT + ": " + requestedTenant);
        headers.add(X_REQUEST_ID + ": " + MDC.get(X_REQUEST_ID));
        return headers;
    }
}
