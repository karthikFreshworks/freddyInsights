package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.*;
import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.converter.AIInsightConverter;
import com.freshworks.freddy.insights.dto.AIBulkResponseDTO;
import com.freshworks.freddy.insights.dto.AIResponseDTO;
import com.freshworks.freddy.insights.dto.AIResponsePaginationDTO;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.central.CentralRequestDTO;
import com.freshworks.freddy.insights.dto.insight.*;
import com.freshworks.freddy.insights.dto.promotion.AIPromoteDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AIInsightHelper;
import com.freshworks.freddy.insights.helper.AITenantHelper;
import com.freshworks.freddy.insights.helper.AddonHelper;
import com.freshworks.freddy.insights.modelobject.*;
import com.freshworks.freddy.insights.modelobject.central.AIInsightCentralPayload;
import com.freshworks.freddy.insights.repository.AIDismissRepository;
import com.freshworks.freddy.insights.service.central.CentralProducerService;
import com.freshworks.freddy.insights.validator.AICommonValidateException;
import com.freshworks.freddy.insights.validator.AICommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.*;

@Slf4j
@Service
public class AIInsightService extends AbstractAIPromotionService<AIInsightEntity> {
    private AIPromptService promptService;
    private CentralProducerService centralProducerService;
    private AICommonValidator aiCommonValidator;
    private AITenantHelper aiTenantHelper;
    private AddonHelper addonHelper;
    private AIInsightHelper aiInsightHelper;
    @Value("#{'${allow_dismissed_tenant}'.split(',')}")
    public List<String> allowDismissedTenant;

    @Autowired
    private AIDismissRepository aiDismissRepository;

    @Autowired
    public void setCentralProducerService(
            CentralProducerService centralProducerService) {
        this.centralProducerService = centralProducerService;
    }

    @Autowired
    public void setAddonHelper(
            AddonHelper addonHelper) {
        this.addonHelper = addonHelper;
    }

    @Autowired
    public void setPromptService(AIPromptService promptService) {
        this.promptService = promptService;
    }

    @Autowired
    public void setAiTenantHelper(AITenantHelper aiTenantHelper) {
        this.aiTenantHelper = aiTenantHelper;
    }

    @Autowired
    public void setAiInsightHelper(AIInsightHelper aiInsightHelper) {
        this.aiInsightHelper = aiInsightHelper;
    }

    @Autowired
    public void setAiCommonValidator(AICommonValidator aiCommonValidator) {
        this.aiCommonValidator = aiCommonValidator;
    }

    public AIResponseDTO createInsight(AIInsightCreateDTO createDTO) {
        aiCommonValidator.validateCreateOrUpdateInsight(createDTO.getTimeToLive(),
                createDTO.getStatus(),
                createDTO.getTranslatedFields(),
                createDTO.getLanguageCode());

        var insightEntity = AIInsightConverter.convertToAIInsightEntity(getContextVO(), createDTO);
        var isPromptExist = isPromptExist(insightEntity);
        if (!isPromptExist) {
            AICommonValidateException.notAcceptableException(ExceptionConstant.NOT_VALID_INSIGHT_PROMPTS);
        }
        log.info("Insight entity create initiated");
        String response = getESIndexResponse(insightEntity);
        sendInsightToCentral(insightEntity, response);
        return new AIResponseDTO(response);
    }

    public AIResponseDTO updateInsight(String insightId,
                                       AIInsightUpdateDTO updateDTO) {
        var insightEntity = getInsightFromESById(insightId);
        var newStatus = updateDTO.getStatus();
        aiCommonValidator.validateCreateOrUpdateInsight(updateDTO.getTimeToLive(),
                updateDTO.getStatus(),
                updateDTO.getTranslatedFields(),
                updateDTO.getLanguageCode() != null ? updateDTO.getLanguageCode() : insightEntity.getLanguageCode());

        if (insightEntity.getStatus() != StatusEnum.ACTIVE) {
            if (StatusEnum.ACTIVE != newStatus) {
                AICommonValidateException.notAcceptableException(ExceptionConstant.NOT_ACTIVE_INSIGHT);
            } else {
                if (updateDTO.getTimeToLive() == null) {
                    insightEntity.setTimeToLive(null);
                }
            }
        }

        insightEntity.setPrompts(null);
        validateUpdateInsightPrompts(updateDTO, insightEntity);
        AIInsightConverter.modifyToAIInsightEntity(getContextVO(), insightEntity, updateDTO);
        log.info("Insight entity update initiated, id: {}", insightId);
        String response = getESIndexResponse(insightEntity);
        return new AIResponseDTO(response);
    }

    public AIResponseDTO updateInsightTranslatedField(String id,
                                                      AIInsightTranslationDTO incomingInsightTranslationField) {
        var insightEntity = getInsightFromESById(id);

        // don't allow to update the inactive insight
        if (insightEntity.getStatus() != StatusEnum.ACTIVE) {
            AICommonValidateException.notAcceptableException(ExceptionConstant.NOT_ACTIVE_INSIGHT);
        }

        // throw exception when the language code conflicts with parent language code
        if (incomingInsightTranslationField.getLanguageCode().equals(insightEntity.getLanguageCode())) {
            AICommonValidateException.conflictDataException(ExceptionConstant.CONFLICT_PARENT_LANGUAGE_CODE);
        }

        List<AIInsightTranslationDTO> updatedTranslatedFields =
                getInsightTranslatedFields(incomingInsightTranslationField, insightEntity);

        AIInsightUpdateDTO aiInsightUpdateDTO = new AIInsightUpdateDTO();
        aiInsightUpdateDTO.setTranslatedFields(updatedTranslatedFields);
        AIInsightConverter.modifyToAIInsightEntity(getContextVO(), insightEntity, aiInsightUpdateDTO);
        log.info("Insight entity update initiated for translated fields, id: {}", id);
        String response = getESIndexResponse(insightEntity);
        return new AIResponseDTO(response);
    }

    private List<AIInsightTranslationDTO> getInsightTranslatedFields(AIInsightTranslationDTO incomingTranslationField,
                                                                     AIInsightEntity aiInsightEntity) {
        var existingTranslatedFields = aiInsightEntity.getTranslatedFields();
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

    private List<AIInsightTranslationDTO> getUpdatedExistingTranslatedFields(
            List<AIInsightTranslationDTO> existingTranslatedFields, AIInsightTranslationDTO incomingTranslationField) {
        boolean hasCommonLanguageCode = existingTranslatedFields.stream()
                .anyMatch(obj -> incomingTranslationField.getLanguageCode().equals(obj.getLanguageCode()));
        if (hasCommonLanguageCode) {
            for (int i = 0; i < existingTranslatedFields.size(); i++) {
                AIInsightTranslationDTO existingField = existingTranslatedFields.get(i);
                if (incomingTranslationField.getLanguageCode().equals(existingField.getLanguageCode())) {
                    existingField.setTitle(incomingTranslationField.getTitle() != null
                            ? incomingTranslationField.getTitle() : existingField.getTitle());
                    existingTranslatedFields.set(i, existingField);
                    break;
                }
            }
        } else {
            existingTranslatedFields.add(incomingTranslationField);
        }
        return existingTranslatedFields;
    }

    private String getESIndexResponse(AIInsightEntity insightEntity) {
        var response = queryHelper.index(getEsIndexMO(insightEntity));
        if (response == null) {
            throw new AIResponseStatusException(ExceptionConstant.ERROR_IN_INDEXING,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }
        log.info("Insight record ES status: {}, id: {}, version: {}", response.getResult().name(), response.getId(),
                response.getVersion());
        return response.getId();
    }

    public AIInsightEntity getInsightById(String id) {
        AIInsightEntity insightEntity = getInsightFromESById(id);
        insightEntity.setPrompts(getPrompts(insightEntity.getPromptIds()));
        return insightEntity;
    }

    private AIInsightEntity getInsightFromESById(String id) {
        var tenants = getTenantsByBundleOrSuperAdminOrRequestedTenant(null);
        var responseMO = getSearchMOResponseById(id, tenants);
        log.info("AIInsight docs: {}, found for id: {} ", responseMO.getCount(), id);
        if (responseMO.getCount() != 1) {
            AICommonValidateException.notFoundException(
                    String.format(AIInsightConstant.STRING_FORMATTER, ExceptionConstant.NO_RECORD_FOR_ID, id));
        }
        var insightEntity = responseMO.getRecords().get(0);
        if (!isSuperAdmin() && insightEntity.getStatus() == StatusEnum.ARCHIVED) {
            throw new AIResponseStatusException(
                    String.format(AIInsightConstant.STRING_FORMATTER, ExceptionConstant.ARCHIVED_RECORD, id),
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }
        return insightEntity;
    }

    private ESResponseMO<AIInsightEntity> getSearchMOResponseById(String id, List<TenantEnum> tenants) {
        var searchMO = new ESCriteriaBuilder.Builder(ESIndexNameEnum.insight)
                .tenants(tenants)
                .in(AIInsightConstant.ID, id)
                .buildSearch();
        return queryHelper.search(searchMO, AIInsightEntity.class);
    }

    private List<AIPromptEntity> getPrompts(List<String> promptIds) {
        return promptIds.stream()
                .map(id -> promptService.getPromptById(id))
                .collect(Collectors.toList());
    }

    public AIResponsePaginationDTO<AIInsightResponseDTO> getInsights(
            AIInsightParamDTO paramDTO, PaginationDTO paginationDTO, String acceptLanguage,
            Map<String, String> queryHashParameters) {
        paramDTO = getUpdatedParamDTO(paramDTO, queryHashParameters);
        log.info("accept Language header value: {}", acceptLanguage);
        var acceptLanguageCodes = aiCommonValidator.validateAndReturnAcceptLanguageCodes(acceptLanguage);
        List<String> languageCodes = (acceptLanguageCodes != null && !acceptLanguageCodes.isEmpty())
                ? acceptLanguageCodes : List.of(paramDTO.getLanguageCode());
        List<TenantEnum> tenants = getTenantsByBundleOrSuperAdminOrRequestedTenant(paramDTO.getTenant());
        var searchMOBuilder = aiInsightHelper.buildBaseSearchMo(getContextVO(),
                paginationDTO, paramDTO, isSuperAdmin());

        log.info("Filtering Dismissed insights");
        if (allowDismissedTenant.contains(getContextVO().getTenant().name())) {
            var dismissInsights = aiDismissRepository.findByUserId(getContextVO().getUserId());
            if (Objects.nonNull(searchMOBuilder) && Objects.nonNull(dismissInsights)) {
                List<String> dismissInsightsList =  dismissInsights.stream()
                        .map(AIInsightsDismissDTO::getInsightId).toList();
                searchMOBuilder.notIn(ID,dismissInsightsList);
            }
        }

        AIFreddyAddonMO aiFreddyAddonMO = AIFreddyAddonMO.builder()
                .tenants(tenants)
                .aiInsightParamDTO(paramDTO)
                .baseESSearchBuilder(searchMOBuilder)
                .build();

        if (appConfigHelper.isAddonSupportApplicable(getContextVO())) {
            return getAIInsightsBasedOnAddons(aiFreddyAddonMO, languageCodes);
        } else {
            searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.DEFAULT_HANDLER_WITHOUT_ADDON)
                    .getInsightSearchMOBuilder(aiFreddyAddonMO);
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, constructInsightTenantFilter(tenants,
                    aiFreddyAddonMO.getAiInsightParamDTO()));
            return getAIInsightResponseByLanguageCode(searchMOBuilder, languageCodes);
        }
    }

    private AIInsightParamDTO getUpdatedParamDTO(AIInsightParamDTO paramDTO, Map<String, String> queryHashParameters) {
        var validQueryHash = aiInsightHelper.extractQueryHash(queryHashParameters);
        paramDTO.setQueryHash(validQueryHash);
        return paramDTO;
    }

    private AIResponsePaginationDTO<AIInsightResponseDTO> getAIInsightsBasedOnAddons(
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

    private AIResponsePaginationDTO<AIInsightResponseDTO> executeSelfServiceAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        // Need to show only freshbot insights
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.SELF_SERVICE_ADDON_HANDLER)
                .getInsightSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                constructInsightTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiInsightParamDTO()));
        return getAIInsightResponseByLanguageCode(searchMOBuilder, languageCodes);
    }

    private AIResponsePaginationDTO<AIInsightResponseDTO> executeCopilotAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        return new AIResponsePaginationDTO<>(new ArrayList<>(), false);
    }

    private AIResponsePaginationDTO<AIInsightResponseDTO> executeCopilotSelfServiceAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.COPILOT_SELF_SERVICE_ADDON_HANDLER)
                .getInsightSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                constructInsightTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiInsightParamDTO()));
        return getAIInsightResponseByLanguageCode(searchMOBuilder, languageCodes);
    }

    private AIResponsePaginationDTO<AIInsightResponseDTO> executeInsightAddonHandler(
            AIFreddyAddonMO aiFreddyAddonMO, List<String> languageCodes) {
        var searchMOBuilder = aiFreddyAddonMO.getBaseESSearchBuilder();
        searchMOBuilder = addonHandlerMap.get(AIHandlerConstant.INSIGHTS_ADDON_HANDLER)
                .getInsightSearchMOBuilder(aiFreddyAddonMO);
        searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY,
                constructInsightTenantFilter(aiFreddyAddonMO.getTenants(),
                aiFreddyAddonMO.getAiInsightParamDTO()));
        return getAIInsightResponseByLanguageCode(searchMOBuilder, languageCodes);
    }

    private AIResponsePaginationDTO<AIInsightResponseDTO> getAIInsightResponseByLanguageCode(
            ESCriteriaBuilder.Builder searchMOBuilder, List<String> languageCodes) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        var getAllAIInsightResponse = new ArrayList<AIInsightResponseDTO>();
        var isNextPage = false;
        for (String language : languageCodes) {
            boolQuery.should(QueryBuilders.termQuery(LANGUAGE_CODE, language));
            boolQuery.should(QueryBuilders.nestedQuery(TRANSLATED_FIELDS,
                    QueryBuilders.termQuery(TRANSLATED_FIELDS + "." + LANGUAGE_CODE, language),
                    ScoreMode.Avg));
            searchMOBuilder.customQuery(ESConstant.MUST_CUSTOM_QUERY, boolQuery);
            var searchMO = searchMOBuilder.buildSearch();
            ESResponseMO<AIInsightEntity> responseMO = null;
            responseMO = queryHelper.search(searchMO, AIInsightEntity.class);
            isNextPage = responseMO != null && responseMO.isNextPage();
            log.info("AIInsight docs: {}, found for given parameter.", responseMO != null ? responseMO.getCount() : 0);
            var insights = responseMO != null
                    ? responseMO.getRecords() : new ArrayList<AIInsightEntity>();
            if (!insights.isEmpty()) {
                insights.forEach(insight -> insight.setPrompts(getPrompts(insight.getPromptIds())));
                getAllAIInsightResponse = (ArrayList<AIInsightResponseDTO>)
                        AIInsightConverter.getAllInsightResponseByLanguageCode(insights, language);
                break;
            }
        }
        return new AIResponsePaginationDTO<>(getAllAIInsightResponse, isNextPage);
    }

    public AIBulkResponseDTO createBulkInsight(List<AIInsightCreateDTO> bulkCreateDTO) {
        List<ESIndexMO<AIInsightEntity>> indexMOList = new ArrayList<>();
        Set<String> duplicateIds = new HashSet<>();
        List<String> createdIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        bulkCreateDTO.forEach(createDTO -> {
            aiCommonValidator.validateCreateOrUpdateInsight(createDTO.getTimeToLive(),
                    createDTO.getStatus(),
                    createDTO.getTranslatedFields(),
                    createDTO.getLanguageCode());
            var insightEntity = AIInsightConverter.convertToAIInsightEntity(getContextVO(), createDTO);
            if (isPromptExist(insightEntity)) {
                indexMOList.add(getEsIndexMO(insightEntity));
            } else {
                failedIds.add("PromptIds: " + insightEntity.getPromptIds());
            }
        });
        queryHelper.bulkIndex(indexMOList);
        return new AIBulkResponseDTO(bulkCreateDTO.size(), createdIds, failedIds, duplicateIds);
    }

    private boolean isPromptExist(AIInsightEntity insightEntity) {
        return insightEntity.getPromptIds().stream()
                .allMatch(promptId -> isESPromptExist(promptId, insightEntity.getTenant()));
    }

    public boolean isESPromptExist(String id, TenantEnum tenant) {
        var responseMO = promptService.getPromptById(id);
        return responseMO != null && responseMO.getTenant() == tenant;
    }

    private ESIndexMO<AIInsightEntity> getEsIndexMO(AIInsightEntity insightEntity) {
        var indexKey = insightEntity.getId();
        insightEntity.setId(null);
        return new ESCriteriaBuilder.IndexBuilder<AIInsightEntity>(ESIndexNameEnum.insight)
                .indexKey(indexKey)
                .routingKey(insightEntity.getTenant().name())
                .source(insightEntity)
                .waitUntil(true)
                .build();
    }

    private BoolQueryBuilder constructInsightTenantFilter(List<TenantEnum> tenants,
                                                         AIInsightParamDTO aiInsightParamDTO) {
        if (getContextVO().getUserId() == null && getContextVO().getAccountId() == null) {
            return null;
        }
        if (getContextVO().getBundle() != null) {
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            var bundle = getContextVO().getAiBundleEntity();
            if (bundle.getTenantList() != null && bundle.getTenantFilters() != null) {
                return aiInsightHelper.constructUsingBundleFilters(getContextVO(), shouldQuery, bundle, tenants,
                        aiInsightParamDTO);
            } else {
                return constructWhenNoBundleFilters(shouldQuery, tenants);
            }
        }
        return null;
    }

    // will be removed once the filters are added till production
    private BoolQueryBuilder constructWhenNoBundleFilters(BoolQueryBuilder shouldQuery, List<TenantEnum> tenants) {
        for (TenantEnum tenant : tenants) {
            List<String> filters = AIInsightConstant.TENANT_FILTERS.get(tenant);
            BoolQueryBuilder tenantQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery(AIInsightConstant.TENANT, tenant));
            if (filters != null && !filters.isEmpty()) {
                for (String filter : filters) {
                    switch (filter) {
                    case ORG_ID:
                        tenantQuery.must(QueryBuilders.termQuery(ORG_ID, getContextVO().getOrgId()));
                        break;
                    case ACCOUNT_ID:
                        tenantQuery.must(QueryBuilders.termQuery(ACCOUNT_ID,
                                getContextVO().getAccountId()));
                        break;
                    case BUNDLE_ID:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.BUNDLE_ID,
                                getContextVO().getBundleId()));
                        break;
                    case USER_ID:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.USER_ID,
                                getContextVO().getUserId()));
                        break;
                    case DOMAIN:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.DOMAIN, getContextVO().getDomain()));
                        break;
                    case GROUP_ID:
                        if (getContextVO().getGroupId() != null) {
                            tenantQuery.must(QueryBuilders.termsQuery(AIInsightConstant.GROUP_ID,
                                    getContextVO().getGroupId()));
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            shouldQuery.should(tenantQuery);
            log.info("tenant filter should query {}", shouldQuery);
        }
        return shouldQuery;
    }

    public void updateInsightsForDeletedPrompts(Set<String> promptIds) {
        List<AIInsightEntity> insights = getInsightsByPromptId(promptIds);
        List<AIInsightEntity> deleteIds = new ArrayList<>();
        List<AIInsightEntity> updateIds = new ArrayList<>();
        log.info("Updating insight records: {}, for deleted prompts", insights.size());
        insights.forEach(insight -> {
            insight.getPromptIds().removeAll(promptIds);
            if (insight.getPromptIds().isEmpty()) {
                deleteIds.add(insight);
            } else {
                updateIds.add(insight);
            }
        });
        bulkUpdateInsights(updateIds);
        bulkDeleteInsights(deleteIds);
    }

    public List<AIInsightEntity> getInsightsByPromptId(Set<String> promptIds) {
        var tenants = getAllTenantEnumList();
        var page = 1;
        var searchMO = new ESCriteriaBuilder.Builder(ESIndexNameEnum.insight)
                .tenants(tenants)
                .in(AIInsightConstant.PROMPT_IDS, new ArrayList<>(promptIds))
                .page(page)
                .limit(50)
                .buildSearch();
        var responseMO = queryHelper.search(searchMO, AIInsightEntity.class);
        List<AIInsightEntity> insightEntityList = new ArrayList<>(responseMO.getRecords());

        while (responseMO.isNextPage()) {
            searchMO.getEsQueryMO().setPage(++page);
            responseMO = queryHelper.search(searchMO, AIInsightEntity.class);
            insightEntityList.addAll(responseMO.getRecords());
        }
        log.info("AIInsight records: {}, found for given promptIds: {}", insightEntityList.size(),
                String.join(", ", promptIds));
        return insightEntityList;
    }

    private void bulkUpdateInsights(List<AIInsightEntity> updateInsights) {
        List<ESIndexMO<AIInsightEntity>> indexMOList = new ArrayList<>();
        updateInsights.forEach(updateInsight -> indexMOList.add(getEsIndexMO(updateInsight)));
        log.info("AIInsight bulk record will be updated for count: {}", indexMOList.size());
        queryHelper.bulkIndex(indexMOList);
    }

    public void bulkDeleteInsights(List<AIInsightEntity> deleteInsights) {
        List<ESBaseMO> esBaseMOList = new ArrayList<>();
        deleteInsights.forEach(deleteInsight -> esBaseMOList.add(
                getDeleteIndexMO(deleteInsight.getId(), deleteInsight.getTenant().name())));
        log.info("AIInsight bulk record will be deleted for count: {}", esBaseMOList.size());
        queryHelper.bulkDeleteById(esBaseMOList);
    }

    public void deleteInsight(String id) {
        var tenant = getContextVO().getTenant().name();
        if (isSuperAdmin()) {
            tenant = getInsightFromESById(id).getTenant().name();
        }
        boolean isDeleted = queryHelper.delete(getDeleteIndexMO(id, tenant));
        log.info("AIInsight record for id: {}, got deleted: {}", id, isDeleted);
        if (!isDeleted) {
            AICommonValidateException.notFoundException(ExceptionConstant.NOT_VALID_INSIGHT_ID);
        }
    }

    public AIResponseDTO deleteInsightTranslatedField(String id, String languageCode) {
        var insightEntity = getInsightFromESById(id);

        // don't allow to delete translated field in the inactive insight
        if (insightEntity.getStatus() != StatusEnum.ACTIVE) {
            AICommonValidateException.notAcceptableException(ExceptionConstant.NOT_ACTIVE_INSIGHT);
        }

        if (insightEntity.getTranslatedFields() == null) {
            insightEntity.setTranslatedFields(new ArrayList<>());
        }

        List<AIInsightTranslationDTO> translatedFieldsAfterDelete = insightEntity.getTranslatedFields().stream()
                .filter(field -> !languageCode.equals(field.getLanguageCode()))
                .collect(Collectors.toList());
        AIInsightUpdateDTO aiInsightUpdateDTO = new AIInsightUpdateDTO();
        aiInsightUpdateDTO.setTranslatedFields(translatedFieldsAfterDelete);
        AIInsightConverter.modifyToAIInsightEntity(getContextVO(), insightEntity, aiInsightUpdateDTO);
        log.info("Insight entity delete initiated for translated fields, id: {} with language code {}", id,
                languageCode);
        String response = getESIndexResponse(insightEntity);
        return new AIResponseDTO(response);
    }

    private ESIndexMO<AIInsightEntity> getDeleteIndexMO(String id, String tenant) {
        return new ESCriteriaBuilder.IndexBuilder<AIInsightEntity>(ESIndexNameEnum.insight)
                .indexKey(id)
                .routingKey(tenant)
                .build();
    }

    public void deleteInsightsByTenant(TenantEnum requestTenant,
                                       List<String> promptIds) {
        var searchMO = new ESCriteriaBuilder.Builder(ESIndexNameEnum.insight)
                .tenants(List.of(requestTenant))
                .in(AIInsightConstant.PROMPT_IDS, promptIds)
                .buildSearch();
        queryHelper.bulkDeleteByQuery(searchMO);
        log.info("AIInsight record for tenant: {}, got deleted.", requestTenant);
    }

    @Override
    public void promoteAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException {
        List<AIPromoteMO> promoteMOList = getAiPromoteMOList(aiPromoteDTOs);
        log.info("Starting insight promotion async.");
        var promotionHandlerStrategy = promotionHandler.get(AIHandlerConstant.AI_INSIGHT_PROMOTE_STRATEGY);
        promotionHandlerStrategy.executeStrategy(promoteMOList);
    }

    @Override
    public void promoteUpdateAsync(List<AIPromoteDTO> aiPromoteDTOs) throws AIResponseStatusException {
        List<AIPromoteMO> promoteMOList = getAiPromoteMOList(aiPromoteDTOs);
        log.info("Starting update prompt promotion async.");
        var promotionHandlerStrategy = promotionHandler.get(AIHandlerConstant.AI_INSIGHT_UPDATE_PROMOTE_STRATEGY);
        promotionHandlerStrategy.executeStrategy(promoteMOList);
    }

    @Override
    protected List<Object> promoteAttributesToEntities(List<AIPromoteDTO.Attribute> attributes) {
        return attributes.stream()
                .map(AIPromoteDTO.Attribute::getId)
                .map(this::getInsightFromESById)
                .collect(Collectors.toList());
    }

    private void sendInsightToCentral(AIInsightEntity insightEntity, String response) {
        var payload = AIInsightConverter.prepareInsightCentralPayload(response, insightEntity);
        CentralRequestDTO<AIInsightCentralPayload> centralRequest =
                new CentralRequestDTO<>(CentralConstant.insight_meta_info, payload);
        centralRequest.setPayloadVersion(CentralConstant.insight_payload_version);
        centralProducerService.sendEventsToCentral(centralRequest);
    }

    public AIResponseDTO updateInsightStatus(String id, StatusEnum requestedStatus) {
        var insightEntity = getInsightFromESById(id);
        AIInsightConverter.modifyStatusToAIInsightEntity(insightEntity, requestedStatus);
        log.info("Insight entity status update initiated, id: {}", id);
        String response = getESIndexResponse(insightEntity);
        return new AIResponseDTO(response);
    }

    private void validateUpdateInsightPrompts(AIInsightUpdateDTO updateDTO, AIInsightEntity insightEntity) {
        if ((updateDTO.getPromptIds() != null && !updateDTO.getPromptIds().isEmpty())
                || updateDTO.getTenant() != null) {
            var isPromptExist = isPromptExist(insightEntity);
            if (!isPromptExist) {
                AICommonValidateException.notAcceptableException(ExceptionConstant.NOT_VALID_INSIGHT_PROMPTS);
            }
        }
    }

    public AIResponseDTO dismissInsight(String insightId) {
        log.info("Insight dismissal Initiated");

        log.info("validating the insightId: {}", insightId);
        getInsightFromESById(insightId); // for validating the insightId , insight id should be present in Insight Index

        log.info("checking whether the insight is already dismissed or not");
        var isInsightExist = aiDismissRepository.findByInsightId(insightId,
                                getContextVO().getUserId()); // check insight is already dismissed or not
        if (Objects.nonNull(isInsightExist)) {
            AICommonValidateException.notAcceptableException(ExceptionConstant.INSIGHT_DISMISSED);
        }

        AIInsightsDismissDTO insightDismissEntity = AIInsightConverter.prepareInsightDismissDTO(
                getContextVO(), insightId);
        String response = dismissInsights(insightDismissEntity);
        return new AIResponseDTO(response);
    }

    public AIInsightEntity undoInsight(String insightId) {
        var isInsightExist = aiDismissRepository.findByInsightId(insightId,getContextVO().getUserId());
        if (Objects.isNull(isInsightExist)) {
            AICommonValidateException.notFoundException(
                    String.format(AIInsightConstant.STRING_FORMATTER, ExceptionConstant.NO_RECORD_FOR_ID, insightId));
        }
        if (Objects.nonNull(isInsightExist)) {
            aiDismissRepository.deleteByInsightId(isInsightExist.getInsightId(),getContextVO().getUserId());
        }
        log.info("Dismiss Insight record for id: {}, got deleted", insightId);
        return getInsightFromESById(insightId);
    }

    private String dismissInsights(AIInsightsDismissDTO insightDimissEntity) {
        try {
            AIInsightsDismissDTO response = aiDismissRepository.save(insightDimissEntity);
            log.info("Insight record Dismiss status: , id: {}", response.getId());
            return response.getId();
        } catch (Exception e) {
            throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_MODEL + e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
