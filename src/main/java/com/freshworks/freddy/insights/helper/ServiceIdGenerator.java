package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.octomix.josson.commons.StringUtils;
import lombok.Builder;

import static java.util.Objects.requireNonNull;

@Builder(toBuilder = true)
public class ServiceIdGenerator {
    private final TenantEnum tenantName;
    private final PlatformEnum platformName;
    private final String aiModel;
    private final String service;
    private final String serviceVersion;

    public ServiceIdGenerator(TenantEnum tenantName,
                              PlatformEnum platformName,
                              String aiModel,
                              String service,
                              String serviceVersion) {
        this.tenantName = requireNonNull(tenantName, "Tenant is required");
        this.platformName = requireNonNull(platformName, "Platform is required");
        this.aiModel = requireNonEmpty(aiModel, "AI models is required");
        this.service = requireNonEmpty(service, "Service Name Is Required");
        this.serviceVersion = requireNonEmpty(serviceVersion, "Service Version is required");
    }

    public String generate() {
        return String.format("%s-%s-%s-%s-%s",
            this.tenantName.name(),
            this.platformName.name(),
            this.aiModel,
            this.service,
            this.serviceVersion);
    }

    public static String requireNonEmpty(String content, String message) {
        if (StringUtils.isBlank(content)) {
            throw new IllegalArgumentException(message);
        }
        return content;
    }
}
