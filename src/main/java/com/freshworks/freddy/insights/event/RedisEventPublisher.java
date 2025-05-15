package com.freshworks.freddy.insights.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.freddy.insights.config.RedisListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisEventPublisher {
    private final RedisTemplate<String, String> template;

    private final String pod;

    private final ObjectMapper objectMapper;

    public RedisEventPublisher(RedisConnectionFactory connectionFactory,
                               @Value("${pod.name}") String pod,
                               ObjectMapper objectMapper) {
        this.template = new StringRedisTemplate(connectionFactory);
        this.pod = pod;
        this.objectMapper = objectMapper;
    }

    public void publish(Object payload, Event.EventType eventType) {
        Event event = Event.builder()
                .payload(payload)
                .eventType(eventType)
                .pod(pod)
                .build();
        try {
            template.convertAndSend(RedisListenerConfig.EVENT_BUS, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Cache Event Push Failed :: Payload {} EventType {}", payload, eventType);
        }
    }
}
