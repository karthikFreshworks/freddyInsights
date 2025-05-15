package com.freshworks.freddy.insights.aspect;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIBundleAuthorization {
    // No value attribute here
}
