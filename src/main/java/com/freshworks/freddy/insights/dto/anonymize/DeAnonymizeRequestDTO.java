package com.freshworks.freddy.insights.dto.anonymize;

import lombok.*;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeAnonymizeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String anonymizationId;
    private String text;
}
