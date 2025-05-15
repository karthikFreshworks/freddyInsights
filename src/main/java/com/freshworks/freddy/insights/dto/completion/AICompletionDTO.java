package com.freshworks.freddy.insights.dto.completion;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AICompletionDTO {
    @NotNull(message = ExceptionConstant.NOT_VALID_BODY)
    private Map<String, Object> body = new HashMap<>();

    private Map<String, String> header;
}
