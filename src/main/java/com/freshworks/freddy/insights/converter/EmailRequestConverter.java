package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.constant.enums.RegionEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.email.EmailRequestDTO;
import com.freshworks.freddy.insights.dto.promotion.AIPromoteEmailDTO;
import com.freshworks.freddy.insights.entity.AITenantEntity;
import com.freshworks.freddy.insights.helper.AIServiceHelper;
import com.freshworks.freddy.insights.helper.AppConfigHelper;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class EmailRequestConverter {
    private final AppConfigHelper appConfigHelper;

    public EmailRequestDTO transformToEmailDTO(AIPromoteEmailDTO aiPromoteEmailDTO,
                                               List<String> toEmailList,
                                               TenantEnum tenantEnum) {
        if (toEmailList == null || toEmailList.isEmpty()) {
            toEmailList = Arrays.asList(appConfigHelper.getToEmail().split(","));
        }
        EmailRequestDTO.EmailRequestDTOBuilder builder = EmailRequestDTO.builder()
                .emailFrom(ObjectMapperHelper.createObjectNode("email", appConfigHelper.getFromEmail()))
                .emailTOList(ObjectMapperHelper.createListObjectNode("email", toEmailList))
                .accountId(tenantEnum.name());
        Map.Entry<RegionEnum, AIPromoteEmailDTO.RegionStatus> firstEntry = null;
        Iterator<Map.Entry<RegionEnum, AIPromoteEmailDTO.RegionStatus>> iterator =
                aiPromoteEmailDTO.getRegionStatusMap().entrySet().iterator();
        if (iterator.hasNext()) {
            firstEntry = iterator.next();
        }
        Map<String, Object> m = Map.of("successServiceIds",
                firstEntry.getValue().getSuccessServiceIds().toString(), "failureServiceIds",
                firstEntry.getValue().getFailureServiceIdsWithMessages(), "region", firstEntry.getKey().toString());
        String replacedBody = AIServiceHelper.getTemplate(m, appConfigHelper.getPromoteServiceBody(), "{{", "}}");

        builder.emailBody(replacedBody);
        builder.emailBodyHtml(replacedBody);
        log.info("Email will be send to: {} with body: {}", toEmailList, aiPromoteEmailDTO);
        return builder.build();
    }

    public EmailRequestDTO convertEmailRequestDTO(AITenantEntity aiTenantEntity) {
        String toEmail = aiTenantEntity.getEmail();
        EmailRequestDTO.EmailRequestDTOBuilder builder = EmailRequestDTO
                .builder()
                .emailFrom(ObjectMapperHelper.createObjectNode("email", appConfigHelper.getFromEmail()))
                .emailTOList(List.of(ObjectMapperHelper.createObjectNode("email", toEmail)))
                .accountId(aiTenantEntity.getTenant().name());
        Map<String, Object> m = Map.of("tenant", aiTenantEntity.getTenant().name(), "adminKey",
                aiTenantEntity.getAdminKey(), "userKey", aiTenantEntity.getUserKey(), "region",
                appConfigHelper.getRegion(), "userName", toEmail.substring(0, toEmail.indexOf('@')));
        String replacedBody = AIServiceHelper.getTemplate(m, appConfigHelper.getBody(), "{{", "}}");
        String replacedSubject = AIServiceHelper.getTemplate(m, appConfigHelper.getSubject(), "{{", "}}");
        builder.subject(replacedSubject);
        builder.emailBody(replacedBody);
        builder.emailBodyHtml(replacedBody);
        return builder.build();
    }
}
