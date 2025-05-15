package com.freshworks.freddy.insights.handler.promotion;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIPromptConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.entity.AIEntityInterface;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component(AIHandlerConstant.AI_PROMPT_UPDATE_PROMOTE_STRATEGY)
public class AIPromptUpdatePromotionHandlerImpl extends AbstractAIPromotionHandler<List<AIPromoteMO>,
        Void> {
    @Override
    public Void executeStrategy(List<AIPromoteMO> aiPromoteMOList) {
        super.execute(aiPromoteMOList, AIPromptConstant.PROMOTE_UPDATE_PROMPT_SUBJECT);
        return null;
    }

    @Override
    public AIEntityInterface getEntity(Object entityObject) {
        return (AIPromptEntity) entityObject;
    }

    @Override
    public String getURL(String regionHostUrl, AIEntityInterface aiEntityInterface) {
        return String.format("%s/v1/prompt/%s", regionHostUrl, aiEntityInterface.getId());
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
