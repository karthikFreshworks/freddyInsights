package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIInsightCreateDTO extends AIInsightBaseDTO implements Serializable {
    @NotBlank(message = ExceptionConstant.NOT_VALID_INSIGHT_NAME)
    @Length(min = 3, max = 110)
    private String name;
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant = TenantEnum.global;
    private List<@Valid AIInsightTranslationDTO> translatedFields;
    private String accountId = AIInsightConstant.DEFAULT_ACCOUNT;
    @NotNull(message = ExceptionConstant.NOT_VALID_USER_ID)
    private String userId = AIInsightConstant.DEFAULT_USER_ID;
    @Length(min = 1, max = 255)
    private String group = AIInsightConstant.DEFAULT_GROUP;
    @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
    private String languageCode = LanguageCodeEnum.en.getValue();
    @Size(min = 1, message = ExceptionConstant.NOT_VALID_PROMPT_ID)
    @NotEmpty(message = ExceptionConstant.NOT_VALID_PROMPT_ID)
    @Valid
    private List<String> promptIds;
    @NotNull(message = ExceptionConstant.NOT_VALID_STATUS)
    private StatusEnum status = StatusEnum.ACTIVE;
    @Pattern(regexp = "^v\\d+$", message = ExceptionConstant.NOT_VALID_VERSION)
    private String version;
}
