package com.freshworks.freddy.insights.service.adaptor;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ServiceAdaptorUpdateRequest {
    @NotNull
    private Map<String, Object> validations;

    @NotNull
    private Map<PlatformEnum, ServiceAdaptorEntity.PlatformMapping> platformMappings;
}
