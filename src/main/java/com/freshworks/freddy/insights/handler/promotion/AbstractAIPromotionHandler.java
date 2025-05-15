package com.freshworks.freddy.insights.handler.promotion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.RegionEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.promotion.AIPromoteEmailDTO;
import com.freshworks.freddy.insights.entity.AIEntityInterface;
import com.freshworks.freddy.insights.handler.AbstractAIHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractAIPromotionHandler<A, R> extends AbstractAIHandler<A, R> {
    private AIServiceMO getAiServiceMO(List<String> headers, String jsonString, String url) {
        return AIServiceMO.builder()
                .url(url)
                .remoteRequestHeaders(headers.toArray(String[]::new))
                .remoteRequestBody(jsonString)
                .build();
    }

    private AIPromoteMO getFutureAiPromoteMO(RegionEnum region,
                                             String id,
                                             AIServiceMO aiServiceMO,
                                             ApiMethodEnum apiMethodEnum) {
        return AIPromoteMO.builder()
                .id(id)
                .region(region)
                .completableFuture(
                        asyncRequestHandler.get(AIHandlerConstant.APACHE_ASYNC_REQUEST).execute(
                                aiServiceMO, apiMethodEnum))
                .build();
    }

    public abstract AIEntityInterface getEntity(Object entityObject);

    public abstract String getURL(String regionHostUrl, AIEntityInterface entityInterface);

    public abstract ApiMethodEnum getRequestMethod();

    public List<String> getHeaders(String region, String authToken) {
        List<String> headers = new ArrayList<>();
        headers.add("Authorization");
        headers.add(appConfigHelper.getFreddyAIPlatformBearerTokens().get(region));
        headers.add("Freddy-Ai-Platform-Authorization");
        headers.add(authToken);
        headers.add("Content-Type");
        headers.add(getContentType());
        return headers;
    }

    public abstract String getContentType();

    public AIPromoteEmailDTO promote(List<AIPromoteMO> aiPromoteMOList) {
        AIPromoteEmailDTO aiPromoteEmailDTO = new AIPromoteEmailDTO();
        List<AIPromoteMO> futureAiPromoteMOList = new ArrayList<>();
        ConcurrentHashMap<RegionEnum, AIPromoteEmailDTO.RegionStatus> regionStatusMap = new ConcurrentHashMap<>();
        aiPromoteEmailDTO.setRegionStatusMap(regionStatusMap);

        aiPromoteMOList.forEach(promoteMO -> {
            var region = promoteMO.getRegion();
            var authToken = promoteMO.getAuthToken();
            var regionHostUrl = appConfigHelper.getFreddyAIPlatformHostUrls().get(region.name());
            List<String> headers = getHeaders(region.name(), authToken);

            promoteMO.getEntityList().forEach(entityObj -> {
                var entity = getEntity(entityObj);
                try {
                    String jsonString = objectMapper.writeValueAsString(entity);
                    String url = getURL(regionHostUrl, entity);
                    AIServiceMO aiServiceMO = getAiServiceMO(headers, jsonString, url);

                    var futureAiPromoteMO = getFutureAiPromoteMO(region, entity.getId(),
                            aiServiceMO, getRequestMethod());
                    futureAiPromoteMOList.add(futureAiPromoteMO);
                } catch (JsonProcessingException e) {
                    log.error("JsonProcessingException for in service promotion {}", ExceptionHelper.stackTrace(e));
                }
            });
        });

        for (AIPromoteMO futureAiPromoteMO : futureAiPromoteMOList) {
            CompletableFuture<CustomHttpResponse<String>> futureResponse = futureAiPromoteMO.getCompletableFuture();
            RegionEnum region = futureAiPromoteMO.getRegion();
            futureResponse.thenAccept(response -> {
                var regionStatus = handleResponse(response, futureAiPromoteMO, aiPromoteEmailDTO.getRegionStatusMap());
                aiPromoteEmailDTO.getRegionStatusMap().put(region, regionStatus);
            }).exceptionally(ex -> {
                var regionStatus = handleException(ex, futureAiPromoteMO, aiPromoteEmailDTO.getRegionStatusMap());
                aiPromoteEmailDTO.getRegionStatusMap().put(region, regionStatus);
                return null;
            });
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureAiPromoteMOList.stream()
                .map(AIPromoteMO::getCompletableFuture)
                .toArray(CompletableFuture[]::new));
        allFutures.join();
        return aiPromoteEmailDTO;
    }

    private AIPromoteEmailDTO.RegionStatus handleResponse(CustomHttpResponse<String> response,
                                                          AIPromoteMO futureAiPromoteMO,
                                                          Map<RegionEnum, AIPromoteEmailDTO.RegionStatus> statusMap) {
        RegionEnum region = futureAiPromoteMO.getRegion();
        AIPromoteEmailDTO.RegionStatus regionStatus =
                statusMap.computeIfAbsent(region, k -> new AIPromoteEmailDTO.RegionStatus());
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody();
        log.info("Response from promote service: " + "status code {}, response {}", statusCode, responseBody);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            regionStatus.getSuccessServiceIds().add(futureAiPromoteMO.getId());
        } else {
            try {
                JSONObject json = new JSONObject(responseBody);
                regionStatus.getFailureServiceIds().put(futureAiPromoteMO.getId(), json.optString("message"));
            } catch (JSONException e) {
                log.error("Error parsing JSON response: {}", responseBody);
                regionStatus.getFailureServiceIds().put(futureAiPromoteMO.getId(), "Error parsing JSON response");
            }
        }
        return regionStatus;
    }

    private AIPromoteEmailDTO.RegionStatus handleException(Throwable ex,
                                                           AIPromoteMO futureAiPromoteMO,
                                                           Map<RegionEnum, AIPromoteEmailDTO.RegionStatus> statusMap) {
        log.info("Exception occurred from Email service {} ", ex.getMessage());
        RegionEnum region = futureAiPromoteMO.getRegion();
        AIPromoteEmailDTO.RegionStatus regionStatus =
                statusMap.computeIfAbsent(region, k -> new AIPromoteEmailDTO.RegionStatus());
        regionStatus.getFailureServiceIds().put(futureAiPromoteMO.getId(), ex.getMessage());
        return regionStatus;
    }

    protected void sendPromoteMail(AIPromoteEmailDTO aiPromoteEmailDTO, TenantEnum tenant, String subject) {
        var emailList = getEmailToList(tenant);
        var emailRequestDTO = emailRequestConverter.transformToEmailDTO(aiPromoteEmailDTO, emailList, tenant);
        emailRequestDTO.setSubject(subject);
        emailServiceHelper.send(emailRequestDTO);
        log.info("Sending promotion mail for tenant {} with receivers list {}", tenant, emailList);
    }

    public void execute(List<AIPromoteMO> aiPromoteMOList, String subject) {
        var map = MDC.getCopyOfContextMap();
        var tenant = getContextVO().getTenant();
        CompletableFuture.runAsync(() -> {
            MDC.setContextMap(map);
            try {
                AIPromoteEmailDTO aiPromoteEmailDTO = promote(aiPromoteMOList);
                sendPromoteMail(aiPromoteEmailDTO, tenant, subject);
            } catch (Exception e) {
                log.error("Promotion failed due to AIException {}", ExceptionHelper.stackTrace(e));
            } finally {
                MDC.clear();
            }
        });
    }
}
