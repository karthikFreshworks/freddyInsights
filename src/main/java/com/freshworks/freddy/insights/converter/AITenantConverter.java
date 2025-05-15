package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.tenant.AITenantCreateDTO;
import com.freshworks.freddy.insights.dto.tenant.AITenantUpdateDTO;
import com.freshworks.freddy.insights.entity.AITenantEntity;

import java.time.Instant;
import java.util.UUID;

public class AITenantConverter {
    public static AITenantEntity convertToAITenantEntity(
            AITenantCreateDTO aiTenantCreateDTO) {
        return AITenantEntity.builder()
                .tenant(aiTenantCreateDTO.getTenant())
                .adminKey(String.format("%s-%s-%s", UUID.randomUUID(), Instant.now().toEpochMilli(), "admin-key"))
                .userKey(String.format("%s-%s-%s", UUID.randomUUID(), Instant.now().toEpochMilli(), "user-key"))
                .email(aiTenantCreateDTO.getEmail())
                .semanticCache(aiTenantCreateDTO.isSemanticCache())
                .build();
    }

    public static AITenantEntity convertToAITenantEntity(AITenantEntity aiTenantEntity,
                                                         AITenantUpdateDTO aiTenantUpdateDTO) {
        String adminKey = aiTenantEntity.getAdminKey();
        String userKey = aiTenantEntity.getUserKey();

        if (aiTenantUpdateDTO.getEmail() != null
                && !aiTenantEntity.getEmail().equals(aiTenantUpdateDTO.getEmail())) {
            adminKey = String.format("%s-%s-%s", UUID.randomUUID(), Instant.now().toEpochMilli(), "admin-key");
            userKey = String.format("%s-%s-%s", UUID.randomUUID(), Instant.now().toEpochMilli(), "user-key");
        }
        return AITenantEntity.builder()
                .id(aiTenantEntity.getId())
                .tenant(aiTenantEntity.getTenant())
                .adminKey(adminKey)
                .userKey(userKey)
                .email(aiTenantUpdateDTO.getEmail() != null ? aiTenantUpdateDTO.getEmail() : aiTenantEntity.getEmail())
                .semanticCache(aiTenantUpdateDTO.isSemanticCache() != aiTenantEntity.isSemanticCache()
                        ? aiTenantUpdateDTO.isSemanticCache() : aiTenantEntity.isSemanticCache())
                .build();
    }
}
