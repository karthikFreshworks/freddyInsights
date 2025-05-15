package com.freshworks.freddy.insights.constant.enums;

public enum EntityEnum {
    prompt("prompt"),
    insight("insight"),
    intent_handler("intent-handler");

    private final String value;
    EntityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
