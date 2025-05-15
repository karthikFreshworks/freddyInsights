package com.freshworks.freddy.insights.exception;

import com.freshworks.freddy.insights.helper.ExceptionHelper;
import io.opentelemetry.api.trace.Span;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.error("HttpRequestMethodNotSupportedException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(
                new ApiErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(),
                        ErrorCode.REQUEST_PARAMETER_MISMATCH_EXCEPTION));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     WebRequest request) {
        log.error("HttpMediaTypeNotSupportedException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      WebRequest request) {
        log.error("HttpMediaTypeNotAcceptableException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.NOT_ACCEPTABLE,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, WebRequest request) {
        log.error("MissingPathVariableException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          WebRequest request) {
        log.error("MissingServletRequestParameterException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, WebRequest request) {
        log.error("MissingServletRequestParameterException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(ConversionNotSupportedException.class)
    protected ResponseEntity<Object> handleConversionNotSupported(
            ConversionNotSupportedException ex, WebRequest request) {
        log.error("ConversionNotSupportedException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(TypeMismatchException.class)
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, WebRequest request) {
        log.error("TypeMismatchException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(),
                        ErrorCode.REQUEST_PARAMETER_MISMATCH_EXCEPTION));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.error("HttpMessageNotReadableException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, WebRequest request) {
        log.error("HttpMessageNotReadableException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("MethodArgumentNotValidException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                        ex.getBindingResult().getAllErrors().stream().map(
                                        DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.toList()).toString(),
                        ErrorCode.REQUEST_PARAMETER_MISMATCH_EXCEPTION));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, WebRequest request) {
        log.error("MissingServletRequestPartException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(),
                        ErrorCode.REQUEST_PARAMETER_MISMATCH_EXCEPTION));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<Object> handleBindException(
            BindException ex, WebRequest request) {
        log.error("BindException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getBindingResult().getAllErrors().stream().map(
                                DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toList()).toString(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        log.error("NoHandlerFoundException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, WebRequest webRequest) {
        log.error("AsyncRequestTimeoutException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_WRITEABLE));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("ResourceNotFoundException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(Exception ex) {
        log.error("ConstraintViolationException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponseObj(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), ErrorCode.HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> constraintViolationExceptionResponse(ApiException ex) {
        log.error("Api Exception Class {} : {}", ex.getClass().getSimpleName(), ExceptionHelper.stackTrace(ex));
        Span.current().recordException(ex);
        ApiErrorResponse apiErrorResponse = ex.getApiError();
        return buildApiResponse(apiErrorResponse);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ApiErrorResponse> completionExceptionResponse(AIResponseStatusException ex) {
        log.error("CompletionException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponse(new ApiErrorResponse(ex.getApiError().getHttpStatus(), ex.getMessage(),
                ErrorCode.HTTP_MESSAGE_NOT_WRITEABLE));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<ApiErrorResponse> multipartExceptionResponse(Exception ex) {
        log.error("MultipartException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponse(new ApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
                ErrorCode.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ExceptionHelper.stackTrace(ex));
        Span.current().recordException(ex);
        return buildApiResponse(
                new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(),
                        ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleRuntimeException(AIResponseStatusException ex) {
        log.error("AIResponseStatusException: {}", ExceptionHelper.stackTrace(ex));
        return buildApiResponse(ex.getApiError());
    }

    protected ResponseEntity<ApiErrorResponse> buildApiResponse(ApiErrorResponse apiErrorResponse) {
        return new ResponseEntity<>(apiErrorResponse, apiErrorResponse.getHttpStatus());
    }

    protected ResponseEntity<Object> buildApiResponseObj(ApiErrorResponse apiErrorResponse) {
        return new ResponseEntity<>(apiErrorResponse, apiErrorResponse.getHttpStatus());
    }
}
