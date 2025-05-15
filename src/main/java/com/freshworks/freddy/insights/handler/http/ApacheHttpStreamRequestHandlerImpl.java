package com.freshworks.freddy.insights.handler.http;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Component(AIHandlerConstant.APACHE_STREAM_REQUEST)
public class ApacheHttpStreamRequestHandlerImpl extends AbstractRequestHandler<CustomHttpResponse<InputStream>> {
    @Override
    public CustomHttpResponse<InputStream> execute(AIServiceMO aiServiceMO, ApiMethodEnum apiMethodEnum) {
        HttpUriRequest httpRequest = super.apacheHttpRequestHandlerMap.get(
                super.getApacheHttpRequestMethod(apiMethodEnum)).buildHttpRequest(aiServiceMO);
        return httpConnectionHelper
                .builder(httpRequest)
                .withThrowWhenRetryOnResponseExceeded(true)
                .withMaxAttempts(appConfigHelper.getHttpConnectionMaxRetry())
                .withRetryDelay(Duration.ofMillis(appConfigHelper.getHttpConnectionDelayMillis()))
                .buildApacheHttpStream()
                .connect();
    }
}
