package com.freshworks.freddy.insights.dto.bundle;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIBundleBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Valid
    @NotEmpty(message = ExceptionConstant.NOT_VALID_PARAMS)
    private List<TenantEnum> tenantList;
    protected Map<TenantEnum, List<String>> tenantFilters;
}
