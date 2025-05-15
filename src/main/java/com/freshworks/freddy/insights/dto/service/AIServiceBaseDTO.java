package com.freshworks.freddy.insights.dto.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AIServiceBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Valid
    private Rule rule;
    private String responseParser;
    private String requestParser;
    private TreeSet<@Valid Templates> templates;
    private Map<String, String> header;
    private String url;
    private ApiMethodEnum method;

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Param implements Serializable {
        private static final long serialVersionUID = 1L;
        @NotBlank(message = ExceptionConstant.NOT_VALID_NAME)
        private String name;
        @NotBlank(message = ExceptionConstant.NOT_VALID_DESCRIPTION)
        private String description;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Rule implements Serializable {
        private static final long serialVersionUID = 1L;
        @NotNull(message = ExceptionConstant.NOT_VALID_FEATURES)
        private Set<String> enabledFeature = new HashSet<>();
        private List<ExternalResponseHeader> externalResponseHeaders;
        @NotNull(message = ExceptionConstant.NOT_VALID_BODY)
        private Map<String, Object> body = new HashMap<>();
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ExternalResponseHeader implements Serializable {
        private static final long serialVersionUID = 1L;
        private String key;
        private String rename;
        private String transformationParser;
    }

    @ToString
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Templates implements Serializable, Comparable<Templates> {
        private static final long serialVersionUID = 1L;
        @NotBlank(message = ExceptionConstant.NOT_VALID_TEMPLATE_KEY)
        private String templateKey;
        @NotBlank(message = ExceptionConstant.NOT_VALID_TEMPLATE)
        private String template;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Templates templates = (Templates) o;
            return templateKey.equals(templates.templateKey);
        }

        @Override
        public int hashCode() {
            return templateKey.hashCode();
        }

        @Override
        public int compareTo(Templates t) {
            return this.templateKey.compareToIgnoreCase(t.templateKey);
        }
    }
}
