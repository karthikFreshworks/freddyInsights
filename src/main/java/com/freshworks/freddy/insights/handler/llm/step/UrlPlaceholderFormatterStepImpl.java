package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * A step to format placeholders in a URL.
 */
@Slf4j
@Component
public class UrlPlaceholderFormatterStepImpl extends AbstractProcessStep {
    private final AnonymizeRequestDataStepImpl nextStep;

    /**
     * Constructs a new UrlPlaceholderFormatterStepImpl instance.
     */
    @Autowired
    public UrlPlaceholderFormatterStepImpl(AnonymizeRequestDataStepImpl anonymizeRequestDataStep) {
        super(UrlPlaceholderFormatterStepImpl.class.getSimpleName());
        this.nextStep = anonymizeRequestDataStep;
    }

    /**
     * Executes the step to format placeholders in the URL.
     *
     * @param input The input for the step.
     * @throws AIResponseStatusException If an error occurs during step execution.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            String placeholderUrl =
                    input.getAiServiceRequestData() != null && !input.getAiServiceRequestData().isEmpty()
                            ? AIServiceHelper.getTemplate(input.getAiServiceRequestData(), input.getUrl(),
                            AIServiceConstant.PLACEHOLDER_PREFIX, AIServiceConstant.PLACEHOLDER_SUFFIX) :
                            input.getUrl();
            String secretPlaceholderUrl = AIServiceHelper.getTemplate(appConfigHelper.getFreddyAIPlatformLLMSecrets(),
                    placeholderUrl, AIServiceConstant.PLACEHOLDER_PREFIX, AIServiceConstant.PLACEHOLDER_SUFFIX);
            String removedPlaceholderUrl = removeQueryParamsWithPlaceholder(
                    secretPlaceholderUrl, AIServiceConstant.PLACEHOLDER_PREFIX, AIServiceConstant.PLACEHOLDER_SUFFIX);
            input.setUrl(removedPlaceholderUrl);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to template the URL for step '%s': %s.",
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
        log.debug("Executing next LLM step {}", nextStep.getClass().getName());
        return nextStep.execute(input);
    }

    /**
     * Determines whether the step should be skipped.
     *
     * @param input The input for the step.
     * @return Always returns false as this step should not be skipped.
     */
    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        return false;
    }
}
