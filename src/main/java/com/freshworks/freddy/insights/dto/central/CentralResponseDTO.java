package com.freshworks.freddy.insights.dto.central;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class CentralResponseDTO {
    private String service;
    private String topic;
    private String partition;
    private String requestId;
    private int offset;
}
