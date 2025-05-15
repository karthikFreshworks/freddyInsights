package com.freshworks.freddy.insights.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO {
    @Min(1)
    int page = 1;
    @Min(1) @Max(100)
    int size = 10;
}
