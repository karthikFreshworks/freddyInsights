package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.handler.http.client.ApacheHttp4ClientHandlerImpl;
import com.freshworks.freddy.insights.handler.http.connection.ApacheHttp4SyncConnectionHandlerImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Predicate;

@Slf4j
@Component
public class Http4ConnectionHelper extends AbstractAIBaseHelper {
    protected static final int DEFAULT_MAX_ATTEMPTS = 5;
    protected static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(5);
    protected static final boolean DEFAULT_THROW_WHEN_RETRY_ON_RESPONSE_EXCEEDED = true;
    protected static final Predicate<Throwable> DEFAULT_RETRY_ON_THROWABLE = ex -> ex instanceof IOException;
    protected static final HttpResponse.BodyHandler<Void> DEFAULT_BODY_HANDLER = HttpResponse.BodyHandlers.discarding();
    protected static final Predicate<HttpResponse<?>> DEFAULT_RETRY_ON_JAVA11_RESPONSE =
            resp -> resp.statusCode() >= 500 || resp.statusCode() == 408;
    protected static final Predicate<CloseableHttpResponse> DEFAULT_RETRY_ON_OKHTTP_RESPONSE =
            resp -> resp.getStatusLine().getStatusCode() >= 500 || resp.getStatusLine().getStatusCode() == 408;
    protected static final Predicate<CloseableHttpResponse> DEFAULT_RETRY_ON_APACHE_HTTP_RESPONSE =
            resp -> resp.getStatusLine().getStatusCode() >= 500 || resp.getStatusLine().getStatusCode() == 408;
    private final ApacheHttp4ClientHandlerImpl apacheHttp4ClientHandler;

    @Autowired
    public Http4ConnectionHelper(ApacheHttp4ClientHandlerImpl apacheHttp4ClientHandler) {
        this.apacheHttp4ClientHandler = apacheHttp4ClientHandler;
    }

    public ApacheHttpBuilder builder(HttpUriRequest request) {
        return new ApacheHttpBuilder(apacheHttp4ClientHandler.httpClient(), request);
    }

    @Getter
    public static final class ApacheHttpBuilder {
        private final HttpUriRequest request;
        private final CloseableHttpClient client;
        private final Duration delay;
        private Integer maxAttempts;
        private Duration retryDelay;
        private Predicate<CloseableHttpResponse> retryOnResponse;
        private Predicate<Throwable> retryOnThrowable;
        private Boolean throwWhenRetryOnResponseExceeded;

        public ApacheHttpBuilder(CloseableHttpClient client, HttpUriRequest request) {
            this.client = client;
            this.request = request;
            this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
            this.retryOnResponse = DEFAULT_RETRY_ON_APACHE_HTTP_RESPONSE;
            this.retryOnThrowable = DEFAULT_RETRY_ON_THROWABLE;
            this.throwWhenRetryOnResponseExceeded = DEFAULT_THROW_WHEN_RETRY_ON_RESPONSE_EXCEEDED;
            this.delay = DEFAULT_RETRY_DELAY;
        }

        public ApacheHttpBuilder withMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public ApacheHttpBuilder withRetryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public ApacheHttpBuilder withRetryOnResponse(
                Predicate<CloseableHttpResponse> retryOnResponse) {
            this.retryOnResponse = retryOnResponse;
            return this;
        }

        public ApacheHttpBuilder withRetryOnThrowable(Predicate<Throwable> retryOnThrowable) {
            this.retryOnThrowable = retryOnThrowable;
            return this;
        }

        public ApacheHttpBuilder withThrowWhenRetryOnResponseExceeded(
                boolean throwWhenRetryOnResponseExceeded) {
            this.throwWhenRetryOnResponseExceeded = throwWhenRetryOnResponseExceeded;
            return this;
        }

        public ApacheHttp4SyncConnectionHandlerImpl buildApacheHttpSync() {
            return new ApacheHttp4SyncConnectionHandlerImpl(this);
        }
    }
}
