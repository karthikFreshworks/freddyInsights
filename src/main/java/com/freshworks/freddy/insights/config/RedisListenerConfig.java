package com.freshworks.freddy.insights.config;

import com.freshworks.freddy.insights.RedisEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisListenerConfig {
    public static final String EVENT_BUS = "eventbus";

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(EVENT_BUS));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisEventListener redisReceiver) {
        return new MessageListenerAdapter(redisReceiver, "onMessage");
    }
}
