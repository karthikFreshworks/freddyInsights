package com.freshworks.freddy.insights.config;

import com.freshworks.freddy.insights.constant.ESConstant;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Opensearch settings and client initialization.
 */
@Slf4j
@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
public class ESConfig {
    @Value("${freddy.insights.es.api.url}")
    private String urls;

    @Value("${freddy.insights.es.pool.max_total:90}")
    private int maxTotal;

    @Value("${freddy.insights.es.pool.max_per_route:30}")
    private int maxPerRoute;

    private RestHighLevelClient client;

    private PoolingNHttpClientConnectionManager connectionManager;

    /**
     * Initializes the Opensearch client.
     */
    @PostConstruct
    public void init() {
        RestClientBuilder builder = RestClient.builder(getHttpHosts())
                .setRequestConfigCallback(config -> config
                        .setConnectTimeout(ESConstant.ES_CONFIG_CONNECT_TIMEOUT)
                        .setConnectionRequestTimeout(ESConstant.ES_CONFIG_REQUEST_TIMEOUT)
                        .setSocketTimeout(ESConstant.ES_CONFIG_SOCKET_TIMEOUT))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    try {
                        connectionManager = this.openSearchConnectionManager();
                        httpClientBuilder.setConnectionManager(connectionManager);
                        return httpClientBuilder;
                    } catch (IOReactorException e) {
                        handleException(e, "ES IOReactor encountered a runtime exception");
                    } catch (Exception e) {
                        handleException(e, "ES Exception encountered a runtime exception");
                    }
                    return null;
                });
        client = new RestHighLevelClient(builder);
    }

    /**
     * Creates and configures the connection manager for Opensearch.
     *
     * @return The connection manager.
     * @throws IOReactorException If an IOReactor error occurs.
     */
    @Bean
    public PoolingNHttpClientConnectionManager openSearchConnectionManager() throws IOReactorException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(2 * Runtime.getRuntime().availableProcessors())
                .setSoTimeout(ESConstant.ES_CONFIG_SOCKET_TIMEOUT)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .setSoLinger(0)
                .build();

        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
            @Override
            public boolean handle(IOException ex) {
                handleIOException(ex);
                return true;
            }

            @Override
            public boolean handle(RuntimeException ex) {
                handleRuntimeException(ex);
                return true;
            }
        });

        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.closeIdleConnections(ESConstant.ES_CONFIG_IDEAL_TIMEOUT, TimeUnit.SECONDS);
        connectionManager.closeExpiredConnections();
        return connectionManager;
    }

    /**
     * Handles IO exceptions occurred during connection manager initialization.
     *
     * @param ex The IOException.
     */
    private void handleIOException(IOException ex) {
        log.error("System may be unstable: ES IOReactor encountered a checked exception CAUSE: {}",
                ExceptionHelper.stackTrace(ex));
    }

    /**
     * Handles runtime exceptions occurred during connection manager initialization.
     *
     * @param ex The RuntimeException.
     */
    private void handleRuntimeException(RuntimeException ex) {
        log.error("System may be unstable: ES IOReactor encountered a runtime exception CAUSE: {}",
                ExceptionHelper.stackTrace(ex));
    }

    /**
     * Handles exceptions occurred during connection manager initialization.
     *
     * @param e       The exception.
     * @param message The error message.
     */
    private void handleException(Exception e, String message) {
        log.error("System may be unstable: " + message + " CAUSE: {}", ExceptionHelper.stackTrace(e));
        throw new RuntimeException(e);
    }

    /**
     * Retrieves the Opensearch HTTP hosts from the configuration.
     *
     * @return An array of HttpHost instances representing the Opensearch hosts.
     */
    private HttpHost[] getHttpHosts() {
        String[] urlList = urls.split(",");
        HttpHost[] hosts = new HttpHost[urlList.length];
        for (int i = 0; i < urlList.length; i++) {
            hosts[i] = HttpHost.create(urlList[i]);
        }
        log.info("OpenSearch URLs:: {}, urlList::{}", urls, Arrays.toString(urlList));
        return hosts;
    }

    /**
     * Destroys the Opensearch client and pool upon application shutdown.
     *
     * @throws IOException If an I/O error occurs.
     */
    @PreDestroy
    public void destroy() throws IOException {
        if (client != null) {
            client.close();
        }

        if (connectionManager != null) {
            connectionManager.shutdown();
        }
    }

    /**
     * Exposes the configured RestHighLevelClient as a bean.
     *
     * @return The RestHighLevelClient bean.
     */
    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        return client;
    }
}
