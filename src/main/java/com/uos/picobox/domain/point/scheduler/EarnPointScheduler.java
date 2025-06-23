package com.uos.picobox.domain.point.scheduler;

import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.payment.repository.PaymentRepository;
import com.uos.picobox.domain.point.entity.PointHistory;
import com.uos.picobox.domain.point.repository.PointHistoryRepository;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import com.uos.picobox.domain.screening.repository.ScreeningSeatRepository;
import com.uos.picobox.global.enumClass.PointChangeType;
import com.uos.picobox.user.entity.Customer;
import jakarta.persistence.EntityNotFoundException;
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
public class EarnPointScheduler {
    
    private final ReservationRepository reservationRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 매 30분마다 포인트 적립을 실행합니다.
     */
    @Scheduled(fixedRate = 1800000) // 30분마다 실행
    @Transactional
    public void earnPoints() {
        // 회원이 예매하고 상태가 COMPLETED인 예매들.
        List<Long> reservationIds = reservationRepository.findCompletedReservationIds();
        for (Long reservationId : reservationIds) {
            List<PointHistory> pointHistoryList = pointHistoryRepository.findAllByRelatedReservationId(reservationId);

            // 이미 적립된 내역이 있는지 확인
            boolean alreadyEarned = pointHistoryList.stream()
                    .anyMatch(ph -> ph.getChangeType() == PointChangeType.EARNED);

            if (alreadyEarned) {
                continue;
            }

            // 적립 내역이 없으므로 포인트 적립 수행
            log.info("적립 수행: reservationId={}", reservationId);
             Customer customer = reservationRepository.findById(reservationId).orElseThrow(() ->
                            new EntityNotFoundException("예매 정보를 찾을 수 없습니다.")).getCustomer();
             Payment payment = paymentRepository.findByReservationId(reservationId).orElseThrow(() ->
                     new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));
             int amount = (int) (payment.getFinalAmount() * 0.1);
             if (amount > 0) {
                 customer.addPoints(amount);
                 PointHistory pointHistory = PointHistory.builder()
                         .customer(customer)
                         .changeType(PointChangeType.EARNED)
                         .amount(amount)
                         .relatedReservationId(reservationId)
                         .build();
                 pointHistoryRepository.save(pointHistory);
             }
        }
    }
} 