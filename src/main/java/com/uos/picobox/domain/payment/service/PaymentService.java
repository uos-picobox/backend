package com.uos.picobox.domain.payment.service;

import com.uos.picobox.domain.payment.dto.BeforePaymentRequestDto;
import com.uos.picobox.domain.payment.dto.BeforePaymentResponseDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentRequestDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentResponseDto;
import com.uos.picobox.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public BeforePaymentResponseDto registerBeforePaymentInfo(BeforePaymentRequestDto paymentRequestDto) {

    }

    @Transactional
    public ConfirmPaymentResponseDto confirmPayment(ConfirmPaymentRequestDto paymentRequestDto) {

    }

    public ConfirmPaymentResponseDto findPaymentHistoryByReservationId(Long reservationId) {

    }

    public ConfirmPaymentResponseDto findPaymentHistoryByCustomerId(Long customerId) {

    }

    public ConfirmPaymentResponseDto findPaymentHistoryByGuestId(Long guestId) {

    }
}
