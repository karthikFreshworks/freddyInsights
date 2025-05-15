package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Implementation of a step responsible for forming the JSON body using templates and placeholder mappings.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class JsonPayloadBuilderStepImpl extends AbstractProcessStep {
    private final RequestParserProcessorStepImpl nextStep;

    /**
     * Constructs a new JsonBodyFormationStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public JsonPayloadBuilderStepImpl(RequestParserProcessorStepImpl nextStep) {
        super(JsonPayloadBuilderStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by forming the JSON body using templates and placeholder mappings.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            Object jsonBody = ObjectMapperHelper.objectToJsonString(input.getTemplatedRuleBodyMap());
            input.setRemoteRequestBody(jsonBody);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while forming JSON payload from template text "
                            + "and placeholder map: Step: %s, Error Message: %s",
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
                && MediaType.APPLICATION_JSON_VALUE.equals(input.getAiServiceMediaType()));
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
