package com.freshworks.freddy.insights.handler.llm.step.json;

import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.step.AbstractProcessStep;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Implementation of the step responsible for parsing the request body to JSON format.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class CompletionJsonBodyParsingStepImpl extends AbstractProcessStep {
    private final TemplateKeysGenerationStepImpl nextStep;

    /**
     * Constructs a new CompletionJsonBodyParsingStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public CompletionJsonBodyParsingStepImpl(TemplateKeysGenerationStepImpl nextStep) {
        super(CompletionJsonBodyParsingStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by parsing the request body to JSON format and updating the input's JSON body.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            Object bodyObject = input.getAiServiceRequestData().get(AIServiceConstant.BODY);
            input.setRemoteRequestBody(ObjectMapperHelper.objectToJsonString(bodyObject));
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST,
                    "Unable to parse the request body with JSON key 'body' to JSON for step '%s': %s",
                    stepName, e.getMessage());
        }
    }

    /**
     * Executes the next step in the processing pipeline.
     *
     * @param input The input containing information necessary for processing.
     * @return The result of executing the next step.
     * @throws AIResponseStatusException If an error occurs during the execution of the next step.
     */
    @Override
    protected AIServiceMO executeNextStep(AIServiceMO input) throws AIResponseStatusException {
        log.debug("Executing next LLM step {}", nextStep.getClass().getName());
        return nextStep.execute(input);
    }

    /**
     * Determines whether this step should be skipped based on the input.
     *
     * @param input The input containing information necessary for processing.
     * @return True if the step should be skipped, false otherwise.
     */
    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        boolean shouldSkip = !(input.getRemoteRequestBody() == null
                && input.getEnabledFeature() != null
                && !input.getEnabledFeature().isEmpty()
                && input.getEnabledFeature().contains("completion")
                && input.getAiServiceRequestData() != null
                && !input.getAiServiceRequestData().isEmpty()
                && input.getAiServiceRequestData().containsKey(AIServiceConstant.BODY)
                && MediaType.APPLICATION_JSON_VALUE.equals(input.getAiServiceMediaType()));
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
