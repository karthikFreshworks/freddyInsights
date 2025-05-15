package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIDialogueFeedbackCentralPayload extends  AbstractAIFeedbackCentralPayload{
    private String dialogueId;
    private String messageId;
    private String insightId;

    @Builder
    AIDialogueFeedbackCentralPayload(double rating,
                                     List<String> buttonTexts,
                                     String comments,
                                     String tenant,
                                     String bundleName,
                                     String accountId,
                                     String agentId,
                                     String feature,
                                     String feedbackType,
                                     boolean isHelpful,
                                     String createdAt,
                                     String createdBy,
                                     String dialogueId,
                                     String messageId,
                                     String insightId) {
        super(rating, buttonTexts, comments, tenant, bundleName, accountId, agentId, feature, feedbackType,
                isHelpful, createdAt, createdBy);
        this.messageId = messageId;
        this.dialogueId = dialogueId;
        this.insightId = insightId;
    }
}
