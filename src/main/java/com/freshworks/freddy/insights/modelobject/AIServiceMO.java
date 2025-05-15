package com.freshworks.freddy.insights.modelobject;

import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@ToString
@Getter
@Setter
@Builder
public class AIServiceMO {
    // Service Entity attribute
    String url;
    String model;
    String service;
    PlatformEnum platform;
    ApiMethodEnum method;
    TenantEnum tenant;
    String requestParser;
    String responseParser;
    Set<String> enabledFeature;
    String template;
    TreeSet<AIServiceBaseDTO.Templates> templates;
    Map<String, Object> ruleBody;
    Map<String, String> header;
    // llm response headers
    List<AIServiceBaseDTO.ExternalResponseHeader> externalResponseHeaders;
    Map<String, String> runServiceResponseHeader;
    // Service Request attribute
    String aiServiceMediaType;
    Set<String> templateKeys;
    Map<String, Object> aiServiceRequestData;
    List<MultipartFile> multipartFiles;
    // steps attribute
    Map<String, Object> templatedMap;
    Map<String, Object> templatedRuleBodyMap;
    //api
    String[] remoteRequestHeaders;
    Object remoteRequestBody;
    Object originalRequestBody;
    //llm calls
    Object llmResponse;
    String serviceAdaptorId;
    SseEmitter sseEmitter;
    String anonymizationId;
    Set<String> features;
    boolean isCot;
}
