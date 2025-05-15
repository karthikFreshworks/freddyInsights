package com.freshworks.freddy.insights.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AIResponseStatusException extends ApiException {
    public AIResponseStatusException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN_ERROR);
    }

    public AIResponseStatusException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, ErrorCode.UNKNOWN_ERROR);
    }

    public AIResponseStatusException(String message, HttpStatus httpStatus, ErrorCode errorCode) {
        super(httpStatus, message, new ApiErrorResponse(httpStatus, message, errorCode));
    }

    public AIResponseStatusException(HttpStatus httpStatus, String message, int errorCode, String cause) {
        super(httpStatus, message, new ApiErrorResponse(httpStatus, errorCode, message, cause));
    }
}
