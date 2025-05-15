package com.freshworks.freddy.insights.dto.promotion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.RegionEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIPromoteDTO {
    @Valid
    @Size(max = 50, message = ExceptionConstant.NOT_VALID_LIST_SIZE)
    private List<Attribute> attributes;
    @NotNull(message = ExceptionConstant.NOT_VALID_PROMOTE_REGION_KEY)
    private RegionEnum region;
    @NotBlank(message = ExceptionConstant.NOT_VALID_PROMOTE_REGION_AUTH)
    private String authToken;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attribute {
        @NotEmpty(message = ExceptionConstant.NOT_VALID_ID)
        private String id;
        private String url;
        private Map<String, String> header;
        private String location;
    }
}
