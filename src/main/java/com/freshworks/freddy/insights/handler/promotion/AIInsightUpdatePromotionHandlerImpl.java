package com.freshworks.freddy.insights.handler.promotion;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.entity.AIEntityInterface;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component(AIHandlerConstant.AI_INSIGHT_UPDATE_PROMOTE_STRATEGY)
public class AIInsightUpdatePromotionHandlerImpl
        extends AbstractAIPromotionHandler<List<AIPromoteMO>, Void> {
    public Void executeStrategy(List<AIPromoteMO> aiPromoteMOList) {
        super.execute(aiPromoteMOList, AIInsightConstant.PROMOTE_UPDATE_INSIGHT_SUBJECT);
        return null;
    }

    @Override
    public AIEntityInterface getEntity(Object entityObject) {
        return (AIInsightEntity) entityObject;
    }

    @Override
    public String getURL(String regionHostUrl, AIEntityInterface entityInterface) {
        return String.format("%s/v1/insight/%s", regionHostUrl, entityInterface.getId());
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
