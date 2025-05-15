package com.freshworks.freddy.insights.constant;

import java.util.stream.Stream;

public interface AIModelConstant {
    String[] NON_SUPER_ADMIN_EXCLUDE_FIELDS =
            Stream.of("header", "url", "method", "modelMapping", "nestedFields").toArray(String[]::new);
    String PROMOTE_CREATED_MODEL_SUBJECT = "Freshworks Freddy AI Platform - Model create promotion Status";
    String PROMOTE_UPDATED_MODEL_SUBJECT = "Freshworks Freddy AI Platform - Model update promotion Status";
}
