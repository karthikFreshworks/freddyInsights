package com.freshworks.freddy.insights.helper;

import com.mongodb.MongoException;
import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.opensearch.OpenSearchCorruptionException;
import org.opensearch.OpenSearchException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Objects.requireNonNullElse;

/**
 * The {@code DBHelper} class provides utility methods for measuring and logging
 * the response time of database operations. It uses the Slf4j logger and MDC
 * (Mapped Diagnostic Context) for logging and context management.
 * <p>
 * This class includes methods to measure the response time of a database operation,
 * log information about the operation, and handle errors by logging relevant details.
 * </p>
 * <p>
 * The measured response time, call count, and other metrics are stored in the MDC
 * for later logging. Error handling includes capturing error details and updating
 * MDC with specific error information for different database types.
 * </p>
 *
 * @version 1.0
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see MDC
 */
@Slf4j
@Component
public class DBHelper {
    /**
     * Measures the response time of a database operation, updates MDC with metrics,
     * logs database information, and handles errors.
     *
     * @param joinPoint The proceeding join point of the intercepted method.
     * @param dbType    The type of the database operation.
     * @return The result of the intercepted method.
     * @throws Throwable If an error occurs during the intercepted method execution.
     */
    public static Object measureDBTime(ProceedingJoinPoint joinPoint, String dbType) throws Throwable {
        try {
            long startTimeMillis = System.currentTimeMillis();
            final Object methodResult = joinPoint.proceed();
            long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;

            String storedDuration = MDC.get(dbType + ".duration");
            String storedCallCount = MDC.get(dbType + ".calls.count");

            long totalElapsedTimeMillis = requireNonNullElse(elapsedTimeMillis, 0L)
                    + Optional.ofNullable(storedDuration).map(Long::parseLong).orElse(0L);
            int callCount = Optional.ofNullable(storedCallCount).map(Integer::parseInt).orElse(0) + 1;

            MDC.put(dbType + ".calls.count", String.valueOf(callCount));
            MDC.put(dbType + ".duration", String.valueOf(totalElapsedTimeMillis));

            String storedFlow = MDC.get(dbType + ".duration.flow");
            String durationFlow = (storedFlow != null)
                    ? String.format("%s->%s", storedFlow, elapsedTimeMillis)
                    : String.format("%s", elapsedTimeMillis);
            MDC.put(dbType + ".duration.flow", durationFlow);

            logDatabaseInfo(dbType, joinPoint, elapsedTimeMillis);
            return methodResult;
        } catch (Exception e) {
            logDatabaseError(dbType, e);
            handleDatabaseError(dbType, e);
            throw e;
        }
    }

    /**
     * Logs information about a database operation, including database type,
     * method signature, and response time.
     *
     * @param dbType            The type of the database operation.
     * @param joinPoint         The proceeding join point of the intercepted method.
     * @param elapsedTimeMillis The response time of the database operation.
     */
    private static void logDatabaseInfo(String dbType, ProceedingJoinPoint joinPoint, long elapsedTimeMillis) {
        log.info("database={}, method={}, response_time={}", dbType, joinPoint.getSignature(), elapsedTimeMillis);
    }

    /**
     * Logs an error message when a database operation fails.
     *
     * @param dbType The type of the database operation.
     * @param e      The exception that occurred during the database operation.
     */
    private static void logDatabaseError(String dbType, Exception e) {
        log.error("The database operation of type {} encountered a failure, and the error details, "
                + "including the stack trace, have been recorded: {}", dbType, ExceptionHelper.stackTrace(e));
    }

    /**
     * Handles errors during a database operation, updates MDC with specific error
     * information based on the database type.
     *
     * @param dbType The type of the database operation.
     * @param e      The exception that occurred during the database operation.
     */
    private static void handleDatabaseError(String dbType, Exception e) {
        if ("opensearch".equals(dbType)) {
            MDC.put(dbType + ".error.status_code", String.valueOf(getOpenSearchDbFailureCode(e)));
            MDC.put(dbType + ".error.message", getOpenSearchDbFailureMessage(e));
        } else if ("mongodb".equals(dbType)) {
            MDC.put(dbType + ".error.status_code", String.valueOf(getMongoDbFailureCode(e)));
            MDC.put(dbType + ".error.message", getMongoDbFailureMessage(e));
        } else if ("redis".equals(dbType)) {
            MDC.put(dbType + ".error.status_code", "0");
            MDC.put(dbType + ".error.message", getRedisDbFailureMessage(e));
        }
    }

    /**
     * Returns the error code for a MongoDB failure.
     *
     * @param exception The exception that occurred during the MongoDB operation.
     * @return The MongoDB error code.
     */
    private static int getMongoDbFailureCode(Exception exception) {
        if (exception instanceof MongoException) {
            MongoException mongoException = (MongoException) exception;
            return mongoException.getCode();
        }
        return 0;
    }

    /**
     * Returns the error message for a MongoDB failure.
     *
     * @param exception The exception that occurred during the MongoDB operation.
     * @return The MongoDB error message.
     */
    private static String getMongoDbFailureMessage(Exception exception) {
        if (exception instanceof MongoException) {
            MongoException mongoException = (MongoException) exception;
            return mongoException.getMessage();
        }
        return exception.getMessage();
    }

    /**
     * Returns the error code for an OpenSearch failure.
     *
     * @param exception The exception that occurred during the OpenSearch operation.
     * @return The OpenSearch error code.
     */
    private static int getOpenSearchDbFailureCode(Exception exception) {
        if (exception instanceof OpenSearchException) {
            OpenSearchException elasticsearchException = (OpenSearchException) exception;
            return elasticsearchException.status().getStatus();
        }
        return 0;
    }

    /**
     * Returns the error message for an OpenSearch failure.
     *
     * @param exception The exception that occurred during the OpenSearch operation.
     * @return The OpenSearch error message.
     */
    private static String getOpenSearchDbFailureMessage(Exception exception) {
        if (exception instanceof OpenSearchException) {
            OpenSearchException exp = (OpenSearchException) exception;
            return exp.getMessage();
        } else if (exception instanceof OpenSearchCorruptionException) {
            OpenSearchCorruptionException exp = (OpenSearchCorruptionException) exception;
            return exp.getMessage();
        }
        return exception.getMessage();
    }

    /**
     * Returns the error message for a Redis failure.
     *
     * @param exception The exception that occurred during the Redis operation.
     * @return The Redis error message.
     */
    private static String getRedisDbFailureMessage(Exception exception) {
        if (exception instanceof RedisException) {
            RedisException exp = (RedisException) exception;
            return exp.getMessage();
        }
        return exception.getMessage();
    }
}
