package com.freshworks.freddy.insights.handler;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dao.ErrorMappingDao;
import com.freshworks.freddy.insights.dao.ServiceAdaptorDao;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import com.freshworks.freddy.insights.entity.error.mapping.ErrorDetail;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorHandler {
    private final ErrorMappingDao errorMappingDao;

    private final ServiceAdaptorDao serviceAdaptorDao;

    private final ExpressionEvaluator expressionEvaluator;

    public AIResponseStatusException resolveErrors(AIResponseStatusException e, AIServiceMO aiServiceMO) {
        String serviceAdaptorId = aiServiceMO.getServiceAdaptorId();
        Optional<ServiceAdaptorEntity> optionalServiceAdaptorEntity;
        if (serviceAdaptorId == null) {
            log.info("Service Adaptor Id not available");
            optionalServiceAdaptorEntity = getServiceAdaptorDefaultForTenant(aiServiceMO);
        } else {
            optionalServiceAdaptorEntity = serviceAdaptorDao.findById(serviceAdaptorId)
                .or(() -> getServiceAdaptorDefaultForTenant(aiServiceMO));
        }

        if (optionalServiceAdaptorEntity.isEmpty()) {
            log.info("Service Adaptor not available");
            return e;
        }

        ServiceAdaptorEntity serviceAdaptorEntity = optionalServiceAdaptorEntity.get();
        PlatformEnum platform = aiServiceMO.getPlatform();
        ServiceAdaptorEntity.PlatformMapping platformMapping = serviceAdaptorEntity
                .getPlatformMappings().get(platform);
        if (platformMapping == null) {
            log.warn("Service Adaptor doesn't have platform {}", platform);
            return e;
        }
        Map<String, ErrorDetail> errorDetailMap = getErrorDetailMap(aiServiceMO.getTenant());
        String errorCodeExpression = platformMapping.getErrorCodeMappings().get(e.getStatusCode().value());
        String expression = Optional.ofNullable(errorCodeExpression)
                .orElseGet(platformMapping::getGenericErrorCodeMapping);

        Map<String, Object> parsedErrorRequest = getParsedResponseBody(e.getReason());
        return getErrorDetail(expression, errorDetailMap, parsedErrorRequest, e).map(errorDetail ->
            new AIResponseStatusException(e.getApiError().getHttpStatus(), errorDetail.getMessage(),
                    errorDetail.getCode(), e.getApiError().getCause())).orElse(e);
    }

    private Optional<ServiceAdaptorEntity> getServiceAdaptorDefaultForTenant(AIServiceMO aiServiceMO) {
        ServiceAdaptorEntity optionalServiceAdaptorEntity = null;
        List<ServiceAdaptorEntity> adaptorEntityList = serviceAdaptorDao.findByTenantInAndType(
                List.of(aiServiceMO.getTenant(), TenantEnum.global), ServiceAdaptorEntity.ServiceAdaptorType.DEFAULT);
        for (ServiceAdaptorEntity serviceAdaptorEntity : adaptorEntityList) {
            optionalServiceAdaptorEntity = serviceAdaptorEntity;
            if (aiServiceMO.getTenant().equals(serviceAdaptorEntity.getTenant())) {
                break;
            }
        }
        return Optional.ofNullable(optionalServiceAdaptorEntity);
    }

    private Map<String, Object> getParsedResponseBody(String response) {
        try {
            return ObjectMapperHelper.readMapOfStrings(response);
        } catch (Exception e) {
            log.error("Unable to parse json {}", response, e);
            return Collections.emptyMap();
        }
    }

    private Map<String, ErrorDetail> getErrorDetailMap(TenantEnum tenantEnum) {
        Map<String, ErrorDetail> errorDetailMap = errorMappingDao.findByTenant(TenantEnum.global)
                .map(ErrorMappingEntity::getErrors)
                .map(HashMap::new)
                .orElseGet(HashMap::new);
        if (tenantEnum != TenantEnum.global) {
            errorMappingDao.findByTenant(tenantEnum)
                    .ifPresent(errorMappingEntity -> errorDetailMap.putAll(errorMappingEntity.getErrors()));
        }
        log.info("Error Details {}", errorDetailMap);
        return errorDetailMap;
    }

    private Optional<ErrorDetail> getErrorDetail(String expression,
                                                 Map<String, ErrorDetail> stringErrorDetailMap,
                                                 Map<String, Object> parsedErrorRequest,
                                                 AIResponseStatusException aiResponseStatusException) {
        if (expression == null) {
            return Optional.empty();
        }
        try {
            Map<String, Object> context = Map.of("response", parsedErrorRequest,
                    "exception", aiResponseStatusException);
            String errorCode = expressionEvaluator.evaluate(expression, context, String.class);
            return Optional.ofNullable(stringErrorDetailMap.get(errorCode));
        } catch (Exception e) {
            log.error("Unable to evaluate expression {}", expression, e);
            return Optional.empty();
        }
    }
}
