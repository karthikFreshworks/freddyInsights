package com.freshworks.freddy.insights.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends ApiException {
    public static final String VALIDATION_EXCEPTION = "Validation failed for field [%s] - [%s]";

    public ValidationException(String field, String entityName) {
        super(HttpStatus.BAD_REQUEST, getMessage(field, entityName),
                new ApiErrorResponse(HttpStatus.BAD_REQUEST, getMessage(field, entityName), ErrorCode.BAD_REQUEST));
    }

    private static String getMessage(String field, String entityName) {
        return String.format(VALIDATION_EXCEPTION, field, entityName);
    }
}
