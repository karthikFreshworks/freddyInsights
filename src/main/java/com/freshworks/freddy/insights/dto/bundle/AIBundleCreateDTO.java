package com.freshworks.freddy.insights.dto.bundle;

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
public class AIBundleCreateDTO extends AIBundleBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank
    private String bundle;
}
