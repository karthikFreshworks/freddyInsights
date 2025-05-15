package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.prompt.AIPromptCreateDTO;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import org.mapstruct.Mapper;

@Mapper
public interface IPromptConverter {
    AIPromptEntity convertToEntity(AIPromptCreateDTO promptCreateDTO);
}
