package com.freshworks.freddy.insights.validator;

import com.freshworks.freddy.insights.constant.enums.LanguageCodeEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LanguageCodeValidator implements ConstraintValidator<AllowedLanguageCodes, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            var languageCode = LanguageCodeEnum.getByValue(value);
            return languageCode != LanguageCodeEnum.none;
        } catch (Exception ex) {
            return false;
        }
    }
}
