package com.freshworks.freddy.insights.aspect;

import com.freshworks.freddy.insights.constant.enums.CustomAccessTypeEnum;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AICustomAuthorization {
    CustomAccessTypeEnum[] value() default {};
}
