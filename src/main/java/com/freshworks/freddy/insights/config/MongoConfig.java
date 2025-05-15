package com.freshworks.freddy.insights.config;

import com.freshworks.freddy.insights.handler.observability.metrics.MongoPoolMetricsHandlerImpl;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for MongoDB setup.
 * This class provides configuration for connecting to MongoDB using Spring Data MongoDB.
 */
@Configuration
public class MongoConfig {
    /**
     * Default maximum wait time for a connection in seconds.
     */
    private static final int DEFAULT_MAX_WAIT_TIME_SECONDS = 5;

    /**
     * Default read timeout for a connection in seconds.
     */
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 5;

    /**
     * Default connection timeout in seconds.
     */
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 2;

    /**
     * Default maximum time a connection can remain idle in the connection pool in seconds.
     */
    private static final int DEFAULT_MAX_CONNECTION_IDLE_TIME_SECONDS = 30;

    /**
     * Default maximum time a connection can exist in the connection pool in seconds.
     */
    private static final int DEFAULT_MAX_CONNECTION_LIFE_TIME_SECONDS = 600;

    @Value("${spring.data.mongodb.database}")
    String mongoDbName;

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${mongodb.max.pool.size:#{2 * T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int mongoDBMaxPoolSize;

    @Value("${mongodb.min.pool.size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int mongoDBMinPoolSize;

    @Autowired
    private MongoPoolMetricsHandlerImpl mongoPoolMetricsHandlerImpl;

    /**
     * Creates and configures a MongoClient bean.
     *
     * @return MongoClient bean configured based on the specified properties.
     */
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)

                // Connection pool optimizations:
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(mongoDBMaxPoolSize)
                                .minSize(mongoDBMinPoolSize)
                                .maxWaitTime(DEFAULT_MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS)
                                .maxConnectionIdleTime(DEFAULT_MAX_CONNECTION_IDLE_TIME_SECONDS, TimeUnit.SECONDS)
                                .maxConnectionLifeTime(DEFAULT_MAX_CONNECTION_LIFE_TIME_SECONDS, TimeUnit.SECONDS)
                                .addConnectionPoolListener(mongoPoolMetricsHandlerImpl))

                // Socket settings:
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .readTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))

                // Write concern:
                .writeConcern(WriteConcern.W1)

                // Read preferences:
                .readConcern(ReadConcern.LOCAL)
                .readPreference(ReadPreference.secondaryPreferred())

                // Retry logic:
                //.retryWrites(true)
                .retryReads(true)
                .build();
        return MongoClients.create(settings);
    }

    /**
     * Creates and configures a MongoTemplate bean.
     *
     * @param mongoClient The MongoClient bean to use for creating the MongoTemplate.
     * @return MongoTemplate bean configured with the specified database name.
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, mongoDbName);
    }
}
