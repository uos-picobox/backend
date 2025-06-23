package com.uos.picobox.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uos.picobox.domain.payment.dto.*;
import com.uos.picobox.domain.payment.entity.Payment;
import com.uos.picobox.domain.payment.entity.PaymentDiscount;
import com.uos.picobox.domain.payment.entity.Refund;
import com.uos.picobox.domain.payment.repository.PaymentDiscountRepository;
import com.uos.picobox.domain.payment.repository.PaymentRepository;
import com.uos.picobox.domain.payment.repository.RefundRepository;
import com.uos.picobox.domain.point.entity.PointHistory;
import com.uos.picobox.domain.point.repository.PointHistoryRepository;
import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.global.enumClass.PaymentStatus;
import com.uos.picobox.global.enumClass.PointChangeType;
import com.uos.picobox.global.utils.PaymentUtils;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.entity.Guest;
import com.uos.picobox.user.repository.CustomerRepository;
import com.uos.picobox.user.repository.GuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentDiscountRepository paymentDiscountRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PaymentUtils paymentUtils;
    private final GuestRepository guestRepository;
    @Value("${TOSS_API_SECRET_KEY}")
    private String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RefundRepository refundRepository;

    @Transactional
    public BeforePaymentResponseDto registerBeforePaymentInfo(BeforePaymentRequestDto paymentRequestDto) {
        Reservation reservation = reservationRepository.findById(paymentRequestDto.getReservationId()).orElseThrow(() ->
                new EntityNotFoundException("존재하지 않는 예약 ID입니다."));
        Long paymentDiscountId = paymentRequestDto.getPaymentDiscountId();
        if (paymentDiscountId != null && !paymentDiscountRepository.existsById(paymentDiscountId)) {
            throw new EntityNotFoundException("존재하지 않는 할인 ID입니다.");
        }
        if (paymentRequestDto.getFinalAmount() > paymentRequestDto.getAmount()) {
            throw new IllegalArgumentException("최종 결제 금액이 총 결제 금액보다 높습니다.");
        }

        Payment payment = paymentRequestDto.toEntity(reservation);
        payment = paymentRepository.save(payment);

        // 결제 전 orderId, amount 저장
        paymentUtils.saveAmount(payment.getOrderId(), payment.getFinalAmount());

        if (paymentDiscountId != null) {
            PaymentDiscount paymentDiscount = paymentDiscountRepository.findById(paymentDiscountId).orElseThrow(() ->
                    new EntityNotFoundException("존재하지 않는 할인 ID입니다."));
            return BeforePaymentResponseDto.builder()
                    .payment(payment)
                    .paymentDiscountInfo(new PaymentDiscountResponseDto(paymentDiscount))
                    .build();
        }
        else {
            return BeforePaymentResponseDto.builder()
                    .payment(payment)
                    .build();
        }
    }

    @Transactional
    public ConfirmPaymentResponseDto confirmPayment(ConfirmPaymentRequestDto paymentRequestDto, Map<String, Object> userInfo) {
        log.info("Confirm payment request: {}", paymentRequestDto);
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");

        String orderId = paymentRequestDto.getOrderId();
        Integer finalAmount = paymentRequestDto.getFinalAmount();

        // 결제 후 orderId, amount 비교
        if (!paymentUtils.compareAmount(orderId, finalAmount)) {
            throw new IllegalArgumentException("결제 전과 결제 후 결제 금액이 다릅니다.");
        }
        else {
            paymentUtils.evictPayment(orderId);
        }

        Payment payment = paymentRepository.findById(paymentRequestDto.getPaymentId()).orElseThrow(() ->
            new EntityNotFoundException("올바르지 못한 paymentId입니다."));
        payment.setPaymentKey(paymentRequestDto.getPaymentKey());

        // 결제 confirm
        try {
            log.info("secretKey: "+ secretKey);
            String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
            log.info("encodedAuth: "+ encodedAuth);

            URI uri = new URI("https://api.tosspayments.com/v1/payments/confirm");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String requestJson = String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%d}",
                    paymentRequestDto.getPaymentKey(), paymentRequestDto.getOrderId(), paymentRequestDto.getFinalAmount());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            // 결제 성공 로직
            if (responseCode == 200) {
                InputStream inputStream = conn.getInputStream();

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }
                JsonNode responseJson = objectMapper.readTree(responseBuilder.toString());

                // payment 테이블 update.
                PaymentStatus paymentStatus = PaymentStatus.valueOf(responseJson.get("status").asText());
                OffsetDateTime approvedOffset = OffsetDateTime.parse(responseJson.get("approvedAt").asText());
                LocalDateTime approvedAt = approvedOffset.toLocalDateTime();
                OffsetDateTime requestedOffset = OffsetDateTime.parse(responseJson.get("requestedAt").asText());
                LocalDateTime requestedAt = requestedOffset.toLocalDateTime();
                payment.updateStatus(paymentStatus);
                payment.setApprovedAt(approvedAt);
                payment.updateRequestedAt(requestedAt);

                // 포인트 사용 (회원만 가능)
                Customer customer = null;
                if ("customer".equals(userType)) {
                    customer = customerRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + userId));
                    customer.usePoints(payment.getUsedPointAmount());
                } else if (payment.getUsedPointAmount() > 0) {
                    throw new IllegalArgumentException("게스트는 포인트를 사용할 수 없습니다.");
                }

                // 포인트 사용 내역 저장 (회원만 가능)
                if (payment.getUsedPointAmount() > 0 && "customer".equals(userType)) {
                    PointHistory pointHistory = PointHistory.builder()
                            .customer(customer)
                            .changeType(PointChangeType.USED)
                            .amount(payment.getUsedPointAmount())
                            .relatedReservationId(payment.getReservation().getId())
                            .build();
                    pointHistoryRepository.save(pointHistory);
                }

                Long paymentDiscountId = payment.getPaymentDiscountId();
                if (paymentDiscountId != null) {
                    PaymentDiscount paymentDiscount = paymentDiscountRepository.findById(paymentDiscountId).orElseThrow(() ->
                            new EntityNotFoundException("존재하지 않는 할인 ID입니다."));
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .paymentDiscountInfo(new PaymentDiscountResponseDto(paymentDiscount))
                            .build();
                }
                else {
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .build();
                }
            }
            // 결제 실패 로직
            else {
                PaymentStatus failedStatus = PaymentStatus.ABORTED;
                payment.updateStatus(failedStatus);
                log.error("Confirm Failed" + conn.getResponseMessage());
                throw new RuntimeException("Toss 결제 승인 요청 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            log.error("Confirm Exception" + e.getMessage());
            throw new RuntimeException("Toss 결제 승인 요청 중 오류가 발생했습니다.");
        }
    }

    public ConfirmPaymentResponseDto findPaymentHistoryByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId).orElseThrow(() ->
                new EntityNotFoundException("해당 예매에 대한 결제 정보가 없습니다."));
        if (payment.getPaymentDiscountId() != null) {
            Optional<PaymentDiscount> option = paymentDiscountRepository.findById(payment.getPaymentDiscountId());
            if (option.isPresent()) {
                PaymentDiscount paymentDiscount = option.get();
                return ConfirmPaymentResponseDto.builder()
                        .payment(payment)
                        .paymentDiscountInfo(new PaymentDiscountResponseDto(paymentDiscount))
                        .build();
            }
            else {
                return ConfirmPaymentResponseDto.builder()
                        .payment(payment)
                        .build();
            }
        }
        else {
            return ConfirmPaymentResponseDto.builder()
                    .payment(payment)
                    .build();
        }
    }
    public ConfirmPaymentResponseDto[] findPaymentHistoryByUser(Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");

        if ("customer".equals(userType)) {
            Customer customer = customerRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
            List<Payment> paymentList = reservationRepository.findPaymentsByCustomer(customer);
            List<ConfirmPaymentResponseDto> dto = paymentList.stream().map(payment -> {
                Long paymentDiscountId = payment.getPaymentDiscountId();
                if (paymentDiscountId != null) {
                    PaymentDiscount paymentDiscount = paymentDiscountRepository.findById(paymentDiscountId).orElseThrow(()
                    -> new EntityNotFoundException("할인 정보를 찾을 수 없습니다."));
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .paymentDiscountInfo(new PaymentDiscountResponseDto(paymentDiscount))
                            .build();
                }
                else {
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .build();
                }
            }).toList();
            return dto.toArray(new ConfirmPaymentResponseDto[0]);
        }
        else if ("guest".equals(userType)) {
            Guest guest = guestRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException("비회원 정보를 찾을 수 없습니다."));
            List<Payment> paymentList = reservationRepository.findPaymentsByGuest(guest);
            List<ConfirmPaymentResponseDto> dto = paymentList.stream().map(payment -> {
                Long paymentDiscountId = payment.getPaymentDiscountId();
                if (paymentDiscountId != null) {
                    PaymentDiscount paymentDiscount = paymentDiscountRepository.findById(paymentDiscountId).orElseThrow(()
                            -> new EntityNotFoundException("할인 정보를 찾을 수 없습니다."));
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .paymentDiscountInfo(new PaymentDiscountResponseDto(paymentDiscount))
                            .build();
                }
                else {
                    return ConfirmPaymentResponseDto.builder()
                            .payment(payment)
                            .build();
                }
            }).toList();
            return dto.toArray(new ConfirmPaymentResponseDto[0]);
        }
        else {
            throw new AccessDeniedException("회원, 비회원이 아닌 사용자는 결제 정보를 조회할 수 없습니다.");
        }
    }

    @Transactional
    public void refundPayment(Long reservationId, String refundReason, Map<String, Object> userInfo) {
        String userType = (String) userInfo.get("type");
        Long userId = (Long) userInfo.get("id");

        // paymentStatus 업데이트
        Payment payment = paymentRepository.findByReservationId(reservationId).orElseThrow(() ->
                new EntityNotFoundException("해당 예매에 대한 결제 정보를 찾을 수 없습니다."));
        payment.updateStatus(PaymentStatus.REFUNDED);

        // 회원일 경우, 포인트 원복
        if ("customer".equals(userType)) {
            List<PointHistory> pointHistoryList = pointHistoryRepository.findAllByRelatedReservationId(reservationId);
            for (PointHistory pointHistory : pointHistoryList) {
                Customer customer = pointHistory.getCustomer();
                Integer amount = pointHistory.getAmount();
                PointChangeType pointChangeType = pointHistory.getChangeType();
                // 사용 시 포인트 다시 원복.
                if (pointChangeType == PointChangeType.USED) {
                    customer.addPoints(amount);
                    PointHistory refundPointHistory = PointHistory.builder()
                            .customer(customer)
                            .changeType(PointChangeType.REFUNDED)
                            .amount(amount)
                            .relatedReservationId(reservationId)
                            .build();
                    pointHistoryRepository.save(refundPointHistory);
                }
            }
        }
        // 비회원일 경우, 포인트 적용 x.

        // 환불 레코드 등록
        Refund refund = Refund.builder()
                .payment(payment)
                .refundAmount(payment.getFinalAmount())
                .refundReason(refundReason)
                .build();
        refundRepository.save(refund);
    }
}
