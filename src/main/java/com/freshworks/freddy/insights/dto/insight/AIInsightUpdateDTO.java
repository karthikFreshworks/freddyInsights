package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.Valid;
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
public class AIInsightUpdateDTO extends AIInsightBaseDTO implements Serializable {
    @Length(min = 3, max = 128)
    private String name;
    private TenantEnum tenant;
    private String userId;
    @Length(min = 3, max = 100)
    private String group;
    private String languageCode;
    private List<@Valid AIInsightTranslationDTO> translatedFields;
    private List<String> promptIds;
    private StatusEnum status;
}
