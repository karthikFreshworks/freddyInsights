package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Document(collection = "bundle")
@CompoundIndex(def = "{'bundle': 1, 'tenantList': 1}", unique = true)
public class AIBundleEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    @NotBlank
    private String bundle;
    @NotNull
    @NotEmpty
    private List<TenantEnum> tenantList;
    private Map<TenantEnum, List<String>> tenantFilters;
}
