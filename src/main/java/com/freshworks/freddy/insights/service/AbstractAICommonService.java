package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.constant.AIRequestConstant;
import com.freshworks.freddy.insights.constant.DocumentDBConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.helper.ESQueryHelper;
import com.freshworks.freddy.insights.repository.AIServiceRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
public abstract class AbstractAICommonService<T> extends AbstractAIBaseHelper {
    protected AIServiceRepository aiServiceRepository;

    protected MongoTemplate mongoTemplate;

    protected ESQueryHelper queryHelper;

    @Autowired
    public void setAiServiceRepository(AIServiceRepository aiServiceRepository) {
        this.aiServiceRepository = aiServiceRepository;
    }

    @Autowired
    public void setQueryHelper(ESQueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<T> findByModelIdAndTenantExcludeFields(String modelId, String[] exclude, Class<T> tClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where(DocumentDBConstant.MODEL_ID)
                        .is(modelId)
                        .and(AIRequestConstant.TENANT)
                        .in(TenantEnum.global, getContextVO().getTenant()))
                .fields().exclude(exclude);
        return Optional.ofNullable(getMongoTemplate().findOne(query, tClass));
    }

    public Optional<T> findByIdAndTenantAndGlobalExcludeFields(String id, String[] exclude, Class<T> tClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where(DocumentDBConstant.ID)
                        .is(id)
                        .and(AIRequestConstant.TENANT)
                        .is(getContextVO().getTenant()))
                .fields().exclude(exclude);
        return Optional.ofNullable(getMongoTemplate().findOne(query, tClass));
    }

    public Optional<T> findByIdAndTenantExcludeFields(String id, String[] exclude, Class<T> tClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where(DocumentDBConstant.ID)
                        .is(id)
                        .and(AIRequestConstant.TENANT)
                        .in(TenantEnum.global, getContextVO().getTenant()))
                .fields().exclude(exclude);
        return Optional.ofNullable(getMongoTemplate().findOne(query, tClass));
    }

    public Optional<T> findByIdAndTenantExcludeFields(Criteria criteria,
                                                      String[] exclude,
                                                      Class<T> tClass) {
        Query query = new Query();
        exclude = modifyExcludeIfSuperAdmin(exclude);
        query.addCriteria(criteria)
                .fields().exclude(exclude);
        log.info("Query {}", query);
        return Optional.ofNullable(getMongoTemplate().findOne(query, tClass));
    }

    public List<T> findAllByTenantExcludeFields(Criteria criteria,
                                                String[] exclude,
                                                Class<T> tClass) {
        Query query = new Query();
        exclude = modifyExcludeIfSuperAdmin(exclude);
        query.addCriteria(criteria)
                .with(Sort.by(Sort.Direction.DESC, DocumentDBConstant.CREATED_AT))
                .fields().exclude(exclude);
        return getMongoTemplate().find(query, tClass);
    }

    public List<T> findAllByTenantExcludeFields(Criteria criteria,
                                                String[] exclude,
                                                Class<T> tClass,
                                                Pageable pageable) {
        Query query = new Query();
        exclude = modifyExcludeIfSuperAdmin(exclude);
        query.with(pageable).addCriteria(criteria)
                .with(Sort.by(Sort.Direction.DESC, DocumentDBConstant.CREATED_AT))
                .fields().exclude(exclude);
        return getMongoTemplate().find(query, tClass);
    }
}
