package com.freshworks.freddy.insights.handler.llm.header;

import com.freshworks.freddy.insights.config.RedisConfig;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.helper.AppConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractLLMHeaderHandler {
    protected AppConfigHelper appConfigHelper;

    protected RedisConfig redisConfig;

    @Autowired
    public void setAppConfigHelper(AppConfigHelper appConfigHelper) {
        this.appConfigHelper = appConfigHelper;
    }

    @Autowired
    public void setRedisConfigHelper(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    protected abstract String getAccessToken(String redisKey, String redisValue);

    public String[] headerMapToArray(Map<String, String> headers) {
        List<String> headersArray = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                headersArray.add(header.getKey());
                String headerValue = AIServiceHelper.getTemplate(
                        appConfigHelper.getFreddyAIPlatformLLMSecrets(), header.getValue(), "%(", ")");
                headersArray.add(getAccessToken(header.getValue(), headerValue));
            }
        } else {
            headersArray.add("Content-Type");
            headersArray.add("application/json");
        }
        return headersArray.toArray(new String[0]);
    }
}
