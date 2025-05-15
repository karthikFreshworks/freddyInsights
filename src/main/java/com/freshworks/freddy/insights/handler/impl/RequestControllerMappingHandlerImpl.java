package com.freshworks.freddy.insights.handler.impl;

import com.freshworks.freddy.insights.helper.ExceptionHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@Slf4j
public class RequestControllerMappingHandlerImpl {
    private final ApplicationContext applicationContext;

    public RequestControllerMappingHandlerImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String getControllerMethod(HttpServletRequest request) {
        HandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);

        try {
            HandlerExecutionChain executionChain = handlerMapping.getHandler(request);

            if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
                String className = getControllerClassName(handlerMethod.getBean());
                String methodName = handlerMethod.getMethod().getName();
                log.info("Request is handled by controller={} and method={}", className, methodName);
                return String.format("%s.%s", className, methodName);
            } else {
                log.info("Request is not handled by any controller.");
            }
        } catch (Exception ex) {
            log.info("Request is not handled by any controller. Error={}", ExceptionHelper.stackTrace(ex));
        }
        return null;
    }

    private String getControllerClassName(Object controller) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(controller);
        return targetClass.getName();
    }
}
