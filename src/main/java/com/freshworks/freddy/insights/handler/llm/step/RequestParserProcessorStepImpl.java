package com.freshworks.freddy.insights.handler.llm.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.octomix.josson.Josson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implementation of a step responsible for processing the request body using a JSON parser.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class RequestParserProcessorStepImpl extends AbstractProcessStep {
    private final HeaderPlaceholderFormatterStepImpl nextStep;

    /**
     * Constructs a new RequestParserProcessorStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public RequestParserProcessorStepImpl(HeaderPlaceholderFormatterStepImpl nextStep) {
        super(RequestParserProcessorStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by processing the request body using a JSON parser.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            Josson josson = Josson.fromJsonString(input.getRemoteRequestBody().toString());
            JsonNode jsonNode = josson.getNode(input.getRequestParser());
            input.setRemoteRequestBody(jsonNode.toString());
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while applying request parser: Step: %s, Reason: %s",
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
        boolean shouldSkip = !(input.getRemoteRequestBody() != null && input.getRequestParser() != null);
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
