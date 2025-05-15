package com.freshworks.freddy.insights.dao;

import com.freshworks.freddy.insights.config.filter.CacheManagerConfig;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import com.freshworks.freddy.insights.repository.ServiceAdaptorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServiceAdaptorDao {
    public static final String SERVICE_ADAPTOR_BY_TENANT_CACHE_NAME = "serviceAdaptorByTenant";
    public static final String SERVICE_ADAPTOR_BY_ID_CACHE_NAME = "serviceAdaptorById";
    public static final String SERVICE_ADAPTOR_BY_TENANT_AND_TYPE_CACHE = "serviceAdaptorByTenantAndType";

    private static final String SELF = "T(com.freshworks.freddy.insights.dao.ServiceAdaptorDao)";

    private final ServiceAdaptorRepository repository;

    @Cacheable(cacheNames = SERVICE_ADAPTOR_BY_TENANT_CACHE_NAME, key = "#tenant.name()",
            cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    public List<ServiceAdaptorEntity> findByTenant(TenantEnum tenant) {
        return repository.findByTenant(tenant);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = SERVICE_ADAPTOR_BY_TENANT_CACHE_NAME, key = "#entity.tenant.name()",
                cacheManager = CacheManagerConfig.IN_MEMORY_CACHE),
            @CacheEvict(cacheNames = SERVICE_ADAPTOR_BY_TENANT_AND_TYPE_CACHE,
                allEntries = true, cacheManager = CacheManagerConfig.IN_MEMORY_CACHE),
            @CacheEvict(cacheNames = SERVICE_ADAPTOR_BY_ID_CACHE_NAME, key = "#entity.id",
                cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    })
    public ServiceAdaptorEntity save(ServiceAdaptorEntity entity) {
        return repository.save(entity);
    }

    @Cacheable(cacheNames = SERVICE_ADAPTOR_BY_ID_CACHE_NAME, key = "#id",
            cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    public Optional<ServiceAdaptorEntity> findById(String id) {
        return repository.findById(id);
    }

    public Optional<ServiceAdaptorEntity> findByTenantAndName(TenantEnum tenant,
                                                              String name) {
        return repository.findByTenantAndName(tenant, name);
    }

    public List<ServiceAdaptorEntity> findAll() {
        return repository.findAll();
    }

    public Optional<ServiceAdaptorEntity> findByIdAndTenant(String id, TenantEnum tenantEnum) {
        return repository.findByIdAndTenant(id, tenantEnum);
    }

    @Cacheable(cacheNames = SERVICE_ADAPTOR_BY_TENANT_AND_TYPE_CACHE, key = SELF + ".cacheKey(#tenantEnums, #type)",
            cacheManager = CacheManagerConfig.IN_MEMORY_CACHE)
    public List<ServiceAdaptorEntity> findByTenantInAndType(Collection<TenantEnum> tenantEnums,
                                                            ServiceAdaptorEntity.ServiceAdaptorType type) {
        return repository.findByTenantInAndType(tenantEnums, type);
    }

    public static String cacheKey(Collection<TenantEnum> tenants, ServiceAdaptorEntity.ServiceAdaptorType name) {
        String tenantString = tenants.stream().sorted().map(TenantEnum::name).collect(Collectors.joining("-"));
        return "%s::%s".formatted(tenantString, name);
    }

    public boolean existsByTenantAndTypeAndIdNot(TenantEnum tenant, ServiceAdaptorEntity.ServiceAdaptorType type,
                                                 String id) {
        return repository.existsByTenantAndTypeAndIdNot(tenant, type, id);
    }
}
