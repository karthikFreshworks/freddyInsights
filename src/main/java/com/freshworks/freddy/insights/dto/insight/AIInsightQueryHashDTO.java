package com.freshworks.freddy.insights.dto.insight;

import lombok.*;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightQueryHashDTO implements Serializable {
    private String condition;
    private String operator;
    private String value;
}
