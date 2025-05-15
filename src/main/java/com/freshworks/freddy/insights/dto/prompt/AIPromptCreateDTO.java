package com.freshworks.freddy.insights.dto.prompt;

import com.freshworks.freddy.insights.constant.AIPromptConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class AIPromptCreateDTO extends AIPromptBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Length(min = 5, max = 1000)
    @NotBlank(message = ExceptionConstant.NOT_VALID_TEXT)
    private String text;
    @Length(min = 3, max = 100)
    @NotBlank(message = ExceptionConstant.NOT_VALID_PROMPT_NAME)
    private String name;
    private List<@Valid AIPromptTranslationDTO> translatedFields;
    @NotNull
    private String accountId = AIPromptConstant.DEFAULT_ACCOUNT;
    @NotNull(message = ExceptionConstant.NOT_VALID_USER_ID)
    private String userId = AIPromptConstant.DEFAULT_USER_ID;
    @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
    private String languageCode = LanguageCodeEnum.en.getValue();
    @Pattern(regexp = "^v\\d+$", message = ExceptionConstant.NOT_VALID_VERSION)
    @NotBlank(message = ExceptionConstant.NOT_VALID_VERSION)
    private String version = "v0";
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant = TenantEnum.global;
    private List<String> tags;
}
