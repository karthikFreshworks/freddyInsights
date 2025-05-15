package com.freshworks.freddy.insights.handler.llm.header;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component(AIHandlerConstant.GOOGLE_HEADER_STRATEGY)
public class GoogleLLMHeaderHandlerImpl extends AbstractLLMHeaderHandler {
    @Override
    protected String getAccessToken(String redisKey, String redisValue) {
        try {
            if (redisKey.contains(AIServiceConstant.GOOGLE_TOKEN)) {
                RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate();
                String token = (String) redisTemplate.opsForValue().get(redisKey);
                if (token == null || redisTemplate.getExpire(redisKey) < 0) {
                    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                    GoogleCredential credentials = GoogleCredential.fromStream(
                            new ByteArrayInputStream(redisValue.getBytes()),
                            httpTransport, jsonFactory);
                    credentials = credentials.createScoped(Collections.singleton(AIServiceConstant.VECTOR_SCOPED));
                    credentials.refreshToken();
                    token = credentials.getAccessToken();
                    redisTemplate.opsForValue().set(redisKey, token, 1, TimeUnit.HOURS);
                }
                return String.format("Bearer %s", token);
            }
            return redisValue;
        } catch (Exception e) {
            log.error("Error occurred while getting access token for Google header: {}", ExceptionHelper.stackTrace(e));
            return null;
        }
    }
}
