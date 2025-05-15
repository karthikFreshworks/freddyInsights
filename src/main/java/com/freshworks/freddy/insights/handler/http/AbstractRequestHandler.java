package com.freshworks.freddy.insights.handler.http;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.handler.http.request.AbstractHttpRequestHandler;
import com.freshworks.freddy.insights.handler.http.request.http4.AbstractHttp4RequestHandler;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.helper.Http4ConnectionHelper;
import com.freshworks.freddy.insights.helper.HttpConnectionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public abstract class AbstractRequestHandler<T> extends AbstractAIBaseHelper {
    protected HttpConnectionHelper httpConnectionHelper;
    protected Http4ConnectionHelper http4ConnectionHelper;
    protected Map<String, AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest>> apacheHttpRequestHandlerMap;
    protected Map<String, AbstractHttp4RequestHandler<AIServiceMO, org.apache.http.client.methods.HttpUriRequest>>
            apacheHttp4RequestHandlerMap;

    @Autowired
    public void setHttpConnectionHelper(HttpConnectionHelper httpConnectionHelper) {
        this.httpConnectionHelper = httpConnectionHelper;
    }

    @Autowired
    public void setHttp4ConnectionHelper(Http4ConnectionHelper http4ConnectionHelper) {
        this.http4ConnectionHelper = http4ConnectionHelper;
    }

    @Autowired
    public void setAbstractHttpRequestHandler(
            Map<String, AbstractHttpRequestHandler<AIServiceMO, HttpUriRequest>> apacheHttpRequestHandlerMap) {
        this.apacheHttpRequestHandlerMap = apacheHttpRequestHandlerMap;
    }

    @Autowired
    public void setAbstractHttp4RequestHandler(
            Map<String, AbstractHttp4RequestHandler<AIServiceMO, org.apache.http.client.methods.HttpUriRequest>>
                    apacheHttp4RequestHandlerMap) {
        this.apacheHttp4RequestHandlerMap = apacheHttp4RequestHandlerMap;
    }

    protected String getApacheHttpRequestMethod(ApiMethodEnum apiMethodEnum) {
        String value = null;
        switch (apiMethodEnum.name()) {
        case "post":
            value = AIHandlerConstant.APACHE_HTTP_POST;
            break;
        case "put":
            value = AIHandlerConstant.APACHE_HTTP_PUT;
            break;
        case "get":
            value = AIHandlerConstant.APACHE_HTTP_GET;
            break;
        case "delete":
            value = AIHandlerConstant.APACHE_HTTP_DELETE;
            break;
        case "multipart_post":
            value = AIHandlerConstant.APACHE_MULTIPART_POST;
            break;
        case "multipart_put":
            value = AIHandlerConstant.APACHE_MULTIPART_PUT;
            break;
        case "stream_post":
            value = AIHandlerConstant.APACHE_HTTP_POST_STREAM;
            break;
        case "http4_put":
            value = AIHandlerConstant.APACHE_HTTP4_PUT;
            break;
        case "http4_post":
            value = AIHandlerConstant.APACHE_HTTP4_POST;
            break;
        case "http4_get":
            value = AIHandlerConstant.APACHE_HTTP4_GET;
            break;
        case "http4_delete":
            value = AIHandlerConstant.APACHE_HTTP4_DELETE;
            break;
        case "http4_multipart_post":
            value = AIHandlerConstant.APACHE_HTTP4_MULTIPART_POST;
            break;
        case "http4_multipart_put":
            value = AIHandlerConstant.APACHE_HTTP4_MULTIPART_PUT;
            break;
        default:
            break;
        }
        return value;
    }

    public abstract T execute(AIServiceMO aiServiceMO, ApiMethodEnum apiMethodEnum);
}
