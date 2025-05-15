package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Component
public class AppConfigHelper {
    private final Map<String, Object> freddyAIPlatformLLMSecrets = new HashMap<>();
    private final Map<String, String> freddyAIPlatformHostUrls = new HashMap<>();
    private final Map<String, String> freddyAIPlatformBearerTokens = new HashMap<>();
    private final Map<TenantEnum, String> tenantModelKey = new HashMap<>();

    @Value("#{'${freddy.insights.host.keys.name}'.split(',')}")
    private List<String> freddyAIPlatformHostKeysName;
    @Value("#{'${freddy.insights.auth.llm.keys.name}'.split(',')}")
    private List<String> freddyAIPlatformLLMKeysName;
    @Value("${freddy.insights.anonymize.api.key.name}")
    private String anonymizeApiKeyName;
    @Value("${freddy.insights.anonymize.service.enabled}")
    private boolean anonymizeEnabled;
    @Value("${freddy.insights.analytics.tenants}")
    private List<String> analyticsTenantsData;
    @Value("${freddy.insights.anonymize.url}")
    private String anonymizeUrl;
    @Value("${freddy.insights.deanonymize.url}")
    private String deanonymizeUrl;
    @Value("${freddy.insights.http.connection.max.retry:3}")
    private int httpConnectionMaxRetry;
    @Value("${freddy.insights.http.connection.delay.millis:1000}")
    private int httpConnectionDelayMillis;
    @Value("${freddy.insights.http.connection.timeout:120}")
    private int httpConnectionTimeout;
    @Value("${freddy.insights.emailbot.service.host}")
    private String emailbotServiceHost;
    @Value("${freddy.insights.emailbot.service.auth}")
    private String emailbotServiceAuth;
    @Value("${freddy.insights.emailbot.service.readTimeoutInSec}")
    private int emailbotServiceReadTimeoutInSec;
    @Value("${freddy.insights.emailbot.service.fromEmail}")
    private String emailbotServiceFromEmail;
    @Value("${freddy.insights.emailbot.service.body.template}")
    private String emailbotServiceBodyTemplate;
    @Value("${freddy.insights.emailbot.service.subject.template}")
    private String emailbotServiceSubjectTemplate;
    @Value("${freddy.insights.aws.region}")
    private String awsRegion;
    @Value("${freddy.insights.host}")
    private String platformHost;
    @Value("${freddy.insights.is.prod}")
    private boolean isProd;
    @Value("${freddy.insights.emailbot.service.fromEmail}")
    private String fromEmail;
    @Value("${freddy.insights.emailbot.service.toEmail}")
    private String toEmail;
    @Value("${freddy.insights.emailbot.service.body.template}")
    private String body;
    @Value("${freddy.insights.promote.service.body.template}")
    private String promoteServiceBody;
    @Value("${freddy.insights.emailbot.service.subject.template}")
    private String subject;
    @Value("${freddy.insights.aws.region}")
    private String region;
    @Value("${freddy.ai.intent.classification.service.id}")
    private String intentClassificationServiceId;
    @Value("#{'${freddy.ai.model.ids}'.split(',')}")
    private List<String> freddyAIModelIds;
    @Value("${freddy.ai.central.auth.token}")
    private String centralAuthKey;
    @Value("${freddy.ai.central.url}")
    private String centralUrl;
    @Value("${freddy.insights.semantic.cache.host}")
    private String semanticCacheHost;
    @Value("${freddy.insights.superadmin.semantic.cache.enable}")
    private boolean superAdminSemanticCache;
    @Value("${freddy.insights.http.client.thread.pool:5}")
    private int httpClientThreadPool;
    @Value("${freddy.insights.intent.handler.range:2}")
    private int handlerRange;
    @Value("${apache.http.connection.pool.max:200}")
    private int apacheHttpMaxConnection;
    @Value("${apache.http.connection.default.max.per.route:50}")
    private int apacheHttpMaxConnectionPerRoute;
    @Value("${freddy.insights.intent.smalltalk.enable:false}")
    private boolean smallTalkEnabled;
    @Value("${freddy.insights.addon.enabled}")
    private boolean addonSupportEnabled;
    @Value("#{'${freddy.insights.allowed.addon.bundles}'.split(',')}")
    private List<String> allowedAddonBundles;
    @Value("#{'${freddy.insights.allowed.addon.tenants}'.split(',')}")
    private List<String> allowedAddonTenants;

    @PostConstruct
    public void init() {
        for (String keyName : freddyAIPlatformLLMKeysName) {
            freddyAIPlatformLLMSecrets.put(keyName.trim(), System.getenv(keyName.trim()));
        }
        for (String keyName : freddyAIPlatformHostKeysName) {
            freddyAIPlatformHostUrls.put(keyName.trim(), System.getenv(keyName.trim()));
            freddyAIPlatformBearerTokens.put(keyName.trim(),
                    System.getenv(String.format("%s%s", keyName.trim(), "_BEARER_KEY")));
        }
        for (String keyName : freddyAIModelIds) {
            String[] tenantKeySplit = keyName.split("-");

            if (tenantKeySplit.length != 0) {
                tenantModelKey.put(TenantEnum.valueOf(tenantKeySplit[0]), keyName);
            }
        }
    }

    public boolean isAddonSupportApplicable(ContextVO contextVO) {
        if (addonSupportEnabled) {
            if (contextVO.getBundle() != null) {
                var bundle = contextVO.getAiBundleEntity();
                return allowedAddonBundles.contains(bundle.getBundle().toLowerCase());
            } else {
                // Added this to support test tenant in automations
                return allowedAddonTenants.contains(contextVO.getTenant().name());
            }
        }
        return false;
    }

    public List<TenantEnum> getAnalyticsTenantsEnums() {
        List<String> tenantStrings = this.analyticsTenantsData;
        return tenantStrings.stream()
                .map(String::trim)
                .map(AITenantHelper::convertToTenantEnum)
                .filter(Objects::nonNull) // Ignore null (invalid) tenants
                .collect(Collectors.toList());
    }

    public List<String> getAnalyticsTenantsStrings() {
        List<String> tenantStrings = this.analyticsTenantsData;
        return tenantStrings.stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
