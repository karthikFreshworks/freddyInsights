package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.constant.enums.insights.FrequencyEnum;
import com.freshworks.freddy.insights.dto.prompt.AIPromptResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIInsightResponseDTO implements Serializable {
    private String id;
    private TenantEnum tenant;
    private String name;
    private String accountId;
    private String userId;
    private String serviceId;
    private String usecaseId;
    private String orgId;
    private String bundleId;
    private String groupId;
    private String group;
    private String domain;
    private String sku;
    private String title;
    private String uiTag;
    private List<String> plans;
    private List<String> addons;
    private List<String> tags;
    private List<String> promptIds;
    private List<String> timeZones;
    @Transient
    private List<AIPromptResponseDTO> prompts;
    private String languageCode;
    private String imageUrl;
    private StatusEnum status;
    private String version;
    private String context;
    private String aggregate;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String timeToLive;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String createdAt;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String updatedAt;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String archivedAt;
    private TenantEnum createdByTenant;
    private String createdBy;
    private String updatedBy;
    private String businessKpi;
    private String metric;
    private String department;
    private FrequencyEnum frequency;
    private String type;
    private Float importanceScore;
    private String scenarioType;
}
