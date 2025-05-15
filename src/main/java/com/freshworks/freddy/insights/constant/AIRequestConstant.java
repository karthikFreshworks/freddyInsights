package com.freshworks.freddy.insights.constant;

import java.util.regex.Pattern;

public interface AIRequestConstant extends AIBaseConstant {
    String START_TIME = "start_time";
    String END_TIME = "end_time";
    String URI = "uri";
    String STATUS = "status";
    String METHOD = "method";
    String DURATION = "duration";
    String TYPE = "type";
    String ACCOUNT_ID = "account_id";
    String PORTAL_ID = "portal_id";
    String TENANT_ID = "tenant_id";
    String BUNDLE_ID = "bundle_id";
    String PRODUCT = "product";
    String AI_MODEL = "AI-Model";
    String AI_MODEL_VERSION = "AI-Model-Version";
    String AI_MODEL_VERSION_PATTERN = "^v\\d+$";
    String AI_SERVICE_VERSION = "AI-Service-Version";
    String X_FW_DYNAMIC_HEADERS = "x-fw-dynamic-headers";
    String CONTENT_TYPE = "Content-Type";
    String AUTHORIZATION = "authorization";
    String X_FW_REDIRECT_AUTHORIZATION = "x-fw-redirect-authorization";
    String SUPER_ADMIN_ID = "super-admin-id";
    String MASK = "[***]";
    String CLASS = "class";
    String PASSED = "passed";
    String HOST = "host";
    String ORIGIN = "origin";
    String ACCEPT = "accept";
    String USER_AGENT = "user-agent";
    String CONNECTION = "connection";
    String POSTMAN_TOKEN = "postman-token";
    String CONTENT_LENGTH = "content-length";
    String X_FREDDY_AI_PLATFORM_AUTHORIZATION = "Freddy-Ai-Platform-Authorization";
    String X_FW_CLOUD_TYPE = "x-fw-cloud-type";
    String ACCEPT_LANGUAGE = "Accept-Language";
    String X_FREDDY_AI_PLATFORM_BUNDLE = "freddy-ai-platform-bundle";
    String TRACE_ID = "trace_id";
    String X_FW_TRACE_ID = "x-fw-trace-id";
    String X_FW_FORWARDED_FOR = "x-fw-forwarded-for";
    String X_FW_DIALOGUE_ID = "x-fw-dialogue-id";
    String X_REQUEST_ID = "x-request-id";
    String X_FW_REQUEST_ID = "x-fw-request-id";
    String X_KAFKA_TRACE_ID = "x-kafka-traceId";
    String ADVICE_V2_REQUEST_TRACE_ID = "advice-v2-request-trace-id";
    String ADVICE_V2_RESPONSE_TRACE_ID = "advice-v2-response-trace-id";
    String TRACEPARENT = "traceparent";
    String X_FW_AUTH_ORG_ID = "x-fw-auth-org-id";
    String X_FW_BUNDLE_ID = "x-fw-bundle-id";
    String X_FW_AUTH_USER_ID = "x-fw-auth-user-id";
    String X_FW_AUTH_DOMAIN = "x-fw-auth-domain";
    String X_FW_AUTH_ACCOUNT_ID = "x-fw-auth-account-id";
    String X_FW_AUTH_GROUP_ID = "x-fw-auth-group-id";
    String COLLAPSE_BY = "collapse_by";
    String X_FW_RETRY_AFTER = "x-fw-retry-after";
    String X_FW_REMAINING_TOKEN = "x-fw-remaining-token";
    String X_FORWARDED_PROTO = "x-forwarded-proto";
    String X_FORWARDED_CLIENT_CERT = "x-forwarded-client-cert";
    // request and controller method mapping
    String AI_SERVICE_CONTROLLER_RUN = "com.freshworks.freddy.insights.controller.AIServiceController.runService";
    String AI_UNIFIED_SERVICE_CONTROLLER_RUN = "com.freshworks.freddy.insights.controller"
            + ".UnifiedFreddyServiceController";
    String AI_DIALOGUE_CONTROLLER_TRIGGER =
            "com.freshworks.freddy.insights.controller.AIDialogueController.triggerDialogue";
    String AI_DIALOGUE_CONTROLLER_HANDLER =
            "com.freshworks.freddy.insights.controller.AIDialogueController.getDialogueLLMResponse";
    String AI_COMPLETION_CONTROLLER_SERVICE =
            "com.freshworks.freddy.insights.controller.AICompletionController.completionService";
    String AI_FIRST_BUNDLE_CONTROLLER_TRIGGER =
            "com.freshworks.freddy.insights.controller.AIFirstBundleController.triggerDialogue";
    String TAGS = "tags";
    String X_FW_DIALOGUE_CHRONOLOGY = "x-fw-dialogue-chronology";
    String X_FW_SPRINKLER_CHRONOLOGY = "x-fw-sprinkler-chronology";
    String X_FW_FREDDY_ADDONS = "x-fw-freddy-addons";
    String JWT_PARTS_DELIMITER = "\\.";

    static Pattern CONTENT_LENGTH_PATTERN =
            Pattern.compile(
                    "\r?\ncontent-length:\\s*[0-9]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    String ACCEPT_ENCODING = "accept-encoding";
    String X_FWI_TENANT_ACCOUNT_ID = "x-fwi-tenant-account-id";
    String FEATURES = "features";
}
