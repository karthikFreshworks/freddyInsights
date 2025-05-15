package com.freshworks.freddy.insights.handler.llm.step.json;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a step responsible for generating template keys for the request body.
 * This step is part of the processing pipeline to handle AI service requests.
 */
@Component
@Slf4j
public class TemplateKeysGenerationStepImpl extends AbstractProcessStep {
    private final LocaleTemplateGenerationStepImpl nextStep;

    /**
     * Constructs a new TemplateKeysGenerationStepImpl with the specified next step in the processing pipeline.
     *
     * @param nextStep The next step in the processing pipeline.
     */
    @Autowired
    public TemplateKeysGenerationStepImpl(LocaleTemplateGenerationStepImpl nextStep) {
        super(TemplateKeysGenerationStepImpl.class.getName());
        this.nextStep = nextStep;
    }

    /**
     * Executes the step by generating template keys for the request body and updating the input's template map.
     *
     * @param input The input containing information necessary for processing.
     * @throws AIResponseStatusException If an error occurs during the execution of the step.
     */
    @Override
    protected void executeStep(AIServiceMO input) throws AIResponseStatusException {
        try {
            var templateKeys = input.getTemplateKeys();
            log.info("Provided template keys for the AI service request: {}", templateKeys);
            var templates = input.getTemplates();
            Map<String, Object> resultTemplateMap = new HashMap<>();
            templateKeys.forEach(key -> {
                var searchKey = new AIServiceBaseDTO.Templates(key, "");
                if (templates.contains(searchKey)) {
                    var searchResult = templates.floor(searchKey);
                    if (searchResult != null) {
                        var templateText = AIServiceHelper.getTemplate(
                                input.getAiServiceRequestData(), searchResult.getTemplate(),
                                AIServiceConstant.PLACEHOLDER_PREFIX, AIServiceConstant.PLACEHOLDER_SUFFIX);
                        resultTemplateMap.put(searchResult.getTemplateKey(), templateText);
                    }
                } else {
                    log.warn("Template key '{}' is not present in any templates.", searchKey);
                }
            });

            if (!resultTemplateMap.isEmpty()) {
                log.info("Selected template keys for processing: {}", resultTemplateMap.keySet());
                input.setTemplatedMap(resultTemplateMap);
            } else {
                log.warn("Failed to find any templates for processing using the provided template keys: {}",
                        templateKeys);
            }
        } catch (Exception e) {
            handleException(e,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unable to generate templates from the provided template keys for step '%s': %s",
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
                && input.getTemplateKeys() != null
                && !input.getTemplateKeys().isEmpty()
                && input.getRuleBody() != null
                && !input.getRuleBody().isEmpty()
                && input.getTemplates() != null
                && !input.getTemplates().isEmpty()
                && (input.getTemplatedMap() == null || input.getTemplatedMap().isEmpty())
                && MediaType.APPLICATION_JSON_VALUE.equals(input.getAiServiceMediaType()));
        log.info("Skipping LLM step '{}': {}", stepName, shouldSkip);
        return shouldSkip;
    }
}
