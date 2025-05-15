package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.APACHE_MULTIPART_PUT)
public class ApacheHttpMultipartPutRequestHandlerImpl extends AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        HttpEntity customEntity = new CustomHttpEntity(buildApacheMultipartEntity(aiServiceMO));
        HttpPut httpPut = new HttpPut(aiServiceMO.getUrl());
        return createHttRequest(httpPut, aiServiceMO, customEntity);
    }
}
