package com.freshworks.freddy.insights.config.filter;

import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.handler.impl.RequestControllerMappingHandlerImpl;
import com.freshworks.freddy.insights.handler.observability.AbstractObservabilityHandler;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * HTTP Filter for transaction tracing in logs.
 * Use x-request-id sent in the API request header.
 * If not present, generate a unique UUID and set in the header.
 */
@Component
@Order(2)
@Slf4j
public class ObservabilityFilter extends OncePerRequestFilterWrapper {
    private RequestControllerMappingHandlerImpl reqControllerMappingHandler;
    private Map<String, AbstractObservabilityHandler> observabilityHandlerMap;

    @Autowired
    public void setReqControllerMappingHandler(RequestControllerMappingHandlerImpl reqControllerMappingHandler) {
        this.reqControllerMappingHandler = reqControllerMappingHandler;
    }

    @Autowired
    public void setObservabilityHandlerMap(Map<String, AbstractObservabilityHandler> observabilityHandlerMap) {
        this.observabilityHandlerMap = observabilityHandlerMap;
    }

    /**
     * Performs the internal filter logic for transaction tracing.
     *
     * @param httpServletRequest  The HTTP servlet request.
     * @param httpServletResponse The HTTP servlet response.
     * @param chain               The filter chain.
     * @throws IOException      If an I/O error occurs.
     * @throws ServletException If a servlet-specific error occurs.
     */
    @Override
    public void doFilterInternal(
            @NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse,
            @NotNull FilterChain chain) {
        try {
            Map<String, String> changedHeaderMap = extractNonBlankHeaders(httpServletRequest);
            String method = reqControllerMappingHandler.getControllerMethod(httpServletRequest);
            boolean isDialogueRequest = isDialogueRequest(method);
            boolean isSprinklerRequest = isSprinklerRequest(method);

            setCommonMDCFields(httpServletRequest, httpServletResponse, changedHeaderMap, isDialogueRequest);

            chain.doFilter(httpServletRequest, httpServletResponse);

            String observabilityType = getObservabilityType(isSprinklerRequest, isDialogueRequest);
            observabilityHandlerMap.get(observabilityType).recordMetrics();
            log.info(observabilityHandlerMap.get(observabilityType).getLogBuilder().toString());
        } catch (Throwable ex) {
            handleException(ex);
        } finally {
            MDC.clear();
        }
    }

    private Map<String, String> extractNonBlankHeaders(HttpServletRequest httpServletRequest) {
        return Collections.list(httpServletRequest.getHeaderNames())
                .stream()
                .filter(headerName -> {
                    String headerValue = httpServletRequest.getHeader(headerName);
                    return StringUtils.isNotBlank(headerValue);
                })
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        httpServletRequest::getHeader,
                        (existingValue, newValue) -> newValue));
    }

    private void handleException(Throwable ex) {
        log.error("Error while preparing observability - ERROR:{}", ExceptionHelper.stackTrace(ex));
        if (ex instanceof AIResponseStatusException) {
            throw new AIResponseStatusException(
                    String.format("Error while preparing observability. %s", ex.getMessage()),
                    ((AIResponseStatusException) ex).getApiError().getHttpStatus());
        }
        throw new AIResponseStatusException(
                String.format("Error while preparing observability. %s", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void setCommonMDCFields(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    Map<String, String> changedHeaderMap, boolean isDialogueRequest) {
        //host
        MDC.put(ObservabilityConstant.HOST, httpServletRequest.getHeader(ObservabilityConstant.HOST));

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_FORWARDED_FOR) != null) {
            MDC.put(ObservabilityConstant.X_FW_FORWARDED_FOR,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_FORWARDED_FOR));
        }

        // x-request-id
        String requestId = httpServletRequest.getHeader(ObservabilityConstant.X_REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(ObservabilityConstant.X_REQUEST_ID, requestId);
        httpServletResponse.setHeader(ObservabilityConstant.X_REQUEST_ID, requestId);
        httpServletResponse.setHeader(ObservabilityConstant.X_FW_REQUEST_ID, requestId);
        changedHeaderMap.put(ObservabilityConstant.X_REQUEST_ID, requestId);

        // traceparent
        String traceId = httpServletRequest.getHeader(ObservabilityConstant.TRACEPARENT);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(ObservabilityConstant.TRACE_ID, traceId);
        httpServletResponse.setHeader(ObservabilityConstant.TRACEPARENT, traceId);
        httpServletResponse.setHeader(ObservabilityConstant.X_FW_TRACE_ID, traceId);
        changedHeaderMap.put(ObservabilityConstant.TRACEPARENT, traceId);

        //uri
        MDC.put(ObservabilityConstant.URI, httpServletRequest.getRequestURI());

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID) != null) {
            MDC.put(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_ORG_ID) != null) {
            MDC.put(ObservabilityConstant.X_FW_AUTH_ORG_ID,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_ORG_ID));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_USER_ID) != null) {
            MDC.put(ObservabilityConstant.X_FW_AUTH_USER_ID,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_USER_ID));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_GROUP_ID) != null) {
            MDC.put(ObservabilityConstant.X_FW_AUTH_GROUP_ID,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_GROUP_ID));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_FREDDY_ADDONS) != null) {
            MDC.put(ObservabilityConstant.X_FW_FREDDY_ADDONS,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_FREDDY_ADDONS));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_DOMAIN) != null) {
            MDC.put(ObservabilityConstant.X_FW_AUTH_DOMAIN,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_AUTH_DOMAIN));
        }

        if (changedHeaderMap.get(ObservabilityConstant.X_FW_BUNDLE_ID) != null) {
            MDC.put(ObservabilityConstant.X_FW_BUNDLE_ID,
                    changedHeaderMap.get(ObservabilityConstant.X_FW_BUNDLE_ID));
        }

        String model = httpServletRequest.getHeader(ObservabilityConstant.AI_MODEL);
        if (model != null) {
            MDC.put(ObservabilityConstant.MODEL, model);
        }

        String aiServiceVersion = httpServletRequest.getHeader(ObservabilityConstant.AI_SERVICE_VERSION);
        if (model != null) {
            MDC.put(ObservabilityConstant.AI_SERVICE_VERSION, aiServiceVersion);
        }

        String dynamicHeaders = httpServletRequest.getHeader(ObservabilityConstant.X_FW_DYNAMIC_HEADERS);
        if (dynamicHeaders != null) {
            MDC.put(ObservabilityConstant.X_FW_DYNAMIC_HEADERS, dynamicHeaders);
        }

        String pltfrm = null;
        for (PlatformEnum platformEnum : PlatformEnum.values()) {
            if (httpServletRequest.getRequestURI().contains(platformEnum.name())) {
                pltfrm = platformEnum.name();
                break;
            }
        }
        if (pltfrm != null) {
            MDC.put(ObservabilityConstant.PLATFORM, pltfrm);
        }

        // x-fwi-dialogue-id
        if (isDialogueRequest) {
            String xFwiDialogueId = httpServletRequest.getHeader(ObservabilityConstant.X_FW_DIALOGUE_ID);
            if (xFwiDialogueId == null || xFwiDialogueId.isEmpty()) {
                xFwiDialogueId = UUID.randomUUID().toString();
            }
            MDC.put(ObservabilityConstant.X_FW_DIALOGUE_ID, xFwiDialogueId);
            httpServletResponse.setHeader(ObservabilityConstant.X_FW_DIALOGUE_ID, xFwiDialogueId);
            changedHeaderMap.put(ObservabilityConstant.X_FW_DIALOGUE_ID, xFwiDialogueId);
        }
        MDC.put(ObservabilityConstant.AI_PLATFORM_HEADERS, AICommonHelper.headerToJsonString(changedHeaderMap));
    }
}
