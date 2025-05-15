package com.freshworks.freddy.insights.dto.service;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AIServiceUpdateDTO extends AIServiceBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private List<AIServiceBaseDTO.Param> params;
    private String template;
}
