package com.freshworks.freddy.insights.handler.http.connection;

import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractApacheHttpConnectionHandler<T> extends AbstractHttpConnectionHandler<T> {
    protected CustomHttpResponse<String> convertHttpResponse(CloseableHttpResponse response) throws Exception {
        return CustomHttpResponse
                .<String>builder()
                .statusCode(response.getCode())
                .headers(getConvertedResponseHeaders(response.getHeaders()))
                .body(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8))
                .build();
    }

    protected CustomHttpResponse<String> convertHttp4Response(
            org.apache.http.client.methods.CloseableHttpResponse response) throws IOException {
        return CustomHttpResponse
                .<String>builder()
                .statusCode(response.getStatusLine().getStatusCode())
                .body(org.apache.http.util.EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8))
                .build();
    }

    protected CustomHttpResponse<InputStream> convertHttpStreamResponse(CloseableHttpResponse response)
            throws Exception {
        return CustomHttpResponse
                .<InputStream>builder()
                .statusCode(response.getCode())
                .body(response.getEntity().getContent())
                .build();
    }

    private Map<String, String> getConvertedResponseHeaders(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(
                        Header::getName,
                        Header::getValue,
                        (existingValue, newValue) -> existingValue + ", " + newValue));
    }
}
