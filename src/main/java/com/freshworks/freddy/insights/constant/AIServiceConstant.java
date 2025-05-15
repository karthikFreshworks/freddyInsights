package com.freshworks.freddy.insights.constant;

import java.util.stream.Stream;

public interface AIServiceConstant {
    String ANONYMIZE = "anonymize";
    String DEANONYMIZE = "deanonymize";
    String DEFAULT_VERSION = "v0";
    String BODY = "body";
    String TEMPLATE_KEYS = "template-keys";
    String PLACEHOLDER_PREFIX = "%(";
    String PLACEHOLDER_SUFFIX = ")";
    String[] NON_SUPER_ADMIN_EXCLUDE_FIELDS = Stream.of("header", "url", "method", "nestedFields")
            .toArray(String[]::new);
    String[] SUGGEST_SERVICE_EXCLUDE_FIELDS =
            Stream.of("id", "tenant", "header", "url", "method", "modelTenant", "modelId", "createdAt",
                            "updatedAt", "createdBy", "updatedBy", "nestedFields", "curl")
                    .toArray(String[]::new);
    String PROMOTE_CREATE_SERVICE_SUBJECT = "Freshworks Freddy AI Platform - Service create promotion Status";
    String PROMOTE_UPDATE_SERVICE_SUBJECT = "Freshworks Freddy AI Platform - Service update promotion Status";
    String GOOGLE_TOKEN = "google_token";
    String VECTOR_SCOPED = "https://www.googleapis.com/auth/cloud-platform";
}
