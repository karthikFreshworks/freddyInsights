package com.freshworks.freddy.insights.constant;

public interface ESConstant {
    Integer ES_CONFIG_CONNECT_TIMEOUT = 5000;
    Integer ES_CONFIG_REQUEST_TIMEOUT = 3000;
    Integer ES_CONFIG_SOCKET_TIMEOUT = 5000;
    Integer ES_CONFIG_IDEAL_TIMEOUT = 30;
    int RETRY_MAX_ATTEMPTS = 3;
    int RETRY_TIME_INTERVAL = 2000;
    String IS_OPERATOR = "is";
    String CONTAINS_OPERATOR = "contains";
    String NESTED_CONTAINS_OPERATOR = "nested_contains";
    String NOT_IN = "not_in";
    String MORE_LIKE = "more_like";
    String IN_OPERATOR = "in";
    String SPAN_NEAR = "span_near";
    String LESS_THAN = "less_than";
    String GREATER_THAN = "greater_than";
    String MUST_NOT = "must_not";
    String MUST = "must";
    String MUST_CUSTOM_QUERY = "must_custom_query";
    String SHOULD = "should";
}
