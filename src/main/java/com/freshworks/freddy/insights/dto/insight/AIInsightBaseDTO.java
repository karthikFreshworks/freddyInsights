package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.freshworks.freddy.insights.constant.enums.insights.FrequencyEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIInsightBaseDTO implements Serializable {
    private String serviceId;
    private String usecaseId;
    private String orgId;
    private String bundleId;
    private String groupId;
    private List<String> plans;
    private List<String> addons;
    private List<String> tags;
    private String uiTag;
    private String imageUrl;
    @Length(max = 100)
    private String domain;
    @Length(max = 100)
    private String sku;
    @Length(max = 1000)
    private String title;
    @Length(max = 200)
    private String context;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String timeToLive;
    @Length(max = 250)
    private String aggregate;
    private String businessKpi;
    private String metric;
    private String department;
    private List<String> timeZones;
    private FrequencyEnum frequency;
    private String type;
    private Float importanceScore;
    private String scenarioType;
}
