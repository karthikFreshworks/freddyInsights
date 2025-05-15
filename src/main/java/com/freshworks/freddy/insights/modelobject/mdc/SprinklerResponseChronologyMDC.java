package com.freshworks.freddy.insights.modelobject.mdc;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class SprinklerResponseChronologyMDC {
    private boolean isLlmCot;
    private String duration;
    private String statusCode;
    private String promptTokens;
    private String completionTokens;
    private String totalTokens;
}
