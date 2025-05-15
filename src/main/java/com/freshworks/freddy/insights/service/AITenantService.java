package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.converter.AITenantConverter;
import com.freshworks.freddy.insights.converter.EmailRequestConverter;
import com.freshworks.freddy.insights.dto.email.EmailRequestDTO;
import com.freshworks.freddy.insights.dto.tenant.AITenantCreateDTO;
import com.freshworks.freddy.insights.dto.tenant.AITenantUpdateDTO;
import com.freshworks.freddy.insights.entity.AITenantEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.EmailServiceHelper;
import com.freshworks.freddy.insights.repository.AITenantRepository;
import com.freshworks.freddy.insights.validator.AICommonValidateException;
import com.freshworks.freddy.insights.validator.AITenantValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AITenantService {
    private final AITenantRepository aiTenantRepository;
    private final EmailServiceHelper emailServiceHelper;
    private final EmailRequestConverter emailRequestConverter;
    private final AITenantValidator aiTenantValidator;

    @CachePut(value = "tenant", key = "#result.id")
    public AITenantEntity createTenant(AITenantCreateDTO aiTenantCreateDTO) throws AIResponseStatusException {
        aiTenantValidator.validateDuplicateTenantWithEmail(aiTenantRepository, aiTenantCreateDTO.getTenant(),
                aiTenantCreateDTO.getEmail());
        var aiTenantEntity = AITenantConverter.convertToAITenantEntity(aiTenantCreateDTO);
        aiTenantEntity.setSemanticCache(true);
        EmailRequestDTO requestBody = emailRequestConverter.convertEmailRequestDTO(aiTenantEntity);
        emailServiceHelper.send(requestBody);
        return aiTenantRepository.save(aiTenantEntity);
    }

    public List<AITenantEntity> getAllTenants(TenantEnum requestTenant) {
        if (requestTenant != null) {
            return aiTenantRepository.findTenantByTenantName(requestTenant);
        }
        return aiTenantRepository.findAll();
    }

    @Cacheable(value = "tenant", key = "#id", condition = "#id!=null", unless = "#result==null")
    public AITenantEntity getTenant(String id) throws AIResponseStatusException {
        return aiTenantRepository.findById(id)
                .orElseThrow(() -> {
                    AICommonValidateException.notFoundException(
                            String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id));
                    // The below line will never be reached
                    return null;
                });
    }

    @Cacheable(value = "tenant", key = "#authId", condition = "#authId!=null", unless = "#result==null")
    public AITenantEntity getTenantByAdminKeyOrUserKey(String authId) {
        var aiTenant = aiTenantRepository.findTenantByAdminKeyOrUserKey(authId, authId);
        return aiTenant.orElse(null);
    }

    @Caching(put = {
            @CachePut(value = "tenant", key = "#id", condition = "#id!=null"),
            @CachePut(value = "tenant", key = "#result.adminKey", condition = "#result.adminKey!=null"),
            @CachePut(value = "tenant", key = "#result.userKey", condition = "#result.userKey!=null")})
    public AITenantEntity updateTenant(String id, AITenantUpdateDTO aiTenantUpdateDTO)
            throws AIResponseStatusException {
        var aiTenantEntity = aiTenantRepository.findById(id).orElseThrow(() -> new AIResponseStatusException(
                String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id),
                HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DOES_NOT_EXIST));
        var aiUpdatedTenantEntity = AITenantConverter.convertToAITenantEntity(aiTenantEntity, aiTenantUpdateDTO);
        if (aiTenantUpdateDTO.getEmail() != null && !aiTenantEntity.getEmail().equals(aiTenantUpdateDTO.getEmail())) {
            aiTenantValidator.validateDuplicateTenantWithEmail(aiTenantRepository, aiTenantEntity.getTenant(),
                    aiTenantUpdateDTO.getEmail());
        }
        var tenant = aiTenantRepository.save(aiUpdatedTenantEntity);
        EmailRequestDTO requestBody = emailRequestConverter.convertEmailRequestDTO(tenant);
        emailServiceHelper.send(requestBody);
        return tenant;
    }

    @Caching(
            evict = {
                @CacheEvict(value = "tenant", key = "#id", condition = "#id!=null",
                        beforeInvocation = true),
                @CacheEvict(value = "tenant",
                        key = "#root.target.getTenant(#id).getAdminKey()", condition = "#id!=null",
                        beforeInvocation = true),
                @CacheEvict(value = "tenant",
                        key = "#root.target.getTenant(#id).getUserKey()", condition = "#id!=null",
                        beforeInvocation = true)
        })
    public void deleteTenant(String id) {
        var delRecord = aiTenantRepository.deleteTenantById(id);
        if (delRecord != 1) {
            throw new AIResponseStatusException(
                    String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id),
                    HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DELETION_FAILED);
        }
        log.info("AITenant record for id: {}, got deleted: {}", id, delRecord);
    }

    @CacheEvict(value = "tenant", allEntries = true, beforeInvocation = true)
    public void deleteAllTenantsInCache() {
        log.info("All the entries in the tenant cache are deleted.");
    }
}
