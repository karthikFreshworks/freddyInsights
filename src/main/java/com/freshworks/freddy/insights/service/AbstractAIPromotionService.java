package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.dto.promotion.AIPromoteDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.handler.addon.AbstractAIAddonHandler;
import com.freshworks.freddy.insights.handler.promotion.AbstractAIPromotionHandler;
import com.freshworks.freddy.insights.modelobject.AIFreddyAddonMO;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractAIPromotionService<T> extends AbstractAICommonService<T> {
    protected Map<String, AbstractAIPromotionHandler<List<AIPromoteMO>, Void>> promotionHandler;

    protected Map<String, AbstractAIAddonHandler<AIFreddyAddonMO>> addonHandlerMap;

    @Autowired
    public void setPromotionHandler(Map<String, AbstractAIPromotionHandler<List<AIPromoteMO>,
            Void>> promotionHandler) {
        this.promotionHandler = promotionHandler;
    }

    @Autowired
    public void setAddonHandlerMap(Map<String, AbstractAIAddonHandler<AIFreddyAddonMO>> addonHandlerMap) {
        this.addonHandlerMap = addonHandlerMap;
    }

    protected abstract void promoteAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException;

    protected abstract void promoteUpdateAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException;

    protected abstract List<Object> promoteAttributesToEntities(List<AIPromoteDTO.Attribute> attributes);

    protected List<AIPromoteMO> getAiPromoteMOList(List<AIPromoteDTO> aiPromoteDTOs) {
        return aiPromoteDTOs.stream()
                .map(promoteDTO -> AIPromoteMO.builder()
                        .region(promoteDTO.getRegion())
                        .authToken(promoteDTO.getAuthToken())
                        .entityList(promoteAttributesToEntities(promoteDTO.getAttributes()))
                        .build())
                .collect(Collectors.toList());
    }

    protected Map<String, String> getIdMappedUrlFromAttributesList(List<AIPromoteDTO.Attribute> attributes) {
        return attributes.stream()
                .filter(attribute -> attribute.getUrl() != null)
                .collect(Collectors.toMap(AIPromoteDTO.Attribute::getId, AIPromoteDTO.Attribute::getUrl));
    }

    protected Map<String, Map<String, String>> getIdMappedHeadersFromList(List<AIPromoteDTO.Attribute> attributes) {
        return attributes.stream()
                .filter(attribute -> attribute.getHeader() != null)
                .collect(Collectors.toMap(AIPromoteDTO.Attribute::getId, AIPromoteDTO.Attribute::getHeader));
    }
}
