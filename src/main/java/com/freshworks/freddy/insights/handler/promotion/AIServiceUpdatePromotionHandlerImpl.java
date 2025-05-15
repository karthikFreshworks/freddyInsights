package com.freshworks.freddy.insights.handler.promotion;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIServiceConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.entity.AIEntityInterface;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component(AIHandlerConstant.AI_SERVICE_UPDATE_PROMOTE_STRATEGY)
public class AIServiceUpdatePromotionHandlerImpl
        extends AbstractAIPromotionHandler<List<AIPromoteMO>, Void> {
    public Void executeStrategy(List<AIPromoteMO> aiPromoteMOList) {
        super.execute(aiPromoteMOList, AIServiceConstant.PROMOTE_UPDATE_SERVICE_SUBJECT);
        return null;
    }

    @Override
    public AIEntityInterface getEntity(Object entityObject) {
        return (AIServiceEntity) entityObject;
    }

    @Override
    public String getURL(String regionHostUrl, AIEntityInterface entityInterface) {
        return String.format("%s/v1/ai-service/%s", regionHostUrl, entityInterface.getId());
    }

    @Override
    public ApiMethodEnum getRequestMethod() {
        return ApiMethodEnum.put;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }
}
