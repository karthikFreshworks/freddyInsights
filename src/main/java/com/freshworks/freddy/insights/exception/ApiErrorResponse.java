package com.freshworks.freddy.insights.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiErrorResponse {
    @JsonIgnore
    private HttpStatus httpStatus;

    private int errorCode;

    private String message;

    //@JsonIgnore
    private String cause;

    public ApiErrorResponse(HttpStatus status, String message, ErrorCode errorCode) {
        this.httpStatus = status;
        this.message = errorCode.getMessage();
        this.errorCode = errorCode.getCode();
        this.cause = message;
    }

    public ApiErrorResponse(HttpStatus httpStatus, ErrorCode errorCode) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public ApiErrorResponse(HttpStatus httpStatus, int errorCode, String message, String cause) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
        this.cause = cause;
    }
}
