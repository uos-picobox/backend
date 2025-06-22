package com.uos.picobox.global.scheduler;

import com.uos.picobox.domain.ticket.entity.Ticket;
import com.uos.picobox.global.enumClass.TicketStatus;
import com.uos.picobox.domain.ticket.repository.TicketRepository;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
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
public class TicketStatusUpdateScheduler {

    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;

    /**
     * 매 10분마다 실행되어 상영 시작 후 10분이 지난 티켓들을 USED 상태로 변경합니다.
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    @Transactional
    public void updateTicketsToUsed() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime tenMinutesAgo = currentTime.minusMinutes(10);
        
        // 상영 시작 후 10분이 지난 상영들을 조회
        List<Screening> screeningsToUpdate = screeningRepository.findScreeningsStartedBefore(tenMinutesAgo);
        
        int updatedCount = 0;
        
        for (Screening screening : screeningsToUpdate) {
            // 해당 상영의 ISSUED 상태 티켓들을 USED로 변경
            List<Ticket> issuedTickets = ticketRepository.findByScreeningIdAndTicketStatus(
                screening.getId(), TicketStatus.ISSUED);
            
            for (Ticket ticket : issuedTickets) {
                ticket.updateStatus(TicketStatus.USED);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            log.info("티켓 상태 자동 업데이트 완료: {}개 티켓을 USED로 변경", updatedCount);
        }
    }
} 