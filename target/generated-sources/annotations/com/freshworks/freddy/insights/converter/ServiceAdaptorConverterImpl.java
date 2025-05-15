package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.dto.ServiceAdaptorResponseDTO;
import com.freshworks.freddy.insights.entity.ServiceAdaptorEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-14T14:21:02+0530",
    comments = "version: 1.5.1.Final, compiler: javac, environment: Java 21.0.6 (Eclipse Adoptium)"
)
public class ServiceAdaptorConverterImpl implements ServiceAdaptorConverter {

    @Override
    public ServiceAdaptorResponseDTO convertToServiceAdaptorResponseDto(ServiceAdaptorEntity serviceAdaptorEntity) {
        if ( serviceAdaptorEntity == null ) {
            return null;
        }

        ServiceAdaptorResponseDTO serviceAdaptorResponseDTO = new ServiceAdaptorResponseDTO();

        serviceAdaptorResponseDTO.setId( serviceAdaptorEntity.getId() );
        serviceAdaptorResponseDTO.setTenant( serviceAdaptorEntity.getTenant() );
        serviceAdaptorResponseDTO.setName( serviceAdaptorEntity.getName() );
        Map<String, Object> map = serviceAdaptorEntity.getValidations();
        if ( map != null ) {
            serviceAdaptorResponseDTO.setValidations( new LinkedHashMap<String, Object>( map ) );
        }
        Map<PlatformEnum, ServiceAdaptorEntity.PlatformMapping> map1 = serviceAdaptorEntity.getPlatformMappings();
        if ( map1 != null ) {
            serviceAdaptorResponseDTO.setPlatformMappings( new LinkedHashMap<PlatformEnum, ServiceAdaptorEntity.PlatformMapping>( map1 ) );
        }

        serviceAdaptorResponseDTO.setType( getType(serviceAdaptorEntity) );

        return serviceAdaptorResponseDTO;
    }

    @Override
    public List<ServiceAdaptorResponseDTO> convertToAllServiceAdaptorResponseDto(List<ServiceAdaptorEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ServiceAdaptorResponseDTO> list = new ArrayList<ServiceAdaptorResponseDTO>( entities.size() );
        for ( ServiceAdaptorEntity serviceAdaptorEntity : entities ) {
            list.add( convertToServiceAdaptorResponseDto( serviceAdaptorEntity ) );
        }

        return list;
    }
}
