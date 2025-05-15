package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.handler.es.ESQueryHandlerImpl;
import com.freshworks.freddy.insights.modelobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.TRANSLATED_FIELDS;
import static com.freshworks.freddy.insights.constant.ESConstant.CONTAINS_OPERATOR;
import static com.freshworks.freddy.insights.constant.ESConstant.NESTED_CONTAINS_OPERATOR;

@Slf4j
@Component
@RequiredArgsConstructor
public class ESQueryHelper extends AbstractAIBaseHelper {
    private final OpenSearchRestHighLevelClientHelper esClient;
    private final ESQueryHandlerImpl esQueryHandler;
    @Value("${freddy.insights.es.api.timeout}")
    private int esTimeout;
    @Value("${freddy.insights.es.index.prefix}")
    private String indexPrefix;
    @Value("${freddy.insights.es.delete.max.size}")
    private Integer esDeleteMaxSize;

    public <T> IndexResponse index(ESIndexMO<T> esIndexMO) {
        var indexName = indexPrefix + esIndexMO.getIndexName().name();
        try {
            String jsonStr = objectMapper.writeValueAsString(esIndexMO.getSource());
            DocWriteRequest.OpType op = DocWriteRequest.OpType.INDEX;
            IndexRequest request = new IndexRequest(indexName)
                    .source(jsonStr, XContentType.JSON)
                    .opType(op)
                    .routing(esIndexMO.getRoutingKey());
            if (StringUtils.isNotEmpty(esIndexMO.getIndexKey())) {
                request.id(esIndexMO.getIndexKey());
            }
            request.timeout(TimeValue.timeValueSeconds(esTimeout));
            if (esIndexMO.isWaitUntil()) {
                request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            }
            log.info("Indexing ES document for index={}, id={}, routing={}, timeout={} and payload={}",
                    esIndexMO.getIndexName().name(), esIndexMO.getIndexKey(), esIndexMO.getRoutingKey(), esTimeout,
                    jsonStr);
            IndexResponse response = esClient.sendIndexRequest(request, RequestOptions.DEFAULT);
            log.info("ES Indexing successful for index={}, id={}, routing={}, timeout={}",
                    esIndexMO.getIndexName().name(), esIndexMO.getIndexKey(), esIndexMO.getRoutingKey(), esTimeout);
            return response;
        } catch (Exception ex) {
            log.error("Error indexing in ES for index={}, id={}, routing={}, timeout={}  with an error={}",
                    esIndexMO.getIndexName().name(), esIndexMO.getIndexKey(), esIndexMO.getRoutingKey(),
                    esTimeout, ExceptionHelper.stackTrace(ex));
            throw new AIResponseStatusException(ExceptionConstant.ERROR_IN_INDEXING,
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean delete(ESBaseMO esDeleteMO) {
        var indexName = indexPrefix + esDeleteMO.getIndexName().name();
        try {
            DeleteRequest request = new DeleteRequest(indexName, esDeleteMO.getIndexKey())
                    .routing(esDeleteMO.getRoutingKey());
            request.timeout(TimeValue.timeValueSeconds(esTimeout));
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            DeleteResponse deleteResponse = esClient.sendDeleteRequest(request, RequestOptions.DEFAULT);
            if (deleteResponse != null) {
                if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
                    log.info("Deleted record for indexId={}, routing={}, indexName={} with Response version: {}",
                            esDeleteMO.getIndexKey(), esDeleteMO.getRoutingKey(), indexName,
                            deleteResponse.getVersion());
                    return true;
                } else {
                    log.info("Delete status, indexId={}, routing={}, indexName={} with Response type: {}",
                            esDeleteMO.getIndexKey(), esDeleteMO.getRoutingKey(), indexName,
                            deleteResponse.getResult());
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error deleting in ES for indexId={}, routing={}, indexName={}  with an error: {}",
                    esDeleteMO.getIndexKey(), esDeleteMO.getRoutingKey(), indexName,
                    ExceptionHelper.stackTrace(e));
        }
        return false;
    }

    public BoolQueryBuilder constructFieldQueryWithTranslatedFields(List<TenantEnum> tenants,
                                                                    String parentField, String value) {
        ESRuleMO rule = new ESRuleMO();
        rule.setOperator(CONTAINS_OPERATOR);
        rule.setOperand(parentField);
        rule.setValue(List.of(value));

        ESRuleMO nestedFieldRule = new ESRuleMO();
        nestedFieldRule.setOperator(NESTED_CONTAINS_OPERATOR);
        nestedFieldRule.setOperand(TRANSLATED_FIELDS + "." + parentField);
        nestedFieldRule.setNestedOperand(TRANSLATED_FIELDS);
        nestedFieldRule.setValue(List.of(value));

        ESQueryMO esQueryMO = new ESQueryMO();
        ArrayList<ESRuleMO> rules = new ArrayList<>();

        rules.add(rule);
        rules.add(nestedFieldRule);
        esQueryMO.setRules(rules);
        esQueryMO.setAutocomplete(true);

        return esQueryHandler.buildEsQuery(tenants, esQueryMO);
    }

    public void bulkDeleteByQuery(ESSearchMO esSearchMO) {
        var indexName = indexPrefix + esSearchMO.getIndexName().name();
        try {
            BoolQueryBuilder boolQueryBuilder = esQueryHandler.buildEsQuery(esSearchMO.getTenants(),
                    esSearchMO.getEsQueryMO());
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder);
            searchSourceBuilder.timeout(TimeValue.timeValueSeconds(esTimeout));
            searchSourceBuilder.size(esDeleteMaxSize);

            SearchRequest searchRequest = new SearchRequest(indexName);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = esClient.sendSearchRequest(searchRequest, RequestOptions.DEFAULT);
            log.info("Total docs: {}, found to be deleted from index: {}",
                    searchResponse.getHits().getTotalHits(), indexName);

            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            searchResponse.getHits().forEach(hit -> {
                DeleteRequest deleteRequest = new DeleteRequest(indexName, hit.getId());
                bulkRequest.add(deleteRequest);
            });
            esClient.processBulkAsync(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("Error deleting in ES for indexId={}, routing={}, indexName={}  with an error: {}",
                    esSearchMO.getIndexKey(), esSearchMO.getRoutingKey(), indexName,
                    ExceptionHelper.stackTrace(e));
        }
    }

    public <T> void bulkIndex(List<ESIndexMO<T>> esIndexMOList) {
        BulkRequest bulkRequest = new BulkRequest();
        esIndexMOList.forEach(elem -> {
            var indexName = indexPrefix + elem.getIndexName().name();
            try {
                DocWriteRequest.OpType op = DocWriteRequest.OpType.INDEX;
                IndexRequest request = new IndexRequest(indexName)
                        .id(elem.getIndexKey())
                        .source(objectMapper.writeValueAsString(elem.getSource()), XContentType.JSON).opType(op)
                        .routing(elem.getRoutingKey());
                if (elem.isWaitUntil()) {
                    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
                }
                bulkRequest.add(request);
            } catch (Exception e) {
                log.error("Error bulk indexing in ES for index={}, routing={} with an error={}", elem.getIndexKey(),
                        elem.getRoutingKey(), ExceptionHelper.stackTrace(e));
            }
        });
        esClient.processBulkAsync(bulkRequest, RequestOptions.DEFAULT);
    }

    public void bulkDeleteById(List<ESBaseMO> esBaseMOList) {
        BulkRequest bulkRequest = new BulkRequest();
        esBaseMOList.forEach(elem -> {
            var indexName = indexPrefix + elem.getIndexName().name();
            try {
                DeleteRequest request = new DeleteRequest(indexName, elem.getIndexKey())
                        .routing(elem.getRoutingKey());
                bulkRequest.add(request);
            } catch (Exception e) {
                log.error("Error bulk deleting in ES for index={}, routing={}, indexName={} with an error={}",
                        elem.getIndexKey(), elem.getRoutingKey(), indexName, ExceptionHelper.stackTrace(e));
            }
        });
        esClient.processBulkAsync(bulkRequest, RequestOptions.DEFAULT);
    }

    public <T> ESResponseMO<T> executeQuery(ESSearchMO esSearchMO,
                                            SearchSourceBuilder searchSourceBuilder, Class<T> converter)
            throws IOException {
        var indexName = indexPrefix + esSearchMO.getIndexName().name();
        SearchRequest searchRequest = new SearchRequest(indexName).source(searchSourceBuilder);
        log.info("ES search request for index name: {}, query: {}", indexName,
                searchRequest.source().query().toString().replaceAll("\\s+", " "));
        SearchResponse searchResponse = esClient.sendSearchRequest(searchRequest, RequestOptions.DEFAULT);
        log.info("ES search response: {}", searchResponse.toString());
        ArrayList<T> esResponseList = new ArrayList<>();
        ESResponseMO<T> esResponseMO = new ESResponseMO<>();
        if (searchResponse.status() == RestStatus.OK) {
            if (searchResponse.getHits().getHits().length > 0) {
                esResponseMO.setCount(searchResponse.getHits().getTotalHits().value);
                for (SearchHit esItem : searchResponse.getHits()) {
                    var sourceAsMap = esItem.getSourceAsMap();
                    sourceAsMap.put("id", esItem.getId());
                    esResponseList.add(objectMapper.convertValue(sourceAsMap, converter));
                }
                if (searchResponse.getHits().getHits().length == esSearchMO.getEsQueryMO().getLimit()) {
                    esResponseMO.setNextPage(true);
                }
                esResponseMO.setRecords(esResponseList);
            } else {
                log.info("No records present in ES matching the given criteria for index: {}", indexName);
            }
        }
        return esResponseMO;
    }

    public <T> ESResponseMO<T> search(ESSearchMO esSearchMO, Class<T> converter) {
        try {
            SearchSourceBuilder searchSourceBuilder = this.searchBuilder(esSearchMO);
            return this.executeQuery(esSearchMO, searchSourceBuilder, converter);
        } catch (Exception e) {
            log.error("Error in search ES Query= {}", ExceptionHelper.stackTrace(e));
            return null;
        }
    }

    public SearchSourceBuilder searchBuilder(ESSearchMO esSearchMO) {
        BoolQueryBuilder boolQueryBuilder = esQueryHandler.buildEsQuery(esSearchMO.getTenants(),
                esSearchMO.getEsQueryMO());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(esTimeout));
        esQueryHandler.applyCollapse(searchSourceBuilder, esSearchMO.getEsQueryMO().getCollapse());
        esQueryHandler.applySort(searchSourceBuilder, esSearchMO.getEsQueryMO());
        esQueryHandler.paginate(searchSourceBuilder, esSearchMO.getEsQueryMO().getPage(),
                esSearchMO.getEsQueryMO().getLimit());
        esQueryHandler.filterFieldsFromSource(searchSourceBuilder, esSearchMO.getInclude(), esSearchMO.getExclude());
        return searchSourceBuilder;
    }
}
