package com.freshworks.freddy.insights.dto.tenant;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AITenantCreateDTO extends AITenantBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant;
}
