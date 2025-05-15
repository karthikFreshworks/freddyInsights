package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import org.springframework.stereotype.Component;

@Component
public class AIRequestContextHelper {
    private final ThreadLocal<ContextVO> contextThreadLocal;

    public AIRequestContextHelper() {
        this.contextThreadLocal = new ThreadLocal<>();
    }

    public ContextVO getContextVO() {
        return contextThreadLocal.get();
    }

    public void setContextVO(ContextVO contextVO) {
        if (contextVO != null) {
            contextThreadLocal.set(contextVO);
        }
    }

    public void removeContextVO() {
        contextThreadLocal.remove();
    }

    public AccessType getAccessTypeFromContext() {
        return getContextVO().getAccessType();
    }

    public TenantEnum getTenantFromContext() {
        return getContextVO().getTenant();
    }
}
