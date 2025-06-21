package com.uos.picobox.global.enumClass;

public enum AdminRole implements BaseEnum {
    SUPER, USER_MANAGE, MOVIE_MANAGE, SCREEN_MANAGE;

    @Override
    public String getValue() { return name(); }
}
