package com.freshworks.freddy.insights.dto.anonymize;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnonymizeResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String anonymizedText;
    private MetaDataBody meta;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaDataBody implements Serializable {
        private String anonymizationId;
        private String language;
        private Integer noOfEntities;
        private List<String> entities;
    }
}
