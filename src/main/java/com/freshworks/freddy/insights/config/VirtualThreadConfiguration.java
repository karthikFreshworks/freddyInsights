package com.freshworks.freddy.insights.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.java21.instrument.binder.jdk.VirtualThreadMetrics;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfiguration {
    private final MeterRegistry meterRegistry;

    public VirtualThreadConfiguration(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    AsyncTaskExecutor applicationTaskExecutor() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        return new TaskExecutorAdapter(executorService);
    }

    @Bean
    TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        new ExecutorServiceMetrics(executorService, "tomcatThreadsExecutor",
                Collections.emptyList()).bindTo(meterRegistry);
        return protocolHandler -> protocolHandler.setExecutor(executorService);
    }

    @Bean
    public MeterBinder virtualThreadMetricsBinder() {
        return new VirtualThreadMetrics();
    }

    @Bean
    public TomcatConnectorCustomizer customizer() {
        return (connector) -> connector.addUpgradeProtocol(new Http2Protocol());
    }
}
