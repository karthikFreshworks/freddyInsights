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
@Component(AIHandlerConstant.AI_INSIGHT_PROMOTE_STRATEGY)
public class AIInsightPromotionHandlerImpl
        extends AbstractAIPromotionHandler<List<AIPromoteMO>, Void> {
    public Void executeStrategy(List<AIPromoteMO> aiPromoteMOList) {
        super.execute(aiPromoteMOList, AIInsightConstant.PROMOTE_CREATE_INSIGHT_SUBJECT);
        return null;
    }

    @Override
    public AIEntityInterface getEntity(Object entityObject) {
        var insightEntity = (AIInsightEntity) entityObject;
        return insightEntity;
    }

    @Override
    public String getURL(String regionHostUrl, AIEntityInterface entityInterface) {
        var accountId = ((AIInsightEntity) entityInterface).getAccountId();
        return String.format("%s/v1/insight?account-id=%s", regionHostUrl, accountId);
    }

    @Override
    public ApiMethodEnum getRequestMethod() {
        return ApiMethodEnum.post;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }
}
