package com.freshworks.freddy.insights.dto.unified;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UnifiedDetectLanguagesRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 50)
    private List<String> texts;
}
