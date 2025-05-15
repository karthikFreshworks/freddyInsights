package com.freshworks.freddy.insights.handler.http.request.http4;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.google.api.client.http.HttpMethods;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.stereotype.Component;

@Component(AIHandlerConstant.APACHE_HTTP4_DELETE)
public class ApacheHttp4DeleteRequestHandlerImpl extends AbstractHttp4RequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        return super.createHtt4Request(aiServiceMO, HttpMethods.DELETE, null);
    }
}
