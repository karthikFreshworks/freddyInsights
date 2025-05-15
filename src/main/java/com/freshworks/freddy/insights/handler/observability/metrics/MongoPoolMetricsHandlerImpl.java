package com.freshworks.freddy.insights.handler.observability.metrics;

import com.mongodb.event.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a MongoDB connection pool metrics handler.
 * It extends AbstractMetricsHandler and implements ConnectionPoolListener to listen for connection pool events.
 * This class monitors various metrics related to the MongoDB connection pool and provides methods
 * to record and update these metrics.
 */
@Component
public class MongoPoolMetricsHandlerImpl extends AbstractMetricsHandler implements ConnectionPoolListener {
    private static final String METRIC_TYPE = "type";
    private static final String METRIC_TYPE_POOL_MAX = "pool.max";
    private static final String METRIC_TYPE_POOL_LEASED = "pool.leased";
    private static final String METRIC_TYPE_POOL_PENDING = "pool.pending";
    private static final String METRIC_TYPE_POOL_AVAILABLE = "pool.available";
    private static final String METRIC_MONGO_CONNECTION = "mongodb.connection";

    private final MetricBuilder metricBuilder;
    private final Counter pendingConnectionsCounter;

    private final AtomicInteger maxConnections = new AtomicInteger(0);
    private final AtomicInteger availableConnections = new AtomicInteger(0);
    private final AtomicInteger leasedConnections = new AtomicInteger(0);
    private final AtomicInteger pendingConnections = new AtomicInteger(0);

    /**
     * Constructs a new MongoPoolMetricsHandlerImpl.
     * Initializes the metric builder and pending connections counter.
     */
    public MongoPoolMetricsHandlerImpl() {
        AbstractMetricsHandler.meterRegistry = Metrics.globalRegistry;
        this.metricBuilder = new MetricBuilder(METRIC_MONGO_CONNECTION);
        this.pendingConnectionsCounter =
                this.metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_PENDING).buildCounter();
    }

    /**
     * Callback method called when a connection pool is created.
     * Updates the max connections metric and records the metrics.
     *
     * @param event ConnectionPoolCreatedEvent representing the connection pool creation event.
     */
    @Override
    public void connectionPoolCreated(ConnectionPoolCreatedEvent event) {
        maxConnections.set(event.getSettings().getMaxSize());
        recordMetrics();
    }

    /**
     * Callback method called when a connection is checked out from the pool.
     * Updates the available and leased connections metrics and records the metrics.
     *
     * @param event ConnectionCheckedOutEvent representing the connection checked out event.
     */
    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        availableConnections.decrementAndGet();
        leasedConnections.incrementAndGet();
        pendingConnectionsCounter.increment();
        recordMetrics();
    }

    /**
     * Callback method called when a connection is checked into the pool.
     * Updates the available and leased connections metrics and records the metrics.
     *
     * @param event ConnectionCheckedInEvent representing the connection checked in event.
     */
    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        availableConnections.incrementAndGet();
        leasedConnections.decrementAndGet();
        recordMetrics();
    }

    /**
     * Callback method called when a connection checkout request is started.
     * Updates the pending connections metric and records the metrics.
     *
     * @param event ConnectionCheckOutStartedEvent representing the connection checkout started event.
     */
    @Override
    public void connectionCheckOutStarted(ConnectionCheckOutStartedEvent event) {
        pendingConnections.incrementAndGet();
        recordMetrics();
    }

    /**
     * Callback method called when a connection is closed.
     * Updates the max connections metric and records the metrics.
     *
     * @param event ConnectionClosedEvent representing the connection closed event.
     */
    @Override
    public void connectionClosed(ConnectionClosedEvent event) {
        maxConnections.decrementAndGet();
        recordMetrics();
    }

    /**
     * Callback method called when a connection is created.
     * Updates the max connections metric and records the metrics.
     *
     * @param event ConnectionCreatedEvent representing the connection created event.
     */
    @Override
    public void connectionCreated(ConnectionCreatedEvent event) {
        maxConnections.incrementAndGet();
        recordMetrics();
    }

    /**
     * Records all metrics related to the MongoDB connection pool.
     * Updates the Gauge metrics with the latest values.
     */
    @Override
    public void recordMetrics() {
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_LEASED).buildGauge(leasedConnections::get);
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_AVAILABLE).buildGauge(availableConnections::get);
        metricBuilder.withTag(METRIC_TYPE, METRIC_TYPE_POOL_MAX).buildGauge(maxConnections::get);
    }
}
