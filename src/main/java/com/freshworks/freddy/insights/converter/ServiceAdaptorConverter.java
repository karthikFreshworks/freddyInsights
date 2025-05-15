package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.ServiceAdaptorResponseDTO;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Mapper
public interface ServiceAdaptorConverter {
    @Mapping(target = "type", expression = "java(getType(serviceAdaptorEntity))")
    ServiceAdaptorResponseDTO convertToServiceAdaptorResponseDto(ServiceAdaptorEntity serviceAdaptorEntity);

    default ServiceAdaptorEntity.ServiceAdaptorType getType(ServiceAdaptorEntity serviceAdaptorEntity) {
        return defaultIfNull(serviceAdaptorEntity.getType(), ServiceAdaptorEntity.ServiceAdaptorType.CUSTOM);
    }

    List<ServiceAdaptorResponseDTO> convertToAllServiceAdaptorResponseDto(List<ServiceAdaptorEntity> entities);
}
