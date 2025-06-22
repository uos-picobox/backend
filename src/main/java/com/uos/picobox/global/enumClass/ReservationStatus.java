package com.uos.picobox.global.enumClass;

public enum ReservationStatus implements BaseEnum {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELED;

    @Override
    public String getValue() {
        return name();
    }
} 