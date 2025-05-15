package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.AIPromptConstant;
import com.freshworks.freddy.insights.constant.ESConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.converter.AIPromptConverter;
import com.freshworks.freddy.insights.dto.AIResponseDTO;
import com.freshworks.freddy.insights.dto.AIResponsePaginationDTO;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.promotion.AIPromoteDTO;
import com.freshworks.freddy.insights.dto.prompt.*;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.AITenantHelper;
import com.freshworks.freddy.insights.helper.AddonHelper;
import com.freshworks.freddy.insights.modelobject.AIFreddyAddonMO;
import com.freshworks.freddy.insights.modelobject.AIPromoteMO;
import com.freshworks.freddy.insights.modelobject.ESResponseMO;
import com.freshworks.freddy.insights.validator.AICommonValidateException;
import com.freshworks.freddy.insights.validator.AICommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.*;

@Slf4j
@Service
public class AIPromptService extends AbstractAIPromotionService<AIPromptEntity> {
    private AIInsightService insightService;
    private AICommonValidator aiCommonValidator;
    private AITenantHelper aiTenantHelper;
    private AddonHelper addonHelper;
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setAddonHelper(
            AddonHelper addonHelper) {
        this.addonHelper = addonHelper;
    }

    @Autowired
    public void setInsightService(AIInsightService insightService) {
        this.insightService = insightService;
    }

    @Autowired
    public void setAiCommonValidator(AICommonValidator commonValidator) {
        this.aiCommonValidator = commonValidator;
    }

    @Autowired
    public void setAiTenantHelper(AITenantHelper aiTenantHelper) {
        this.aiTenantHelper = aiTenantHelper;
    }

    public AIResponseDTO registerPrompt(AIPromptCreateDTO aiPromptCreateDTO) {
        aiCommonValidator.validateCreateOrUpdatePrompt(aiPromptCreateDTO.getTranslatedFields(),
                aiPromptCreateDTO.getLanguageCode());
        var aiPromptEntity = AIPromptConverter.convertToAIPromptEntity(aiPromptCreateDTO, getContextVO());
        log.info("Creating prompt entity with id: {}", aiPromptEntity.getId());
        var indexMO = new ESCriteriaBuilder.IndexBuilder<AIPromptEntity>(ESIndexNameEnum.prompt)
                .indexKey(aiPromptEntity.getId())
                .routingKey(aiPromptEntity.getTenant().name())
                .source(aiPromptEntity)
                .waitUntil(true)
                .build();
        var response = queryHelper.index(indexMO);
        return new AIResponseDTO(response.getId());
    }

    @Cacheable(value = "prompt",
            key = "T(String).format('%s_%s', #id, #root.target.getContextVO().getTenant())",
            condition = "#id != null",
            unless = "#result==null")
    public AIPromptEntity getPromptById(String id) {
        var searchMO = new ESCriteriaBuilder.Builder(ESIndexNameEnum.prompt)
                .tenants(getTenantsByBundleOrSuperAdminOrRequestedTenant(null))
                .in(AIPromptConstant.ID, id)
                .buildSearch();
        var responseMO = queryHelper.search(searchMO, AIPromptEntity.class);
        log.info("AIPrompt docs: {} found for id: {} ", responseMO.getCount(), id);
        if (responseMO.getCount() != 1) {
            AICommonValidateException.notFoundException(ExceptionConstant.NOT_VALID_PROMPT_ID);
        }
        return responseMO.getRecords().getFirst();
    }

    public AIResponsePaginationDTO<AIPromptResponseDTO> getAllPrompts(AIPromptParamDTO paramDTO,
                                                                      PaginationDTO paginationDTO,
                                                                      String acceptLanguage) {
        log.info("accept Language header value: {}", acceptLanguage);
        var acceptLanguageCodes = aiCommonValidator.validateAndReturnAcceptLanguageCodes(acceptLanguage);
        List<String> tags = getTagsFromHeaderIfPresent(paramDTO.getTags());
        paramDTO.setTags(tags);
        List<String> languageCodes = (acceptLanguageCodes != null && !acceptLanguageCodes.isEmpty())
                ? acceptLanguageCodes
                : List.of(paramDTO.getLanguageCode());
        List<TenantEnum> tenants = getTenantsByBundleOrSuperAdminOrRequestedTenant(paramDTO.getTenant());
        var searchMOBuilder = baseBuildSearchMo(paginationDTO, paramDTO);
        AIFreddyAddonMO aiFreddyAddonMO = AIFreddyAddonMO.builder()
                .tenants(tenants)
                .aiPromptParamDTO(paramDTO)
                .baseESSearchBuilder(searchMOBuilder)
                .build();
        if (appConfigHelper.isAddonSupportApplicable(getContextVO())) {
            return getAIPromptsBasedOnAddons(aiFreddyAddonMO, languageCodes);
        } else {
            searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.DEFAULT_HANDLER_WITHOUT_ADDON)
                    .getPromptSearchMOBuilder(aiFreddyAddonMO);
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                    constructTenantFilter(tenants, paramDTO.getTags()));
            return getAIPromptResponseByLanguageCode(languageCodes, searchMOBuilder);
        }
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> getAIPromptsBasedOnAddons(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        if (addonHelper.isInsightAddonAvailable()) {
            return executeInsightAddonHandler(aiFreddyAddonMO, languageCodes);
        } else if (addonHelper.isCopilotAddonAvailable() && addonHelper.isSelfServiceAddonAvailable()) {
            return executeCopilotSelfServiceAddonHandler(aiFreddyAddonMO, languageCodes);
        } else if (addonHelper.isCopilotAddonAvailable()) {
            return executeCopilotAddonHandler(aiFreddyAddonMO, languageCodes);
        } else if (addonHelper.isSelfServiceAddonAvailable()) {
            return executeSelfServiceAddonHandler(aiFreddyAddonMO, languageCodes);
        }
        return new AIResponsePaginationDTO<>(new ArrayList<>(), false);
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> executeInsightAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.INSIGHTS_ADDON_HANDLER)
                .getPromptSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, constructTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiPromptParamDTO().getTags()));
        return getAIPromptResponseByLanguageCode(languageCodes, searchMOBuilder);
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> executeSelfServiceAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        return new AIResponsePaginationDTO<>(new ArrayList<>(), false);
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> executeCopilotAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.COPILOT_ADDON_HANDLER)
                .getPromptSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, constructTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiPromptParamDTO().getTags()));
        return getAIPromptResponseByLanguageCode(languageCodes, searchMOBuilder);
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> executeCopilotSelfServiceAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.COPILOT_SELF_SERVICE_ADDON_HANDLER)
                .getPromptSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, constructTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiPromptParamDTO().getTags()));
        return getAIPromptResponseByLanguageCode(languageCodes, searchMOBuilder);
    }

    private List<String> getTagsFromHeaderIfPresent(List<String> tags) {
        return aiRequestContext.getContextVO().getTags() != null
                ? Arrays.stream(aiRequestContext.getContextVO().getTags().split(","))
                .map(String::trim)
                .collect(Collectors.toList())
                : tags;
    }

    private AIResponsePaginationDTO<AIPromptResponseDTO> getAIPromptResponseByLanguageCode(
            List<String> languageCodes, ESCriteriaBuilder.Builder searchMOBuilder) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        var getAllAIPromptResponse = new ArrayList<AIPromptResponseDTO>();
        var isNextPage = false;
        for (String language : languageCodes) {
            boolQuery.should(QueryBuilders.termQuery(LANGUAGE_CODE, language));
            boolQuery.should(QueryBuilders.nestedQuery(TRANSLATED_FIELDS,
                    QueryBuilders.termQuery(TRANSLATED_FIELDS + "." + LANGUAGE_CODE, language),
                    ScoreMode.Avg));
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, boolQuery);
            var searchMO = searchMOBuilder.buildSearch();
            ESResponseMO<AIPromptEntity> responseMO = null;
            responseMO = queryHelper.search(searchMO, AIPromptEntity.class);
            isNextPage = responseMO != null && responseMO.isNextPage();
            log.info("AIPrompt docs: {} found for given parameter.", responseMO != null ? responseMO.getCount() : 0);
            List<AIPromptEntity> records = responseMO != null ? responseMO.getRecords() : Collections.emptyList();
            if (!records.isEmpty()) {
                getAllAIPromptResponse = (ArrayList<AIPromptResponseDTO>)
                        AIPromptConverter.getAllPromptResponseByLanguageCode(records, language);
                break;
            }
        }
        return new AIResponsePaginationDTO<>(getAllAIPromptResponse, isNextPage);
    }

    public AIPromptEntity updatePrompt(String promptId, AIPromptUpdateDTO updateDTO) {
        var promptEntity = getPromptById(promptId);
        aiCommonValidator.validateCreateOrUpdatePrompt(
                updateDTO.getTranslatedFields(),
                promptEntity.getLanguageCode());
        clearPromptCacheByTenantsAndId(promptId);
        var updatedPromptEntity = AIPromptConverter.modifyToAIPromptEntity(updateDTO, promptEntity, getContextVO());
        return saveUpdatedPrompt(updatedPromptEntity);
    }

    private AIPromptEntity saveUpdatedPrompt(AIPromptEntity updatedPromptEntity) {
        var indexMO = new ESCriteriaBuilder.IndexBuilder<AIPromptEntity>(ESIndexNameEnum.prompt)
                .indexKey(String.valueOf(updatedPromptEntity.getId()))
                .routingKey(updatedPromptEntity.getTenant().name())
                .source(updatedPromptEntity).build();
        queryHelper.index(indexMO);
        return updatedPromptEntity;
    }

    public AIPromptEntity updatePromptTranslatedFields(String promptId,
                                                       AIPromptTranslationDTO incomingTranslationField) {
        var promptEntity = getPromptById(promptId);
        clearPromptCacheByTenantsAndId(promptId);
        if (incomingTranslationField.getLanguageCode().equals(promptEntity.getLanguageCode())) {
            AICommonValidateException.conflictDataException(ExceptionConstant.CONFLICT_PARENT_LANGUAGE_CODE);
        }
        List<AIPromptTranslationDTO> updatedTranslatedFields =
                getTranslatedFields(incomingTranslationField, promptEntity);
        AIPromptUpdateDTO aiPromptUpdateDTO = new AIPromptUpdateDTO();
        aiPromptUpdateDTO.setTranslatedFields(updatedTranslatedFields);
        var updatedPromptEntity = AIPromptConverter.modifyToAIPromptEntity(aiPromptUpdateDTO,
                promptEntity, getContextVO());
        return saveUpdatedPrompt(updatedPromptEntity);
    }

    private List<AIPromptTranslationDTO> getTranslatedFields(AIPromptTranslationDTO incomingTranslationField,
                                                             AIPromptEntity promptEntity) {
        var existingTranslatedFields = promptEntity.getTranslatedFields();
        if (existingTranslatedFields != null) {
            return getUpdatedExistingTranslatedFields(existingTranslatedFields,
                    incomingTranslationField);
        } else {
            existingTranslatedFields = new ArrayList<>();
            existingTranslatedFields.add(incomingTranslationField);
        }
        // returning updated translated fields
        return existingTranslatedFields;
    }

    private List<AIPromptTranslationDTO> getUpdatedExistingTranslatedFields(
            List<AIPromptTranslationDTO> existingTranslatedFields, AIPromptTranslationDTO incomingTranslationField) {
        boolean hasCommonLanguageCode = existingTranslatedFields.stream()
                .anyMatch(obj -> incomingTranslationField.getLanguageCode().equals(obj.getLanguageCode()));
        if (hasCommonLanguageCode) {
            for (int i = 0; i < existingTranslatedFields.size(); i++) {
                AIPromptTranslationDTO existingField = existingTranslatedFields.get(i);
                if (incomingTranslationField.getLanguageCode().equals(existingField.getLanguageCode())) {
                    existingField.setText(incomingTranslationField.getText());
                    existingTranslatedFields.set(i, existingField);
                    break;
                }
            }
        } else {
            existingTranslatedFields.add(incomingTranslationField);
        }
        return existingTranslatedFields;
    }

    public void deletePrompt(String id) {
        var tenant = getContextVO().getTenant().name();
        if (isSuperAdmin()) {
            tenant = getPromptById(id).getTenant().name();
        }
        var indexMO = new ESCriteriaBuilder.DeleteBuilder(ESIndexNameEnum.prompt)
                .indexKey(id)
                .routingKey(tenant)
                .build();
        boolean isDeleted = queryHelper.delete(indexMO);
        log.info("AIPrompt record for id: {}, got deleted: {}", id, isDeleted);
        if (!isDeleted) {
            AICommonValidateException.notFoundException(ExceptionConstant.NOT_VALID_PROMPT_ID);
        }
        clearPromptCacheByTenantsAndId(id);
        insightService.updateInsightsForDeletedPrompts(Set.of(id));
    }

    private ESCriteriaBuilder.Builder baseBuildSearchMo(PaginationDTO paginationDTO, AIPromptParamDTO paramDTO) {
        return new ESCriteriaBuilder.Builder(ESIndexNameEnum.prompt)
                .page(paginationDTO.getPage())
                .limit(paginationDTO.getSize())
                .in(AIPromptConstant.ACCOUNT_ID, paramDTO.getAccountId())
                .contains(AIPromptConstant.GROUP, paramDTO.getGroup()).autoComplete(true)
                .in(AIPromptConstant.NAME, paramDTO.getName())
                .in(AIPromptConstant.VERSION, paramDTO.getVersion())
                .in(AIPromptConstant.ID, paramDTO.getPromptIds())
                .in(AIPromptConstant.USER_ID, paramDTO.getUserId())
                .in(AIPromptConstant.TAGS, paramDTO.getTags())
                .sort("weight|desc");
    }

    public BoolQueryBuilder constructTenantFilter(List<TenantEnum> tenants, List<String> tags) {
        if (getContextVO().getUserId() == null) {
            return null;
        }
        BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
        for (TenantEnum tenant : tenants) {
            List<String> filters = AIPromptConstant.PROMPT_TENANT_FILTERS.get(tenant);
            BoolQueryBuilder tenantQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery(AIPromptConstant.TENANT, tenant));
            if (filters != null && !filters.isEmpty()) {
                for (String filter : filters) {
                    if (TAGS.equals(filter) && getContextVO().getTags() != null) {
                        tenantQuery.must(QueryBuilders.termsQuery(AIPromptConstant.TAGS, tags));
                    }
                }
            }
            shouldQuery.should(tenantQuery);
        }
        log.info("Should query {}", shouldQuery);
        return shouldQuery;
    }

    public AIPromptEntity deletePromptTranslatedField(String promptId, String languageCodeEnum) {
        var promptEntity = getPromptById(promptId);
        clearPromptCacheByTenantsAndId(promptId);
        AIPromptUpdateDTO aiPromptUpdateDTO = new AIPromptUpdateDTO();
        if (promptEntity.getTranslatedFields() == null) {
            promptEntity.setTranslatedFields(new ArrayList<>());
        }
        List<AIPromptTranslationDTO> translatedFieldsAfterDelete = promptEntity.getTranslatedFields().stream()
                .filter(field -> !languageCodeEnum.equals(field.getLanguageCode()))
                .collect(Collectors.toList());
        aiPromptUpdateDTO.setTranslatedFields(translatedFieldsAfterDelete);
        var updatedPromptEntity = AIPromptConverter.modifyToAIPromptEntity(aiPromptUpdateDTO,
                promptEntity, getContextVO());
        return saveUpdatedPrompt(updatedPromptEntity);
    }

    @CacheEvict(value = "prompt", allEntries = true)
    public void deleteAllPrompts(TenantEnum requestTenant) {
        var searchMO = new ESCriteriaBuilder.Builder(ESIndexNameEnum.prompt)
                .tenants(List.of(requestTenant))
                .buildSearch();
        queryHelper.bulkDeleteByQuery(searchMO);
        insightService.deleteInsightsByTenant(requestTenant, null);
        log.info("AIPrompt record for tenant(s): {}, got deleted.", requestTenant);
    }

    @Override
    protected List<Object> promoteAttributesToEntities(List<AIPromoteDTO.Attribute> attributes) {
        return attributes.stream()
                .map(AIPromoteDTO.Attribute::getId)
                .map(this::getPromptById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void promoteAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException {
        List<AIPromoteMO> promoteMOList = getAiPromoteMOList(aiPromoteDTOs);
        log.info("Starting prompt promotion async.");
        var promotionHandlerStrategy = promotionHandler.get(AIHandlerConstant.AI_PROMPT_PROMOTE_STRATEGY);
        promotionHandlerStrategy.executeStrategy(promoteMOList);
    }

    @Override
    public void promoteUpdateAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException {
        List<AIPromoteMO> promoteMOList = getAiPromoteMOList(aiPromoteDTOs);
        log.info("Starting update prompt promotion async.");
        var promotionHandlerStrategy =
                promotionHandler.get(AIHandlerConstant.AI_PROMPT_UPDATE_PROMOTE_STRATEGY);
        promotionHandlerStrategy.executeStrategy(promoteMOList);
    }

    private void clearPromptCacheByTenantsAndId(String id) {
        List.of(TenantEnum.values())
                .forEach(tenant ->
                        redisTemplate.delete(String.format("prompt::%s_%s", id, tenant)));
    }

    public void invalidatePromptCache() {
        // Get all keys matching the pattern
        Set<String> keys = redisTemplate.keys(AIPromptConstant.PROMPT_CACHE_PATTERN);

        // Delete all keys
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("successfully deleted the prompt caches");
        }
    }
}
