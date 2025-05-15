package com.freshworks.freddy.insights.config.filter;

import com.freshworks.freddy.insights.dao.ErrorMappingDao;
import com.freshworks.freddy.insights.dao.ServiceAdaptorDao;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class CacheManagerConfig {
    public static final String IN_MEMORY_CACHE = "inMemory";

    @Primary
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        return RedisCacheManager.builder(factory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration("service",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(7)))
                .withCacheConfiguration("model",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(7)))
                .build();
    }

    @Bean(IN_MEMORY_CACHE)
    public CacheManager inMemory() {
        return new ConcurrentMapCacheManager(
                ServiceAdaptorDao.SERVICE_ADAPTOR_BY_TENANT_AND_TYPE_CACHE,
                ServiceAdaptorDao.SERVICE_ADAPTOR_BY_ID_CACHE_NAME,
                ServiceAdaptorDao.SERVICE_ADAPTOR_BY_TENANT_CACHE_NAME,
                ErrorMappingDao.ERROR_MAPPING_CACHE_NAME);
    }
}
