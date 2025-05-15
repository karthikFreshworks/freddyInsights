package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.converter.AIBundleConverter;
import com.freshworks.freddy.insights.dto.bundle.AIBundleCreateDTO;
import com.freshworks.freddy.insights.dto.bundle.AIBundleUpdateDTO;
import com.freshworks.freddy.insights.entity.AIBundleEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.repository.AIBundleRepository;
import com.freshworks.freddy.insights.validator.AICommonValidateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AIBundleService {
    private final AIBundleRepository aiBundleRepository;

    @CachePut(value = "bundle", key = "#result.id")
    public AIBundleEntity createBundle(AIBundleCreateDTO aiBundleCreateDTO) throws AIResponseStatusException {
        var aiBundle = aiBundleRepository.findOneByBundle(aiBundleCreateDTO.getBundle());
        if (aiBundle.isPresent()) {
            AICommonValidateException.conflictDataException(ExceptionConstant.DUPLICATE_RECORD_FOR_ID);
        }
        var aiBundleEntity = AIBundleConverter.convertToAIBundleEntity(aiBundleCreateDTO);
        log.info("aiBundleEntity {}", aiBundleEntity);
        return aiBundleRepository.save(aiBundleEntity);
    }

    public List<AIBundleEntity> getAllBundles() {
        return aiBundleRepository.findAll();
    }

    @Cacheable(value = "bundle", key = "#id", condition = "#id!=null", unless = "#result==null")
    public AIBundleEntity getBundle(String id) throws AIResponseStatusException {
        return aiBundleRepository.findById(id)
                .orElseThrow(() -> new AIResponseStatusException(
                        String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id),
                        HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DOES_NOT_EXIST));
    }

    public AIBundleEntity getBundleByName(String bundle) throws AIResponseStatusException {
        return aiBundleRepository.findOneByBundle(bundle)
                .orElseThrow(() -> new AIResponseStatusException(
                        String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, bundle),
                        HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DOES_NOT_EXIST));
    }

    @CachePut(value = "bundle", key = "#id", condition = "#id!=null")
    public AIBundleEntity updateBundle(String id, AIBundleUpdateDTO aiBundleUpdateDTO)
            throws AIResponseStatusException {
        var aiBundleRegEntity = aiBundleRepository.findById(id);

        if (aiBundleRegEntity.isPresent()) {
            var aiBundleEntity =
                    AIBundleConverter.convertToAIBundleEntity(aiBundleRegEntity.get(), aiBundleUpdateDTO);
            return aiBundleRepository.save(aiBundleEntity);
        } else {
            AICommonValidateException.notFoundException(
                    String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id));
            // The below line will never be reached
            return null;
        }
    }

    @CacheEvict(value = "bundle", key = "#id", condition = "#id!=null")
    public void deleteBundle(String id) {
        var delRecord = aiBundleRepository.deleteBundleById(id);
        if (delRecord != 1) {
            AICommonValidateException.notFoundException(
                    String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id));
        }
        log.info("AIBundle record for id: {}, got deleted: {}", id, delRecord);
    }

    @CacheEvict(value = "bundle", allEntries = true, beforeInvocation = true)
    public void deleteAllBundleCache() {
        log.info("All the entries in the bundle cache are deleted.");
    }
}
