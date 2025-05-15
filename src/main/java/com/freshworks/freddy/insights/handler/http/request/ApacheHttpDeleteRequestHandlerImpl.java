package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.springframework.stereotype.Component;

@Component(AIHandlerConstant.APACHE_HTTP_DELETE)
public class ApacheHttpDeleteRequestHandlerImpl extends AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        HttpDelete httpDelete = new HttpDelete(aiServiceMO.getUrl());
        return createHttRequest(httpDelete, aiServiceMO, null);
    }
}
