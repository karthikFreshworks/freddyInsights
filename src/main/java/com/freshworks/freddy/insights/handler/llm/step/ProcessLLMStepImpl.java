package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.step.json.CompletionJsonBodyParsingStepImpl;
import com.freshworks.freddy.insights.handler.llm.step.multipart.CompletionMultipartDataParsingStepImpl;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Implementation of a process step for handling Large Language Model (LLM) requests.
 * This step determines the type of request data and forwards it to the appropriate sub-step for further processing.
 */
@Slf4j
@Component
public class ProcessLLMStepImpl extends AbstractProcessStep {
    private final CompletionJsonBodyParsingStepImpl jsonCompletionStep;
    private final CompletionMultipartDataParsingStepImpl multipartCompletionStep;

    /**
     * Constructs a new ProcessLLMStepImpl with the specified sub-steps.
     *
     * @param jsonStep      The sub-step for handling JSON request bodies.
     * @param multipartStep The sub-step for handling multipart/form-data request bodies.
     */
    @Autowired
    public ProcessLLMStepImpl(CompletionJsonBodyParsingStepImpl jsonStep,
                              CompletionMultipartDataParsingStepImpl multipartStep) {
        super(ProcessLLMStepImpl.class.getName());
        this.jsonCompletionStep = jsonStep;
        this.multipartCompletionStep = multipartStep;
    }

    /**
     * Executes the main processing logic of this step.
     * This step does not perform any processing on its own.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        // This step does not perform any processing on its own
    }

    /**
     * Executes the next sub-step in the processing pipeline based on the type of request data.
     *
     * @param input The input containing information necessary for processing.
     * @return The result of executing the next sub-step.
     * @throws AIResponseStatusException If an error occurs during the execution of the next sub-step.
     */
    @Override
    protected AIServiceMO executeNextStep(AIServiceMO input) throws AIResponseStatusException {
        if (MediaType.APPLICATION_JSON_VALUE.equals(input.getAiServiceMediaType())) {
            log.debug("Executing next LLM step {}", jsonCompletionStep.getClass().getName());
            return jsonCompletionStep.execute(input);
        } else if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(input.getAiServiceMediaType())) {
            log.debug("Executing next LLM step {}", multipartCompletionStep.getClass().getName());
            return multipartCompletionStep.execute(input);
        } else {
            String errorMessage = String.format(
                    "Unsupported media type: '%s'. Only '%s' and '%s' media types are supported.",
                    input.getAiServiceMediaType(), MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE);
            log.error(errorMessage);
            throw new AIResponseStatusException(errorMessage, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * Determines whether this step should be skipped based on the input.
     * This step is always skipped because it only forwards the request to the appropriate sub-step.
     *
     * @param input The input containing information necessary for processing.
     * @return Always returns true since this step is always skipped.
     */
    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        log.info("Skipping LLM step '{}': {}", stepName, true);
        return true;
    }
}
