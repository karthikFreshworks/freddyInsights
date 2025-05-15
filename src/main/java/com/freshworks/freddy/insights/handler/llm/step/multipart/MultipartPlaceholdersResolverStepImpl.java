package com.freshworks.freddy.insights.handler.llm.step.multipart;

import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.step.AbstractProcessStep;
import com.freshworks.freddy.insights.handler.llm.step.RequestBodyResolverStepImpl;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementation of a step responsible for resolving placeholders in the multipart request body.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class MultipartPlaceholdersResolverStepImpl extends AbstractProcessStep {
    private final RequestBodyResolverStepImpl nextStep;

    /**
     * Constructs a new MultipartPlaceholdersResolverStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public MultipartPlaceholdersResolverStepImpl(RequestBodyResolverStepImpl nextStep) {
        super(MultipartPlaceholdersResolverStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by resolving placeholders in the multipart request body and setting the placeholder map in
     * * the input.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            Map<String, Object> placeholderMap = AICommonHelper.resolvePlaceholders(
                    input.getRuleBody(),
                    input.getAiServiceRequestData(),
                    AIServiceConstant.PLACEHOLDER_PREFIX,
                    AIServiceConstant.PLACEHOLDER_SUFFIX,
                    false);
            input.setTemplatedRuleBodyMap(placeholderMap);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST,
                    "An error occurred while resolving multipart placeholders for step '%s': %s",
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
        boolean shouldSkip = !((input.getEnabledFeature() == null || !input.getEnabledFeature().contains("completion"))
                && MediaType.MULTIPART_FORM_DATA_VALUE.equals(input.getAiServiceMediaType())
                && input.getAiServiceRequestData() != null
                && !input.getAiServiceRequestData().isEmpty()
                && input.getRuleBody() != null
                && !input.getRuleBody().isEmpty());
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
