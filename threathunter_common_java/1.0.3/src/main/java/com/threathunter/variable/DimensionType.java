package com.threathunter.variable;

/**
 * created by www.threathunter.cn
 */
public enum DimensionType {
    IP("c_ip"),
    UID("uid"),
    DID("did"),
    PAGE("page"),
    GLOBAL(""),
    OTHER("");

    private String field;

    DimensionType(String field) {
        this.field = field;
    }

    public String getFieldName() {
        return this.field;
    }

    public static DimensionType getDimension(String dimension) {
        return DimensionType.valueOf(dimension.toUpperCase());
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
