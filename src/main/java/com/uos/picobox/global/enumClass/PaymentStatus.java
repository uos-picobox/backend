package com.uos.picobox.global.enumClass;

public enum PaymentStatus implements BaseEnum {
    // RESERVATION 테이블용
    PENDING,
    COMPLETED,
    FAILED,
    CANCELED,
    
    // PAYMENT 테이블용
    ABORTED,
    DONE,
    EXPIRED,
    IN_PROGRESS,
    PARTIAL_CANCELED,
    READY,
    WAITING_FOR_DEPOSIT,
    REFUNDED;

    @Override
    public String getValue() { return name(); }

    // 상태 확인 메서드들
    public boolean isCompleted() {
        return this == DONE;
    }
    
    public boolean isPending() {
        return this == IN_PROGRESS || this == READY || this == WAITING_FOR_DEPOSIT;
    }
    
    public boolean isFailed() {
        return this == ABORTED || this == EXPIRED || this == CANCELED || this == FAILED;
    }
}