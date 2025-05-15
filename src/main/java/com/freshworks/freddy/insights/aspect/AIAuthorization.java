package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.constant.enums.AccessType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIAuthorization {
    AccessType[] value() default {};
}
