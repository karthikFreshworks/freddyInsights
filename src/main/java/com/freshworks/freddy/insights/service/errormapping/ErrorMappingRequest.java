package com.freshworks.freddy.insights.service.errormapping;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.error.mapping.ErrorDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ErrorMappingRequest {
    @NotNull
    private TenantEnum tenant;

    @NotNull
    @Valid
    private Map<String, ErrorDetail> errors;
}
