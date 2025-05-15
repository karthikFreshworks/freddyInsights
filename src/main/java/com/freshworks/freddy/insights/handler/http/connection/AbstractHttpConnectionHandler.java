package com.freshworks.freddy.insights.handler.http.connection;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.io.InterruptedIOException;
import java.net.*;
import java.net.http.HttpConnectTimeoutException;

import static com.freshworks.freddy.insights.constant.ExceptionConstant.EXTERNAL_SERVICE_API_ERROR;

@Slf4j
public abstract class AbstractHttpConnectionHandler<T> {
    abstract T connect();

    protected <U> AIResponseStatusException throwCustomException(
            @NotNull Throwable th, @NotNull U request) {
        String url = null;
        String method = null;

        if (request instanceof HttpUriRequest) {
            HttpUriRequest httpRequest = (HttpUriRequest) request;
            url = httpRequest.getRequestUri();
            method = httpRequest.getMethod();
        } else if (request instanceof org.apache.http.client.methods.HttpUriRequest) {
            org.apache.http.client.methods.HttpUriRequest httpRequest =
                    (org.apache.http.client.methods.HttpUriRequest) request;
            url = httpRequest.getURI().toString();
            method = httpRequest.getMethod();
        }

        Throwable cause = (th.getCause() != null) ? th.getCause() : th;
        String message = String.format("%s, method=%s, url=%s, message=%s, cause=%s",
                EXTERNAL_SERVICE_API_ERROR, method, url, th.getMessage(), cause);
        log.error("AbstractHttpConnectionHandler- {}", message);
        HttpStatus httpStatus;

        if (cause instanceof HttpServerErrorException.GatewayTimeout
                || cause instanceof HttpConnectTimeoutException
                || cause instanceof SocketTimeoutException) {
            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
        } else if (cause instanceof HttpServerErrorException.BadGateway
                || cause instanceof SocketException
                || cause instanceof UnknownServiceException) {
            httpStatus = HttpStatus.BAD_GATEWAY;
        } else if (cause instanceof HttpServerErrorException.NotImplemented) {
            httpStatus = HttpStatus.NOT_IMPLEMENTED;
        } else if (cause instanceof HttpServerErrorException.ServiceUnavailable
                || cause instanceof UnknownHostException
                || cause instanceof HttpRetryException) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (cause instanceof MalformedURLException
                || cause instanceof ProtocolException
                || cause instanceof URISyntaxException
                || cause instanceof IllegalArgumentException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (cause instanceof AIResponseStatusException) {
            throw (AIResponseStatusException) cause;
        } else if (cause instanceof InterruptedIOException) {
            httpStatus = HttpStatus.REQUEST_TIMEOUT;
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        throw AICommonHelper.responseException(message, httpStatus.value());
    }
}
