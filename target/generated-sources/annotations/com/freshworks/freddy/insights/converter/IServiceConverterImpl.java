package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import com.freshworks.freddy.insights.dto.service.AIServiceCreateDTO;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-14T14:21:02+0530",
    comments = "version: 1.5.1.Final, compiler: javac, environment: Java 21.0.6 (Eclipse Adoptium)"
)
public class IServiceConverterImpl implements IServiceConverter {

    @Override
    public AIServiceCreateDTO convertToDTO(AIServiceEntity target) {
        if ( target == null ) {
            return null;
        }

        AIServiceCreateDTO.AIServiceCreateDTOBuilder<?, ?> aIServiceCreateDTO = AIServiceCreateDTO.builder();

        aIServiceCreateDTO.rule( target.getRule() );
        aIServiceCreateDTO.responseParser( target.getResponseParser() );
        aIServiceCreateDTO.requestParser( target.getRequestParser() );
        TreeSet<AIServiceBaseDTO.Templates> treeSet = target.getTemplates();
        if ( treeSet != null ) {
            aIServiceCreateDTO.templates( new TreeSet<AIServiceBaseDTO.Templates>( treeSet ) );
        }
        Map<String, String> map = target.getHeader();
        if ( map != null ) {
            aIServiceCreateDTO.header( new LinkedHashMap<String, String>( map ) );
        }
        aIServiceCreateDTO.url( target.getUrl() );
        aIServiceCreateDTO.method( target.getMethod() );
        aIServiceCreateDTO.service( target.getService() );
        aIServiceCreateDTO.description( target.getDescription() );
        List<AIServiceBaseDTO.Param> list = target.getParams();
        if ( list != null ) {
            aIServiceCreateDTO.params( new ArrayList<AIServiceBaseDTO.Param>( list ) );
        }
        aIServiceCreateDTO.template( target.getTemplate() );
        aIServiceCreateDTO.version( target.getVersion() );
        aIServiceCreateDTO.tenant( target.getTenant() );

        return aIServiceCreateDTO.build();
    }
}
