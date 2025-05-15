package com.freshworks.freddy.insights.dao;

import com.freshworks.freddy.insights.config.filter.CacheManagerConfig;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import com.freshworks.freddy.insights.repository.ErrorMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ErrorMappingDao {
    public static final String ERROR_MAPPING_CACHE_NAME = "errorMappingCache";
    private final ErrorMappingRepository repository;

    @Cacheable(cacheNames = ERROR_MAPPING_CACHE_NAME, key = "#tenant.name()",
            cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    public Optional<ErrorMappingEntity> findByTenant(TenantEnum tenant) {
        return repository.findByTenant(tenant);
    }

    @CacheEvict(cacheNames = ERROR_MAPPING_CACHE_NAME, key = "#entity.tenant.name()",
            cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    public ErrorMappingEntity save(ErrorMappingEntity entity) {
        return repository.save(entity);
    }
}
