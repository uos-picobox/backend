package com.uos.picobox.global.enumClass;

public enum TicketStatus implements BaseEnum {
    ISSUED,
    USED,
    CANCELED,
    REFUNDED;

    @Override
    public String getValue() { return name(); }
}