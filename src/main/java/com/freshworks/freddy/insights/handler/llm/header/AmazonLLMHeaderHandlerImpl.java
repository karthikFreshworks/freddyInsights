package com.freshworks.freddy.insights.handler.llm.header;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.AMAZON_HEADER_STRATEGY)
public class AmazonLLMHeaderHandlerImpl extends AbstractLLMHeaderHandler {
    @Override
    protected String getAccessToken(String redisKey, String redisValue) {
        return redisValue;
    }
}
