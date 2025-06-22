package com.uos.picobox.global.enumClass;

public enum PointChangeType implements BaseEnum {
    EARNED, USED, EXPIRED, ADJUSTED, REFUNDED;

    @Override
    public String getValue() { return name(); }
}