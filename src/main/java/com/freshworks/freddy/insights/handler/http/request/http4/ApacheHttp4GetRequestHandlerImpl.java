package com.freshworks.freddy.insights.handler.http.request.http4;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.google.api.client.http.HttpMethods;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.APACHE_HTTP4_GET)
public class ApacheHttp4GetRequestHandlerImpl extends AbstractHttp4RequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        return super.createHtt4Request(aiServiceMO, HttpMethods.GET, null);
    }
}
