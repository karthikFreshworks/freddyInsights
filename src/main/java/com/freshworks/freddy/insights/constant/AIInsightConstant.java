package com.freshworks.freddy.insights.constant;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;

import java.util.*;

public class AIInsightConstant {
    private AIInsightConstant() {
        throw new IllegalStateException("AIInsightConstant class");
    }

    public static final String ID = "_id";
    public static final String INSIGHT_ID = "insight_id";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ORG_ID = "org_id";
    public static final String TENANT = "tenant";
    public static final String GROUP = "group";
    public static final String DOMAIN = "domain";
    public static final String GROUP_ID = "group_id";
    public static final String SERVICE_ID = "service_id";
    public static final String USECASE_ID = "usecase_id";
    public static final String TRANSLATED_FIELDS = "translated_fields";
    public static final String BUNDLE_ID = "bundle_id";
    public static final String SKU = "sku";
    public static final String PLANS = "plans";
    public static final String ADDONS = "addons";
    public static final String USER_ID = "user_id";
    public static final String TITLE = "title";
    public static final String STATUS = "status";
    public static final String PROMPT_IDS = "prompt_ids";
    public static final String TAGS = "tags";
    public static final String UI_TAG = "ui_tag";
    public static final String BUSINESS_KPI = "businessKpi";
    public static final String METRIC = "metric";
    public static final String TIME_ZONES = "time_zones";
    public static final String DEPARTMENT = "department";
    public static final String FREQUENCY = "frequency";
    public static final String TYPE = "type";
    public static final String SCENARIO_TYPE = "scenarioType";
    public static final String LANGUAGE_CODE = "language_code";
    public static final String TEXT = "text";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String DEFAULT_ACCOUNT = "defacct";
    public static final String DEFAULT_GROUP = "defgrp";
    public static final String DEFAULT_USER_ID = "defuser";
    public static final String DEFAULT_VERSION = "v0";
    public static final String PROMOTE_CREATE_INSIGHT_SUBJECT = "Freshworks Freddy AI Platform "
            + "- Insight create promotion Status";
    public static final String PROMOTE_UPDATE_INSIGHT_SUBJECT = "Freshworks Freddy AI Platform "
            + "- Insight update promotion Status";
    public static final String INSIGHT_CONTROLLER_V2 = "aiInsightControllerV2";
    public static final String STRING_FORMATTER = "%s : %s";

    //In future these filters will be coming from Product proxy or freshId
    public static final Map<TenantEnum, List<String>> TENANT_FILTERS;

    static {
        Map<TenantEnum, List<String>> tenantFilters = new EnumMap<>(TenantEnum.class);
        tenantFilters.put(TenantEnum.freshchat, Arrays.asList(ORG_ID, BUNDLE_ID));
        tenantFilters.put(TenantEnum.freshdesk, Arrays.asList(ACCOUNT_ID));
        tenantFilters.put(TenantEnum.freshservice, Arrays.asList(ACCOUNT_ID, GROUP_ID));
        tenantFilters.put(TenantEnum.freshcaller, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.freshsales, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.freshbot, Arrays.asList(ORG_ID, BUNDLE_ID));
        tenantFilters.put(TenantEnum.freshmarketer, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.global, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.test, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.neoanalytics, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.neosearch, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        tenantFilters.put(TenantEnum.neomarketplace, Arrays.asList(ORG_ID, BUNDLE_ID, USER_ID));
        TENANT_FILTERS = Collections.unmodifiableMap(tenantFilters);
    }
}
