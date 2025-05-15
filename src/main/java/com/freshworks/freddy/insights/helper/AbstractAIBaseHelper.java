package com.freshworks.freddy.insights.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.freddy.insights.constant.AIRequestConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.AddonEnum;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.converter.EmailRequestConverter;
import com.freshworks.freddy.insights.entity.AITenantEntity;
import com.freshworks.freddy.insights.repository.AITenantRepository;
import com.freshworks.freddy.insights.service.AIBundleService;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
public abstract class AbstractAIBaseHelper {
    protected ObjectMapper objectMapper;

    protected AppConfigHelper appConfigHelper;
    protected AIBundleService aiBundleService;
    protected AITenantRepository tenantRepository;
    protected EmailServiceHelper emailServiceHelper;
    protected AIRequestContextHelper aiRequestContext;
    protected EmailRequestConverter emailRequestConverter;

    public static String getTemplate(Map<String, Object> clientBody, Object template, String prefix, String suffix) {
        StringSubstitutor sub = new StringSubstitutor(clientBody, prefix, suffix);
        sub.setDisableSubstitutionInValues(true);
        return sub.replace(template);
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Autowired
    public void setAiRequestContext(AIRequestContextHelper aiRequestContext) {
        this.aiRequestContext = aiRequestContext;
    }

    @Autowired
    public void setAIBundleService(AIBundleService aiBundleService) {
        this.aiBundleService = aiBundleService;
    }

    @Autowired
    public void setAppConfigHelper(AppConfigHelper appConfigHelper) {
        this.appConfigHelper = appConfigHelper;
    }

    @Autowired
    public void setTenantRepository(AITenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Autowired
    public void setEmailServiceHelper(EmailServiceHelper emailServiceHelper) {
        this.emailServiceHelper = emailServiceHelper;
    }

    @Autowired
    public void setEmailRequestConverter(EmailRequestConverter emailRequestConverter) {
        this.emailRequestConverter = emailRequestConverter;
    }

    public ContextVO getContextVO() {
        return getAiRequestContext().getContextVO();
    }

    protected boolean isSuperAdmin() {
        return getContextVO().getAccessType() == AccessType.SUPER_ADMIN;
    }

    protected String[] modifyExcludeIfSuperAdmin(String[] exclude) {
        if (isSuperAdmin()) {
            return new String[0];
        }
        return exclude;
    }

    protected List<TenantEnum> getTenantsByBundleOrSuperAdminOrRequestedTenant(TenantEnum requestedTenant) {
        if (isSuperAdmin()) {
            if (requestedTenant != null) {
                return List.of(requestedTenant);
            }
            return getAllTenantEnumList();
        }

        if (getContextVO().getBundle() != null) {
            return getContextVO().getAiBundleEntity().getTenantList();
        }
        return List.of(getContextVO().getTenant());
    }

    public List<TenantEnum> getAllTenantEnumList() {
        return Arrays.stream(TenantEnum.values()).collect(Collectors.toUnmodifiableList());
    }

    public boolean noAddonAvailable(ContextVO contextVO) {
        var allAddonValues = AddonEnum.getAllValues();
        return contextVO.getAddons() == null || contextVO.getAddons().stream().noneMatch(allAddonValues::contains);
    }

    protected List<String> getEmailToList(TenantEnum tenantEnum) {
        var tenantEntities = tenantRepository.findTenantByTenantName(tenantEnum);
        return tenantEntities.stream().map(AITenantEntity::getEmail).collect(Collectors.toList());
    }

    public Map<String, String> excludeHeaders(Map<String, String> headers) {
        if (headers != null) {
            headers.remove(AIRequestConstant.AUTHORIZATION.toLowerCase());
            if (headers.containsKey(AIRequestConstant.X_FW_REDIRECT_AUTHORIZATION.toLowerCase())
                    && headers.get(AIRequestConstant.X_FW_REDIRECT_AUTHORIZATION.toLowerCase()) != null) {
                log.info("The header contains {}, and these values are being set in the authorization key.",
                        AIRequestConstant.X_FW_REDIRECT_AUTHORIZATION.toLowerCase());
                headers.put(AIRequestConstant.AUTHORIZATION.toLowerCase(),
                        headers.get(AIRequestConstant.X_FW_REDIRECT_AUTHORIZATION.toLowerCase()));
                headers.remove(AIRequestConstant.X_FW_REDIRECT_AUTHORIZATION.toLowerCase());
            }
            headers.remove(AIRequestConstant.X_FREDDY_AI_PLATFORM_AUTHORIZATION.toLowerCase());
            headers.remove(AIRequestConstant.CONTENT_TYPE.toLowerCase());
            headers.remove(AIRequestConstant.ORIGIN.toLowerCase());
            headers.remove(AIRequestConstant.CONTENT_LENGTH.toLowerCase());
            headers.remove(AIRequestConstant.ACCEPT.toLowerCase());
            headers.remove(AIRequestConstant.HOST.toLowerCase());
            headers.remove(AIRequestConstant.POSTMAN_TOKEN.toLowerCase());
            headers.remove(AIRequestConstant.USER_AGENT.toLowerCase());
            headers.remove(AIRequestConstant.CONNECTION.toLowerCase());
            headers.remove(AIRequestConstant.X_FORWARDED_PROTO.toLowerCase());
            headers.remove(AIRequestConstant.X_FORWARDED_CLIENT_CERT.toLowerCase());
            headers.put(AIRequestConstant.ACCEPT_ENCODING.toLowerCase(), "gzip");
        }
        return headers;
    }

    protected boolean isSupportedPlatform(PlatformEnum platform) {
        return platform == PlatformEnum.azure || platform == PlatformEnum.openai;
    }
}
