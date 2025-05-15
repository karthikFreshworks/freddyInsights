package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AIServiceRepository extends MongoRepository<AIServiceEntity, String> {
    @Query("{tenant : {$in :[?0, ?1]}}")
    List<AIServiceEntity> findByGlobalAndProductTenant(TenantEnum global, TenantEnum productTenant);

    @Query(value = "{id: ?0}", delete = true)
    Long deleteServiceById(String id);

    @Query(value = "{$and :[{id: ?0},{tenant: ?1}]}", delete = true)
    Long deleteServiceByIdAndTenant(String id, TenantEnum productTenant);

    void deleteAllByTenant(TenantEnum tenant);

    List<AIServiceEntity> findByTenantInAndIdIn(Collection<TenantEnum> tenantEnums, Collection<String> idIn);
}
