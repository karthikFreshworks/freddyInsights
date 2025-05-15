package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.service.AIServiceCreateDTO;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import org.mapstruct.Mapper;

@Mapper
public interface IServiceConverter {
    AIServiceCreateDTO convertToDTO(AIServiceEntity target);
}
