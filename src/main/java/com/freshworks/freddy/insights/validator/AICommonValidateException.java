package com.freshworks.freddy.insights.validator;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AICommonValidateException {
    public static void notAcceptableException(String message) {
        throw new AIResponseStatusException(message, HttpStatus.NOT_ACCEPTABLE, ErrorCode.NOT_ACCEPTABLE);
    }

    public static void conflictDataException(String message) {
        throw new AIResponseStatusException(message, HttpStatus.CONFLICT, ErrorCode.CONFLICT);
    }

    public static void badRequestException(String message) {
        throw new AIResponseStatusException(message, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
    }

    public static void notFoundException(String message) {
        throw new AIResponseStatusException(message,
                HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_DOES_NOT_EXIST);
    }

    public static void forbiddenException(String message) {
        throw new AIResponseStatusException(message, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}
