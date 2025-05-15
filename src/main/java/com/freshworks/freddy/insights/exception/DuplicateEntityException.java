package com.freshworks.freddy.insights.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateEntityException extends ApiException {
    public static final String DUPLICATE_MESSAGE = "An entity already existing for field [%s] value [%s]";

    public DuplicateEntityException(String field, String entityName) {
        super(HttpStatus.CONFLICT,  getMessage(field, entityName),
                new ApiErrorResponse(HttpStatus.CONFLICT, getMessage(field, entityName), ErrorCode.CONFLICT));
    }

    private static String getMessage(String field, String entityName) {
        return String.format(DUPLICATE_MESSAGE, field, entityName);
    }
}
