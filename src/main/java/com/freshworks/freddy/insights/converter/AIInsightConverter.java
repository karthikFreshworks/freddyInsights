package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.dto.insight.AIInsightCreateDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightResponseDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightUpdateDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightsDismissDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.DateHelper;
import com.freshworks.freddy.insights.modelobject.central.AIInsightCentralPayload;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class AIInsightConverter extends AbstractAICommonConverter {
    public static final String DATE_FORMATTER = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static AIInsightEntity convertToAIInsightEntity(ContextVO contextVO,
                                                           AIInsightCreateDTO createDTO) {
        var insightEntity = Mappers.getMapper(IInsightConverter.class).convertToEntity(createDTO);
        insightEntity.setCreatedBy(contextVO.getId());
        insightEntity.setUpdatedBy(contextVO.getId());
        insightEntity.setCreatedAt(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
        insightEntity.setUpdatedAt(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
        insightEntity.setCreatedByTenant(contextVO.getTenant());
        insightEntity.setTenant(getTenant(contextVO.getAccessType(), createDTO.getTenant(), contextVO.getTenant()));
        insightEntity.setTimeToLive(StatusEnum.INACTIVE == createDTO.getStatus()
                ? DateHelper.getFormattedDate(new Date(), DATE_FORMATTER) :
                (createDTO.getTimeToLive() != null ? createDTO.getTimeToLive() : null));
        if (StringUtils.isEmpty(insightEntity.getVersion())) {
            insightEntity.setVersion(AIInsightConstant.DEFAULT_VERSION);
        }
        return insightEntity;
    }

    public static List<AIInsightResponseDTO> getAllInsightResponseByLanguageCode(List<AIInsightEntity> insightEntities,
                                                                                 String languageCode) {
        var aiInsightResponseList = new ArrayList<AIInsightResponseDTO>();
        for (var insightEntity : insightEntities) {
            var aiInsightResponse =
                    convertInsightEntitiesToInsightResponseBasedOnLanguage(insightEntity, languageCode);
            aiInsightResponseList.add(aiInsightResponse);
        }
        return aiInsightResponseList;
    }

    private static AIInsightResponseDTO convertInsightEntitiesToInsightResponseBasedOnLanguage(
            AIInsightEntity insightEntity, String languageCode) {
        var prompts = AIPromptConverter.getAllPromptResponseByLanguageCode(
                insightEntity.getPrompts(), languageCode);
        var identifiedTitle = getTitleBasedOnLanguage(insightEntity, languageCode);
        return AIInsightResponseDTO.builder()
                .id(insightEntity.getId())
                .accountId(insightEntity.getAccountId())
                .createdByTenant(insightEntity.getCreatedByTenant())
                .bundleId(insightEntity.getBundleId())
                .addons(insightEntity.getAddons())
                .title(identifiedTitle)
                .aggregate(insightEntity.getAggregate())
                .context(insightEntity.getContext())
                .archivedAt(insightEntity.getArchivedAt())
                .domain(insightEntity.getDomain())
                .orgId(insightEntity.getOrgId())
                .group(insightEntity.getGroup())
                .imageUrl(insightEntity.getImageUrl())
                .name(insightEntity.getName())
                .plans(insightEntity.getPlans())
                .prompts(prompts)
                .promptIds(insightEntity.getPromptIds())
                .timeZones(insightEntity.getTimeZones())
                .serviceId(insightEntity.getServiceId())
                .status(insightEntity.getStatus())
                .uiTag(insightEntity.getUiTag())
                .sku(insightEntity.getSku())
                .tags(insightEntity.getTags())
                .usecaseId(insightEntity.getUsecaseId())
                .groupId(insightEntity.getGroupId())
                .languageCode(languageCode)
                .version(insightEntity.getVersion())
                .timeToLive(insightEntity.getTimeToLive())
                .userId(insightEntity.getUserId())
                .tenant(insightEntity.getTenant())
                .type(insightEntity.getType())
                .businessKpi(insightEntity.getBusinessKpi())
                .department(insightEntity.getDepartment())
                .frequency(insightEntity.getFrequency())
                .metric(insightEntity.getMetric())
                .importanceScore(insightEntity.getImportanceScore())
                .scenarioType(insightEntity.getScenarioType())
                .createdBy(insightEntity.getCreatedBy())
                .updatedBy(insightEntity.getUpdatedBy())
                .createdAt(insightEntity.getCreatedAt())
                .updatedAt(insightEntity.getUpdatedAt())
                .build();
    }

    private static String getTitleBasedOnLanguage(AIInsightEntity insightEntity, String languageCode) {
        if (languageCode.equals(insightEntity.getLanguageCode())) {
            return insightEntity.getTitle();
        } else if (insightEntity.getTranslatedFields() != null) {
            var index = IntStream.range(0, insightEntity.getTranslatedFields().size())
                    .filter(i -> insightEntity.getTranslatedFields().get(i).getLanguageCode()
                            .equals(languageCode))
                    .findFirst()
                    .orElse(-1);
            return index != -1 ? insightEntity.getTranslatedFields().get(index).getTitle() : null;
        }
        return null;
    }

    public static AIInsightEntity modifyToAIInsightEntity(ContextVO contextVO,
                                                          AIInsightEntity insight,
                                                          AIInsightUpdateDTO updateDTO) {
        if (updateDTO != null) {
            if (contextVO.getAccessType() == AccessType.SUPER_ADMIN && updateDTO.getTenant() != null) {
                insight.setTenant(updateDTO.getTenant());
            }
            insight.setName(updateDTO.getName() != null ? updateDTO.getName() : insight.getName());
            insight.setUserId(updateDTO.getUserId() != null ? updateDTO.getUserId() : insight.getUserId());
            insight.setServiceId(updateDTO.getServiceId() != null ? updateDTO.getServiceId() : insight.getServiceId());
            insight.setUsecaseId(updateDTO.getUsecaseId() != null ? updateDTO.getUsecaseId() : insight.getUsecaseId());
            insight.setOrgId(updateDTO.getOrgId() != null ? updateDTO.getOrgId() : insight.getOrgId());
            insight.setBundleId(updateDTO.getBundleId() != null ? updateDTO.getBundleId() : insight.getBundleId());
            insight.setGroupId(updateDTO.getGroupId() != null ? updateDTO.getGroupId() : insight.getGroupId());
            insight.setGroup(updateDTO.getGroup() != null ? updateDTO.getGroup() : insight.getGroup());
            insight.setTimeZones(updateDTO.getTimeZones() != null ? updateDTO.getTimeZones() : insight.getTimeZones());
            insight.setDomain(updateDTO.getDomain() != null ? updateDTO.getDomain() : insight.getDomain());
            insight.setSku(updateDTO.getSku() != null ? updateDTO.getSku() : insight.getSku());
            insight.setTitle(updateDTO.getTitle() != null ? updateDTO.getTitle() : insight.getTitle());
            insight.setTranslatedFields(updateDTO.getTranslatedFields() != null ? updateDTO.getTranslatedFields()
                    : insight.getTranslatedFields());
            insight.setLanguageCode(
                    updateDTO.getLanguageCode() != null ? updateDTO.getLanguageCode() : insight.getLanguageCode());
            insight.setUiTag(updateDTO.getUiTag() != null ? updateDTO.getUiTag() : insight.getUiTag());
            insight.setPlans(updateDTO.getPlans() != null ? updateDTO.getPlans() : insight.getPlans());
            insight.setAddons(updateDTO.getAddons() != null ? updateDTO.getAddons() : insight.getAddons());
            insight.setTags(updateDTO.getTags() != null ? updateDTO.getTags() : insight.getTags());
            insight.setPromptIds(updateDTO.getPromptIds() != null ? updateDTO.getPromptIds() : insight.getPromptIds());
            insight.setImageUrl(updateDTO.getImageUrl() != null ? updateDTO.getImageUrl() : insight.getImageUrl());
            insight.setStatus(updateDTO.getStatus() != null ? updateDTO.getStatus() : insight.getStatus());
            insight.setContext(updateDTO.getContext() != null ? updateDTO.getContext() : insight.getContext());
            insight.setAggregate(updateDTO.getAggregate() != null ? updateDTO.getAggregate() : insight.getAggregate());
            insight.setTimeToLive(StatusEnum.INACTIVE == updateDTO.getStatus()
                    ? DateHelper.getFormattedDate(new Date(), DATE_FORMATTER) :
                    (updateDTO.getTimeToLive() != null ? updateDTO.getTimeToLive() : insight.getTimeToLive()));
        }
        insight.setUpdatedAt(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
        insight.setUpdatedBy(contextVO.getId());
        return insight;
    }

    public static void modifyStatusToAIInsightEntity(AIInsightEntity insightEntity,
                                                     StatusEnum requestedStatus) {
        var currentStatus = insightEntity.getStatus();
        switch (requestedStatus) {
        case ACTIVE:
            switch (currentStatus) {
            case INACTIVE:
                insightEntity.setTimeToLive(null);
                break;
            case ARCHIVED:
                insightEntity.setTimeToLive(null);
                insightEntity.setArchivedAt(null);
                break;
            case ACTIVE:
            default:
                throwInvalidStatusException();
            }
            insightEntity.setUpdatedAt(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
            break;
        case INACTIVE:
            switch (currentStatus) {
            case ACTIVE:
                insightEntity.setTimeToLive(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
                break;
            case ARCHIVED:
                insightEntity.setArchivedAt(null);
                break;
            case INACTIVE:
            default:
                throwInvalidStatusException();
            }
            break;
        case ARCHIVED:
            switch (currentStatus) {
            case INACTIVE:
                insightEntity.setArchivedAt(DateHelper.getFormattedDate(new Date(), DATE_FORMATTER));
                break;
            case ACTIVE:
            case ARCHIVED:
            default:
                throwInvalidStatusException();
            }
            break;
        default:
            throwInvalidStatusException();
        }
        insightEntity.setStatus(requestedStatus);
    }

    private static void throwInvalidStatusException() {
        throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_STATUS,
                HttpStatus.BAD_REQUEST,
                ErrorCode.NOT_ACCEPTABLE);
    }

    public static AIInsightCentralPayload prepareInsightCentralPayload(String id,
                                                                       AIInsightEntity insightEntity) {
        var payload = Mappers.getMapper(IInsightConverter.class).prepareInsightPayload(insightEntity);
        payload.setInsightId(id);
        return payload;
    }

    public static AIInsightsDismissDTO prepareInsightDismissDTO(ContextVO contextVO, String insightId) {
        var insightDismissEntity = Mappers.getMapper(IInsightConverter.class).prepareDismissPayload(insightId);
        insightDismissEntity.setInsightId(insightId);
        insightDismissEntity.setTenant(contextVO.getTenant());
        insightDismissEntity.setAccountId(contextVO.getAccountId());
        insightDismissEntity.setUserId(contextVO.getUserId());
        insightDismissEntity.setOrgId(contextVO.getOrgId());
        insightDismissEntity.setBundleId(contextVO.getBundleId());
        insightDismissEntity.setTimeToLive(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        SimpleDateFormat utcFormatter = new SimpleDateFormat(DATE_FORMATTER);
        utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        insightDismissEntity.setCreatedAt(utcFormatter.format(new Date()));
        insightDismissEntity.setCreatedBy(contextVO.getUserId());

        return insightDismissEntity;
    }
}
