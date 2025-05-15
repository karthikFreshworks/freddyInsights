package com.freshworks.freddy.insights.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${redis.host}")
    private String redisHostName;
    @Value("${redis.password}")
    private String redisPassword;
    @Value("${redis.port}")
    private Integer redisPort;
    @Value("${redis.connection-pool-size}")
    private int connectionPoolSize;
    @Value("${redis.max-wait-millis}")
    private int maxIdleTime;
    @Value("${redis.max-idle-time}")
    private int minIdleTime;
    @Value("${redis.min-idle-time}")
    private int maxWaitMillis;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHostName);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);
        return new JedisConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public JedisPool getJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMinIdle(minIdleTime);
        jedisPoolConfig.setMaxIdle(maxIdleTime);
        jedisPoolConfig.setMaxTotal(connectionPoolSize);
        jedisPoolConfig.setJmxEnabled(false);
        return new JedisPool(jedisPoolConfig,
                redisHostName,
                redisPort,
                Protocol.DEFAULT_TIMEOUT,
                StringUtils.defaultIfEmpty(redisPassword, null));
    }
}
