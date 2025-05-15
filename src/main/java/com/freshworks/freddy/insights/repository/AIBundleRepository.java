package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.AIBundleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIBundleRepository extends MongoRepository<AIBundleEntity, String> {
    @Query("{ bundle: ?0 }")
    Optional<AIBundleEntity> findOneByBundle(String bundle);

    @Query(value = "{id: ?0}", delete = true)
    Long deleteBundleById(String id);

    List<AIBundleEntity> findByTenantListContaining(TenantEnum tenantEnum);
}
