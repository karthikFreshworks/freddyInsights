package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AIRequestContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class AccessValidationAspect {
    private final AIRequestContextHelper aiRequestContext;

    @Autowired
    public AccessValidationAspect(AIRequestContextHelper aiRequestContext) {
        this.aiRequestContext = aiRequestContext;
    }

    @Around("@annotation(aiAuthorization)")
    public Object validateAccess(ProceedingJoinPoint joinPoint, AIAuthorization aiAuthorization) throws
            Throwable {
        Set<AccessType> requiredAccessTypes = Arrays.stream(aiAuthorization.value()).collect(Collectors.toSet());
        var accessType = aiRequestContext.getContextVO().getAccessType();

        if (accessType == null || !requiredAccessTypes.contains(accessType)) {
            throw new AIResponseStatusException(ExceptionConstant.FORBIDDEN,
                    HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }
}
