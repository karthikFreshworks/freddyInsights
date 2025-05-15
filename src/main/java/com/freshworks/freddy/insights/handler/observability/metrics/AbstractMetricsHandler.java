package com.freshworks.freddy.insights.handler.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Abstract class for handling metrics.
 */
@Slf4j
public abstract class AbstractMetricsHandler {
    /**
     * The meter registry for managing metrics.
     */
    protected static MeterRegistry meterRegistry;

    /**
     * Setter method for injecting the meter registry.
     *
     * @param meterRegistry The meter registry to set.
     */
    @Autowired
    private void setMeterRegistry(MeterRegistry meterRegistry) {
        AbstractMetricsHandler.meterRegistry = meterRegistry;
    }

    public abstract void recordMetrics();

    /**
     * Builder class for constructing metrics with tags.
     */
    protected static class MetricBuilder {
        private final String name;
        private final Map<String, String> tags = new HashMap<>();

        /**
         * Constructs a MetricBuilder with the given name.
         *
         * @param name The name of the metric.
         */
        public MetricBuilder(String name) {
            this.name = name;
        }

        /**
         * Adds a tag with the specified key and value to the metric.
         *
         * @param key   The key of the tag.
         * @param value The value of the tag.
         * @return The MetricBuilder instance for method chaining.
         */
        public MetricBuilder withTag(String key, String value) {
            tags.put(key, value);
            return this;
        }

        /**
         * Builds a timer metric with the provided name and tags.
         *
         * @return The constructed timer metric.
         */
        public Timer buildTimer() {
            return Timer.builder(name)
                    .tags(convertTagsToStringArray())
                    .register(meterRegistry);
        }

        /**
         * Builds a timer metric with the provided name, tags, and percentiles.
         *
         * @return The constructed timer metric.
         */
        public Timer buildTimerWithPercentiles() {
            return Timer.builder(name)
                    .tags(convertTagsToStringArray())
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);
        }

        /**
         * Builds a counter metric with the provided name and tags.
         *
         * @return The constructed counter metric.
         */
        public Counter buildCounter() {
            return Counter.builder(name)
                    .tags(convertTagsToStringArray())
                    .register(meterRegistry);
        }

        /**
         * Builds a gauge metric with the provided name, value supplier, and tags.
         *
         * @param valueSupplier The supplier for obtaining the gauge value.
         * @return The constructed gauge metric.
         */
        public Gauge buildGauge(Supplier<Number> valueSupplier) {
            Gauge gauge = meterRegistry.find(name).gauge();
            if (gauge != null) {
                return Gauge.builder(name, valueSupplier)
                        .tags(convertTagsToStringArray()).register(meterRegistry);
            }
            return gauge;
        }

        /**
         * Converts the tags map to an array of strings.
         *
         * @return The array of strings representing tags.
         */
        private String[] convertTagsToStringArray() {
            return tags.entrySet().stream()
                    .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
                    .toArray(String[]::new);
        }
    }
}
