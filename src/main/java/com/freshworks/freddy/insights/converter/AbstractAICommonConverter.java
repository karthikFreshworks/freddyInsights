package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;

public abstract class AbstractAICommonConverter extends AbstractAIBaseHelper {
    public static TenantEnum getTenant(AccessType accessType, TenantEnum requestTenant, TenantEnum contextTenant) {
        if (accessType == AccessType.SUPER_ADMIN) {
            return requestTenant;
        }
        return contextTenant;
    }

    public TenantEnum getTenant(TenantEnum requestTenant, TenantEnum contextTenant) {
        if (isSuperAdmin()) {
            return requestTenant;
        }
        return contextTenant;
    }
}
