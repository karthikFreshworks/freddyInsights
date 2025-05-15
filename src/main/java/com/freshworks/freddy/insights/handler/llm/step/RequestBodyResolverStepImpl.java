package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of a step responsible for resolving the request body from various sources.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class RequestBodyResolverStepImpl extends AbstractProcessStep {
    private final JsonPayloadBuilderStepImpl nextStep;

    /**
     * Constructs a new RequestBodyResolverStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public RequestBodyResolverStepImpl(JsonPayloadBuilderStepImpl nextStep) {
        super(RequestBodyResolverStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by resolving the request body from various sources.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            Map<String, Object> requestBodyMap = Optional.ofNullable(input.getTemplatedRuleBodyMap())
                    .filter(map -> !map.isEmpty())
                    .orElse(Optional.ofNullable(input.getAiServiceRequestData())
                            .filter(map -> !map.isEmpty())
                            .orElse(Optional.ofNullable(input.getRuleBody())
                                    .filter(map -> !map.isEmpty())
                                    .orElse(new HashMap<>())));
            input.setTemplatedRuleBodyMap(requestBodyMap);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occurred while resolving the request body: Step: %s, Reason: %s",
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
     * This step is never skipped.
     *
     * @param input The input containing information necessary for processing.
     * @return Always returns false since this step is never skipped.
     */
    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        boolean shouldSkip = !(input.getRemoteRequestBody() == null);
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
