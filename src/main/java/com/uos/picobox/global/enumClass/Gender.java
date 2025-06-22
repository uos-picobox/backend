package com.uos.picobox.global.enumClass;

public enum Gender implements BaseEnum {
    MALE, FEMALE;

    @Override
    public String getValue() { return name(); }
}
