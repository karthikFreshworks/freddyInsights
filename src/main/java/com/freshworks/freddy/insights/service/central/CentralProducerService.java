package com.freshworks.freddy.insights.service.central;

import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.dto.central.CentralRequestDTO;
import com.freshworks.freddy.insights.handler.http.AbstractRequestHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CentralProducerService extends AbstractAIBaseHelper {
    protected Map<String, AbstractRequestHandler<CompletableFuture<CustomHttpResponse<String>>>> asyncRequestHandler;

    @Autowired
    public void setAsyncRequestHandler(
            Map<String, AbstractRequestHandler<CompletableFuture<CustomHttpResponse<String>>>> asyncRequestHandler) {
        this.asyncRequestHandler = asyncRequestHandler;
    }

    public <T> void sendEventsToCentral(CentralRequestDTO<T> requestDTO) {
        try {
            List<String> headers = getHeaders(appConfigHelper.getCentralAuthKey());
            var centralPayload = objectMapper.writeValueAsString(requestDTO);
            log.info("Central payload : {}", centralPayload);

            AIServiceMO aiServiceMO = getAiServiceMO(headers, centralPayload, appConfigHelper.getCentralUrl());
            var result = asyncRequestHandler
                    .get(AIHandlerConstant.APACHE_ASYNC_REQUEST).execute(aiServiceMO, ApiMethodEnum.post);
            result.thenApply(response -> {
                var centralResponse = response.getBody();
                log.debug("Response from Central ::: {}", centralResponse);
                return response;
            }).handle((res, ex) -> {
                if (ex != null) {
                    log.error("exception occurred while pushing data to Central::: {}", ExceptionHelper.stackTrace(ex));
                }
                return null;
            });
        } catch (Exception e) {
            log.error("exception occurred while pushing data to Central::: {}", ExceptionHelper.stackTrace(e));
        }
    }

    public List<String> getHeaders(String authKey) {
        List<String> headers = new ArrayList<>();
        headers.add("Content-Type");
        headers.add(MediaType.APPLICATION_JSON_VALUE);
        headers.add("Cache-Control");
        headers.add(CacheControl.noCache().getHeaderValue());
        headers.add("service");
        headers.add(authKey);
        return headers;
    }

    private AIServiceMO getAiServiceMO(List<String> headers, String jsonString, String url) {
        return AIServiceMO.builder()
                .url(url)
                .remoteRequestHeaders(headers.toArray(String[]::new))
                .remoteRequestBody(jsonString)
                .build();
    }
}
