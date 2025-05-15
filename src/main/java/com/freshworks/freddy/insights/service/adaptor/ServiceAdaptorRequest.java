package com.freshworks.freddy.insights.service.adaptor;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class ServiceAdaptorRequest {
    @NotBlank
    @Pattern(regexp = "[a-zA-Z-]+")
    private String name;

    @NotNull
    private TenantEnum tenant;

    @NotNull
    private Map<String, Object> validations;

    private ServiceAdaptorEntity.ServiceAdaptorType type = ServiceAdaptorEntity.ServiceAdaptorType.CUSTOM;

    @NotNull
    private Map<PlatformEnum, ServiceAdaptorEntity.PlatformMapping> platformMappings;
}
