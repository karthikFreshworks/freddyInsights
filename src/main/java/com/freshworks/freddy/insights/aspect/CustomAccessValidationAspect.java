package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.CustomAccessTypeEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.helper.AIRequestContextHelper;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class CustomAccessValidationAspect {
    private final AIRequestContextHelper aiRequestContext;

    @Autowired
    public CustomAccessValidationAspect(AIRequestContextHelper aiRequestContext) {
        this.aiRequestContext = aiRequestContext;
    }

    @Around("@annotation(aiCustomAuthorization)")
    public Object validateAccess(ProceedingJoinPoint joinPoint, AICustomAuthorization aiCustomAuthorization) throws
            Throwable {
        Set<CustomAccessTypeEnum> requiredAccessTypes =
                Arrays.stream(aiCustomAuthorization.value()).collect(Collectors.toSet());
        if (isCustomAccessAllowed(requiredAccessTypes) && isValidTenantRequests()) {
            log.info("Neo Analytics is executing request as SuperAdmin.");
            ContextVO neoAnalyticsContext = aiRequestContext.getContextVO();
            aiRequestContext.setContextVO(ContextVO.builder().id(neoAnalyticsContext.getId())
                    .tenant(neoAnalyticsContext.getTenant())
                    .email(neoAnalyticsContext.getEmail())
                    .userKey(neoAnalyticsContext.getUserKey())
                    .adminKey((neoAnalyticsContext.getAdminKey()))
                    .accessType(AccessType.SUPER_ADMIN).build());
        }
        return joinPoint.proceed();
    }

    private boolean isCustomAccessAllowed(Set<CustomAccessTypeEnum> requiredAccessTypes) {
        return requiredAccessTypes.contains(CustomAccessTypeEnum.NEO_ANALYTICS)
                || requiredAccessTypes.contains(CustomAccessTypeEnum.JEEVES);
    }

    private boolean isValidTenantRequests() {
        return this.aiRequestContext.getContextVO().getTenant() == TenantEnum.neoanalytics
                || this.aiRequestContext.getContextVO().getTenant() == TenantEnum.jeeves;
    }
}
