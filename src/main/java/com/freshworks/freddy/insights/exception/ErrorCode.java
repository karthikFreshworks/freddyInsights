package com.freshworks.freddy.insights.exception;

public enum ErrorCode {
    WRONG_SERVICE(1, "wrong service called"),
    // System errors 1-1000
    VALIDATION_FAILED(10, "Validation failed"),
    MISSING_REQUEST_PARAM(11, "some params are missing in the request"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    METHOD_ARGUMENT_NOT_VALID(13, "Exception dude to invalid method argument"),
    HTTP_MESSAGE_NOT_READABLE(14, "Not able to read the input please check the json provided"),
    HTTP_MESSAGE_NOT_WRITEABLE(15, "something went wrong"),
    MISSING_PATH_VARIABLE_EXCEPTION(16, "Missing Path variable in request"),
    SERVLET_REQUEST_BINDING_EXCEPTION(17, "Servlet request binding exception"),
    WRONG_PATH_VARIABLE_EXCEPTION(18, "Wrong path variable in request"),
    REQUEST_PARAMETER_MISMATCH_EXCEPTION(19, "Request parameter mismatch in request"),
    RESOURCE_CREATION_FAILED(90, "resource creation failed"),
    RESOURCE_UPDATION_FAILED(91, "resource updation failed"),
    RESOURCE_DELETION_FAILED(92, "resource deletion failed"),
    ONE_OF_VALIDATION_FAILURE(93, "resource contains multiple items when only one is allowed"),

    UNKNOWN_ERROR(99, "Unknown error"),
    NOT_AUTHORIZED(403, "Not Authorized"),
    PAYLOAD_TOO_LARGE(999, "File/Payload size is too large. Please check."),
    TRIED_TO_MODIFY_OLDER_VERSION(100,
            "Entity update failed due to older version payload. Try again with latest version of payload"),
    RESOURCE_DOES_NOT_EXIST(102, "Requested resource does not exist"),
    UNAUTHORIZED_ACCESS(401, "Unauthorized access"),
    FORBIDDEN(403, "Not allowed"),
    BAD_REQUEST(400, "Bad request"),
    NO_HANDLER_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method not found"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    CONFLICT(409, "Conflicting item found. Please retry with a different identifier"),
    FAILED_DEPENDENCY(424, "Dependency failed. Please retry after sometime"),
    INTERNAL_SERVER_ERROR(500, "something went wrong"),
    // INVALID_ENUM_VALUE(1002, "Invalid Enum value found in the payload"),
    TOO_MANY_REQUESTS(429,  "Too many Requests"),

    // below error code related to DM. starts from 1000
    EXTERNAL_SERVICE_INTERNAL_SERVER_ERROR(1000,
            "Internal server error from external service. Please retry later."),
    EXTERNAL_SERVICE_SERVER_NOT_REACHABLE(1001,
            "External service is unreachable. Check network or try again later."),
    EXTERNAL_SERVICE_PRODUCT_CALL_FAILED(1002,
            "Product API call to external service failed. Verify operation and retry."),
    EXTERNAL_SERVICE_BAD_SERVICE_RESPONSE(1003,
            "Incompatible response from external service. Invalid format."),
    EXTERNAL_SERVICE_GATEWAY_TIMEOUT_VALUE(1004,
            "External service gateway timed out. Check availability and retry."),
    EXTERNAL_SERVICE_HTTP_CLIENT_TIMEOUT(1005,
            "No timely response from external service's API. Ensure responsiveness and retry."),
    EXTERNAL_SERVICE_BAD_REQUEST(1006,
            "Client request contains an invalid URI. Check URI correctness."),
    EXTERNAL_SERVICE_RESOURCE_NOT_FOUND(1007,
            "Requested resource path not found in external service. Verify path existence.");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
