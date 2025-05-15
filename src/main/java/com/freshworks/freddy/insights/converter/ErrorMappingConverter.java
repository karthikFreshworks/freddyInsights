package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.ErrorResponseDto;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ErrorMappingConverter {
    ErrorResponseDto convertToErrorResponseDto(ErrorMappingEntity errorMappingEntity);
}
