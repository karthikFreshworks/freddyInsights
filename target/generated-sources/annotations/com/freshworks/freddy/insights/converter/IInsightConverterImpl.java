package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.insight.AIInsightCreateDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightTranslationDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightsDismissDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.modelobject.central.AIInsightCentralPayload;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-14T14:21:02+0530",
    comments = "version: 1.5.1.Final, compiler: javac, environment: Java 21.0.6 (Eclipse Adoptium)"
)
public class IInsightConverterImpl implements IInsightConverter {

    @Override
    public AIInsightEntity convertToEntity(AIInsightCreateDTO insightCreateDTO) {
        if ( insightCreateDTO == null ) {
            return null;
        }

        AIInsightEntity.AIInsightEntityBuilder aIInsightEntity = AIInsightEntity.builder();

        aIInsightEntity.tenant( insightCreateDTO.getTenant() );
        aIInsightEntity.name( insightCreateDTO.getName() );
        aIInsightEntity.accountId( insightCreateDTO.getAccountId() );
        aIInsightEntity.userId( insightCreateDTO.getUserId() );
        aIInsightEntity.serviceId( insightCreateDTO.getServiceId() );
        aIInsightEntity.usecaseId( insightCreateDTO.getUsecaseId() );
        aIInsightEntity.orgId( insightCreateDTO.getOrgId() );
        aIInsightEntity.bundleId( insightCreateDTO.getBundleId() );
        aIInsightEntity.groupId( insightCreateDTO.getGroupId() );
        aIInsightEntity.group( insightCreateDTO.getGroup() );
        aIInsightEntity.domain( insightCreateDTO.getDomain() );
        aIInsightEntity.sku( insightCreateDTO.getSku() );
        aIInsightEntity.title( insightCreateDTO.getTitle() );
        aIInsightEntity.uiTag( insightCreateDTO.getUiTag() );
        List<String> list = insightCreateDTO.getPlans();
        if ( list != null ) {
            aIInsightEntity.plans( new ArrayList<String>( list ) );
        }
        List<String> list1 = insightCreateDTO.getAddons();
        if ( list1 != null ) {
            aIInsightEntity.addons( new ArrayList<String>( list1 ) );
        }
        List<String> list2 = insightCreateDTO.getTags();
        if ( list2 != null ) {
            aIInsightEntity.tags( new ArrayList<String>( list2 ) );
        }
        List<String> list3 = insightCreateDTO.getTimeZones();
        if ( list3 != null ) {
            aIInsightEntity.timeZones( new ArrayList<String>( list3 ) );
        }
        List<String> list4 = insightCreateDTO.getPromptIds();
        if ( list4 != null ) {
            aIInsightEntity.promptIds( new ArrayList<String>( list4 ) );
        }
        aIInsightEntity.languageCode( insightCreateDTO.getLanguageCode() );
        List<AIInsightTranslationDTO> list5 = insightCreateDTO.getTranslatedFields();
        if ( list5 != null ) {
            aIInsightEntity.translatedFields( new ArrayList<AIInsightTranslationDTO>( list5 ) );
        }
        aIInsightEntity.imageUrl( insightCreateDTO.getImageUrl() );
        aIInsightEntity.status( insightCreateDTO.getStatus() );
        aIInsightEntity.version( insightCreateDTO.getVersion() );
        aIInsightEntity.context( insightCreateDTO.getContext() );
        aIInsightEntity.aggregate( insightCreateDTO.getAggregate() );
        aIInsightEntity.businessKpi( insightCreateDTO.getBusinessKpi() );
        aIInsightEntity.metric( insightCreateDTO.getMetric() );
        aIInsightEntity.department( insightCreateDTO.getDepartment() );
        aIInsightEntity.frequency( insightCreateDTO.getFrequency() );
        aIInsightEntity.type( insightCreateDTO.getType() );
        aIInsightEntity.importanceScore( insightCreateDTO.getImportanceScore() );
        aIInsightEntity.scenarioType( insightCreateDTO.getScenarioType() );
        aIInsightEntity.timeToLive( insightCreateDTO.getTimeToLive() );

        return aIInsightEntity.build();
    }

    @Override
    public AIInsightCentralPayload prepareInsightPayload(AIInsightEntity insightEntity) {
        if ( insightEntity == null ) {
            return null;
        }

        AIInsightCentralPayload aIInsightCentralPayload = new AIInsightCentralPayload();

        if ( insightEntity.getTenant() != null ) {
            aIInsightCentralPayload.setTenant( insightEntity.getTenant().name() );
        }
        aIInsightCentralPayload.setAccountId( insightEntity.getAccountId() );
        aIInsightCentralPayload.setUserId( insightEntity.getUserId() );
        aIInsightCentralPayload.setServiceId( insightEntity.getServiceId() );
        aIInsightCentralPayload.setUsecaseId( insightEntity.getUsecaseId() );
        aIInsightCentralPayload.setOrgId( insightEntity.getOrgId() );
        aIInsightCentralPayload.setBundleId( insightEntity.getBundleId() );
        aIInsightCentralPayload.setGroupId( insightEntity.getGroupId() );
        aIInsightCentralPayload.setGroup( insightEntity.getGroup() );
        aIInsightCentralPayload.setSku( insightEntity.getSku() );
        List<String> list = insightEntity.getPlans();
        if ( list != null ) {
            aIInsightCentralPayload.setPlans( new ArrayList<String>( list ) );
        }
        List<String> list1 = insightEntity.getAddons();
        if ( list1 != null ) {
            aIInsightCentralPayload.setAddons( new ArrayList<String>( list1 ) );
        }
        List<String> list2 = insightEntity.getTags();
        if ( list2 != null ) {
            aIInsightCentralPayload.setTags( new ArrayList<String>( list2 ) );
        }
        aIInsightCentralPayload.setLanguageCode( insightEntity.getLanguageCode() );
        if ( insightEntity.getStatus() != null ) {
            aIInsightCentralPayload.setStatus( insightEntity.getStatus().name() );
        }
        aIInsightCentralPayload.setCreatedAt( insightEntity.getCreatedAt() );
        aIInsightCentralPayload.setCreatedBy( insightEntity.getCreatedBy() );
        aIInsightCentralPayload.setTimeToLive( insightEntity.getTimeToLive() );
        aIInsightCentralPayload.setArchivedAt( insightEntity.getArchivedAt() );

        return aIInsightCentralPayload;
    }

    @Override
    public AIInsightsDismissDTO prepareDismissPayload(String insightId) {
        if ( insightId == null ) {
            return null;
        }

        AIInsightsDismissDTO.AIInsightsDismissDTOBuilder aIInsightsDismissDTO = AIInsightsDismissDTO.builder();

        aIInsightsDismissDTO.insightId( insightId );

        return aIInsightsDismissDTO.build();
    }
}
