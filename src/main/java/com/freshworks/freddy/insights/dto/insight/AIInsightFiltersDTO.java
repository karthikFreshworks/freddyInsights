package com.freshworks.freddy.insights.dto.insight;

import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsightFiltersDTO {
    private List<Object> filters;
}
