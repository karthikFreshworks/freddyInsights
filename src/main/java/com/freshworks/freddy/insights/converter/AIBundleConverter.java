package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.bundle.AIBundleCreateDTO;
import com.freshworks.freddy.insights.dto.bundle.AIBundleUpdateDTO;
import com.freshworks.freddy.insights.entity.AIBundleEntity;

public class AIBundleConverter {
    public static AIBundleEntity convertToAIBundleEntity(
            AIBundleCreateDTO aiBundleCreateDTO) {
        return AIBundleEntity.builder()
                .tenantList(aiBundleCreateDTO.getTenantList())
                .bundle(aiBundleCreateDTO.getBundle())
                .tenantFilters(aiBundleCreateDTO.getTenantFilters())
                .build();
    }

    public static AIBundleEntity convertToAIBundleEntity(AIBundleEntity aiBundleEntity,
                                                         AIBundleUpdateDTO aiBundleUpdateDTO) {
        return AIBundleEntity.builder()
                .id(aiBundleEntity.getId())
                .bundle(aiBundleEntity.getBundle())
                .tenantList(aiBundleUpdateDTO.getTenantList())
                .tenantFilters(aiBundleUpdateDTO.getTenantFilters() != null
                        ? aiBundleUpdateDTO.getTenantFilters() : aiBundleEntity.getTenantFilters())
                .build();
    }
}
