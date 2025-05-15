package com.freshworks.freddy.insights.handler.ratelimit;

import com.freshworks.freddy.insights.config.RedisClient;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
@Setter
public class RateLimitHandlerImpl {
    private final RedisClient redisClient;

    @Autowired
    public RateLimitHandlerImpl(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public RateLimitResponse executeRateLimit(String luaScript, String luaScriptSha, int luaToken) {
        String tenant = MDC.get(ObservabilityConstant.TENANT);
        String model = MDC.get(ObservabilityConstant.MODEL);
        String accountId = MDC.get(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID);

        List<String> luaKeys = new LinkedList<>();
        String tenantkey = String.format("rl:%s", tenant);
        String maxTenantkey = String.format("max_tokens_%s", tenant);
        String customerKey = String.format("rl:%s:%s", tenant, accountId);
        String maxCustomerkey = String.format("max_tokens_%s_%s", tenant, accountId);
        String modelKey = String.format("rl:%s:%s:%s", tenant, accountId, model);
        String maxModelKey = String.format("max_tokens:%s:%s:%s", tenant, accountId, model);

        luaKeys.add(tenantkey);
        luaKeys.add(maxTenantkey);
        luaKeys.add(customerKey);
        luaKeys.add(maxCustomerkey);
        luaKeys.add(modelKey);
        luaKeys.add(maxModelKey);

        List<Integer> luaArgs = new LinkedList<>();
        // Don't add tokens during request flow
        luaArgs.add(luaToken);

        Pipeline pipeline = redisClient.getPipeline();
        Response<Object> evalResult = null;
        try {
            evalResult = pipeline.evalsha(luaScriptSha, luaKeys,
                    luaArgs.stream().map(String::valueOf).collect(Collectors.toList()));
        } catch (JedisNoScriptException exception) {
            log.error("Evalsha failed: {}", exception.getMessage());
            luaScriptSha = redisClient.scriptLoad(luaScript);
        }
        pipeline.sync();
        return new RateLimitResponse((List) evalResult.get(), luaScriptSha);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class RateLimitResponse {
        private List<Object> result;
        private String luaScriptSha;
    }
}
