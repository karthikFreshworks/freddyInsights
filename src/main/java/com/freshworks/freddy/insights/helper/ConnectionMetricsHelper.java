package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.ESConstant;
import com.freshworks.freddy.insights.handler.observability.metrics.MongoPoolMetricsHandlerImpl;
import com.freshworks.freddy.insights.handler.observability.metrics.OpenSearchPoolMetricsHandlerImpl;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Helper class responsible for monitoring and managing the connection pool for Elasticsearch.
 */
@Component
public class ConnectionMetricsHelper {
    private static final long MONITOR_INTERVAL_MS = 30000; // 30 seconds

    private final RestHighLevelClient restHighLevelClient;
    private final PoolingNHttpClientConnectionManager esConnectionManager;
    private final OpenSearchPoolMetricsHandlerImpl esConnectionPoolMetricsHandler;
    private final MongoPoolMetricsHandlerImpl mongoPoolMetricsHandler;

    /**
     * Constructs a ConnectionPoolHelper with the necessary dependencies.
     *
     * @param restHighLevelClient            The RestHighLevelClient for Elasticsearch operations.
     * @param esConnectionManager            The connection manager for Elasticsearch.
     * @param esConnectionPoolMetricsHandler The metrics handler for Elasticsearch connection pool.
     */
    @Autowired
    public ConnectionMetricsHelper(RestHighLevelClient restHighLevelClient,
                                   PoolingNHttpClientConnectionManager esConnectionManager,
                                   OpenSearchPoolMetricsHandlerImpl esConnectionPoolMetricsHandler,
                                   MongoPoolMetricsHandlerImpl mongoPoolMetricsHandler) {
        this.esConnectionManager = esConnectionManager;
        this.restHighLevelClient = restHighLevelClient;
        this.esConnectionPoolMetricsHandler = esConnectionPoolMetricsHandler;
        this.mongoPoolMetricsHandler = mongoPoolMetricsHandler;
    }

    /**
     * Scheduled method to monitor and manage the connection pool.
     * It closes expired and idle connections, and pushes metrics to the metrics handler.
     */
    @Scheduled(fixedRate = MONITOR_INTERVAL_MS)
    public void monitorMetrics() {
        esConnectionManager.closeExpiredConnections();
        esConnectionManager.closeIdleConnections(ESConstant.ES_CONFIG_IDEAL_TIMEOUT, TimeUnit.SECONDS);
        esConnectionPoolMetricsHandler.recordMetrics();
        mongoPoolMetricsHandler.recordMetrics();
    }
}
