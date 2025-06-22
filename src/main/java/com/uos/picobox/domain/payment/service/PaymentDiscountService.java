package com.uos.picobox.domain.payment.service;

import com.uos.picobox.domain.payment.dto.EditPaymentDiscountRequestDto;
import com.uos.picobox.domain.payment.dto.PaymentDiscountRequestDto;
import com.uos.picobox.domain.payment.dto.PaymentDiscountResponseDto;
import com.uos.picobox.domain.payment.entity.PaymentDiscount;
import com.uos.picobox.domain.payment.repository.PaymentDiscountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentDiscountService {
    private final PaymentDiscountRepository paymentDiscountRepository;

    @Transactional
    public PaymentDiscountResponseDto registerPaymentDiscount(PaymentDiscountRequestDto dto) {
        if (dto.getProviderName() == null || dto.getProviderName().isEmpty()) {
            throw new IllegalArgumentException("할인 제공자 이름은 필수 항목입니다.");
        }
        if (dto.getDiscountRate() == null && dto.getDiscountAmount() == null) {
            throw new IllegalArgumentException("할인율과 할인금액 중 하나는 설정해야 합니다.");
        }
        if (dto.getDiscountRate() != null && dto.getDiscountAmount() != null) {
            throw new IllegalArgumentException("할인율과 할인금액 중 한 가지만 설정할 수 있습니다");
        }
        if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
            throw new IllegalArgumentException("할인 설명은 필수항목입니다.");
        }
        PaymentDiscount paymentDiscount = dto.toEntity();
        paymentDiscount = paymentDiscountRepository.save(paymentDiscount);
        return new PaymentDiscountResponseDto(paymentDiscount);
    }

    @Transactional
    public PaymentDiscountResponseDto updatePaymentDiscount(EditPaymentDiscountRequestDto dto) {
         PaymentDiscount paymentDiscount = paymentDiscountRepository.findById(dto.getId()).orElseThrow(() ->
                 new EntityNotFoundException("해당 할인 정보를 찾을 수 없습니다."));
         if (dto.getProviderName() != null && !dto.getProviderName().isEmpty()) {
            paymentDiscount.setProviderName(dto.getProviderName());
         }
        if (dto.getDiscountRate() != null && dto.getDiscountAmount() != null) {
            throw new IllegalArgumentException("할인율과 할인금액 중 한 가지만 설정할 수 있습니다");
        }
         if (dto.getDiscountRate() != null && dto.getDiscountAmount() == null) {
             paymentDiscount.setDiscountRate(dto.getDiscountRate());
             paymentDiscount.setDiscountAmount(null);
         }
         if (dto.getDiscountAmount() != null && dto.getDiscountRate() == null) {
             paymentDiscount.setDiscountAmount(dto.getDiscountAmount());
             paymentDiscount.setDiscountRate(null);
         }
         if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
             paymentDiscount.setDescription(dto.getDescription());
         }
         return new PaymentDiscountResponseDto(paymentDiscount);
    }

    @Transactional
    public void deletePaymentDiscount(long id) {
        if (!paymentDiscountRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 할인 Id입니다.");
        }
        paymentDiscountRepository.deleteById(id);
    }

    public PaymentDiscountResponseDto[] getPaymentDiscounts() {
        List<PaymentDiscount> paymentDiscounts = paymentDiscountRepository.findAll();
        List<PaymentDiscountResponseDto> dtos = paymentDiscounts.stream()
                .map(PaymentDiscountResponseDto::new).toList();
        return dtos.toArray(new PaymentDiscountResponseDto[0]);
    }
}
