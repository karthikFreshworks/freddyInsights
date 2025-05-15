package com.freshworks.freddy.insights.handler.http.client;

import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHttpClientHandler<T> extends AbstractAIBaseHelper {
    public abstract T httpClient();
}
