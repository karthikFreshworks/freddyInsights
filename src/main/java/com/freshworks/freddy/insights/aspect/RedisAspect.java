package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.helper.DBHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RedisAspect {
    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object measureRedisResponseTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = DBHelper.measureDBTime(joinPoint, "redis");
        return result;
    }
}
