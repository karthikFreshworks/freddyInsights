package com.freshworks.freddy.insights.handler.es;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.modelobject.ESRuleMO;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.index.query.*;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.opensearch.index.query.QueryBuilders.*;

@Slf4j
public class AbstractESQueryHandler extends AbstractAIBaseHelper {
    protected static final String SORT_DESC = "desc";
    protected static final String SORT_ASC = "asc";
    protected static final String WORD_SPLIT_REGEX = "\\P{L}+";
    protected static final String ES_SCORE_FIELD = "_score";
    protected static final String SPLIT_BY_PIPE_REGEX = "\\|";
    protected static final String AUTO_COMPLETE = ".auto_complete";

    protected boolean isValidRule(ESRuleMO rule) {
        List<String> value = rule.getValue();
        if (!value.isEmpty() && value.get(0) != null) {
            for (String data : value) {
                if (data.length() == 0) {
                    return false;
                }
            }
            return true;
        }
        return rule.getQueryBuilder() != null;
    }

    protected BoolQueryBuilder applyDefaultScope(List<TenantEnum> tenants) {
        if (tenants != null && !tenants.isEmpty()) {
            List<String> tenantNames = tenants.stream().map(Enum::name).collect(Collectors.toList());
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.filter(new TermsQueryBuilder("tenant", new HashSet<>(tenantNames)));
            return boolQueryBuilder;
        } else {
            throw new AIResponseStatusException("Tenants cant be null or Empty while generating ES query",
                    HttpStatus.NOT_FOUND);
        }
    }

    protected QueryBuilder isQuery(ESRuleMO rule) {
        return new TermsQueryBuilder(rule.getOperand(), rule.getValue());
    }

    protected QueryBuilder inQuery(ESRuleMO rule) {
        HashSet<String> values = new HashSet<>(rule.getValue());
        return new TermsQueryBuilder(rule.getOperand(), values);
    }

    protected void addAutoCompleteQuery(ESRuleMO rule, boolean autoComplete, BoolQueryBuilder nestedBoolQuery,
                                        String value) {
        if (autoComplete) {
            MatchQueryBuilder matchQueryBuilderForAutoComplete =
                    QueryBuilders.matchQuery(rule.getOperand().concat(AUTO_COMPLETE), value).operator(Operator.AND);
            nestedBoolQuery.should(matchQueryBuilderForAutoComplete);
        }
    }

    protected QueryBuilder contains(ESRuleMO rule, boolean autoComplete, boolean isNested) {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        for (String value : rule.getValue()) {
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(rule.getOperand(), value);
            matchQueryBuilder.operator(Operator.AND);
            if (isNested) {
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery(rule.getNestedOperand(),
                        matchQueryBuilder, rule.getScoreMode());
                boolQuery.should(nestedQueryBuilder).minimumShouldMatch(1);
            } else {
                boolQuery.should(matchQueryBuilder).minimumShouldMatch(1);
            }
            addAutoCompleteQuery(rule, autoComplete, boolQuery, value);
        }
        return boolQuery;
    }

    protected QueryBuilder moreLike(ESRuleMO rule) {
        String[] fields = {rule.getOperand()};
        String[] values = rule.getValueArray();
        int minMatchRequired = 5;
        for (String value : values) {
            int wordLength = value.split(WORD_SPLIT_REGEX).length;
            if (minMatchRequired < wordLength) {
                minMatchRequired = wordLength;
            }
        }
        return moreLikeThisQuery(fields, values, null).minTermFreq(1).minimumShouldMatch(
                Integer.toString(minMatchRequired));
    }

    protected QueryBuilder spanNear(ESRuleMO rule) {
        List<String> values = rule.getValue();
        ArrayList<String> words = new ArrayList<>();
        for (String value : values) {
            words.addAll(Arrays.asList(value.split(WORD_SPLIT_REGEX)));
        }
        if (words.size() > 0) {
            SpanNearQueryBuilder queryBuilder = spanNearQuery(spanMultiTermQueryBuilder(fuzzyQuery(rule.getOperand(),
                    words.get(0)).fuzziness(Fuzziness.TWO).prefixLength(1)), 50).inOrder(false);
            for (int i = 1; i < words.size(); i++) {
                if (words.get(i).length() > 2) {
                    queryBuilder.addClause(spanMultiTermQueryBuilder(fuzzyQuery(rule
                            .getOperand(), words.get(i)).fuzziness(Fuzziness.TWO).prefixLength(1)));
                }
            }
            return queryBuilder;
        }
        return null;
    }
}
