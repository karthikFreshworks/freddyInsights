package com.freshworks.freddy.insights.dto.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIPromptTranslationTextDTO {
    @Length(min = 5, max = 200)
    @NotBlank(message = ExceptionConstant.NOT_VALID_TEXT_IN_TRANSLATED_TEXT)
    private String text;
}
