package com.freshworks.freddy.insights.handler.observability;

import com.freshworks.freddy.insights.handler.observability.metrics.AbstractMetricsHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractObservabilityHandler extends AbstractMetricsHandler {
    protected static final String EMPTY_STRING = "";
    protected static final String DELIMITER = ", ";
    protected static final String ASSIGNMENT = "=";
    protected static final String NA_STRING = "NA";
    protected static final String ZERO_STRING = "0";

    public abstract StringBuilder getLogBuilder();

    protected String getOrDefault(String key, String defaultValue) {
        String value = MDC.get(key);
        return value != null ? value : defaultValue;
    }
}
