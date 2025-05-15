package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.handler.observability.metrics.MongoPoolMetricsHandlerImpl;
import com.freshworks.freddy.insights.helper.DBHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MongoDBAspect {
    MongoPoolMetricsHandlerImpl mongoPoolMetricsHandler;

    @Autowired
    public void setMongoPoolMetricsHandler(MongoPoolMetricsHandlerImpl mongoPoolMetricsHandler) {
        this.mongoPoolMetricsHandler = mongoPoolMetricsHandler;
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoTemplate.*(..)) "
            + "&& !execution(* org.springframework.data.mongodb.core.MongoTemplate.getConverter())"
            + "&& !execution(* org.springframework.data.mongodb.core.MongoTemplate.query(Class))"
            + "&& !execution(* org.springframework.data.mongodb.core.MongoTemplate.executeCommand(String))")
    public Object profileExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        mongoPoolMetricsHandler.recordMetrics();
        Object result = DBHelper.measureDBTime(joinPoint, "mongodb");
        return result;
    }
}
