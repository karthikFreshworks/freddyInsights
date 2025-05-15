package com.freshworks.freddy.insights.handler.ratelimit;

import com.freshworks.freddy.insights.helper.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class UpdateTokenUsageHandlerImpl {
    private final String tokenUpdateLuaScript;
    private final RateLimitHandlerImpl rateLimitHandlerImpl;
    private String tokenUpdateLuaScriptSha;

    @Autowired
    public UpdateTokenUsageHandlerImpl(RateLimitHandlerImpl rateLimitHandlerImpl) throws IOException {
        this.rateLimitHandlerImpl = rateLimitHandlerImpl;
        this.tokenUpdateLuaScript = FileHelper.getFileContent("scripts/RateLimitUpdate.lua");
        this.tokenUpdateLuaScriptSha = rateLimitHandlerImpl.getRedisClient().scriptLoad(tokenUpdateLuaScript);
    }

    public String calculateRemainingTokens() {
        try {
            final var totalToken =
                    (MDC.get("total_tokens") == null) ? 0 : Double.parseDouble(MDC.get("total_tokens"));
            var response = rateLimitHandlerImpl.executeRateLimit(tokenUpdateLuaScript,
                    tokenUpdateLuaScriptSha, (int) totalToken);
            tokenUpdateLuaScriptSha = response.getLuaScriptSha();
            String remaining_tokens = String.format("tenant:%s,customer:%s,model:%s", response.getResult().get(3),
                    response.getResult().get(4),
                    response.getResult().get(5));
            return remaining_tokens;
        } catch (Exception exception) {
            log.error("Exception while updating tokens : {}", exception);
        }
        return null;
    }
}
