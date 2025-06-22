package com.uos.picobox.global.enumClass;

public enum PaymentMethod implements BaseEnum {
    CARD,           // 카드
    VIRTUAL_ACCOUNT,// 가상계좌
    EASY_PAY,       // 간편결제
    GAME_GIFT,      // 게임문화상품권
    TRANSFER,       // 계좌이체
    BOOK_GIFT,      // 도서문화상품권
    CULTURE_GIFT,   // 문화상품권
    MOBILE;          // 휴대폰

    @Override
    public String getValue() { return name(); }
} 