package com.freshworks.freddy.insights.builder;

import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.modelobject.ESQueryMO;
import com.freshworks.freddy.insights.modelobject.ESRuleMO;
import org.opensearch.index.query.BoolQueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.freshworks.freddy.insights.constant.ESConstant.*;

abstract class AbstractESCriteriaBuilder {
    protected ESQueryMO esQueryMO;
    protected ESIndexNameEnum indexName;
    protected String[] include = new String[0];
    protected String[] exclude = new String[0];
    protected List<TenantEnum> tenants = new ArrayList<>();

    AbstractESCriteriaBuilder() {
        this.esQueryMO = new ESQueryMO();
        this.esQueryMO.setSort(new ArrayList<>());
        this.esQueryMO.setRules(new ArrayList<>());
    }

    abstract ESCriteriaBuilder.Builder page(int size);

    abstract ESCriteriaBuilder.Builder rule(ESRuleMO rule);

    abstract ESCriteriaBuilder.Builder limit(int limit);

    abstract ESCriteriaBuilder.Builder sort(String sortText);

    abstract ESCriteriaBuilder.Builder lt(String key, String value);

    abstract ESCriteriaBuilder.Builder gt(String key, String value);

    abstract ESCriteriaBuilder.Builder in(String key, String value);

    abstract ESCriteriaBuilder.Builder like(String key, String value);

    abstract ESCriteriaBuilder.Builder must(String key, String value);

    abstract ESCriteriaBuilder.Builder customQuery(String operand, BoolQueryBuilder queryBuilder);

    abstract ESCriteriaBuilder.Builder notIn(String key, String value);

    abstract ESCriteriaBuilder.Builder should(String key, String value);

    abstract ESCriteriaBuilder.Builder contains(String key, String value);

    abstract ESCriteriaBuilder.Builder autoComplete(boolean isAutoComplete);

    abstract ESCriteriaBuilder.Builder tenants(List<TenantEnum> tenantsArg);

    abstract ESCriteriaBuilder.Builder include(String[] includeArg);

    abstract ESCriteriaBuilder.Builder exclude(String[] excludeArg);

    protected void applyInOperatorRule(String key, String value) {
        addRule(makeRule(key, value, IN_OPERATOR));
    }

    protected void applyInOperatorRule(String key, List<String> value) {
        addRule(makeRule(key, value, IN_OPERATOR));
    }

    protected void applyLTOperatorRule(String key, String value) {
        addRule(makeRule(key, value, LESS_THAN));
    }

    protected void applyGTOperatorRule(String key, String value) {
        addRule(makeRule(key, value, GREATER_THAN));
    }

    protected void applyNotInOperatorRule(String key, String value) {
        addRule(makeRule(key, value, NOT_IN));
    }

    protected void applyNotInOperatorRule(String key, List<String> value) {
        addRule(makeRule(key, value, NOT_IN));
    }

    protected void applyContainsOperatorRule(String key, String value) {
        addRule(makeRule(key, value, CONTAINS_OPERATOR));
    }

    protected void applyLikeOperatorRule(String key, String value) {
        addRule(makeRule(key, value, MORE_LIKE));
    }

    protected void applyShouldOperatorRule(String key, String value) {
        addRule(makeRule(key, value, SHOULD));
    }

    protected void applyMustOperatorRule(String key, String value) {
        addRule(makeRule(key, value, MUST));
    }

    protected void applyCustomQueryOperatorRule(String operator, BoolQueryBuilder queryBuilder) {
        addRule(makeRule(null, null, queryBuilder, operator));
    }

    private ESRuleMO makeRule(String key, String value, String operator) {
        return makeRule(key, value, null, operator);
    }

    private ESRuleMO makeRule(String key, String value, BoolQueryBuilder queryBuilder, String operator) {
        ESRuleMO rule = new ESRuleMO();
        rule.setOperator(operator);
        rule.setOperand(key);
        rule.setValue(Collections.singletonList(value));
        rule.setQueryBuilder(queryBuilder);
        return rule;
    }

    private ESRuleMO makeRule(String key, List<String> value, String operator) {
        ESRuleMO rule = new ESRuleMO();
        rule.setOperator(operator);
        rule.setOperand(key);
        rule.setValue(value);
        return rule;
    }

    protected void addRule(ESRuleMO rule) {
        ArrayList<ESRuleMO> rules = esQueryMO.getRules();
        rules.add(rule);
        esQueryMO.setRules(rules);
    }

    protected void addSortRule(String sortRule) {
        ArrayList<String> sortRules = esQueryMO.getSort();
        sortRules.add(sortRule);
        esQueryMO.setSort(sortRules);
    }
}
