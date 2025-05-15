package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.prompt.AIPromptBaseDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptTranslationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIPromptEntity implements Serializable, AIEntityInterface {
    private static final long serialVersionUID = 1L;
    private String id;
    private String text;
    private String languageCode;
    private String name;
    private List<AIPromptTranslationDTO> translatedFields;
    private String userId;
    private String accountId;
    private String group;
    private Boolean suggest;
    private Float weight;
    private List<String> tags;
    private String version;
    private TenantEnum tenant;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private String createdAt;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private String updatedAt;
    private TenantEnum createdByTenant;
    private String createdBy;
    private String updatedBy;
    private AIPromptBaseDTO.IntentHandler intentHandler;

    @Override
    public String getModelId() {
        return null;
    }
}
