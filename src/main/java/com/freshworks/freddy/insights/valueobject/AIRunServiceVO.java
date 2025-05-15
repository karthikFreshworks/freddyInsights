package com.freshworks.freddy.insights.valueobject;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class AIRunServiceVO {
    String aiModel;
    String service;
    TenantEnum tenant;
    PlatformEnum platform;
    String serviceVersion;
    SseEmitter sseEmitter;
    String mediaType;
    Set<String> templateKeys;
    Set<String> features;
    Map<String, Object> requestData;
    List<MultipartFile> multipartFiles;
    String serviceAdaptorId;
    Long tenantAccountId;
}
