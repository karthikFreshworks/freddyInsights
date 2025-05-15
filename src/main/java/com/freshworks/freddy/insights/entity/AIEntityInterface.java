package com.freshworks.freddy.insights.entity;

public interface AIEntityInterface {
    String getId();

    default String getModelId() {
        return null;
    }
}
