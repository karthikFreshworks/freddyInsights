package com.freshworks.freddy.insights.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Document(collection = "tenant")
@CompoundIndex(def = "{'tenant': 1, 'email': 1}", unique = true)
public class AITenantEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    @NotNull
    private TenantEnum tenant;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@freshworks\\.com$",
            message = "value should be a valid freshworks email in DB")
    private String email;
    @Indexed(unique = true)
    private String adminKey;
    @Indexed(unique = true)
    private String userKey;
    private Map<String, Object> nestedFields;
    private boolean semanticCache;
}
