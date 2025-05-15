package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.AIPromptConstant;
import com.freshworks.freddy.insights.dto.prompt.AIPromptBaseDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptCreateDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptResponseDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptUpdateDTO;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

public class AIPromptConverter extends AbstractAICommonConverter {
    public static AIPromptEntity convertToAIPromptEntity(AIPromptCreateDTO createDTO,
                                                         ContextVO contextVO) {
        var promptEntity = Mappers.getMapper(IPromptConverter.class).convertToEntity(createDTO);
        promptEntity.setCreatedBy(contextVO.getId());
        promptEntity.setCreatedByTenant(contextVO.getTenant());
        promptEntity.setUpdatedBy(contextVO.getId());
        promptEntity.setCreatedAt(getFormattedDate(new Date()));
        promptEntity.setUpdatedAt(getFormattedDate(new Date()));
        promptEntity.setTenant(getTenant(contextVO.getAccessType(), createDTO.getTenant(), contextVO.getTenant()));

        if (StringUtils.isEmpty(promptEntity.getVersion())) {
            promptEntity.setVersion(AIInsightConstant.DEFAULT_VERSION);
        }
        promptEntity.setSuggest(createDTO.getSuggest() != null ? createDTO.getSuggest()
                : AIPromptConstant.PROMPT_DEFAULT_SUGGEST_VALUE);
        promptEntity.setWeight(createDTO.getWeight() != null ? createDTO.getWeight()
                : AIPromptConstant.PROMPT_DEFAULT_WEIGHT);
        return promptEntity;
    }

    public static AIPromptEntity modifyToAIPromptEntity(AIPromptUpdateDTO updateDTO,
                                                        AIPromptEntity aiPromptEntity,
                                                        ContextVO contextVO) {
        return AIPromptEntity.builder()
                .id(aiPromptEntity.getId())
                .text(updateDTO.getText() != null ? updateDTO.getText() : aiPromptEntity.getText())
                .createdAt(aiPromptEntity.getCreatedAt())
                .updatedAt(getFormattedDate(new Date()))
                .accountId(aiPromptEntity.getAccountId())
                .name(aiPromptEntity.getName())
                .group(updateDTO.getGroup() != null ? updateDTO.getGroup() : aiPromptEntity.getGroup())
                .languageCode(aiPromptEntity.getLanguageCode())
                .translatedFields(updateDTO.getTranslatedFields() != null
                        ? updateDTO.getTranslatedFields() : aiPromptEntity.getTranslatedFields())
                .createdBy(aiPromptEntity.getCreatedBy()).updatedBy(contextVO.getId())
                .userId(aiPromptEntity.getUserId())
                .tenant(aiPromptEntity.getTenant())
                .version(aiPromptEntity.getVersion())
                .suggest(updateDTO.getSuggest() != null ? updateDTO.getSuggest() : aiPromptEntity.getSuggest())
                .weight(updateDTO.getWeight() != null ? updateDTO.getWeight() : aiPromptEntity.getWeight())
                .tags(updateDTO.getTags() != null ? updateDTO.getTags() : aiPromptEntity.getTags())
                .intentHandler(updateDTO.getIntentHandler() != null ? AIPromptBaseDTO.IntentHandler.builder()
                        .role(updateDTO.getIntentHandler().getRole() != null
                                ? updateDTO.getIntentHandler().getRole() :
                                aiPromptEntity.getIntentHandler().getRole())
                        .oneWay(updateDTO.getIntentHandler().getOneWay() != null
                                ? updateDTO.getIntentHandler().getOneWay() :
                                aiPromptEntity.getIntentHandler().getOneWay())
                        .hidden(updateDTO.getIntentHandler().getHidden() != null
                                ? updateDTO.getIntentHandler().getHidden() :
                                aiPromptEntity.getIntentHandler().getHidden())
                        .system(updateDTO.getIntentHandler().getSystem() != null
                                ? updateDTO.getIntentHandler().getSystem() :
                                aiPromptEntity.getIntentHandler().getSystem())
                        .mimeType(updateDTO.getIntentHandler().getMimeType() != null
                                ? updateDTO.getIntentHandler().getMimeType() :
                                aiPromptEntity.getIntentHandler().getMimeType())
                        .id(updateDTO.getIntentHandler().getId() != null
                                ? updateDTO.getIntentHandler().getId() :
                                aiPromptEntity.getIntentHandler().getId())
                        .build() : aiPromptEntity.getIntentHandler())
                .build();
    }

    private static String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return dateFormat.format(date);
    }

    public static List<AIPromptResponseDTO> getAllPromptResponseByLanguageCode(List<AIPromptEntity> records,
                                                                               String languageCode) {
        var aiPromptResponseList = new ArrayList<AIPromptResponseDTO>();
        for (var record : records) {
            var promptResponseDto =
                    convertRecordToPromptResponseBasedOnLanguage(record, languageCode);
            if (promptResponseDto != null) {
                aiPromptResponseList.add(promptResponseDto);
            }
        }
        return aiPromptResponseList;
    }

    private static AIPromptResponseDTO convertRecordToPromptResponseBasedOnLanguage(AIPromptEntity record,
                                                                                    String languageCode) {
        var identifiedText = getTextBasedOnLanguage(record, languageCode);
        if (identifiedText == null) {
            return null;
        }
        return AIPromptResponseDTO.builder()
                .id(record.getId())
                .accountId(record.getAccountId())
                .name(record.getName())
                .intentHandler(record.getIntentHandler())
                .tenant(record.getTenant())
                .userId(record.getUserId())
                .group(record.getGroup())
                .suggest(record.getSuggest())
                .weight(record.getWeight())
                .createdBy(record.getCreatedBy())
                .updatedBy(record.getUpdatedBy())
                .version(record.getVersion())
                .createdAt(record.getCreatedAt())
                .createdByTenant(record.getCreatedByTenant())
                .updatedAt(record.getUpdatedAt())
                .languageCode(languageCode)
                .text(identifiedText)
                .tags(record.getTags())
                .build();
    }

    private static String getTextBasedOnLanguage(AIPromptEntity aiPromptEntity, String languageCode) {
        if (languageCode.equals(aiPromptEntity.getLanguageCode())) {
            return aiPromptEntity.getText();
        } else if (aiPromptEntity.getTranslatedFields() != null) {
            var index = IntStream.range(0, aiPromptEntity.getTranslatedFields().size())
                    .filter(i -> aiPromptEntity.getTranslatedFields().get(i).getLanguageCode()
                            .equals(languageCode))
                    .findFirst()
                    .orElse(-1);
            return index != -1 ? aiPromptEntity.getTranslatedFields().get(index).getText() : null;
        }
        return null;
    }
}
