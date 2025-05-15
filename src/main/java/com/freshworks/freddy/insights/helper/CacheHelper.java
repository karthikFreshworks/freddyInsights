package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.repository.AIServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheHelper {
    private AIServiceRepository aiServiceRepository;

    @Autowired
    public void setAiServiceRepository(AIServiceRepository aiServiceRepository) {
        this.aiServiceRepository = aiServiceRepository;
    }

    @Cacheable(value = "service", key = "#id", condition = "#id!=null")
    public AIServiceEntity getUnfilteredServiceById(String id) throws AIResponseStatusException {
        return aiServiceRepository.findById(id)
                .orElseThrow(() ->
                        new AIResponseStatusException(
                                String.format("%s : %s", ExceptionConstant.NO_RECORD_FOR_ID, id),
                                HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DOES_NOT_EXIST));
    }

    @CachePut(value = "service", key = "#id")
    public AIServiceEntity saveToCache(String id, AIServiceEntity aiServiceEntity) {
        return aiServiceEntity;
    }
}
