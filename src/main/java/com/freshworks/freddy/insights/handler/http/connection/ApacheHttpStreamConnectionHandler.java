package com.freshworks.freddy.insights.handler.http.connection;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.HttpConnectionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.InputStream;

@Slf4j
public class ApacheHttpStreamConnectionHandler extends
        AbstractApacheHttpConnectionHandler<CustomHttpResponse<InputStream>> {
    private final CloseableHttpClient client;
    private final HttpUriRequest request;

    public ApacheHttpStreamConnectionHandler(HttpConnectionHelper.ApacheHttpBuilder builder) {
        this.client = builder.getClient();
        this.request = builder.getRequest();
    }

    public CustomHttpResponse<InputStream> connect() {
        log.info("Running the api in async to support the live stream");
        try {
            var response =  client.execute(request);
            return convertHttpStreamResponse(response);
        } catch (Exception ex) {
            log.error("Exception occurred while calling the external api for stream {}", ex.getMessage());
            throw new AIResponseStatusException("Exception occurred while calling the external api for stream");
        }
    }
}
