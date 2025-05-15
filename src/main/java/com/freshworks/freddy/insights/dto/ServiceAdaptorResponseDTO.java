package com.freshworks.freddy.insights.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ServiceAdaptorResponseDTO {
    private String id;

    private TenantEnum tenant;

    private String name;

    private Map<String, Object> validations;

    private ServiceAdaptorEntity.ServiceAdaptorType type = ServiceAdaptorEntity.ServiceAdaptorType.CUSTOM;

    private Map<PlatformEnum, ServiceAdaptorEntity.PlatformMapping> platformMappings;
}
