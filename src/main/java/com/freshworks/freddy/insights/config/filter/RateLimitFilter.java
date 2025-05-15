package com.freshworks.freddy.insights.config.filter;

import com.freshworks.freddy.insights.constant.AIRequestConstant;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.ratelimit.RateLimitHandlerImpl;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.helper.FileHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(4)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    private final String requestLuaScriptSha;
    private final RateLimitHandlerImpl rateLimitHandlerImpl;
    private String requestLuaScript;

    @Autowired
    public RateLimitFilter(RateLimitHandlerImpl rateLimitHandlerImpl) throws IOException {
        this.rateLimitHandlerImpl = rateLimitHandlerImpl;
        this.requestLuaScript = FileHelper.getFileContent("scripts/RateLimit.lua");
        this.requestLuaScriptSha = rateLimitHandlerImpl.getRedisClient().scriptLoad(requestLuaScript);
    }

    @Override
    public void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                 FilterChain filterChain) throws ServletException, IOException {
        try {
            String model = httpServletRequest.getHeader("Ai-Model");
            if (model != null) {
                MDC.put("model", model);
            }
            handleRateLimit(httpServletResponse);
        } catch (AIResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Exception while handling rate limit : {}", exception.getMessage());
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return shouldNotFilterURIs(request);
    }

    public boolean shouldNotFilterURIs(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator")
                || request.getRequestURI().startsWith("/swagger-ui")
                || request.getRequestURI().startsWith("/v3/api-docs")
                || !AIServiceHelper.isAiServiceURL(request);
    }

    public void handleRateLimit(HttpServletResponse httpServletResponse) {
        var result = rateLimitHandlerImpl.executeRateLimit(requestLuaScript, requestLuaScriptSha, 0);
        this.requestLuaScript = result.getLuaScriptSha();
        if (!result.getResult().get(0).toString().equals("0")) {
            String error_message = String.format("Freddy AI:%s", result.getResult().get(2).toString());
            httpServletResponse.setHeader(AIRequestConstant.X_FW_RETRY_AFTER, result.getResult().get(1).toString());
            throw new AIResponseStatusException(error_message,
                    HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS);
        }
    }
}
