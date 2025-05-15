package com.freshworks.freddy.insights.handler.http.request;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component(AIHandlerConstant.APACHE_HTTP_POST_STREAM)
public class ApacheHttpPostStreamRequestHandlerImpl extends AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest> {
    @Override
    public HttpUriRequest buildHttpRequest(AIServiceMO aiServiceMO) {
        HttpPost httpPost = new HttpPost(aiServiceMO.getUrl());

        String requestBody = (String) aiServiceMO.getRemoteRequestBody();
        InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream, ContentType.APPLICATION_JSON);
        httpPost.setEntity(inputStreamEntity);

        for (Header header : apacheHttpHeaders(aiServiceMO.getRemoteRequestHeaders())) {
            httpPost.addHeader(header);
        }

        return createHttRequest(httpPost, aiServiceMO, inputStreamEntity);
    }
}
