package com.freshworks.freddy.insights.constant;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AIPromptConstant {
    String ID = "_id";
    String ACCOUNT_ID = "account_id";
    String GROUP = "group";
    String NAME = "name";
    String VERSION = "version";
    String TENANT = "tenant";
    String LANGUAGE_CODE = "language_code";
    String USER_ID = "user_id";
    String SUGGEST = "suggest";
    String TAGS = "tags";
    String DEFAULT_GROUP = "defgrp";
    String DEFAULT_USER_ID = "defuser";
    String DEFAULT_ACCOUNT = "defacct";
    String DEFAULT_VERSION = "v0";
    String PROMOTE_CREATE_PROMPT_SUBJECT = "Freshworks Freddy AI Platform - Prompt create promotion Status";
    String PROMOTE_UPDATE_PROMPT_SUBJECT = "Freshworks Freddy AI Platform - Prompt update promotion Status";
    String PROMPT_CONTROLLER_V2 = "aiPromptControllerV2";
    boolean PROMPT_DEFAULT_SUGGEST_VALUE = true;
    float PROMPT_DEFAULT_WEIGHT = 0.0f;

    //In future these filters will be coming from Product proxy or freshId
    Map<TenantEnum, List<String>> PROMPT_TENANT_FILTERS = new HashMap<>() {
        {
            put(TenantEnum.freshmarketer, Arrays.asList(TAGS));
        }
    };
    String PROMPT_CACHE_PATTERN = "prompt*";
}
