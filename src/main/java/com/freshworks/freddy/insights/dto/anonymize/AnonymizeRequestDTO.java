package com.freshworks.freddy.insights.dto.anonymize;

import lombok.*;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnonymizeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
}
