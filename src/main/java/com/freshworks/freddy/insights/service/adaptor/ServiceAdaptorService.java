package com.freshworks.freddy.insights.service.adaptor;

import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dao.ServiceAdaptorDao;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import com.freshworks.freddy.insights.event.Event;
import com.freshworks.freddy.insights.event.RedisEventPublisher;
import com.freshworks.freddy.insights.event.cache.CacheEvent;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.DuplicateEntityException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.validator.AIServiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
@RequiredArgsConstructor
public class ServiceAdaptorService {
    public static final String ID_FORMAT = "%s-%s";
    private final ServiceAdaptorDao serviceAdaptorDao;

    private final AIServiceValidator validator;

    private final RedisEventPublisher eventPublisher;

    public ServiceAdaptorEntity createServiceAdaptor(ServiceAdaptorRequest serviceAdaptorRequest) {
        String id = getId(serviceAdaptorRequest);
        Optional<ServiceAdaptorEntity> optionalServiceAdaptorEntity = serviceAdaptorDao.findByTenantAndName(
                serviceAdaptorRequest.getTenant(), serviceAdaptorRequest.getName());
        if (optionalServiceAdaptorEntity.isPresent()) {
            throw new DuplicateEntityException("id", serviceAdaptorRequest.getName());
        }
        ServiceAdaptorEntity serviceAdaptorEntity = buildServiceAdaptorEntity(serviceAdaptorRequest, id);
        validateTypeField(serviceAdaptorRequest, serviceAdaptorEntity);
        serviceAdaptorEntity = serviceAdaptorDao.save(serviceAdaptorEntity);
        publishCacheClearEvent(serviceAdaptorEntity);
        return serviceAdaptorEntity;
    }

    private void validateTypeField(ServiceAdaptorRequest serviceAdaptorRequest, ServiceAdaptorEntity entity) {
        if (ServiceAdaptorEntity.ServiceAdaptorType.DEFAULT.equals(entity.getType())) {
            boolean exists = serviceAdaptorDao.existsByTenantAndTypeAndIdNot(serviceAdaptorRequest.getTenant(),
                    ServiceAdaptorEntity.ServiceAdaptorType.DEFAULT, entity.getId());
            if (exists) {
                throw new DuplicateEntityException("type", "default");
            }
        }
    }

    private void publishCacheClearEvent(ServiceAdaptorEntity serviceAdaptorEntity) {
        eventPublisher.publish(getCacheClearPayload(serviceAdaptorEntity), Event.EventType.INVALIDATE_CACHE);
    }

    private static List<CacheEvent> getCacheClearPayload(ServiceAdaptorEntity entity) {
        return List.of(
                new CacheEvent(ServiceAdaptorDao.SERVICE_ADAPTOR_BY_TENANT_AND_TYPE_CACHE, null),
                new CacheEvent(ServiceAdaptorDao.SERVICE_ADAPTOR_BY_ID_CACHE_NAME, entity.getId()),
                new CacheEvent(ServiceAdaptorDao.SERVICE_ADAPTOR_BY_TENANT_CACHE_NAME, entity.getTenant().name()));
    }

    private static ServiceAdaptorEntity buildServiceAdaptorEntity(ServiceAdaptorRequest request, String id) {
        return ServiceAdaptorEntity.builder()
                .id(id)
                .name(request.getName())
                .tenant(request.getTenant())
                .validations(request.getValidations())
                .platformMappings(request.getPlatformMappings())
                .type(defaultIfNull(request.getType(), ServiceAdaptorEntity.ServiceAdaptorType.CUSTOM))
                .build();
    }

    public ServiceAdaptorEntity updateServiceAdaptor(String id, ServiceAdaptorRequest serviceAdaptorRequest) {
        ServiceAdaptorEntity serviceAdaptorEntity = serviceAdaptorDao.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        validator.validateRequestPathTenant(serviceAdaptorEntity.getTenant());
        if (serviceAdaptorEntity.getTenant() != serviceAdaptorRequest.getTenant()) {
            throw new AIResponseStatusException("Tenant provided in the request body doesn't match with adaptor tenant",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }
        validateTypeField(serviceAdaptorRequest, serviceAdaptorEntity);
        serviceAdaptorEntity.setValidations(serviceAdaptorRequest.getValidations());
        serviceAdaptorEntity.setPlatformMappings(serviceAdaptorRequest.getPlatformMappings());
        serviceAdaptorEntity =  serviceAdaptorDao.save(serviceAdaptorEntity);
        publishCacheClearEvent(serviceAdaptorEntity);
        return serviceAdaptorEntity;
    }

    public ServiceAdaptorEntity getServiceAdaptor(TenantEnum tenantEnum, String id, AccessType accessType) {
        Optional<ServiceAdaptorEntity> adaptorEntity = (accessType == AccessType.SUPER_ADMIN)
                ? serviceAdaptorDao.findById(id) : serviceAdaptorDao.findByIdAndTenant(id, tenantEnum);
        return adaptorEntity.orElseThrow(() -> new ResourceNotFoundException("Given Service Adaptor Not Found"));
    }

    public List<ServiceAdaptorEntity> getAllServiceAdaptors(TenantEnum tenantEnum, AccessType accessType) {
        return accessType == AccessType.SUPER_ADMIN ? serviceAdaptorDao.findAll() :
                serviceAdaptorDao.findByTenant(tenantEnum);
    }

    private String getId(ServiceAdaptorRequest request) {
        return String.format(ID_FORMAT, request.getTenant().name(), request.getName());
    }
}
