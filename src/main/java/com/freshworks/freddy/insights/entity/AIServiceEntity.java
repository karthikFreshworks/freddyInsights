package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Document(collection = "service")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@CompoundIndex(def = "{'tenant': 1, 'platform': 1, 'model': 1, 'service': 1, 'version': 1}", unique = true)
public class AIServiceEntity implements Serializable, AIEntityInterface {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    @Indexed
    private TenantEnum tenant;
    private PlatformEnum platform;
    @NotBlank(message = "service can not be stored as null or blank")
    private String service;
    @NotBlank
    private String model;
    @NotBlank
    private TenantEnum modelTenant;
    @NotBlank
    private String modelVersion;
    @NotBlank
    private String modelId;
    @NotBlank(message = "description can not be stored as null or blank")
    private String description;
    @NotBlank(message = "template can not be stored as null or blank")
    private String template;
    private String url;
    @NotNull
    private ApiMethodEnum method;
    private String responseParser;
    private String requestParser;
    private Map<String, String> header = new HashMap<>();
    @NotEmpty
    @Valid
    private List<AIServiceBaseDTO.Param> params = new ArrayList<>();
    private AIServiceBaseDTO.Rule rule = new AIServiceBaseDTO.Rule();
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date createdAt;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    private String version;
    private String curl;
    private Map<String, Object> nestedFields;
    private TreeSet<AIServiceBaseDTO.Templates> templates = new TreeSet<>();

    @Override
    public String getModelId() {
        return modelId;
    }
}
