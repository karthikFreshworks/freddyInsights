package com.freshworks.freddy.insights.builder;

import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.modelobject.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.opensearch.common.Strings;
import org.opensearch.index.query.BoolQueryBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Getter
public class ESCriteriaBuilder {
    private final ESQueryMO esQueryMO;

    private ESCriteriaBuilder(ESQueryMO esQueryMO) {
        this.esQueryMO = esQueryMO;
    }

    public static class Builder extends AbstractESCriteriaBuilder {
        public Builder(ESIndexNameEnum indexNameArg) {
            indexName = indexNameArg;
        }

        public Builder page(int page) {
            esQueryMO.setPage(page);
            return this;
        }

        public Builder rule(ESRuleMO rule) {
            addRule(rule);
            return this;
        }

        public Builder limit(int limit) {
            esQueryMO.setLimit(limit);
            return this;
        }

        public Builder sort(String value) {
            if (!Strings.isNullOrEmpty(value)) {
                addSortRule(value);
            }
            return this;
        }

        public Builder autoComplete(boolean isAutoComplete) {
            esQueryMO.setAutocomplete(isAutoComplete);
            return this;
        }

        public Builder lt(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyLTOperatorRule(key, value);
            }
            return this;
        }

        public Builder gt(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyGTOperatorRule(key, value);
            }
            return this;
        }

        public Builder in(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyInOperatorRule(key, value);
            }
            return this;
        }

        public Builder in(String key, List<String> value) {
            if (!Strings.isNullOrEmpty(key) && !CollectionUtils.isEmpty(value)) {
                applyInOperatorRule(key, value);
            }
            return this;
        }

        public Builder must(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyMustOperatorRule(key, value);
            }
            return this;
        }

        public Builder customQuery(String operator, BoolQueryBuilder queryBuilder) {
            if (queryBuilder != null) {
                applyCustomQueryOperatorRule(operator, queryBuilder);
            }
            return this;
        }

        public Builder notIn(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyNotInOperatorRule(key, value);
            }
            return this;
        }

        public Builder notIn(String key, List<String> value) {
            if (!Strings.isNullOrEmpty(key) && !CollectionUtils.isEmpty(value)) {
                applyNotInOperatorRule(key, value);
            }
            return this;
        }

        public Builder like(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyLikeOperatorRule(key, value);
            }
            return this;
        }

        public Builder should(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyShouldOperatorRule(key, value);
            }
            return this;
        }

        public Builder contains(String key, String value) {
            if (!Strings.isNullOrEmpty(key) && value != null) {
                applyContainsOperatorRule(key, value);
            }
            return this;
        }

        public Builder tenants(List<TenantEnum> tenantsArg) {
            if (tenantsArg != null && !tenantsArg.isEmpty()) {
                tenants = tenantsArg;
            }
            return this;
        }

        public Builder include(String[] includeArg) {
            if (includeArg != null && includeArg.length > 0) {
                include = includeArg;
            }
            return this;
        }

        public Builder exclude(String[] excludeArg) {
            if (excludeArg != null && excludeArg.length > 0) {
                exclude = excludeArg;
            }
            return this;
        }

        public Builder collapse(String aggregate) {
            esQueryMO.setCollapse(aggregate);
            return this;
        }

        public ESCriteriaBuilder build() {
            return new ESCriteriaBuilder(esQueryMO);
        }

        public ESSearchMO buildSearch() {
            ESSearchMO esSearchMO = new ESSearchMO();
            esSearchMO.setExclude(exclude);
            esSearchMO.setInclude(include);
            esSearchMO.setTenants(tenants);
            esSearchMO.setIndexName(indexName);
            esSearchMO.setEsQueryMO(super.esQueryMO);
            return esSearchMO;
        }
    }

    public static class IndexBuilder<T> {
        private final ESIndexNameEnum indexName;
        private T source;
        private String indexKey;
        private String routingKey;
        private boolean waitUntil;

        public IndexBuilder(@NotNull ESIndexNameEnum indexNameArg) {
            indexName = indexNameArg;
        }

        public final IndexBuilder<T> source(@NotNull T sourceArg) {
            if (sourceArg != null) {
                source = sourceArg;
            }
            return this;
        }

        public IndexBuilder<T> indexKey(@NotBlank String indexKeyArg) {
            if (!Strings.isNullOrEmpty(indexKeyArg)) {
                indexKey = indexKeyArg;
            }
            return this;
        }

        public IndexBuilder<T> routingKey(@NotBlank String routingKeyArg) {
            if (!Strings.isNullOrEmpty(routingKeyArg)) {
                routingKey = routingKeyArg;
            }
            return this;
        }

        public IndexBuilder<T> waitUntil(boolean waitUntilArg) {
            waitUntil = waitUntilArg;
            return this;
        }

        public ESIndexMO<T> build() {
            ESIndexMO<T> esIndexMO = new ESIndexMO<>();
            esIndexMO.setSource(source);
            esIndexMO.setIndexKey(indexKey);
            esIndexMO.setRoutingKey(routingKey);
            esIndexMO.setIndexName(indexName);
            esIndexMO.setWaitUntil(waitUntil);
            return esIndexMO;
        }
    }

    public static class DeleteBuilder {
        private final ESIndexNameEnum indexName;
        private String indexKey;
        private String routingKey;

        public DeleteBuilder(@NotNull ESIndexNameEnum indexNameArg) {
            indexName = indexNameArg;
        }

        public DeleteBuilder indexKey(@NotBlank String indexKeyArg) {
            if (!Strings.isNullOrEmpty(indexKeyArg)) {
                indexKey = indexKeyArg;
            }
            return this;
        }

        public DeleteBuilder routingKey(@NotBlank String routingKeyArg) {
            if (!Strings.isNullOrEmpty(routingKeyArg)) {
                routingKey = routingKeyArg;
            }
            return this;
        }

        public ESBaseMO build() {
            ESBaseMO esBaseMO = new ESBaseMO();
            esBaseMO.setIndexKey(indexKey);
            esBaseMO.setRoutingKey(routingKey);
            esBaseMO.setIndexName(indexName);
            return esBaseMO;
        }
    }
}
