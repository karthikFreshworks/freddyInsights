package com.freshworks.freddy.insights.dto.completion;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AICompletionParamDTO {
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant;
    @NotNull(message = ExceptionConstant.NOT_VALID_PLATFORM)
    private PlatformEnum platform;
    @NotBlank(message = ExceptionConstant.NOT_VALID_MODEL)
    private String model;
}
