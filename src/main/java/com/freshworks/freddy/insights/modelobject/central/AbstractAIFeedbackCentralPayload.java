package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AbstractAIFeedbackCentralPayload {
    private double rating;
    private List<String> buttonTexts;
    private String comments;
    private String tenant;
    private String bundleName;
    private String accountId;
    private String agentId;
    private String feature;
    private String feedbackType;
    private boolean isHelpful;
    private String createdAt;
    private String createdBy;
}
