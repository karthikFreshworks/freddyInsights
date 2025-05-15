package com.freshworks.freddy.insights.helper;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AcceptLanguageParser {
    private static class LanguageTagWithWeight {
        private final String originalLanguageTag;
        private final String lowercaseLanguageTag;
        private final double weight;

        public LanguageTagWithWeight(String originalLanguageTag, double weight) {
            this.originalLanguageTag = originalLanguageTag;
            this.lowercaseLanguageTag = originalLanguageTag.toLowerCase();
            this.weight = weight;
        }

        public String getOriginalLanguageTag() {
            return originalLanguageTag;
        }

        public String getLowercaseLanguageTag() {
            return lowercaseLanguageTag;
        }

        public double getWeight() {
            return weight;
        }
    }

    public static List<String> parseAcceptLanguage(String acceptLanguageHeader) {
        if (StringUtils.isEmpty(acceptLanguageHeader) || "*".equals(acceptLanguageHeader)) {
            return null;
        }

        List<LanguageTagWithWeight> languageTagsWithWeight = Arrays.stream(acceptLanguageHeader.split(","))
                .map(String::trim)
                .map(range -> {
                    String[] parts = range.split(";q=");
                    String languageTag = parts[0];
                    double weight = (parts.length > 1) ? Double.parseDouble(parts[1]) : 1.0;
                    return new LanguageTagWithWeight(languageTag, weight);
                })
                .collect(Collectors.toList());

        List<LanguageTagWithWeight> sortedLanguageTags = languageTagsWithWeight.stream()
                .sorted((lt1, lt2) -> Double.compare(lt2.getWeight(), lt1.getWeight()))
                .collect(Collectors.toList());

        return sortedLanguageTags.stream()
                .map(LanguageTagWithWeight::getOriginalLanguageTag)
                .collect(Collectors.toList());
    }
}
