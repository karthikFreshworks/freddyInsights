package com.freshworks.freddy.insights.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.freddy.insights.constant.AIRequestConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.BundleEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.AITenantEntity;
import com.freshworks.freddy.insights.exception.ApiErrorResponse;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AIRequestContextHelper;
import com.freshworks.freddy.insights.helper.AppConfigHelper;
import com.freshworks.freddy.insights.repository.AITenantRepository;
import com.freshworks.freddy.insights.service.AIBundleService;
import com.freshworks.freddy.insights.service.AITenantService;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(3)
public class AIRequestFilter extends OncePerRequestFilter {
    private final AITenantService aiTenantService;
    private final AIBundleService aiBundleService;
    private final AITenantRepository aiTenantRepository;
    private final AIRequestContextHelper aiRequestContext;
    private final AppConfigHelper appConfigHelper;
    @Value("${freddy.insights.auth.superadmin.key}")
    private String superAdminId;

    @Autowired
    public AIRequestFilter(AITenantService aiTenantService, AIRequestContextHelper aiRequestContext,
                           @Lazy AIBundleService aiBundleService, AITenantRepository aiTenantRepository,
                           AppConfigHelper appConfigHelper) {
        this.aiTenantService = aiTenantService;
        this.aiRequestContext = aiRequestContext;
        this.aiBundleService = aiBundleService;
        this.aiTenantRepository = aiTenantRepository;
        this.appConfigHelper = appConfigHelper;
    }

    private static void unauthorizedErrorResponse(HttpServletResponse response) {
        try {
            var error = new ApiErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_ACCESS);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("Content-Type", "application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
        } catch (Exception e) {
            log.error("Error when writing error to response", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!corsRequest(request)) {
                Optional<ContextVO> contextVO = validateAndGetThecontextVO(response, request);
                if (contextVO.isEmpty()) {
                    return;
                }
                MDC.put(AIRequestConstant.TENANT_ID, contextVO.get().getId());
                MDC.put(AIRequestConstant.TENANT, contextVO.get().getTenant().name());
                aiRequestContext.setContextVO(contextVO.get());
                filterChain.doFilter(request, response);
            } else {
                String requestedOrigin = getAllowedOrigin(request);
                log.info("Request is received with origin header : {}", requestedOrigin);
                response.setHeader("Access-Control-Allow-Origin", requestedOrigin);
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "*");
            }
        } finally {
            aiRequestContext.removeContextVO();
        }
    }

    private String getAllowedOrigin(HttpServletRequest request) {
        String requestOrigin = request.getHeader(AIRequestConstant.ORIGIN);
        log.info("Request is received with origin header : {}", requestOrigin);
        if (requestOrigin != null) {
            return requestOrigin;
        }
        return "*";
    }

    private Optional<ContextVO> validateAndGetThecontextVO(HttpServletResponse response, HttpServletRequest request) {
        final String freddyHeader = request.getHeader(AIRequestConstant.X_FREDDY_AI_PLATFORM_AUTHORIZATION);
        ContextVO.ContextVOBuilder contextBuilder = ContextVO.builder();
        log.info("x fw freddy addons: {}", request.getHeader(AIRequestConstant.X_FW_FREDDY_ADDONS));
        if (StringUtils.isEmpty(freddyHeader)) {
            String freshIdHeader = request.getHeader(AIRequestConstant.X_FW_CLOUD_TYPE) != null
                    ? request.getHeader(AIRequestConstant.X_FW_CLOUD_TYPE) :
                    request.getRequestURI().substring(1, request.getRequestURI().indexOf("/", 1));
            MDC.put(ObservabilityConstant.X_FREDDY_AI_PLATFORM_BUNDLE, freshIdHeader);
            log.info("filter bundleName {}", freshIdHeader);
            if (!StringUtils.isEmpty(freshIdHeader)) {
                var aiBundleEntity = aiBundleService.getBundleByName(freshIdHeader);
                var tenantEntities = aiTenantRepository.findTenantByTenantName(aiBundleEntity.getTenantList().get(0));
                var aiTenant = tenantEntities.get(0);
                contextBuilder.id(aiTenant.getId())
                        .tenant(aiTenant.getTenant())
                        .semanticCache(aiTenant.isSemanticCache())
                        .tags(request.getHeader(AIRequestConstant.TAGS))
                        .bundle(aiBundleEntity.getBundle())
                        .aiBundleEntity(aiBundleEntity)
                        .accessType(AccessType.USER)
                        .orgId(request.getHeader(AIRequestConstant.X_FW_AUTH_ORG_ID))
                        .bundleId(request.getHeader(AIRequestConstant.X_FW_BUNDLE_ID))
                        .userId(request.getHeader(AIRequestConstant.X_FW_AUTH_USER_ID))
                        .domain(request.getHeader(AIRequestConstant.X_FW_AUTH_DOMAIN))
                        .accountId(getAccountIdFromHeader(freshIdHeader, request))
                        .addons(getAddonsByRemovingDoubleQuotes(request
                                .getHeader(AIRequestConstant.X_FW_FREDDY_ADDONS)))
                        .groupId((request.getHeader(AIRequestConstant.X_FW_AUTH_GROUP_ID) != null)
                                ? request.getHeader(AIRequestConstant.X_FW_AUTH_GROUP_ID).split(",") : null)
                        .collapse(request.getHeader(AIRequestConstant.COLLAPSE_BY));
                return Optional.ofNullable(contextBuilder.build());
            } else {
                unauthorizedErrorResponse(response);
            }
            return Optional.empty();
        }
        if (freddyHeader.equals(superAdminId)) {
            contextBuilder.accessType(AccessType.SUPER_ADMIN)
                    .tenant(TenantEnum.global)
                    .semanticCache(appConfigHelper.isSuperAdminSemanticCache())
                    .id(AIRequestConstant.SUPER_ADMIN_ID);
        } else {
            var aiTenant = aiTenantService.getTenantByAdminKeyOrUserKey(freddyHeader);
            if (aiTenant == null) {
                unauthorizedErrorResponse(response);
                return Optional.empty();
            }
            log.info("tenant: {} and id {}", aiTenant.getTenant(), aiTenant.getId());
            contextBuilder.id(aiTenant.getId())
                    .tenant(aiTenant.getTenant())
                    .semanticCache(aiTenant.isSemanticCache())
                    .email(aiTenant.getEmail())
                    .userKey(aiTenant.getUserKey())
                    .adminKey((aiTenant.getAdminKey()))
                    .bundle(request.getHeader(AIRequestConstant.X_FREDDY_AI_PLATFORM_BUNDLE))
                    .accessType(checkAndUpdateAccessType(freddyHeader, aiTenant));
        }
        contextBuilder.addons(getAddonsByRemovingDoubleQuotes(request.getHeader(AIRequestConstant.X_FW_FREDDY_ADDONS)));
        return Optional.ofNullable(contextBuilder.build());
    }

    private List<String> getAddonsByRemovingDoubleQuotes(String addonHeader) {
        var addons = addonHeader != null ? addonHeader.replace("\"", "") : null;
        return addons != null ? Arrays.stream(addons.split(",")).map(String::trim)
                .collect(Collectors.toList()) : null;
    }

    private String getAccountIdFromHeader(String bundleName, HttpServletRequest request) {
        try {
            if (BundleEnum.freshdesk.toString().equals(bundleName)) {
                String authorizationHeader = request.getHeader(AIRequestConstant.AUTHORIZATION);
                if (authorizationHeader != null) {
                    String[] parts = authorizationHeader.split(" ");
                    if (parts.length == 2) {
                        Base64.Decoder decoder = Base64.getUrlDecoder();
                        String[] jwtParts = parts[1].split(AIRequestConstant.JWT_PARTS_DELIMITER);
                        String payload = new String(decoder.decode(jwtParts[1]));
                        JSONObject jsonPayload = new JSONObject(payload);
                        log.info("account_id {}", jsonPayload.getString("account_id"));
                        return jsonPayload.getString("account_id");
                    }
                }
            } else {
                return request.getHeader(AIRequestConstant.X_FW_AUTH_ACCOUNT_ID);
            }
        } catch (Exception e) {
            log.error("Error occurred when retrieving accountId", e);
        }
        return null;
    }

    private AccessType checkAndUpdateAccessType(String freddyAccessId,
                                                AITenantEntity aiTenant) {
        if (freddyAccessId.equals(aiTenant.getUserKey())) {
            return AccessType.USER;
        } else if (freddyAccessId.equals(aiTenant.getAdminKey())) {
            return AccessType.ADMIN;
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator")
                || request.getRequestURI().startsWith("/freddy-ai-platform/health")
                || request.getRequestURI().startsWith("/swagger-ui")
                || request.getRequestURI().startsWith("/v3/api-docs");
    }

    private boolean corsRequest(HttpServletRequest httpServletRequest) {
        String fetchHeader = httpServletRequest.getHeader("sec-fetch-mode");
        String httpMethod = httpServletRequest.getMethod();
        return fetchHeader != null && httpMethod.equalsIgnoreCase("OPTIONS")
                && fetchHeader.equalsIgnoreCase("cors");
    }
}
