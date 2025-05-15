package com.freshworks.freddy.insights.dto.tenant;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AITenantBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@freshworks\\.com$", message = ExceptionConstant.NOT_VALID_EMAIL)
    private String email;
    private boolean semanticCache;
}
