package com.freshworks.freddy.insights.handler.http.request;

public interface RequestHandler<A, R> {
    R buildHttpRequest(A arg);
}
