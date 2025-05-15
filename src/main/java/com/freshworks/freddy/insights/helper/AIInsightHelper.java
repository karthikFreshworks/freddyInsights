package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.builder.ESCriteriaBuilder;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.constant.enums.insights.OperatorEnum;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightParamDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightQueryHashDTO;
import com.freshworks.freddy.insights.entity.AIBundleEntity;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.valueobject.ContextVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.*;

@Slf4j
@Component
public class AIInsightHelper {
    public ESCriteriaBuilder.Builder buildBaseSearchMo(ContextVO contextVO, PaginationDTO paginationDTO,
                                                       AIInsightParamDTO paramDTO, boolean isSuperAdmin) {
        var esCriteriaBuilder = new ESCriteriaBuilder.Builder(ESIndexNameEnum.insight)
                .page(paginationDTO.getPage())
                .limit(paginationDTO.getSize());
        // Should ignore the queryParams except pagination and status when there is query hash data present
        if (paramDTO.getQueryHash().isEmpty()) {
            esCriteriaBuilder.in(AIInsightConstant.ACCOUNT_ID, paramDTO.getAccountId())
                    .contains(AIInsightConstant.GROUP, paramDTO.getGroup())
                    .in(AIInsightConstant.GROUP_ID, paramDTO.getGroupId())
                    .in(AIInsightConstant.SERVICE_ID, paramDTO.getServiceId())
                    .in(AIInsightConstant.USECASE_ID, paramDTO.getUsecaseId())
                    .in(AIInsightConstant.ORG_ID, paramDTO.getOrgId())
                    .in(AIInsightConstant.BUNDLE_ID, paramDTO.getBundleId())
                    .in(AIInsightConstant.USER_ID, paramDTO.getUserId())
                    .in(AIInsightConstant.SKU, paramDTO.getSku())
                    .in(AIInsightConstant.PLANS, paramDTO.getPlans())
                    .in(AIInsightConstant.ADDONS, paramDTO.getAddons())
                    .in(AIInsightConstant.TAGS, paramDTO.getTags())
                    .in(AIInsightConstant.PROMPT_IDS, paramDTO.getPromptIds())
                    .in(AIInsightConstant.TIME_ZONES, paramDTO.getTimeZones())
                    .in(AIInsightConstant.UI_TAG, paramDTO.getUiTag())
                    .in(AIInsightConstant.BUSINESS_KPI, paramDTO.getBusinessKpi())
                    .in(AIInsightConstant.DEPARTMENT, paramDTO.getDepartment())
                    .in(AIInsightConstant.FREQUENCY, paramDTO.getFrequency() != null
                            ? paramDTO.getFrequency().name() : null)
                    .in(AIInsightConstant.METRIC, paramDTO.getMetric())
                    .in(AIInsightConstant.TYPE, paramDTO.getType())
                    .in(AIInsightConstant.SCENARIO_TYPE, paramDTO.getScenarioType())
                    .gt(AIInsightConstant.START_TIME, paramDTO.getStartTime())
                    .lt(AIInsightConstant.END_TIME, paramDTO.getEndTime());
        }
        esCriteriaBuilder.in(AIInsightConstant.STATUS, getStatus(isSuperAdmin, paramDTO.getStatus()))
                .autoComplete(true)
                .sort("importance_score|desc")
                .sort("created_at|desc")
                .collapse(contextVO.getCollapse());
        return esCriteriaBuilder;
    }

    private List<String> getStatus(Boolean isSuperAdmin, String status) {
        if (isSuperAdmin) {
            return status != null ? List.of(status) : Arrays.stream(StatusEnum.values())
                    .map(StatusEnum::name)
                    .collect(Collectors.toList());
        }
        return status != null && !StatusEnum.ARCHIVED.name().equals(status) ? List.of(status) :
                Arrays.asList(StatusEnum.ACTIVE.name(), StatusEnum.INACTIVE.name());
    }

    public BoolQueryBuilder constructUsingBundleFilters(ContextVO contextVO, BoolQueryBuilder shouldQuery,
                                                        AIBundleEntity bundle, List<TenantEnum> tenants,
                                                        AIInsightParamDTO aiInsightParamDTO) {
        var queryHash = aiInsightParamDTO.getQueryHash();
        return getBoolQueryBuilder(shouldQuery, queryHash, contextVO, bundle, tenants);
    }

    private BoolQueryBuilder getBoolQueryBuilder(BoolQueryBuilder shouldQuery, List<AIInsightQueryHashDTO> queryHash,
                                                 ContextVO contextVO, AIBundleEntity bundle, List<TenantEnum> tenants) {
        var bundleTenants = bundle.getTenantList().stream()
                .distinct()
                .filter(tenants::contains)
                .toList();
        for (TenantEnum tenant : bundleTenants) {
            List<String> filters = bundle.getTenantFilters().get(tenant);
            BoolQueryBuilder tenantQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery(AIInsightConstant.TENANT, tenant));
            if (filters != null && !filters.isEmpty()) {
                for (String filter : filters) {
                    switch (filter) {
                    case ORG_ID:
                        tenantQuery.must(
                                QueryBuilders.termQuery(AIInsightConstant.ORG_ID, contextVO.getOrgId()));
                        break;
                    case ACCOUNT_ID:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.ACCOUNT_ID,
                                contextVO.getAccountId()));
                        break;
                    case BUNDLE_ID:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.BUNDLE_ID,
                                contextVO.getBundleId()));
                        break;
                    case USER_ID:
                        tenantQuery.must(QueryBuilders.termQuery(AIInsightConstant.USER_ID,
                                contextVO.getUserId()));
                        break;
                    case DOMAIN:
                        tenantQuery.must(
                                QueryBuilders.termQuery(AIInsightConstant.DOMAIN, contextVO.getDomain()));
                        break;
                    case GROUP_ID:
                        var accessibleGroupIds = contextVO.getGroupId();
                        // This will be changed when freshdesk make the changes
                        if (TenantEnum.freshdesk.equals(tenant) && accessibleGroupIds == null) {
                            break;
                        }
                        var queryHashGroupIds = getConditionObject(queryHash, GROUP_ID);
                        var queryHashListValues = queryHashGroupIds != null
                                ? getListValuesFromCommaSeparatedString(queryHashGroupIds.getValue()) : new String[0];
                        var validGroupIds = getValidGroupIds(queryHashListValues, accessibleGroupIds);
                        tenantQuery.must(QueryBuilders.termsQuery(AIInsightConstant.GROUP_ID, validGroupIds));
                        break;
                    default:
                        var conditionObject = getConditionObject(queryHash, filter);
                        if (conditionObject != null) {
                            switch (OperatorEnum.valueOf(conditionObject.getOperator())) {
                            case is_in:
                                // get the values as string list from comma separated
                                var values = getListValuesFromCommaSeparatedString(conditionObject.getValue());
                                tenantQuery.must(QueryBuilders.termsQuery(filter, values));
                                break;
                            case is:
                                tenantQuery.must(
                                        QueryBuilders.termQuery(filter, conditionObject.getValue()));
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            shouldQuery.should(tenantQuery);
        }
        log.info("tenant filter should query {}", shouldQuery);
        return shouldQuery;
    }

    private String[] getValidGroupIds(String[] queryHashGroupIds, String[] accessibleGroupIds) {
        if (accessibleGroupIds != null && queryHashGroupIds.length > 0) {
            Set<String> set1 = Arrays.stream(queryHashGroupIds)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            return CollectionUtils.select(
                    CollectionUtils.collect(Arrays.asList(accessibleGroupIds), String::trim),
                    set1::contains
            ).toArray(new String[0]);
        }
        return accessibleGroupIds != null ? accessibleGroupIds : new String[0];
    }

    private String[] getListValuesFromCommaSeparatedString(String value) {
        return value != null
                ? Arrays.stream(value.split(","))
                .map(String::trim)  // Remove leading/trailing spaces
                .toArray(String[]::new)
                : new String[0];
    }

    public AIInsightQueryHashDTO getConditionObject(List<AIInsightQueryHashDTO> conditions, String targetCondition) {
        return conditions.stream()
                .filter(condition -> targetCondition.equals(condition.getCondition()))
                .findFirst()
                .orElse(null);
    }

    public List<AIInsightQueryHashDTO> extractQueryHash(Map<String, String> queryHashParameters) {
        try {
            return getTheQueryHashData(queryHashParameters);
        } catch (Exception ex) {
            log.error("Error while parsing the query Hash given and {} cause {} and message {}", queryHashParameters,
                    ex.getCause(), ex.getMessage());
            throw new AIResponseStatusException(ExceptionConstant.ERROR_IN_PARSING_QUERY_HASH,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.BAD_REQUEST);
        }
    }

    // Extracting the query hash from the query
    private List<AIInsightQueryHashDTO> getTheQueryHashData(Map<String, String> queryHashParameters) {
        List<AIInsightQueryHashDTO> queryHash = new ArrayList<>();
        if (queryHashParameters != null) {
            queryHashParameters.forEach((key, value) -> {
                if (key.startsWith("query_hash[")) {
                    String[] parts = key.split("\\[|\\]");
                    int index = Integer.parseInt(parts[1]);
                    String attribute = parts[3];
                    // Ensure the list is large enough
                    while (queryHash.size() <= index) {
                        queryHash.add(new AIInsightQueryHashDTO());
                    }
                    // Set the appropriate index in the DTO
                    AIInsightQueryHashDTO condition = queryHash.get(index);
                    // set the values to the query Hash

                    switch (attribute) {
                    case "condition":
                        condition.setCondition(value);
                        break;
                    case "operator":
                        condition.setOperator(value);
                        break;
                    case "value":
                        condition.setValue(value);
                        break;
                    default:
                        break;
                    }
                }
            });
        }
        return getTheUniqueConditionObjects(queryHash);
    }

    private List<AIInsightQueryHashDTO> getTheUniqueConditionObjects(List<AIInsightQueryHashDTO> queryHashDTOS) {
        // ignore duplicates and add only if it is in allowed operator
        Set<String> seenConditions = new HashSet<>();
        Map<String, AIInsightQueryHashDTO> uniqueConditionsMap = new HashMap<>();
        for (AIInsightQueryHashDTO dto : queryHashDTOS) {
            String condition = dto.getCondition();
            if (!seenConditions.contains(condition) && OperatorEnum.isValidOperator(dto.getOperator())) {
                uniqueConditionsMap.put(condition, dto);
                seenConditions.add(condition);
            }
        }
        var validQueryHash = new ArrayList<>(uniqueConditionsMap.values());
        return validQueryHash.stream()
                .filter(query -> query.getCondition() != null && query.getOperator() != null
                        && query.getValue() != null)
                .toList();
    }
}
