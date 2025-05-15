package com.freshworks.freddy.insights.modelobject.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.CentralConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import lombok.Getter;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AISprinklerCentralBasePayload {
    private String tracingId;
    private String requestId;
    private String orgId;
    private String userId;
    private String bundleId;
    private String accountId;
    private String tenant;
    private String addons;
    private String bundleName;
    private String domain;
    private String groupId;
    private String tenantId;
    private String dynamicHeaders;
    protected String uuid;
    private String eventTimestamp;
    private BigDecimal actionEpoch;
    private String action;

    public AISprinklerCentralBasePayload() {
        this.tracingId = MDC.get(ObservabilityConstant.TRACE_ID);
        this.requestId = MDC.get(ObservabilityConstant.X_REQUEST_ID);
        this.orgId = MDC.get(ObservabilityConstant.X_FW_AUTH_ORG_ID);
        this.userId = MDC.get(ObservabilityConstant.X_FW_AUTH_USER_ID);
        this.bundleId = MDC.get(ObservabilityConstant.X_FW_BUNDLE_ID);
        this.accountId = MDC.get(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID);
        this.tenant = MDC.get(ObservabilityConstant.TENANT);
        this.addons = MDC.get(ObservabilityConstant.X_FW_FREDDY_ADDONS);
        this.bundleName = MDC.get(ObservabilityConstant.X_FREDDY_AI_PLATFORM_BUNDLE);
        this.domain = MDC.get(ObservabilityConstant.X_FW_AUTH_DOMAIN);
        this.groupId = MDC.get(ObservabilityConstant.X_FW_AUTH_GROUP_ID);
        this.tenantId = MDC.get(ObservabilityConstant.TENANT_ID);
        this.dynamicHeaders = MDC.get(ObservabilityConstant.X_FW_DYNAMIC_HEADERS);
        this.eventTimestamp = AICommonHelper.convertToISODate(Instant.now());
        this.action = CentralConstant.action_create;
        this.actionEpoch = new BigDecimal(Instant.now().toEpochMilli());
        this.uuid = UUID.randomUUID().toString();
    }

    public void setTracingId(String tracingId) {
        this.tracingId = tracingId == null ? MDC.get(ObservabilityConstant.TRACE_ID) : tracingId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? MDC.get(ObservabilityConstant.X_REQUEST_ID) : requestId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId == null ? MDC.get(ObservabilityConstant.X_FW_AUTH_ORG_ID) : orgId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? MDC.get(ObservabilityConstant.X_FW_AUTH_USER_ID) : userId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId == null ? MDC.get(ObservabilityConstant.X_FW_BUNDLE_ID) : bundleId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId == null ? MDC.get(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID) : accountId;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant == null ? MDC.get(ObservabilityConstant.TENANT) : tenant;
    }

    public void setAddons(String addons) {
        this.addons = addons == null ? MDC.get(ObservabilityConstant.X_FW_FREDDY_ADDONS) : addons;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName == null ? MDC.get(ObservabilityConstant.X_FREDDY_AI_PLATFORM_BUNDLE) : bundleName;
    }

    public void setDomain(String domain) {
        this.domain = domain == null ? MDC.get(ObservabilityConstant.X_FW_AUTH_DOMAIN) : domain;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? MDC.get(ObservabilityConstant.X_FW_AUTH_GROUP_ID) : groupId;
    }

    public void setDynamicHeaders(String dynamicHeaders) {
        this.dynamicHeaders = dynamicHeaders == null
                ? MDC.get(ObservabilityConstant.X_FW_DYNAMIC_HEADERS) : dynamicHeaders;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null ? MDC.get(ObservabilityConstant.TENANT_ID) : tenantId;
    }

    public void setEventTimeStamp(String eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setActionEpoch(BigDecimal actionEpoch) {
        this.actionEpoch = actionEpoch;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
