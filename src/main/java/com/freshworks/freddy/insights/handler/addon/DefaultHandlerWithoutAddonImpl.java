package com.freshworks.freddy.insights.handler.addon;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.helper.ESQueryHelper;
import com.freshworks.freddy.insights.modelobject.AIFreddyAddonMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.DEFAULT_HANDLER_WITHOUT_ADDON)
public class DefaultHandlerWithoutAddonImpl extends AbstractAIAddonHandler<AIFreddyAddonMO> {
    @Autowired
    protected ESQueryHelper queryHelper;

    @Override
    public ESCriteriaBuilder.Builder getInsightSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO) {
        log.info("Executing default handler for insights");
        var tenants = aiFreddyAddonMO.getTenants();
        return getInsightsSearchBuilder(aiFreddyAddonMO, tenants);
    }

    @Override
    public ESCriteriaBuilder.Builder getPromptSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO) {
        log.info("Executing default handler for prompts");
        var tenants = aiFreddyAddonMO.getTenants();
        return getPromptSearchBuilder(aiFreddyAddonMO, tenants);
    }
}
