package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.error.mapping.ErrorDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "error_mappings")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorMappingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    private TenantEnum tenant;

    private Map<String, ErrorDetail> errors;

    public Map<String, ErrorDetail> getErrors() {
        this.errors = getIfNull(errors, HashMap::new);
        return this.errors;
    }
}
