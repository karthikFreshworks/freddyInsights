package com.freshworks.freddy.insights.handler;

import com.freshworks.freddy.insights.handler.http.AbstractRequestHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.helper.MDCHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractAIHandler<A, R> extends AbstractAIBaseHelper {
    protected MDCHelper mdcHelper;
    protected AIServiceHelper aiServiceHelper;
    protected Map<String, AbstractRequestHandler<CustomHttpResponse<String>>> syncRequestHandler;
    protected Map<String, AbstractRequestHandler<CustomHttpResponse<InputStream>>> streamRequestHandler;
    protected Map<String, AbstractRequestHandler<CompletableFuture<CustomHttpResponse<String>>>> asyncRequestHandler;

    @Autowired
    public void setMdcHelper(MDCHelper mdcHelper) {
        this.mdcHelper = mdcHelper;
    }

    @Autowired
    public void setAIServiceHelper(AIServiceHelper aiServiceHelper) {
        this.aiServiceHelper = aiServiceHelper;
    }

    @Autowired
    public void setAsyncRequestHandler(
            Map<String, AbstractRequestHandler<CompletableFuture<CustomHttpResponse<String>>>> requestHandler) {
        this.asyncRequestHandler = requestHandler;
    }

    @Autowired
    public void setSyncRequestHandler(
            Map<String, AbstractRequestHandler<CustomHttpResponse<String>>> syncRequestHandler) {
        this.syncRequestHandler = syncRequestHandler;
    }

    @Autowired
    public void setStreamRequestHandler(
            Map<String, AbstractRequestHandler<CustomHttpResponse<InputStream>>> streamRequestHandler) {
        this.streamRequestHandler = streamRequestHandler;
    }

    public abstract R executeStrategy(A args);
}
