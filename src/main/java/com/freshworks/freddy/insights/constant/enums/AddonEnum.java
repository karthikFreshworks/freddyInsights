package com.freshworks.freddy.insights.constant.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AddonEnum {
    selfservice("self_service"),
    copilot("copilot"),
    insights("insights");

    private final String value;

    AddonEnum(String value) {
        this.value = value;
    }

    public static List<String> getAllValues() {
        return Arrays.stream(AddonEnum.values()).map(AddonEnum::getValue).collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }
}
