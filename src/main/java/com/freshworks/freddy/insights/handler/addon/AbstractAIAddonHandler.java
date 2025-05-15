package com.freshworks.freddy.insights.handler.addon;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.AIPromptConstant;
import com.freshworks.freddy.insights.constant.ESConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.helper.AppConfigHelper;
import com.freshworks.freddy.insights.helper.ESQueryHelper;
import com.freshworks.freddy.insights.modelobject.AIFreddyAddonMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.TITLE;

@Slf4j
public abstract class AbstractAIAddonHandler<A> {
    protected ESQueryHelper queryHelper;

    protected Map<String, AbstractAIAddonHandler<AIFreddyAddonMO>> addonHandlerMap;

    protected AppConfigHelper appConfigHelper;

    @Autowired
    public void setAddonHandlerMap(
            Map<String, AbstractAIAddonHandler<AIFreddyAddonMO>> addonHandlerMap) {
        this.addonHandlerMap = addonHandlerMap;
    }

    @Autowired
    public void setAppConfigHelper(AppConfigHelper appConfigHelper) {
        this.appConfigHelper = appConfigHelper;
    }

    @Autowired
    public void setQueryHelper(ESQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    public abstract ESCriteriaBuilder.Builder getInsightSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO);

    public abstract ESCriteriaBuilder.Builder getPromptSearchMOBuilder(AIFreddyAddonMO aiFreddyAddonMO);

    protected ESCriteriaBuilder.Builder getPromptSearchBuilder(AIFreddyAddonMO aiFreddyAddonMO,
                                                              List<TenantEnum> tenants) {
        var paramDTO = aiFreddyAddonMO.getAiPromptParamDTO();
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder.tenants(tenants);
        if (paramDTO.getText() != null) {
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                    queryHelper.constructFieldQueryWithTranslatedFields(tenants, AIInsightConstant.TEXT,
                            paramDTO.getText()));
        }
        if (paramDTO.getPromptIds() == null || paramDTO.getPromptIds().isEmpty()) {
            searchMOBuilder.in(AIPromptConstant.SUGGEST,
                    paramDTO.getSuggest() != null ? String.valueOf(paramDTO.getSuggest()) :
                            String.valueOf(AIPromptConstant.PROMPT_DEFAULT_SUGGEST_VALUE));
        }
        return searchMOBuilder;
    }

    protected ESCriteriaBuilder.Builder getInsightsSearchBuilder(AIFreddyAddonMO aiFreddyAddonMO,
                                                              List<TenantEnum> tenants) {
        var paramDTO = aiFreddyAddonMO.getAiInsightParamDTO();
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder.tenants(tenants);
        if (paramDTO.getTitle() != null) {
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                    queryHelper.constructFieldQueryWithTranslatedFields(tenants, TITLE,
                            paramDTO.getTitle()));
        }
        return searchMOBuilder;
    }
}
