package com.freshworks.freddy.insights.dto.prompt;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIPromptParamDTO implements Serializable {
    private String name;
    private String version;
    private String accountId;
    private String text;
    private String group;
    private TenantEnum tenant;
    @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
    private String languageCode = LanguageCodeEnum.en.getValue();
    private String userId;
    private List<String> promptIds;
    private Boolean suggest;
    private List<String> tags;
}
