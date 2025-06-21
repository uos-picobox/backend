package com.uos.picobox.global.scheduler;

import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import com.uos.picobox.domain.screening.repository.ScreeningSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatHoldCleanupScheduler {
    
    private final ScreeningSeatRepository screeningSeatRepository;
    
    /**
     * 매 1분마다 만료된 좌석 선점을 해제합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<ScreeningSeat> expiredSeats = screeningSeatRepository
                .findAllBySeatStatusAndHoldExpiresAtBefore(SeatStatus.HOLD, now);
        
        if (!expiredSeats.isEmpty()) {
            log.info("만료된 좌석 선점 해제: {} 개", expiredSeats.size());
            
            for (ScreeningSeat seat : expiredSeats) {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seat.setHoldExpiresAt(null);
            }
            
            log.info("좌석 선점 해제 완료: {} 개", expiredSeats.size());
        }
    }
} 