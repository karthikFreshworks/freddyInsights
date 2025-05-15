package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component(AIHandlerConstant.APACHE_MULTIPART_POST)
public class ApacheHttpMultipartPostRequestHandlerImpl extends AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        HttpEntity customEntity = new CustomHttpEntity(buildApacheMultipartEntity(aiServiceMO));
        HttpPost httpPost = new HttpPost(aiServiceMO.getUrl());
        return createHttRequest(httpPost, aiServiceMO, customEntity);
    }
}
