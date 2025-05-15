package com.freshworks.freddy.insights.converter;

import com.freshworks.freddy.insights.dto.prompt.AIPromptCreateDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptTranslationDTO;
import com.freshworks.freddy.insights.entity.AIPromptEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-14T14:21:02+0530",
    comments = "version: 1.5.1.Final, compiler: javac, environment: Java 21.0.6 (Eclipse Adoptium)"
)
public class IPromptConverterImpl implements IPromptConverter {

    @Override
    public AIPromptEntity convertToEntity(AIPromptCreateDTO promptCreateDTO) {
        if ( promptCreateDTO == null ) {
            return null;
        }

        AIPromptEntity.AIPromptEntityBuilder aIPromptEntity = AIPromptEntity.builder();

        aIPromptEntity.text( promptCreateDTO.getText() );
        aIPromptEntity.languageCode( promptCreateDTO.getLanguageCode() );
        aIPromptEntity.name( promptCreateDTO.getName() );
        List<AIPromptTranslationDTO> list = promptCreateDTO.getTranslatedFields();
        if ( list != null ) {
            aIPromptEntity.translatedFields( new ArrayList<AIPromptTranslationDTO>( list ) );
        }
        aIPromptEntity.userId( promptCreateDTO.getUserId() );
        aIPromptEntity.accountId( promptCreateDTO.getAccountId() );
        aIPromptEntity.group( promptCreateDTO.getGroup() );
        aIPromptEntity.suggest( promptCreateDTO.getSuggest() );
        aIPromptEntity.weight( promptCreateDTO.getWeight() );
        List<String> list1 = promptCreateDTO.getTags();
        if ( list1 != null ) {
            aIPromptEntity.tags( new ArrayList<String>( list1 ) );
        }
        aIPromptEntity.version( promptCreateDTO.getVersion() );
        aIPromptEntity.tenant( promptCreateDTO.getTenant() );
        aIPromptEntity.intentHandler( promptCreateDTO.getIntentHandler() );

        return aIPromptEntity.build();
    }
}
