package com.freshworks.freddy.insights.handler.http.connection;

import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.helper.HttpConnectionHelper;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.freshworks.freddy.insights.constant.ExceptionConstant.EXTERNAL_SERVICE_API_ERROR;

@Slf4j
public class ApacheHttpAsyncConnectionHandlerImpl
        extends AbstractApacheHttpConnectionHandler<CompletableFuture<CustomHttpResponse<String>>> {
    private final int maxAttempts;
    private final CloseableHttpClient client;
    private final HttpUriRequest request;
    private final Predicate<Throwable> retryOnThrowable;
    private final AtomicInteger attempts = new AtomicInteger();
    private final Predicate<CloseableHttpResponse> retryOnResponse;
    private final boolean throwWhenRetryOnResponseExceeded;
    private final Duration delay;
    private final ExecutorService executor;

    public ApacheHttpAsyncConnectionHandlerImpl(HttpConnectionHelper.ApacheHttpBuilder builder) {
        this.client = builder.getClient();
        this.request = builder.getRequest();
        this.maxAttempts = builder.getMaxAttempts();
        this.retryOnResponse = builder.getRetryOnResponse();
        this.retryOnThrowable = builder.getRetryOnThrowable();
        this.throwWhenRetryOnResponseExceeded = builder.getThrowWhenRetryOnResponseExceeded();
        this.delay = builder.getRetryDelay();
        this.executor = getExecutor();
    }

    @Override
    public CompletableFuture<CustomHttpResponse<String>> connect() {
        return CompletableFuture.supplyAsync(() -> {
            CloseableHttpResponse httpResponse = null;
            log.info("REquest :: TEst: {}", request);
            try {
                httpResponse = client.execute(request);

                if (retryOnResponse.test(httpResponse)) {
                    log.error("Apache HTTP asynchronous connection is being retried due to an unsuccessful attempt. "
                                    + "Status code={}, URI={}, and response body={}",
                            httpResponse.getCode(),
                            request.getRequestUri(),
                            EntityUtils.toString(httpResponse.getEntity()));
                    return attemptRetryAsync(httpResponse, null).join();
                } else {
                    return convertHttpResponse(httpResponse);
                }
            } catch (IOException ex) {
                log.error("IOException encountered while converting HTTP response: {}", ExceptionHelper.stackTrace(ex));
                return attemptRetryAsync(httpResponse, ex).join();
            } catch (Throwable ex) {
                log.error("Apache HTTP asynchronous connection encountered an exception for method={}. CAUSE: {}",
                        request.getMethod(), ExceptionHelper.stackTrace(ex));
                if (retryOnThrowable.test(ex.getCause())) {
                    return attemptRetryAsync(null, ex).join();
                } else {
                    throw throwCustomException(ex, request);
                }
            } finally {
                if (httpResponse != null) {
                    try {
                        httpResponse.close();
                    } catch (IOException ex) {
                        log.error("Error closing Apache HTTP response: {}", ExceptionHelper.stackTrace(ex));
                    }
                }
            }
        }, executor);
    }

    private ExecutorService getExecutor() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        new ExecutorServiceMetrics(executorService, "virtualThreadsExecutor",
                "vt", Collections.emptyList()).bindTo(Metrics.globalRegistry);
        return executorService;
    }

    private CompletableFuture<CustomHttpResponse<String>> attemptRetryAsync(
            CloseableHttpResponse response, Throwable throwable) {
        return CompletableFuture.supplyAsync(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < maxAttempts) {
                log.warn("Apache HTTP asynchronous connection is retrying, attempt={}, URI={}, and delay={}",
                        attempts.get() + 1,
                        request.getRequestUri(),
                        delay.toMillis());
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ex) {
                    log.error("Interrupted during retry delay: {}", ExceptionHelper.stackTrace(ex));
                }
                return connect().join();
            } else {
                return handleRetryExceeded(response, throwable);
            }
        }, executor);
    }

    private CustomHttpResponse<String> handleRetryExceeded(CloseableHttpResponse response, Throwable throwable) {
        if (throwable != null || throwWhenRetryOnResponseExceeded) {
            Throwable ex = throwable;
            if (ex == null) {
                String errorMsg = String.format("%s. retries=%s, "
                                + "path=%s, status-code=%s", EXTERNAL_SERVICE_API_ERROR,
                        attempts.get(), request.getRequestUri(), response.getCode());
                ex = AICommonHelper.responseException(errorMsg, response.getCode());
            }
            throw throwCustomException(ex, request);
        } else {
            try {
                return convertHttpResponse(response);
            } catch (Exception ex) {
                log.error("IOException encountered during retry exceed handling: {}. CAUSE : {}", ex.getMessage(),
                        ExceptionHelper.stackTrace(ex));
                throw throwCustomException(ex, request);
            }
        }
    }
}
