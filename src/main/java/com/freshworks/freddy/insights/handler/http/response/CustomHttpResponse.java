package com.freshworks.freddy.insights.handler.http.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class CustomHttpResponse<T> {
    private int statusCode;
    private T body;
    private Map<String, String> headers;

    public boolean isSuccessful() {
        return this.statusCode >= 200 && this.statusCode < 300;
    }
}
