package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
@AllArgsConstructor
public class AITenantHelper {
    public static TenantEnum convertToTenantEnum(String tenant) {
        try {
            return TenantEnum.valueOf(tenant.toLowerCase()); // Converts string to TenantEnum
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown tenant: " + tenant);
            return null;
        }
    }
}
