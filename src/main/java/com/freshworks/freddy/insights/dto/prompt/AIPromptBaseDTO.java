package com.freshworks.freddy.insights.dto.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIPromptBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String group;
    private Boolean suggest;
    @Min(0)@Max(1)
    private Float weight;
    @Valid
    private IntentHandler intentHandler;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class IntentHandler implements Serializable {
        private static final long serialVersionUID = 1L;
        @NotBlank(message = ExceptionConstant.NOT_VALID_INTENT_HANDLER_ID)
        private String id;
        private String role;
        private Boolean hidden;
        private Boolean system;
        @NotBlank(message = ExceptionConstant.NOT_VALID_MIMETYPE)
        private String mimeType;
        private Boolean oneWay;
    }
}
