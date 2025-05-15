package com.freshworks.freddy.insights.service.errormapping;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dao.ErrorMappingDao;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import com.freshworks.freddy.insights.event.Event;
import com.freshworks.freddy.insights.event.RedisEventPublisher;
import com.freshworks.freddy.insights.event.cache.CacheEvent;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorMappingService {
    private final ErrorMappingDao errorMappingDao;

    private final RedisEventPublisher eventPublisher;

    public ErrorMappingEntity getErrorForTenant(TenantEnum tenantEnum) {
        return errorMappingDao.findByTenant(tenantEnum).orElseGet(ErrorMappingEntity::new);
    }

    public ErrorMappingEntity updateErrorForTenant(TenantEnum tenantEnum, ErrorMappingRequest request) {
        if (!tenantEnum.equals(request.getTenant())) {
            throw new AIResponseStatusException(
                    String.format(ExceptionConstant.NOT_VALID_TENANT_FORMATTER, tenantEnum),
                    HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }
        ErrorMappingEntity errorMappingEntity = getErrorForTenant(tenantEnum);
        errorMappingEntity.setTenant(tenantEnum);
        errorMappingEntity.setErrors(request.getErrors());
        errorMappingEntity = errorMappingDao.save(errorMappingEntity);
        eventPublisher.publish(getCacheClearPayload(errorMappingEntity), Event.EventType.INVALIDATE_CACHE);
        return errorMappingEntity;
    }

    private static List<CacheEvent> getCacheClearPayload(ErrorMappingEntity entity) {
        return List.of(
                new CacheEvent(ErrorMappingDao.ERROR_MAPPING_CACHE_NAME, entity.getTenant().name()));
    }
}
