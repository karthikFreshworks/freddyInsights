package com.freshworks.freddy.insights.constant.enums;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public enum IntentHandlerTagsEnum {
    ask_kb("ask-kb");

    private final String value;

    IntentHandlerTagsEnum(String value) {
        this.value = value;
    }

    public static List<IntentHandlerTagsEnum> getEnumList(List<String> tags) {
        return tags.stream()
                .map(IntentHandlerTagsEnum::getByValue)
                .collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }

    public static IntentHandlerTagsEnum getByValue(String value) {
        for (IntentHandlerTagsEnum enumConstant : IntentHandlerTagsEnum.values()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        throw new AIResponseStatusException("No enum present with the specified value: " + value,
                HttpStatus.NOT_ACCEPTABLE,
                ErrorCode.NOT_ACCEPTABLE);
    }
}
