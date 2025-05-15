package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIInsightTranslationDTO implements Serializable {
    @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE_IN_TRANSLATED_FIELD)
    private String languageCode;

    @Length(max = 200)
    @NotBlank(message = ExceptionConstant.NOT_VALID_TITLE_IN_TRANSLATED_TEXT)
    private String title;
}
