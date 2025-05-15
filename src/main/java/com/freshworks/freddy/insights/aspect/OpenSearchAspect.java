package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.handler.observability.metrics.OpenSearchPoolMetricsHandlerImpl;
import com.freshworks.freddy.insights.helper.DBHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OpenSearchAspect {
    @Autowired
    OpenSearchPoolMetricsHandlerImpl openSearchPoolMetricsHandler;

    @Around("execution(* com.freshworks.freddy.insights.helper.OpenSearchRestHighLevelClientHelper.*(..))")
    public Object queryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        openSearchPoolMetricsHandler.recordMetrics();
        Object result = DBHelper.measureDBTime(joinPoint, "opensearch");
        return result;
    }
}
