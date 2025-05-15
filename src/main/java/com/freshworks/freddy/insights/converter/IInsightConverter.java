package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.insight.AIInsightCreateDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightsDismissDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.modelobject.central.AIInsightCentralPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface IInsightConverter {
    AIInsightEntity convertToEntity(AIInsightCreateDTO insightCreateDTO);

    @Mapping(target = "insightId",ignore = true)
    AIInsightCentralPayload prepareInsightPayload(AIInsightEntity insightEntity);

    AIInsightsDismissDTO prepareDismissPayload(String insightId);
}
