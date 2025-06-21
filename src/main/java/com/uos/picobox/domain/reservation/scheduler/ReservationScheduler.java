package com.uos.picobox.domain.reservation.scheduler;

import com.uos.picobox.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // 1분마다 실행 (fixedDelay = 60000ms)
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredSeatHolds() {
        log.info("선점 만료 좌석 정리 스케줄러 시작...");
        try {
            int releasedCount = reservationService.releaseExpiredHeldSeats();
            log.info("만료된 좌석 {}개를 해제했습니다.", releasedCount);
        } catch (Exception e) {
            log.error("선점 만료 좌석 정리 중 오류 발생", e);
        }
    }
}