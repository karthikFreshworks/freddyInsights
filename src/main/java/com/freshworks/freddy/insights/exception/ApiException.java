package com.freshworks.freddy.insights.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class ApiException extends ResponseStatusException {
    private final transient ApiErrorResponse apiError;

    public ApiException(HttpStatus httpStatus, String message, ApiErrorResponse apiError) {
        super(httpStatus, message);
        this.apiError = apiError;
    }
}
