package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.header.AbstractLLMHeaderHandler;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A step to format placeholders in headers.
 */
@Slf4j
@Component
public class HeaderPlaceholderFormatterStepImpl extends AbstractProcessStep {
    private static final String HEADER_STRATEGY_SUFFIX = "HeaderStrategy";
    private final UrlPlaceholderFormatterStepImpl nextStep;
    private final Map<String, AbstractLLMHeaderHandler> llmHeaderMap;

    @Autowired
    public HeaderPlaceholderFormatterStepImpl(UrlPlaceholderFormatterStepImpl nextStep,
                                              Map<String, AbstractLLMHeaderHandler> llmHeaderMap) {
        super(HeaderPlaceholderFormatterStepImpl.class.getSimpleName());
        this.nextStep = nextStep;
        this.llmHeaderMap = llmHeaderMap;
    }

    /**
     * Executes the step to format placeholders in headers.
     *
     * @param input The input for the step.
     * @throws AIResponseStatusException If an error occurs during step execution.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            String platformHeaderStrategy = String.format("%s%s", input.getPlatform().name(), HEADER_STRATEGY_SUFFIX);
            String[] headers = llmHeaderMap.get(platformHeaderStrategy).headerMapToArray(input.getHeader());
            input.setRemoteRequestHeaders(headers);
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to format headers for step '%s': %s due to an error.",
                    stepName, e.getMessage());
        }
    }

    /**
     * Executes the next step in the process.
     *
     * @param input The input for the step.
     * @return The result of executing the next step.
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
     * @return True if the step should be skipped, false otherwise.
     */
    @Override
    protected boolean shouldSkipStep(AIServiceMO input) {
        boolean shouldSkip = input.getRemoteRequestHeaders() != null && input.getRemoteRequestHeaders().length > 0;
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
