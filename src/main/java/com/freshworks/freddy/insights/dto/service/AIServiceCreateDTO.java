package com.freshworks.freddy.insights.dto.service;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIServiceCreateDTO extends AIServiceBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$")
    @NotBlank(message = ExceptionConstant.NOT_VALID_SERVICE)
    private String service;
    @NotBlank(message = ExceptionConstant.NOT_VALID_DESCRIPTION)
    private String description;
    @Valid
    @NotNull(message = ExceptionConstant.NOT_VALID_PARAMS)
    @Size(max = 50, message = ExceptionConstant.NOT_VALID_LIST_SIZE)
    private List<AIServiceBaseDTO.Param> params;
    @NotBlank(message = ExceptionConstant.NOT_VALID_TEMPLATE)
    private String template;
    @Pattern(regexp = "^v\\d+$", message = ExceptionConstant.NOT_VALID_VERSION)
    private String version = "v0";
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant = TenantEnum.global;
}
