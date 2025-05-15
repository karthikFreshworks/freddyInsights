package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceAdaptorRepository extends MongoRepository<ServiceAdaptorEntity, String> {
    Optional<ServiceAdaptorEntity> findByTenantAndName(TenantEnum tenantEnum, String name);

    List<ServiceAdaptorEntity> findByTenant(TenantEnum tenantEnum);

    Optional<ServiceAdaptorEntity> findByIdAndTenant(String id, TenantEnum tenantEnum);

    List<ServiceAdaptorEntity> findByTenantInAndType(Collection<TenantEnum> tenantEnum,
                                                     ServiceAdaptorEntity.ServiceAdaptorType type);

    boolean existsByTenantAndTypeAndIdNot(TenantEnum tenant, ServiceAdaptorEntity.ServiceAdaptorType type, String id);
}
