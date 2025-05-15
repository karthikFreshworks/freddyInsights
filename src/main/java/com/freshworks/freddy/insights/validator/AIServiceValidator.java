package com.freshworks.freddy.insights.validator;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.service.AIServiceUpdateDTO;
import com.freshworks.freddy.insights.entity.AIServiceEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AIServiceValidator extends AbstractAIBaseHelper {
    /**
     * Retrieves the provided AIServiceEntity if it's not null. Otherwise, throws an exception indicating that
     * no service exists.
     *
     * @param aiServiceEntity The AIServiceEntity to check for non-nullness
     * @return The non-null AIServiceEntity
     * @throws AIResponseStatusException If the provided AIServiceEntity is null
     */
    public AIServiceEntity requireNonNullAIServiceEntity(AIServiceEntity aiServiceEntity, String serviceId) {
        if (aiServiceEntity != null) {
            return aiServiceEntity;
        } else {
            throw new AIResponseStatusException(String.format(ExceptionConstant.NO_SERVICE_EXIST, serviceId),
                    HttpStatus.BAD_REQUEST, ErrorCode.NOT_ACCEPTABLE);
        }
    }

    public void validateRequestPathTenant(TenantEnum tenant) {
        log.info("Tenant: {}, Context tenant: {}", tenant, getContextVO().getTenant());
        if (!isSuperAdmin() && tenant != getContextVO().getTenant()) {
            AICommonValidateException.forbiddenException(
                    String.format(ExceptionConstant.NOT_VALID_TENANT_FORMATTER, tenant));
        }
    }

    public void validateUpdateServiceDTO(AIServiceUpdateDTO aiServiceUpdateDTO) {
        if (aiServiceUpdateDTO.getDescription() != null && aiServiceUpdateDTO.getDescription().isEmpty()) {
            AICommonValidateException.badRequestException(ExceptionConstant.NOT_VALID_DESCRIPTION);
        }
        if (aiServiceUpdateDTO.getTemplate() != null && aiServiceUpdateDTO.getTemplate().isEmpty()) {
            AICommonValidateException.badRequestException(ExceptionConstant.NOT_VALID_TEMPLATE);
        }
    }

    public void validateStreamService(AIServiceMO aiServiceMO) {
        if (!ApiMethodEnum.stream_post.equals(aiServiceMO.getMethod())) {
            throw new AIResponseStatusException(
                    String.format(ExceptionConstant.NOT_VALID_STREAM_SERVICE, aiServiceMO.getService()),
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }
    }

    public void validateNonStreamService(AIServiceMO aiServiceMO) {
        if (ApiMethodEnum.stream_post.equals(aiServiceMO.getMethod())) {
            throw new AIResponseStatusException(
                    String.format(ExceptionConstant.STREAM_NOT_SUPPORTED, aiServiceMO.getService()),
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }
    }
}
