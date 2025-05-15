package com.freshworks.freddy.insights.handler.addon;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIFreddyAddonMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.COPILOT_SELF_SERVICE_ADDON_HANDLER)
public class CopilotSelfServiceAddonHandlerImpl extends AbstractAIAddonHandler<AIFreddyAddonMO> {
    @Override
    public ESCriteriaBuilder.Builder getInsightSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO) {
        log.info("Executing copilot and self_service addon handler for insights");
        return super.addonHandlerMap.get(AIHandlerConstant.SELF_SERVICE_ADDON_HANDLER)
                .getInsightSearchMOBuilder(aiFreddyAddonMO);
    }

    @Override
    public ESCriteriaBuilder.Builder getPromptSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO) {
        log.info("Executing copilot and self_service addon handler for prompts");
        return super.addonHandlerMap.get(AIHandlerConstant.COPILOT_ADDON_HANDLER)
                .getPromptSearchMOBuilder(aiFreddyAddonMO);
    }
}
