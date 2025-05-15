package com.freshworks.freddy.insights.dto.insight;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.BundleEnum;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.constant.enums.insights.FrequencyEnum;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightParamDTO implements Serializable {
    private String accountId;
    private String name;
    private String group;
    private String serviceId;
    private String usecaseId;
    private String orgId;
    private String groupId;
    private String userId;
    private String bundleId;
    @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
    private String languageCode = LanguageCodeEnum.en.getValue();
    private String title;
    private String status;
    private List<String> tags;
    private List<String> promptIds;
    private List<String> timeZones;
    private String startTime;
    private String endTime;
    private TenantEnum tenant;
    private BundleEnum bundle;
    private String sku;
    private List<String> plans;
    private List<String> addons;
    private String uiTag;
    private String businessKpi;
    private String metric;
    private String department;
    private FrequencyEnum frequency;
    private String type;
    private Float importanceScore;
    private String scenarioType;
    private List<AIInsightQueryHashDTO> queryHash;
}
