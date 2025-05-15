package com.freshworks.freddy.insights.constant.enums.insights;

public enum OperatorEnum {
    is_in,
    is;

    public static boolean isValidOperator(String value) {
        for (OperatorEnum op : OperatorEnum.values()) {
            if (op.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
