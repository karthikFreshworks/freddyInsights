package com.freshworks.freddy.insights.validator;

import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.dto.insight.AIInsightTranslationDTO;
import com.freshworks.freddy.insights.dto.prompt.AIPromptTranslationDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AcceptLanguageParser;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Component
public class AICommonValidator {
    private final AICommonValidateException aiCommonValidateException;

    @Value("#{'${freddy.insights.allowed.language.codes}'.split(',')}")
    private final List<String> allowedLanguageCodes;

    public AICommonValidator(AICommonValidateException aiCommonValidateException, List<String> allowedLanguageCodes) {
        this.aiCommonValidateException = aiCommonValidateException;
        this.allowedLanguageCodes = allowedLanguageCodes;
    }

    public void validateAcceptLanguageCodes(List<String> parsedAcceptLanguage) {
        for (String languageCode : parsedAcceptLanguage) {
            if (LanguageCodeEnum.getByValue(languageCode) == LanguageCodeEnum.none) {
                aiCommonValidateException.badRequestException(String.format(ExceptionConstant.NOT_VALID_ACCEPT_LANGUAGE,
                        languageCode, LanguageCodeEnum.getAllValues()));
            }
        }
    }

    public List<String> validateAndReturnAcceptLanguageCodes(String acceptLanguage) {
        validateLength(acceptLanguage, 3);
        if (acceptLanguage != null)  {
            List<String> parsedAcceptLanguageCodes;
            try {
                parsedAcceptLanguageCodes = AcceptLanguageParser.parseAcceptLanguage(acceptLanguage);
            } catch (Exception ex) {
                throw new AIResponseStatusException(String.format(ExceptionConstant.NOT_VALID_ACCEPT_LANGUAGE,
                        acceptLanguage, LanguageCodeEnum.getAllValues()),
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
            }
            if (parsedAcceptLanguageCodes != null) {
                parsedAcceptLanguageCodes = filterTheUnSupportedLanguageCode(parsedAcceptLanguageCodes);
                validateAcceptLanguageCodes(parsedAcceptLanguageCodes);
            }
            if (parsedAcceptLanguageCodes != null && !parsedAcceptLanguageCodes.contains("en")) {
                parsedAcceptLanguageCodes.add("en");
            }
            return parsedAcceptLanguageCodes;
        }
        return null;
    }

    private List<String> filterTheUnSupportedLanguageCode(List<String> parsedAcceptLanguages) {
        List<String> filteredLanguages = new ArrayList<>();
        for (String language : parsedAcceptLanguages) {
            if (allowedLanguageCodes.contains(language)) {
                filteredLanguages.add(language);
            }
        }
        return filteredLanguages;
    }

    public void validateLength(String acceptLanguage, int allowedSize) {
        if (acceptLanguage != null) {
            String[] languageCodes = acceptLanguage.split(",");
            if (languageCodes.length > allowedSize) {
                throw new AIResponseStatusException(ExceptionConstant.LANGUAGE_CODES_LIMIT_EXCEEDED,
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
            }
        }
    }

    public void validateUniqueLanguageCode(
            List<String> languageCodeOfTranslatedFields, String parentLanguageCode) {
        // validate the language code of translated fields are unique
        Set<String> uniqueLanguageCodes = new HashSet<>(languageCodeOfTranslatedFields);
        if (uniqueLanguageCodes.size() != languageCodeOfTranslatedFields.size()) {
            aiCommonValidateException.notAcceptableException(ExceptionConstant.UNIQUE_LANGUAGE_CODE);
        }

        // validate the translated fields not contains the parent language code
        if (uniqueLanguageCodes.contains(parentLanguageCode)) {
            aiCommonValidateException.conflictDataException(ExceptionConstant.CONFLICT_PARENT_LANGUAGE_CODE);
        }
    }

    public void validateDate(String inputDate) {
        try {
            if (StringUtils.isNotBlank(inputDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date parsedDate = sdf.parse(inputDate);
                Date currentDate = new Date();

                if (!parsedDate.after(currentDate)) {
                    throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_DATE,
                            HttpStatus.NOT_ACCEPTABLE,
                            ErrorCode.NOT_ACCEPTABLE);
                }
            }
        } catch (ParseException e) {
            throw new AIResponseStatusException(ExceptionConstant.NOT_VALID_DATE_FORMAT,
                    HttpStatus.NOT_ACCEPTABLE,
                    ErrorCode.NOT_ACCEPTABLE);
        }
    }

    public void validateStatus(StatusEnum statusEnum) {
        if (StatusEnum.ARCHIVED == statusEnum) {
            aiCommonValidateException.notAcceptableException(ExceptionConstant.NOT_VALID_STATUS);
        }
    }

    public void validateCreateOrUpdateInsight(String timeToLive, StatusEnum status,
                                              List<AIInsightTranslationDTO> translatedFields,
                                              String languageCode) {
        validateStatus(status);
        validateDate(timeToLive);
        if (translatedFields != null) {
            List<String> languageCodeOfTranslatedFields = new ArrayList<>();
            for (AIInsightTranslationDTO field : translatedFields) {
                languageCodeOfTranslatedFields.add(field.getLanguageCode());
            }
            validateUniqueLanguageCode(languageCodeOfTranslatedFields, languageCode);
        }
    }

    public void validateCreateOrUpdatePrompt(List<AIPromptTranslationDTO> translatedFields,
                                              String languageCode) {
        if (translatedFields != null) {
            List<String> languageCodeOfTranslatedFields = new ArrayList<>();
            for (AIPromptTranslationDTO field : translatedFields) {
                languageCodeOfTranslatedFields.add(field.getLanguageCode());
            }
            validateUniqueLanguageCode(languageCodeOfTranslatedFields, languageCode);
        }
    }
}
