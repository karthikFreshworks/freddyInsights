package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.AITenantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AITenantRepository extends MongoRepository<AITenantEntity, String> {
    @Query("{$and :[{tenant: ?0},{email: ?1}]}")
    Optional<AITenantEntity> findTenantByTenantNameAndEmail(TenantEnum tenantName, String email);

    @Query("{$or :[{adminKey: ?0},{userKey: ?1}]}")
    Optional<AITenantEntity> findTenantByAdminKeyOrUserKey(String adminKey, String userKey);

    @Query("{$and :[{tenant: ?0}]}")
    List<AITenantEntity> findTenantByTenantName(TenantEnum tenantName);

    @Query(value = "{id: ?0}", delete = true)
    Long deleteTenantById(String id);
}
