package com.uos.picobox.domain.point.dto;

import com.uos.picobox.global.enumClass.PointChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MyPointHistoryResponseDto {
    private Long customerId;
    private String loginId;
    private Integer points;
    private MyPointHistory[] histories;

    @Builder
    public static class MyPointHistory {
        private PointChangeType changeType;
        private Integer amount;
        private Long relatedReservationId;
        private LocalDateTime createdAt;
    }
}
