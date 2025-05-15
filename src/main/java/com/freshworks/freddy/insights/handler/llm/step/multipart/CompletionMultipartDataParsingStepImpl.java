package com.freshworks.freddy.insights.handler.llm.step.multipart;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.step.AbstractProcessStep;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Implementation of a step responsible for parsing multipart form data.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class CompletionMultipartDataParsingStepImpl extends AbstractProcessStep {
    private final MultipartPlaceholdersResolverStepImpl nextStep;

    /**
     * Constructs a new CompletionMultipartDataParsingStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public CompletionMultipartDataParsingStepImpl(MultipartPlaceholdersResolverStepImpl nextStep) {
        super(CompletionMultipartDataParsingStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by parsing the multipart form data and updating the input's templated rule body map.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            input.setTemplatedRuleBodyMap(input.getAiServiceRequestData());
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST,
                    "Failed to parse the multipart form data for step '%s'. Error: %s",
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
        boolean shouldSkip = !(input.getAiServiceRequestData() != null
                && !input.getAiServiceRequestData().isEmpty()
                && input.getEnabledFeature() != null
                && !input.getEnabledFeature().isEmpty()
                && input.getEnabledFeature().contains("completion")
                && MediaType.MULTIPART_FORM_DATA_VALUE.equals(input.getAiServiceMediaType()));
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
