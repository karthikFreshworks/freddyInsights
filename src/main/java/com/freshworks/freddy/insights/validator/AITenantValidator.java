package com.freshworks.freddy.insights.validator;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.repository.AITenantRepository;
import org.springframework.stereotype.Component;

@Component
public class AITenantValidator {
    public void validateDuplicateTenantWithEmail(AITenantRepository aiTenantRepository, TenantEnum tenant,
                                                     String email) {
        var aiTenant = aiTenantRepository.findTenantByTenantNameAndEmail(tenant, email);
        if (aiTenant != null && aiTenant.isPresent()) {
            AICommonValidateException.conflictDataException(ExceptionConstant.DUPLICATE_RECORD_FOR_ID);
        }
    }
}
