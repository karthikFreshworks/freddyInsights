package com.freshworks.freddy.insights.handler.http;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component(AIHandlerConstant.APACHE_HTTP4_SYNC_REQUEST)
public class ApacheHttp4SyncRequestHandlerImpl extends AbstractRequestHandler<CustomHttpResponse<String>> {
    @Override
    public CustomHttpResponse<String> execute(AIServiceMO aiServiceMO, ApiMethodEnum apiMethodEnum) {
        HttpUriRequest httpRequest = super.apacheHttp4RequestHandlerMap.get(
                super.getApacheHttpRequestMethod(apiMethodEnum)).buildHttpRequest(aiServiceMO);
        return http4ConnectionHelper
                .builder(httpRequest)
                .withThrowWhenRetryOnResponseExceeded(true)
                .withMaxAttempts(appConfigHelper.getHttpConnectionMaxRetry())
                .withRetryDelay(Duration.ofMillis(appConfigHelper.getHttpConnectionDelayMillis()))
                .buildApacheHttpSync()
                .connect();
    }
}
