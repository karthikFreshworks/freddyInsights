package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.ESConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.ActionListener;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.*;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.reindex.BulkByScrollResponse;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OpenSearchRestHighLevelClientHelper {
    private final RestHighLevelClient client;

    @Autowired
    public OpenSearchRestHighLevelClientHelper(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Sets headers in the RequestOptions based on MDC values.
     *
     * @param requestOptions The RequestOptions to which headers will be added.
     */
    private void setHeadersFromMDC(RequestOptions.Builder requestOptions) {
        String requestId = MDC.get(ObservabilityConstant.X_REQUEST_ID);
        String traceparent = MDC.get(ObservabilityConstant.TRACEPARENT);
        if (requestId != null) {
            requestOptions.addHeader(ObservabilityConstant.X_REQUEST_ID, requestId);
        }
        if (traceparent != null) {
            requestOptions.addHeader(ObservabilityConstant.TRACEPARENT, traceparent);
        }
    }

    @Retryable(value = Exception.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public IndexResponse sendIndexRequest(IndexRequest request, RequestOptions requestOptions) throws Exception {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.index(request, requestOptions);
        } catch (Exception ex) {
            log.error("An error occurred while indexing doc to ES-{}, Error-{}, Retry Number-{}", request,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = Exception.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public DeleteResponse sendDeleteRequest(DeleteRequest request, RequestOptions requestOptions) throws Exception {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.delete(request, requestOptions);
        } catch (Exception ex) {
            log.error("An error occurred while deleting doc to ES-{}, Error-{}, Retry Number-{}", request,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = Exception.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public BulkByScrollResponse sendDeleteQueryRequest(
            DeleteByQueryRequest request, RequestOptions requestOptions) throws Exception {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.deleteByQuery(request, requestOptions);
        } catch (Exception ex) {
            log.error("An error occurred while deleting cortex from ES-{}, Error-{}, Retry Number-{}", request,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = IOException.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public SearchResponse sendSearchRequest(SearchRequest request, RequestOptions requestOptions) throws IOException {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.search(request, requestOptions);
        } catch (IOException ex) {
            log.error("An error occurred while searching doc to ES-{}, Error-{}, Retry Number-{}", request,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = IOException.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public SearchResponse sendScrollSearchRequest(
            SearchScrollRequest request, RequestOptions requestOptions) throws IOException {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.scroll(request, requestOptions);
        } catch (IOException ex) {
            log.error("An error occurred while searching doc with Scroll to ES-{}, Error-{}, Retry Number-{}",
                    request, ExceptionHelper.stackTrace(ex),
                    RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = IOException.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public ClearScrollResponse sendClearScrollRequest(
            ClearScrollRequest request, RequestOptions requestOptions) throws IOException {
        try {
            setHeadersFromMDC(requestOptions.toBuilder());
            return client.clearScroll(request, requestOptions);
        } catch (IOException ex) {
            log.error("An error occurred while Clearing Scroll to ES-{}, Error-{}, Retry Number-{}", request,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }

    @Retryable(value = IOException.class, maxAttempts = ESConstant.RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = ESConstant.RETRY_TIME_INTERVAL))
    public void processBulkAsync(BulkRequest bulkRequest, RequestOptions requestOptions) {
        try {
            var map = MDC.getCopyOfContextMap();
            ActionListener<BulkResponse> listener = new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    MDC.setContextMap(map);
                    long total = bulkItemResponses.getItems().length;
                    log.info("Total docs processed to ES : {}", total);
                }

                @Override
                public void onFailure(Exception e) {
                    MDC.setContextMap(map);
                    log.error("Bulk processing failed due to exception CAUSE : {}", ExceptionHelper.stackTrace(e));
                }
            };
            setHeadersFromMDC(requestOptions.toBuilder());
            client.bulkAsync(bulkRequest, requestOptions, listener);
        } catch (Exception ex) {
            log.error("An error occurred while processBulkAsync. request-{}, Error-{}, Retry Number-{}", bulkRequest,
                    ExceptionHelper.stackTrace(ex), RetrySynchronizationManager.getContext().getRetryCount());
            throw ex;
        }
    }
}
