package com.freshworks.freddy.insights.dto.prompt;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIPromptResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String text;
    private String languageCode;
    private String name;
    private String userId;
    private String accountId;
    private String group;
    private String version;
    private TenantEnum tenant;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private String createdAt;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private String updatedAt;
    private TenantEnum createdByTenant;
    private String createdBy;
    private String updatedBy;
    private Boolean suggest;
    private Float weight;
    private List<String> tags;
    private AIPromptBaseDTO.IntentHandler intentHandler;
}
