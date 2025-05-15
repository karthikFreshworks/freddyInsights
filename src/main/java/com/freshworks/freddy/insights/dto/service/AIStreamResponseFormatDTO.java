package com.freshworks.freddy.insights.dto.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.SuperBuilder;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIStreamResponseFormatDTO {
    private JsonNode content;
    private boolean streamCompleted;
    private String errorMessage;
}
