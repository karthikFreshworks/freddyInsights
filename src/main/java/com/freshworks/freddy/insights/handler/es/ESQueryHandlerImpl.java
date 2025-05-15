package com.freshworks.freddy.insights.handler.es;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.modelobject.ESQueryMO;
import com.freshworks.freddy.insights.modelobject.ESRuleMO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.index.query.*;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.collapse.CollapseBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.freshworks.freddy.insights.constant.ESConstant.*;

@Slf4j
@Getter
@Component
public class ESQueryHandlerImpl extends AbstractESQueryHandler {
    public BoolQueryBuilder buildEsQuery(List<TenantEnum> tenants, ESQueryMO esQueryMO) {
        BoolQueryBuilder boolQueryBuilder = super.applyDefaultScope(tenants);

        if (esQueryMO.getRules() != null) {
            for (ESRuleMO rule : esQueryMO.getRules()) {
                if (!isValidRule(rule)) {
                    continue;
                }
                String operator = rule.getOperator();
                switch (operator) {
                case IS_OPERATOR:
                    boolQueryBuilder.filter(isQuery(rule));
                    break;
                case IN_OPERATOR:
                    boolQueryBuilder.filter(inQuery(rule));
                    break;
                case CONTAINS_OPERATOR:
                    boolQueryBuilder.should(
                            contains(rule, esQueryMO.isAutocomplete(), false)).minimumShouldMatch(1);
                    esQueryMO.setSearch(true);
                    break;
                case NESTED_CONTAINS_OPERATOR:
                    boolQueryBuilder.should(
                            contains(rule, esQueryMO.isAutocomplete(), true)).minimumShouldMatch(1);
                    esQueryMO.setSearch(true);
                    break;
                case NOT_IN:
                    boolQueryBuilder.mustNot(inQuery(rule));
                    break;
                case MORE_LIKE:
                    boolQueryBuilder.should(moreLike(rule)).minimumShouldMatch(1);
                    break;
                case SPAN_NEAR:
                    QueryBuilder spanQueryBuilder = spanNear(rule);
                    if (spanQueryBuilder != null) {
                        boolQueryBuilder.should(spanQueryBuilder).minimumShouldMatch(1);
                    }
                    break;
                case MUST_NOT:
                    boolQueryBuilder.mustNot(new ExistsQueryBuilder(rule.getValue().get(0)));
                    break;
                case MUST:
                    boolQueryBuilder.must(new ExistsQueryBuilder(rule.getValue().get(0)));
                    break;
                case MUST_CUSTOM_QUERY:
                    boolQueryBuilder.must(rule.getQueryBuilder());
                    break;
                case SHOULD:
                    boolQueryBuilder.should(new TermsQueryBuilder(rule.getOperand(), rule.getValue().get(0)));
                    break;
                case LESS_THAN:
                    boolQueryBuilder.must(new RangeQueryBuilder(rule.getOperand()).lt(rule.getValue().get(0)));
                    break;
                case GREATER_THAN:
                    boolQueryBuilder.must(new RangeQueryBuilder(rule.getOperand()).gt(rule.getValue().get(0)));
                    break;
                default:
                    throw new AIResponseStatusException("Filter Operator not supported");
                }
            }
        }
        return boolQueryBuilder;
    }

    public void applySort(SearchSourceBuilder searchSourceBuilder, ESQueryMO esQueryMO) {
        if (esQueryMO.isSearch()) {
            if (esQueryMO.getSort() != null && !esQueryMO.getSort().isEmpty()) {
                applyCustomSort(searchSourceBuilder, esQueryMO);
            }
            sort(searchSourceBuilder, ES_SCORE_FIELD, SORT_DESC);
        } else {
            if (esQueryMO.getSort() != null && !esQueryMO.getSort().isEmpty()) {
                applyCustomSort(searchSourceBuilder, esQueryMO);
            }
        }
    }

    public void applyCollapse(SearchSourceBuilder searchSourceBuilder, String aggregate) {
        if (aggregate != null) {
            CollapseBuilder collapseBuilder = new CollapseBuilder(aggregate);
            searchSourceBuilder.collapse(collapseBuilder);
        }
    }

    public void applyCustomSort(SearchSourceBuilder searchSourceBuilder, ESQueryMO esQueryMO) {
        for (String sort : esQueryMO.getSort()) {
            String[] sortParam = sort.split(SPLIT_BY_PIPE_REGEX);
            if (sortParam.length >= 2) {
                sort(searchSourceBuilder, sortParam[0], sortParam[1]);
            } else {
                sort(searchSourceBuilder, sortParam[0], SORT_DESC);
            }
        }
    }

    public void filterFieldsFromSource(SearchSourceBuilder searchSourceBuilder, String[] include, String[] exclude) {
        searchSourceBuilder.fetchSource(include, exclude);
    }

    private void sort(SearchSourceBuilder searchSourceBuilder, String orderBy, String sortOrder) {
        if (sortOrder == null || sortOrder.equals(SORT_DESC)) {
            searchSourceBuilder.sort(orderBy, SortOrder.DESC);
        } else {
            searchSourceBuilder.sort(orderBy, SortOrder.ASC);
        }
    }

    public void paginate(SearchSourceBuilder searchSourceBuilder, int page, int limit) {
        int from = (page - 1) * limit;
        int size = Math.min(limit, 100);
        searchSourceBuilder.from(from).size(size);
    }
}
