package com.freshworks.freddy.insights.handler.observability.metrics;

import io.micrometer.core.instrument.Counter;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class represents an OpenSearch connection pool metrics handler.
 * It extends AbstractMetricsHandler to manage metrics and provides methods
 * to record and update these metrics related to the OpenSearch connection pool.
 */
@Component
public class OpenSearchPoolMetricsHandlerImpl extends AbstractMetricsHandler {
    private static final String METRIC_TYPE = "type";
    private static final String METRIC_TYPE_POOL_MAX = "pool.max";
    private static final String METRIC_TYPE_POOL_LEASED = "pool.leased";
    private static final String METRIC_TYPE_POOL_PENDING = "pool.pending";
    private static final String METRIC_TYPE_POOL_AVAILABLE = "pool.available";
    private static final String METRIC_OS_CONNECTION = "openSearch.connection";
    private static final String METRIC_TYPE_POOL_MAX_PER_ROUTE = "pool.max.per.route";

    private final MetricBuilder metricBuilder;
    private final Counter maxConnections;
    private final Counter maxConnectionsPerRoute;

    private final PoolingNHttpClientConnectionManager openSearchPoolManager;

    /**
     * Constructor for OpenSearchPoolMetricsHandlerImpl.
     * Initializes the metric builder, max connections counter, and max connections per route gauge.
     *
     * @param openSearchPoolManager The connection manager for OpenSearch pool.
     */
    @Autowired
    public OpenSearchPoolMetricsHandlerImpl(PoolingNHttpClientConnectionManager openSearchPoolManager) {
        this.openSearchPoolManager = openSearchPoolManager;
        this.metricBuilder = new MetricBuilder(METRIC_OS_CONNECTION);
        this.maxConnections = metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_MAX).buildCounter();
        this.maxConnectionsPerRoute = metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_MAX_PER_ROUTE).buildCounter();
    }

    /**
     * Pushes the metrics related to the OpenSearch connection pool.
     * Records the available, leased, max, max per route, and pending connections.
     */
    @Override
    public void recordMetrics() {
        PoolStats stats = openSearchPoolManager.getTotalStats();
        recordPendingConnections(stats.getPending());
        recordLeasedConnections(stats.getLeased());
        recordMaxConnectionsPerRoute(stats.getMax());
        recordAvailableConnections(stats.getAvailable());
        recordMaxConnections(openSearchPoolManager.getMaxTotal());
    }

    /**
     * Records the maximum connections per route.
     *
     * @param maxPerRoute The maximum connections per route to record.
     */
    public void recordMaxConnectionsPerRoute(int maxPerRoute) {
        maxConnectionsPerRoute.increment(maxPerRoute);
    }

    /**
     * Records the leased connections.
     *
     * @param leased The number of leased connections to record.
     */
    private void recordLeasedConnections(int leased) {
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_LEASED).buildGauge(() -> leased);
    }

    /**
     * Records the available connections.
     *
     * @param available The number of available connections to record.
     */
    private void recordAvailableConnections(int available) {
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_AVAILABLE).buildGauge(() -> available);
    }

    /**
     * Records the pending connections.
     *
     * @param pending The number of pending connections to record.
     */
    private void recordPendingConnections(int pending) {
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_PENDING).buildGauge(() -> pending);
    }

    /**
     * Records the maximum connections.
     *
     * @param max The maximum number of connections to record.
     */
    private void recordMaxConnections(int max) {
        maxConnections.increment(max);
    }
}
