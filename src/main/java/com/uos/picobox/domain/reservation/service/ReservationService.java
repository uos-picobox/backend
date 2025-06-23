package com.uos.picobox.domain.reservation.service;

import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.payment.service.PaymentService;
import com.uos.picobox.domain.ticket.entity.Ticket;
import com.uos.picobox.global.enumClass.ReservationStatus;
import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import com.uos.picobox.domain.price.repository.RoomTicketTypePriceRepository;
import com.uos.picobox.domain.reservation.dto.*;
import com.uos.picobox.domain.reservation.entity.*;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.entity.ScreeningSeat;
import com.uos.picobox.domain.screening.entity.SeatStatus;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
import com.uos.picobox.domain.screening.repository.ScreeningSeatRepository;
import com.uos.picobox.global.enumClass.TicketStatus;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.entity.Guest;
import com.uos.picobox.user.repository.CustomerRepository;
import com.uos.picobox.user.repository.GuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final CustomerRepository customerRepository;
    private final GuestRepository guestRepository;
    private final RoomTicketTypePriceRepository roomTicketTypePriceRepository;
    private final ScreeningRepository screeningRepository;

    private final PaymentService paymentService;

    private static final int SEAT_HOLD_MINUTES = 10;

    /**
     * 사용자가 선택한 좌석들을 일정 시간 동안 선점합니다.
     * 선점된 좌석은 다른 사용자가 선택할 수 없으며, 지정된 시간(10분) 후 자동으로 해제됩니다.
     * 
     * @param dto 선점할 좌석 정보 (상영 ID, 좌석 ID 목록)
     * @param userInfo 사용자 인증 정보
     * @throws IllegalStateException 이미 선점되었거나 판매된 좌석인 경우
     * @throws EntityNotFoundException 존재하지 않는 좌석인 경우
     */
    @Transactional
    public void holdSeats(SeatRequestDto dto, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        log.info("좌석 선점 요청: screeningId={}, seatIds={}, userType={}, userId={}", 
                dto.getScreeningId(), dto.getSeatIds(), userType, userId);
        
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat.ScreeningSeatId id = new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId);
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다: " + seatId));
            
            if (screeningSeat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("이미 선택된 좌석입니다: " + seatId);
            }
            
            screeningSeat.setSeatStatus(SeatStatus.HOLD);
            screeningSeat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(SEAT_HOLD_MINUTES));
            
            if ("customer".equals(userType)) {
                screeningSeat.setHoldCustomerId(userId);
                screeningSeat.setHoldGuestId(null);
            } else {
                screeningSeat.setHoldGuestId(userId);
                screeningSeat.setHoldCustomerId(null);
            }
        }
        
        log.info("좌석 선점 완료: {} 개 좌석", dto.getSeatIds().size());
    }

    /**
     * 사용자가 선점한 좌석들을 해제합니다.
     * 선점한 본인만 해제할 수 있으며, 해제된 좌석은 다시 예매 가능 상태가 됩니다.
     * 
     * @param dto 해제할 좌석 정보 (상영 ID, 좌석 ID 목록)
     * @param userInfo 사용자 인증 정보
     * @throws IllegalStateException 다른 고객이 선점한 좌석을 해제하려는 경우
     * @throws EntityNotFoundException 존재하지 않는 좌석인 경우
     */
    @Transactional
    public void releaseSeats(SeatRequestDto dto, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat.ScreeningSeatId id = new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId);
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다: " + seatId));
            
            // 본인이 선점한 좌석인지 확인
            boolean isOwnSeat = false;
            if ("customer".equals(userType)) {
                isOwnSeat = userId.equals(screeningSeat.getHoldCustomerId());
            } else {
                isOwnSeat = userId.equals(screeningSeat.getHoldGuestId());
            }
            
            if (!isOwnSeat) {
                throw new IllegalStateException("본인이 선점한 좌석이 아닙니다: " + seatId);
            }
            
            screeningSeat.setSeatStatus(SeatStatus.AVAILABLE);
            screeningSeat.setHoldExpiresAt(null);
            screeningSeat.setHoldCustomerId(null);
            screeningSeat.setHoldGuestId(null);
        }
    }

    /**
     * 결제 대기 상태의 예매를 생성합니다.
     * 선점된 좌석들을 바탕으로 예매를 생성하고, 사용된 포인트만큼 고객의 포인트를 차감합니다. -> 결제로 이동.
     * 실제 결제는 별도의 completeReservation 메소드를 통해 완료됩니다.
     * 
     * @param dto 예매 정보 (상영 ID, 티켓 정보, 사용 포인트)
     * @param userInfo 사용자 인증 정보
     * @return 생성된 예매 정보
     * @throws IllegalStateException 선점되지 않은 좌석을 예매하려는 경우
     * @throws EntityNotFoundException 고객, 상영, 가격 정보를 찾을 수 없는 경우
     */
    @Transactional
    public ReservationResponseDto createPendingReservation(ReservationRequestDto dto, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");

        Screening screening = screeningRepository.findByIdWithDetails(dto.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다: " + dto.getScreeningId()));

        // 티켓 유형별 인원수 검증
        int totalPersonCount = dto.getTicketTypes().stream()
                .mapToInt(ReservationRequestDto.TicketTypeInfo::getCount)
                .sum();
        
        if (totalPersonCount != dto.getSeatIds().size()) {
            throw new IllegalArgumentException("선택한 인원수(" + totalPersonCount + "명)와 좌석 수(" + dto.getSeatIds().size() + "개)가 일치하지 않습니다.");
        }

        // 모든 좌석이 해당 사용자가 선점한 좌석인지 확인
        for (Long seatId : dto.getSeatIds()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(dto.getScreeningId(), seatId))
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다: " + seatId));
            
            if (screeningSeat.getSeatStatus() != SeatStatus.HOLD) {
                throw new IllegalStateException("선점되지 않은 좌석을 예매할 수 없습니다: " + seatId);
            }
            
            // 본인이 선점한 좌석인지 확인
            boolean isOwnSeat = false;
            if ("customer".equals(userType)) {
                isOwnSeat = userId.equals(screeningSeat.getHoldCustomerId());
            } else {
                isOwnSeat = userId.equals(screeningSeat.getHoldGuestId());
            }
            
            if (!isOwnSeat) {
                throw new IllegalStateException("본인이 선점한 좌석이 아닙니다: " + seatId);
            }
        }

        // 총 금액 계산
        int totalAmount = 0;
        
        // 조조할인 적용 여부 확인 (06:00 ~ 11:00 상영 시작)
        boolean isEarlyBirdDiscount = false;
        int screeningHour = screening.getScreeningTime().getHour();
        if (screeningHour >= 6 && screeningHour < 11) {
            isEarlyBirdDiscount = true;
        }
        
        for (ReservationRequestDto.TicketTypeInfo ticketTypeInfo : dto.getTicketTypes()) {
            RoomTicketTypePrice priceInfo = roomTicketTypePriceRepository.findById(
                    new RoomTicketTypePrice.RoomTicketTypePriceId(screening.getScreeningRoom().getId(), ticketTypeInfo.getTicketTypeId())
            ).orElseThrow(() -> new EntityNotFoundException("해당 티켓 종류의 가격 정보를 찾을 수 없습니다."));
            
            int ticketPrice = priceInfo.getPrice();
            // 조조할인 적용 (좌석당 3000원 할인)
            if (isEarlyBirdDiscount) {
                ticketPrice = Math.max(0, ticketPrice - 3000); // 가격이 음수가 되지 않도록 보장
            }
            
            totalAmount += ticketPrice * ticketTypeInfo.getCount();
        }
        Customer customer = null;
        if ("customer".equals(userType)) {
            customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + userId));
        }

        // 예매 생성 (회원/게스트 구분)
        Reservation reservation;
        if ("customer".equals(userType)) {
            reservation = Reservation.builder()
                    .customer(customer)
                    .screeningId(dto.getScreeningId())
                    .totalAmount(totalAmount)
                    .reservationStatus(ReservationStatus.PENDING)
                    .build();
        } else {
            Guest guest = guestRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("게스트 정보를 찾을 수 없습니다: " + userId));
            reservation = Reservation.builder()
                    .guest(guest)
                    .screeningId(dto.getScreeningId())
                    .totalAmount(totalAmount)
                    .reservationStatus(ReservationStatus.PENDING)
                    .build();
        }

        // 티켓 생성 로직 - 좌석을 티켓 유형별로 순서대로 배정
        List<String> seatNumbers = new ArrayList<>();
        int seatIndex = 0;
        
        for (ReservationRequestDto.TicketTypeInfo ticketTypeInfo : dto.getTicketTypes()) {
            RoomTicketTypePrice priceInfo = roomTicketTypePriceRepository.findById(
                    new RoomTicketTypePrice.RoomTicketTypePriceId(screening.getScreeningRoom().getId(), ticketTypeInfo.getTicketTypeId())
            ).orElseThrow(() -> new EntityNotFoundException("가격 정보를 찾을 수 없습니다."));

            // 티켓 가격 계산 (조조할인 적용)
            int ticketPrice = priceInfo.getPrice();
            if (isEarlyBirdDiscount) {
                ticketPrice = Math.max(0, ticketPrice - 3000);
            }

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
                        .price(ticketPrice) // 할인된 가격 적용
                        .ticketStatus(TicketStatus.ISSUED)
                        .build();
                reservation.addTicket(ticket);
                seatNumbers.add(screeningSeat.getSeat().getSeatNumber());
            }
        }

        reservationRepository.save(reservation);
        log.info("결제 대기 상태의 예매 생성 완료: reservationId={}, 총 인원: {}명", reservation.getId(), totalPersonCount);

        return ReservationResponseDto.builder()
                .reservation(reservation)
                .movieTitle(screening.getMovie().getTitle())
                .seatNumbers(seatNumbers)
                .build();
    }

    /**
     * 결제를 완료하고 예매를 확정합니다.
     * 예매 상태를 COMPLETED로 변경하고, 좌석을 SOLD 상태로 변경하며,
     * 결제 정보를 저장하고 포인트 적립을 처리합니다.
     * 
     * @param reservationId 예매 ID
     * @param userInfo 사용자 인증 정보
     * @throws IllegalArgumentException 예약자 정보가 일치하지 않는 경우
     * @throws IllegalStateException 이미 처리되었거나 취소된 예약인 경우
     * @throws EntityNotFoundException 예약 정보를 찾을 수 없는 경우
     */
    @Transactional
    public void completeReservation(Long reservationId, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        log.info("결제 완료 처리 시작: reservationId={}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다: " + reservationId));

        // 예약자 정보 확인 (회원/게스트 구분)
        boolean isOwner = false;
        if ("customer".equals(userType)) {
            isOwner = reservation.getCustomer() != null && reservation.getCustomer().getId().equals(userId);
        } else {
            isOwner = reservation.getGuest() != null && reservation.getGuest().getId().equals(userId);
        }
        
        if (!isOwner) {
            throw new IllegalArgumentException("예약자 정보가 일치하지 않습니다.");
        }
        
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("이미 처리되었거나 취소된 예약입니다.");
        }

        reservation.updateReservationStatus(ReservationStatus.COMPLETED);

        // 티켓 상태 'ISSUED'로 변경 (이미 생성 시 ISSUED로 설정됨)
        // 좌석 상태 'SOLD'로 변경
        for (Ticket ticket : reservation.getTickets()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId()))
                    .orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다."));
            screeningSeat.setSeatStatus(SeatStatus.SOLD);
            screeningSeat.setHoldExpiresAt(null);
        }

        log.info("예매 완료 처리 성공: reservationId={}", reservation.getId());
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
     * @param userInfo 사용자 인증 정보
     * @return 예매 내역 목록
     */
    public List<ReservationResponseDto> getReservationsByCustomerId(Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        List<Reservation> reservations = reservationRepository.findByCustomerIdOrderByReservationDateDesc(userId);
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

    /**
     * 사용자의 예매 내역을 조회합니다. (과거/현재 구분, 회원/게스트 지원)
     */
    @Transactional
    public List<ReservationListResponseDto> getReservationList(Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        // 완료된 예매와 취소된 예매 조회 (회원/게스트 구분)
        List<Reservation> reservations;
        if ("customer".equals(userType)) {
            reservations = reservationRepository.findByCustomerIdAndReservationStatusInOrderByIdDesc(
                userId, List.of(ReservationStatus.COMPLETED, ReservationStatus.CANCELED));
        } else {
            reservations = reservationRepository.findByGuestIdAndReservationStatusInOrderByIdDesc(
                userId, List.of(ReservationStatus.COMPLETED, ReservationStatus.CANCELED));
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        return reservations.stream()
                .map(reservation -> {
                    Screening screening = screeningRepository.findByIdWithDetails(reservation.getScreeningId())
                            .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다."));
                    
                    // 좌석 번호 목록 생성
                    List<String> seatNumbers = reservation.getTickets().stream()
                            .map(ticket -> {
                                ScreeningSeat screeningSeat = screeningSeatRepository.findById(
                                    new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId())
                                ).orElse(null);
                                return screeningSeat != null ? screeningSeat.getSeat().getSeatNumber() : "정보없음";
                            })
                            .collect(Collectors.toList());
                    
                    // 최종 금액 계산
                    int usedPoints = reservation.getPayment() != null ? reservation.getPayment().getUsedPointAmount() : 0;
                    int finalAmount = reservation.getTotalAmount() - usedPoints;
                    
                    // 상영 완료 여부 확인 (상영 종료 시간 기준)
                    LocalDateTime screeningEndTime = screening.getScreeningTime().plusMinutes(screening.getMovie().getDuration());
                    boolean isScreeningCompleted = now.isAfter(screeningEndTime);
                    
                    return new ReservationListResponseDto(
                        reservation.getId(),
                        screening.getMovie().getTitle(),
                        screening.getMovie().getPosterUrl(),
                        screening.getScreeningTime(),
                        screening.getScreeningRoom().getRoomName(),
                        seatNumbers,
                        reservation.getReservationDate(),
                        reservation.getReservationStatus().name(),
                        Objects.requireNonNull(reservation.getPayment()).getPaymentStatus().getValue(),
                        finalAmount,
                        isScreeningCompleted
                    );
                })
                .sorted((a, b) -> b.getScreeningTime().compareTo(a.getScreeningTime())) // 상영일 기준 최신순
                .collect(Collectors.toList());
    }

    /**
     * 예매 상세 정보를 조회합니다.
     */
    @Transactional
    public ReservationDetailResponseDto getReservationDetail(Long reservationId, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        // 회원/게스트 구분하여 조회
        Reservation reservation;
        if ("customer".equals(userType)) {
            reservation = reservationRepository.findByIdAndCustomerIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다."));
        } else {
            reservation = reservationRepository.findByIdAndGuestIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다."));
        }
        
        Screening screening = screeningRepository.findByIdWithDetails(reservation.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다."));
        
        // 좌석 번호 목록 생성
        List<String> seatNumbers = reservation.getTickets().stream()
                .map(ticket -> {
                    ScreeningSeat screeningSeat = screeningSeatRepository.findById(
                        new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId())
                    ).orElse(null);
                    return screeningSeat != null ? screeningSeat.getSeat().getSeatNumber() : "정보없음";
                })
                .collect(Collectors.toList());
        
        // 상영 종료 시간 계산
        LocalDateTime screeningEndTime = screening.getScreeningTime().plusMinutes(screening.getMovie().getDuration());
        
        // 결제 정보
        Payment payment = reservation.getPayment();
        int usedPoints = payment != null ? payment.getUsedPointAmount() : 0;
        int finalAmount = reservation.getTotalAmount() - usedPoints;
        String paymentMethod = payment != null ? payment.getPaymentMethod().getValue() : "정보없음";
        LocalDateTime paymentCompletedAt = payment != null ? payment.getApprovedAt() : null;
        
        return new ReservationDetailResponseDto(
            reservation.getId(),
            reservation.getReservationDate(),
            reservation.getReservationStatus().name(),
            Objects.requireNonNull(payment).getPaymentStatus().getValue(),
            screening.getMovie().getTitle(),
            screening.getMovie().getPosterUrl(),
            screening.getMovie().getMovieRating().getRatingName(),
            screening.getScreeningTime(),
            screeningEndTime,
            screening.getScreeningRoom().getRoomName(),
            seatNumbers,
            reservation.getTotalAmount(),
            usedPoints,
            finalAmount,
            paymentMethod,
            paymentCompletedAt
        );
    }

    /**
     * 티켓 정보를 조회합니다.
     */
    public TicketResponseDto getTicket(Long reservationId, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");
        
        // 회원/게스트 구분하여 조회
        Reservation reservation;
        if ("customer".equals(userType)) {
            reservation = reservationRepository.findByIdAndCustomerIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다."));
        } else {
            reservation = reservationRepository.findByIdAndGuestIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다."));
        }
        
        Screening screening = screeningRepository.findByIdWithDetails(reservation.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다."));
        
        // 좌석 번호들을 문자열로 합치기
        String seats = reservation.getTickets().stream()
                .map(ticket -> {
                    ScreeningSeat screeningSeat = screeningSeatRepository.findById(
                        new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId())
                    ).orElse(null);
                    return screeningSeat != null ? screeningSeat.getSeat().getSeatNumber() : "정보없음";
                })
                .collect(Collectors.joining(", "));
        
        // 상영 종료 시간 계산
        LocalDateTime screeningEndTime = screening.getScreeningTime().plusMinutes(screening.getMovie().getDuration());
        
        // 예매자 이름 (회원/게스트 구분)
        String reserverName;
        if (reservation.getCustomer() != null) {
            reserverName = reservation.getCustomer().getName();
        } else {
            reserverName = reservation.getGuest().getName();
        }
        
        return new TicketResponseDto(
            reservation.getId(),
            screening.getMovie().getTitle(),
            screening.getMovie().getPosterUrl(),
            screening.getMovie().getMovieRating().getRatingName(),
            screening.getScreeningTime(),
            screeningEndTime,
            screening.getScreeningRoom().getRoomName(),
            seats,
            reservation.getTickets().size(),
            reserverName,
            reservation.getReservationDate(),
            reservation.getReservationStatus().name()
        );
    }

    /**
     * 예매를 취소합니다.
     * 상영 시작 10분 후까지만 취소 가능하며, 취소 시 좌석을 해제하고 포인트를 환불합니다.
     * 
     * @param dto 취소할 예매 ID, 환불 사유 dto
     * @param userInfo 사용자 인증 정보
     * @throws IllegalArgumentException 예약자 정보가 일치하지 않는 경우
     * @throws IllegalStateException 취소 불가능한 상태인 경우
     * @throws EntityNotFoundException 예약 정보를 찾을 수 없는 경우
     */
    @Transactional
    public void cancelReservation(CancelReservationRequestDto dto, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");

        Long reservationId = dto.getReservationId();
        
        log.info("예매 취소 요청: reservationId={}, userType={}, userId={}", reservationId, userType, userId);
        
        // 예매 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다: " + reservationId));
        
        // 예약자 정보 확인 (회원/게스트 구분)
        boolean isOwner = false;
        if ("customer".equals(userType)) {
            isOwner = reservation.getCustomer() != null && reservation.getCustomer().getId().equals(userId);
        } else {
            isOwner = reservation.getGuest() != null && reservation.getGuest().getId().equals(userId);
        }
        
        if (!isOwner) {
            throw new IllegalArgumentException("예약자 정보가 일치하지 않습니다.");
        }
        
        // 취소 가능한 상태 확인
        if (reservation.getReservationStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        
        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalStateException("완료된 예매만 취소할 수 있습니다.");
        }
        
        // 상영 정보 조회
        Screening screening = screeningRepository.findByIdWithDetails(reservation.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("상영 정보를 찾을 수 없습니다."));
        
        // 취소 가능 시간 확인 (상영 시작 10분 후까지)
        LocalDateTime cancelDeadline = screening.getScreeningTime().plusMinutes(10);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(cancelDeadline)) {
            throw new IllegalStateException("상영 시작 10분 후에는 취소할 수 없습니다.");
        }
        
        // 예매 상태를 CANCELED로 변경
        reservation.updateReservationStatus(ReservationStatus.CANCELED);
        
        // 결제 취소 메서드 호출
        paymentService.refundPayment(reservationId, dto.getRefundReason(), userInfo);

        // 좌석 상태를 AVAILABLE로 변경
        for (Ticket ticket : reservation.getTickets()) {
            ScreeningSeat screeningSeat = screeningSeatRepository.findById(
                new ScreeningSeat.ScreeningSeatId(ticket.getScreeningId(), ticket.getSeatId())
            ).orElseThrow(() -> new EntityNotFoundException("좌석 정보를 찾을 수 없습니다."));
            
            screeningSeat.setSeatStatus(SeatStatus.AVAILABLE);
            screeningSeat.setHoldExpiresAt(null);
            screeningSeat.setHoldCustomerId(null);
            screeningSeat.setHoldGuestId(null);
        }
        
        log.info("예매 취소 완료: reservationId={}", reservationId);
    }
}