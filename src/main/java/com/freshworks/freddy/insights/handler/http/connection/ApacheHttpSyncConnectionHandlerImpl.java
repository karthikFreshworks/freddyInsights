package com.freshworks.freddy.insights.handler.http.connection;

import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.helper.HttpConnectionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.freshworks.freddy.insights.constant.ExceptionConstant.EXTERNAL_SERVICE_API_ERROR;

@Slf4j
public class ApacheHttpSyncConnectionHandlerImpl
        extends AbstractApacheHttpConnectionHandler<CustomHttpResponse<String>> {
    private final int maxAttempts;
    private final CloseableHttpClient client;
    private final HttpUriRequest request;
    private final Predicate<Throwable> retryOnThrowable;
    private final AtomicInteger attempts = new AtomicInteger();
    private final Predicate<CloseableHttpResponse> retryOnResponse;
    private final boolean throwWhenRetryOnResponseExceeded;
    private final Duration delay;

    public ApacheHttpSyncConnectionHandlerImpl(HttpConnectionHelper.ApacheHttpBuilder builder) {
        this.client = builder.getClient();
        this.request = builder.getRequest();
        this.maxAttempts = builder.getMaxAttempts();
        this.retryOnResponse = builder.getRetryOnResponse();
        this.retryOnThrowable = builder.getRetryOnThrowable();
        this.throwWhenRetryOnResponseExceeded = builder.getThrowWhenRetryOnResponseExceeded();
        this.delay = builder.getRetryDelay();
    }

    @Override
    public CustomHttpResponse<String> connect() {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(request);
            CustomHttpResponse<String> result;
            if (retryOnResponse.test(httpResponse)) {
                log.error("Apache HTTP synchronous connection is being retried as the previous attempt was "
                                + "unsuccessful. "
                                + "Status code={}, URI={}, and response body={}",
                        httpResponse.getCode(),
                        request.getRequestUri(), EntityUtils.toString(httpResponse.getEntity()));
                result = attemptRetry(httpResponse, null);
            } else {
                result = convertHttpResponse(httpResponse);
            }
            if (!result.isSuccessful()) {
                log.error("Apache HTTP synchronous connection unsuccessful with status_code={} and body={} and"
                        + " method={}.", result.getStatusCode(), result.getBody(), request.getMethod());
                throw AICommonHelper.responseException(result.getBody(), result.getStatusCode());
            }
            log.info("Apache HTTP synchronous connection successful with status={}", result.getStatusCode());
            return result;
        } catch (Throwable ex) {
            log.error("Apache HTTP synchronous connection encountered an exception for method={}. CAUSE: {}",
                    request.getMethod(), ExceptionHelper.stackTrace(ex));
            if (retryOnThrowable.test(ex.getCause())) {
                try {
                    return attemptRetry(null, ex);
                } catch (Throwable e) {
                    log.error("Apache HTTP synchronous connection encountered an error while attempting to retry for "
                                    + "method={}. CAUSE: {}",
                            request.getMethod(), ExceptionHelper.stackTrace(ex));
                    throw throwCustomException(e, request);
                }
            } else {
                throw throwCustomException(ex, request);
            }
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("Error closing Apache HTTP response: {}", ExceptionHelper.stackTrace(e));
                }
            }
        }
    }

    private CustomHttpResponse<String> attemptRetry(CloseableHttpResponse response, Throwable throwable)
            throws Throwable {
        attempts.incrementAndGet();
        if (attempts.get() < maxAttempts) {
            log.warn("Apache HTTP synchronous connection is retrying, attempt={}, URI={}, and delay={}",
                    attempts.get() + 1, request.getRequestUri(), delay.toMillis());
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException ex) {
                log.error("Apache HTTP synchronous connection retry wait exception: {}",
                        ExceptionHelper.stackTrace(ex));
            }
            return this.connect();
        } else {
            return handleRetryExceeded(response, throwable);
        }
    }

    private CustomHttpResponse<String> handleRetryExceeded(CloseableHttpResponse response, Throwable throwable)
            throws Throwable {
        if (throwable != null || throwWhenRetryOnResponseExceeded) {
            Throwable ex = throwable;
            if (ex == null) {
                String errorMsg = String.format("%s. retries=%s, URI=%s, status-code=%s", EXTERNAL_SERVICE_API_ERROR,
                        attempts.get(), request.getRequestUri(), response.getCode());
                ex = AICommonHelper.responseException(errorMsg, response.getCode());
            }
            throw throwCustomException(ex, request);
        } else {
            return convertHttpResponse(response);
        }
    }
}
