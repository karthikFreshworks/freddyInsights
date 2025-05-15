package com.freshworks.freddy.insights.dto.prompt;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AIPromptUpdateDTO extends AIPromptBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Length(min = 5, max = 200)
    private String text;
    private String group;
    private List<String> tags;
    private List<@Valid AIPromptTranslationDTO> translatedFields;
}
