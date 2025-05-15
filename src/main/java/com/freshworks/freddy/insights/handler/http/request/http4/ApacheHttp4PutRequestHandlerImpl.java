package com.freshworks.freddy.insights.handler.http.request.http4;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.google.api.client.http.HttpMethods;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.APACHE_HTTP4_PUT)
public class ApacheHttp4PutRequestHandlerImpl extends AbstractHttp4RequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        StringEntity entity = new StringEntity(
                aiServiceMO.getRemoteRequestBody().toString(), ContentType.APPLICATION_JSON);
        return super.createHtt4Request(aiServiceMO, HttpMethods.PUT, entity);
    }
}
