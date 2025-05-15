package com.freshworks.freddy.insights.modelobject;

import com.freshworks.freddy.insights.constant.enums.RegionEnum;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ToString
@Getter
@Setter
@Builder
public class AIPromoteMO {
    private CompletableFuture<CustomHttpResponse<String>> completableFuture;
    private RegionEnum region;
    private String authToken;
    private String id;
    private List<Object> entityList;
}
