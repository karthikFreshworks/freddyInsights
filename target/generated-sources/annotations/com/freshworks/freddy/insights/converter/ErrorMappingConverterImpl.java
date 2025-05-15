package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.ErrorResponseDto;
import com.freshworks.freddy.insights.entity.ErrorMappingEntity;
import com.freshworks.freddy.insights.entity.error.mapping.ErrorDetail;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-14T14:21:02+0530",
    comments = "version: 1.5.1.Final, compiler: javac, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class ErrorMappingConverterImpl implements ErrorMappingConverter {

    @Override
    public ErrorResponseDto convertToErrorResponseDto(ErrorMappingEntity errorMappingEntity) {
        if ( errorMappingEntity == null ) {
            return null;
        }

        ErrorResponseDto errorResponseDto = new ErrorResponseDto();

        Map<String, ErrorDetail> map = errorMappingEntity.getErrors();
        if ( map != null ) {
            errorResponseDto.setErrors( new LinkedHashMap<String, ErrorDetail>( map ) );
        }

        return errorResponseDto;
    }
}
