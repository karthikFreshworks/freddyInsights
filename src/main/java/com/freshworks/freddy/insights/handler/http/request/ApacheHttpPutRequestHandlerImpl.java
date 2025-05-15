package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.APACHE_HTTP_PUT)
public class ApacheHttpPutRequestHandlerImpl extends AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        HttpPut httpPut = new HttpPut(aiServiceMO.getUrl());
        HttpEntity entity = new StringEntity(
                aiServiceMO.getRemoteRequestBody().toString(), ContentType.APPLICATION_JSON);
        return createHttRequest(httpPut, aiServiceMO, entity);
    }
}
