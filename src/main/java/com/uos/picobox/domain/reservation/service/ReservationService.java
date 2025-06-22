package com.uos.picobox.domain.reservation.service;

import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.ticket.entity.Ticket;
import com.uos.picobox.global.enumClass.PaymentStatus;
import com.uos.picobox.global.enumClass.PointChangeType;
import com.uos.picobox.domain.point.entity.PointHistory;
import com.uos.picobox.domain.point.repository.PointHistoryRepository;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import com.uos.picobox.domain.price.repository.RoomTicketTypePriceRepository;
import com.uos.picobox.domain.reservation.dto.PaymentRequestDto;
import com.uos.picobox.domain.reservation.dto.ReservationRequestDto;
import com.uos.picobox.domain.reservation.dto.ReservationResponseDto;
import com.uos.picobox.domain.reservation.dto.SeatRequestDto;
import com.uos.picobox.domain.reservation.entity.*;
import com.uos.picobox.domain.payment.repository.PaymentRepository;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
import com.uos.picobox.domain.screening.repository.ScreeningSeatRepository;
import com.uos.picobox.global.enumClass.TicketStatus;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final CustomerRepository customerRepository;
    private final RoomTicketTypePriceRepository roomTicketTypePriceRepository;
    private final ScreeningRepository screeningRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentRepository paymentRepository;

    private static final int SEAT_HOLD_MINUTES = 10;

    /**
     * 사용자가 선택한 좌석들을 일정 시간 동안 선점합니다.
     * 선점된 좌석은 다른 사용자가 선택할 수 없으며, 지정된 시간(10분) 후 자동으로 해제됩니다.
     * 
     * @param dto 선점할 좌석 정보 (상영 ID, 좌석 ID 목록)
     * @param customerId 좌석을 선점하는 고객 ID
     * @throws IllegalStateException 이미 선점되었거나 판매된 좌석인 경우
     * @throws EntityNotFoundException 존재하지 않는 좌석인 경우
     */
    @Transactional
    public void holdSeats(SeatRequestDto dto, Long customerId) {
        log.info("좌석 선점 요청: screeningId={}, seatIds={}, customerId={}", dto.getScreeningId(), dto.getSeatIds(), customerId);
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findByIdWithPessimisticLock(dto.getScreeningId(), seatId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 좌석입니다. screeningId=" + dto.getScreeningId() + ", seatId=" + seatId));

            if (screeningSeat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("이미 선택되었거나 예매가 불가능한 좌석입니다: seatId=" + seatId);
            }

            // 좌석 선점 및 선점한 고객 ID 기록
            screeningSeat.setSeatStatus(SeatStatus.HOLD);
            screeningSeat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(SEAT_HOLD_MINUTES));
            screeningSeat.setHoldCustomerId(customerId);
            log.debug("좌석 선점 완료: screeningId={}, seatId={}, customerId={}", dto.getScreeningId(), seatId, customerId);
        }
    }

    /**
     * 사용자가 선점한 좌석들을 해제합니다.
     * 선점한 본인만 해제할 수 있으며, 해제된 좌석은 다시 예매 가능 상태가 됩니다.
     * 
     * @param dto 해제할 좌석 정보 (상영 ID, 좌석 ID 목록)
     * @param customerId 좌석을 해제하려는 고객 ID
     * @throws IllegalStateException 다른 고객이 선점한 좌석을 해제하려는 경우
     * @throws EntityNotFoundException 존재하지 않는 좌석인 경우
     */
    @Transactional
    public void releaseSeats(SeatRequestDto dto, Long customerId) {
        log.info("좌석 선점 해제 요청: screeningId={}, seatIds={}, customerId={}", dto.getScreeningId(), dto.getSeatIds(), customerId);
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId))
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 좌석입니다. screeningId=" + dto.getScreeningId() + ", seatId=" + seatId));

            if (screeningSeat.getSeatStatus() == SeatStatus.HOLD) {
                // 선점한 고객이 맞는지 확인
                if (screeningSeat.getHoldCustomerId() != null && !screeningSeat.getHoldCustomerId().equals(customerId)) {
                    throw new IllegalStateException("다른 고객이 선점한 좌석은 해제할 수 없습니다: seatId=" + seatId);
                }
                
                screeningSeat.setSeatStatus(SeatStatus.AVAILABLE);
                screeningSeat.setHoldExpiresAt(null);
                screeningSeat.setHoldCustomerId(null);
                log.debug("좌석 선점 해제 완료: screeningId={}, seatId={}, customerId={}", dto.getScreeningId(), seatId, customerId);
            }
        }
    }

    /**
     * 결제 대기 상태의 예매를 생성합니다.
     * 선점된 좌석들을 바탕으로 예매를 생성하고, 사용된 포인트만큼 고객의 포인트를 차감합니다.
     * 실제 결제는 별도의 completeReservation 메소드를 통해 완료됩니다.
     * 
     * @param dto 예매 정보 (상영 ID, 티켓 정보, 사용 포인트)
     * @param customerId 예매하는 고객 ID
     * @return 생성된 예매 정보
     * @throws IllegalStateException 선점되지 않은 좌석을 예매하려는 경우
     * @throws EntityNotFoundException 고객, 상영, 가격 정보를 찾을 수 없는 경우
     */
    @Transactional
    public ReservationResponseDto createPendingReservation(ReservationRequestDto dto, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + customerId));

        Screening screening = screeningRepository.findByIdWithDetails(dto.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다: " + dto.getScreeningId()));

        // 티켓 유형별 인원수 검증
        int totalPersonCount = dto.getTicketTypes().stream()
                .mapToInt(ReservationRequestDto.TicketTypeInfo::getCount)
                .sum();
        
        if (totalPersonCount != dto.getSeatIds().size()) {
            throw new IllegalArgumentException("선택한 인원수(" + totalPersonCount + "명)와 좌석 수(" + dto.getSeatIds().size() + "개)가 일치하지 않습니다.");
        }

        // 모든 좌석이 선점되어 있는지 확인
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId))
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다: " + seatId));
            if (screeningSeat.getSeatStatus() != SeatStatus.HOLD) {
                throw new IllegalStateException("선점되지 않은 좌석을 예매할 수 없습니다: " + seatId);
            }
        }

        // 총 금액 계산
        int totalAmount = 0;
        for (ReservationRequestDto.TicketTypeInfo ticketTypeInfo : dto.getTicketTypes()) {
            RoomTicketTypePrice priceInfo = roomTicketTypePriceRepository.findById(
                    new RoomTicketTypePrice.RoomTicketTypePriceId(screening.getScreeningRoom().getId(), ticketTypeInfo.getTicketTypeId())
            ).orElseThrow(() -> new EntityNotFoundException("해당 티켓 종류의 가격 정보를 찾을 수 없습니다."));
            totalAmount += priceInfo.getPrice() * ticketTypeInfo.getCount();
        }

        customer.usePoints(dto.getUsedPoints());
        int finalAmount = totalAmount - dto.getUsedPoints();

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .screeningId(dto.getScreeningId())
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // 티켓 생성 로직 - 좌석을 티켓 유형별로 순서대로 배정
        List<String> seatNumbers = new ArrayList<>();
        int seatIndex = 0;
        
        for (ReservationRequestDto.TicketTypeInfo ticketTypeInfo : dto.getTicketTypes()) {
            RoomTicketTypePrice priceInfo = roomTicketTypePriceRepository.findById(
                    new RoomTicketTypePrice.RoomTicketTypePriceId(screening.getScreeningRoom().getId(), ticketTypeInfo.getTicketTypeId())
            ).orElseThrow(() -> new EntityNotFoundException("가격 정보를 찾을 수 없습니다."));

            // 해당 티켓 유형의 인원수만큼 티켓 생성
            for (int i = 0; i < ticketTypeInfo.getCount(); i++) {
                Long seatId = dto.getSeatIds().get(seatIndex++);
                
                ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId))
                        .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다: " + seatId));

                Ticket ticket = Ticket.builder()
                        .reservation(reservation)
                        .screeningId(dto.getScreeningId())
                        .seatId(seatId)
                        .ticketTypeId(ticketTypeInfo.getTicketTypeId())
                        .price(priceInfo.getPrice())
                        .ticketStatus(TicketStatus.ISSUED)
                        .build();
                reservation.addTicket(ticket);
                seatNumbers.add(screeningSeat.getSeat().getSeatNumber());
            }
        }

        if (dto.getUsedPoints() > 0) {
            PointHistory pointHistory = PointHistory.builder()
                    .customer(customer)
                    .changeType(PointChangeType.USED)
                    .amount(dto.getUsedPoints())
                    .relatedReservationId(reservation.getId())
                    .build();
            pointHistoryRepository.save(pointHistory);
        }

        reservationRepository.save(reservation);
        log.info("결제 대기 상태의 예매 생성 완료: reservationId={}, 총 인원: {}명", reservation.getId(), totalPersonCount);

        return new ReservationResponseDto(reservation, dto.getUsedPoints(), finalAmount, screening.getMovie().getTitle(), seatNumbers);
    }

    /**
     * 결제를 완료하고 예매를 확정합니다.
     * 예매 상태를 COMPLETED로 변경하고, 좌석을 SOLD 상태로 변경하며,
     * 결제 정보를 저장하고 포인트 적립을 처리합니다.
     * 
     * @param dto 결제 정보 (예매 ID, 주문 ID, 결제 키, 결제 방법 등)
     * @param customerId 결제하는 고객 ID
     * @throws IllegalArgumentException 예약자 정보가 일치하지 않는 경우
     * @throws IllegalStateException 이미 처리되었거나 취소된 예약인 경우
     * @throws EntityNotFoundException 예약 정보를 찾을 수 없는 경우
     */
    @Transactional
    public void completeReservation(PaymentRequestDto dto, Long customerId) {
        log.info("결제 완료 처리 시작: reservationId={}", dto.getReservationId());
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다: " + dto.getReservationId()));

        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("예약자 정보가 일치하지 않습니다.");
        }
        if (reservation.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("이미 처리되었거나 취소된 예약입니다.");
        }

        reservation.updatePaymentStatus(PaymentStatus.COMPLETED);

        // 티켓 상태 'ISSUED'로 변경 (이미 생성 시 ISSUED로 설정됨)
        // 좌석 상태 'SOLD'로 변경
        for (Ticket ticket : reservation.getTickets()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId()))
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다."));
            screeningSeat.setSeatStatus(SeatStatus.SOLD);
            screeningSeat.setHoldExpiresAt(null);
        }

        // 사용된 포인트 계산
        int usedPoints = dto.getUsedPointAmount() != null ? dto.getUsedPointAmount() : 0;
        int finalAmount = reservation.getTotalAmount() - usedPoints;

        // 포인트 적립 (결제 금액의 10%)
        int earnedPoints = (int) (finalAmount * 0.1);
        if (earnedPoints > 0) {
            Customer customer = reservation.getCustomer();
            customer.addPoints(earnedPoints);
            PointHistory pointHistory = PointHistory.builder()
                    .customer(customer)
                    .changeType(PointChangeType.EARNED)
                    .amount(earnedPoints)
                    .relatedReservationId(reservation.getId())
                    .build();
            pointHistoryRepository.save(pointHistory);
        }

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(dto.getOrderId())
                .paymentKey(dto.getPaymentKey())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus("DONE")
                .currency("KRW")
                .paymentDiscountId(null)
                .amount(reservation.getTotalAmount())
                .usedPointAmount(usedPoints)
                .finalAmount(finalAmount)
                .approvedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        log.info("결제 완료 처리 성공: reservationId={}", reservation.getId());
    }

    /**
     * 선점 시간이 만료된 좌석들을 자동으로 해제합니다.
     * 스케줄러에 의해 주기적으로 호출되어 만료된 HOLD 상태의 좌석들을 AVAILABLE로 변경합니다.
     * 
     * @return 해제된 좌석의 개수
     */
    @Transactional
    public int releaseExpiredHeldSeats() {
        List<ScreeningSeat> expiredSeats = screeningSeatRepository.findAllBySeatStatusAndHoldExpiresAtBefore(SeatStatus.HOLD, LocalDateTime.now());
        if (expiredSeats.isEmpty()) {
            return 0;
        }
        for (ScreeningSeat seat : expiredSeats) {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setHoldExpiresAt(null);
            seat.setHoldCustomerId(null);
        }
        return expiredSeats.size();
    }

    /**
     * 고객의 예매 내역을 조회합니다.
     * @param customerId 고객 ID
     * @return 예매 내역 목록
     */
    public List<ReservationResponseDto> getReservationsByCustomerId(Long customerId) {
        List<Reservation> reservations = reservationRepository.findByCustomerIdOrderByReservationDateDesc(customerId);
        return reservations.stream()
                .map(reservation -> {
                    Screening screening = screeningRepository.findById(reservation.getScreeningId())
                            .orElse(null);
                    String movieTitle = screening != null && screening.getMovie() != null ? 
                                       screening.getMovie().getTitle() : "영화 정보 없음";
                    
                    List<String> seatNumbers = reservation.getTickets().stream()
                            .map(ticket -> {
                                ScreeningSeat screeningSeat = screeningSeatRepository.findById(
                                    new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId())
                                ).orElse(null);
                                return screeningSeat != null ? screeningSeat.getSeat().getSeatNumber() : "좌석 정보 없음";
                            })
                            .collect(Collectors.toList());
                    
                    int usedPoints = reservation.getPayment() != null ? reservation.getPayment().getUsedPointAmount() : 0;
                    int finalAmount = reservation.getTotalAmount() - usedPoints;
                    
                    return new ReservationResponseDto(reservation, usedPoints, finalAmount, movieTitle, seatNumbers);
                })
                .collect(Collectors.toList());
    }
}