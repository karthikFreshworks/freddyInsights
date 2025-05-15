package com.freshworks.freddy.insights.valueobject;

import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.entity.AIBundleEntity;
import lombok.Builder;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Builder
@Value
@Setter
public class ContextVO implements Serializable {
    AccessType accessType;
    String id;
    TenantEnum tenant;
    String email;
    String adminKey;
    String userKey;
    String bundle;
    String orgId;
    String domain;
    String userId;
    List<String> addons;
    String bundleId;
    String accountId;
    String[] groupId;
    String collapse;
    String tags;
    boolean semanticCache;
    AIBundleEntity aiBundleEntity;
}
