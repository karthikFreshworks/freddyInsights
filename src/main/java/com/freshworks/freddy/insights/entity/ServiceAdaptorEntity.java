package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "service_adaptors")
@CompoundIndex(def = "{'tenant': 1, 'name': 1}", unique = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ServiceAdaptorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private TenantEnum tenant;

    private Map<String, Object> validations;

    private Map<PlatformEnum, @Valid PlatformMapping> platformMappings;

    private ServiceAdaptorType type;

    public enum ServiceAdaptorType {
        DEFAULT,
        CUSTOM
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PlatformMapping {
        @NotNull
        private Map<Integer, String> errorCodeMappings;
        private String genericErrorCodeMapping;
    }
}
