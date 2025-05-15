package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIInsightCentralPayload {
    private String insightId;
    private String tenant;
    private String accountId;
    private String userId;
    private String serviceId;
    private String usecaseId;
    private String orgId;
    private String bundleId;
    private String groupId;
    private String group;
    private String sku;
    private List<String> plans;
    private List<String> addons;
    private List<String> tags;
    private String languageCode;
    private String status;
    private String createdAt;
    private String createdBy;
    private String timeToLive;
    private String archivedAt;
}
