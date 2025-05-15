package com.freshworks.freddy.insights.handler.llm.step.json;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.llm.step.AbstractProcessStep;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementation of the step responsible for generating templates based on locale.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class LocaleTemplateGenerationStepImpl extends AbstractProcessStep {
    private static final String LOCALE_KEY = "locale";
    private final TemplateGenerationStepImpl nextStep;

    /**
     * Constructs a new LocaleTemplateGenerationStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public LocaleTemplateGenerationStepImpl(TemplateGenerationStepImpl nextStep) {
        super(LocaleTemplateGenerationStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by generating templates based on the provided locale.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            String locale = input.getAiServiceRequestData().get(LOCALE_KEY).toString();
            var searchKey = new AIServiceBaseDTO.Templates(locale, "");
            if (input.getTemplates().contains(searchKey)) {
                var localeTemplate = input.getTemplates().floor(searchKey);
                if (localeTemplate != null) {
                    var templateText = AIServiceHelper.getTemplate(
                            input.getAiServiceRequestData(), localeTemplate.getTemplate(),
                            AIServiceConstant.PLACEHOLDER_PREFIX, AIServiceConstant.PLACEHOLDER_SUFFIX);
                    input.setTemplatedMap(Map.of(AIHandlerConstant.TEMPLATE, templateText));
                }
            } else {
                log.warn("Templates map is not set even though the locale {} is passed as a parameter, "
                        + "resulting in the use of a single template.", locale);
            }
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unable to generate template from template keys provided as locale for step %s: %s",
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
                && input.getAiServiceRequestData() != null
                && !input.getAiServiceRequestData().isEmpty()
                && input.getAiServiceRequestData().containsKey(LOCALE_KEY)
                && input.getTemplates() != null
                && !input.getTemplates().isEmpty()
                && (input.getTemplatedMap() == null || input.getTemplatedMap().isEmpty())
                && MediaType.APPLICATION_JSON_VALUE.equals(input.getAiServiceMediaType()));
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
