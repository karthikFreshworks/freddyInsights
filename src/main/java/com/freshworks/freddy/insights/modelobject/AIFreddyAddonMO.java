package com.freshworks.freddy.insights.modelobject;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.insight.AIInsightParamDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptParamDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@ToString
@Getter
@Setter
@Builder
public class AIFreddyAddonMO {
    private List<TenantEnum> tenants;
    private AIPromptParamDTO aiPromptParamDTO;
    private AIInsightParamDTO aiInsightParamDTO;
    private ESCriteriaBuilder.Builder baseESSearchBuilder;
    private Set<String> excludeIntentHandler;
    private String[] excludedIntentHandlerFields;
    private boolean requireOnlyFallbackHandlers;
    private boolean isCopilotAvailable;
}
