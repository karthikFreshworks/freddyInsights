package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErrorMappingRepository extends MongoRepository<ErrorMappingEntity, String> {
    Optional<ErrorMappingEntity> findByTenant(TenantEnum tenantName);
}
