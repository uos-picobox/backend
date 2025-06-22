package com.uos.picobox.client.controller.paymentDiscount;

import com.uos.picobox.domain.payment.dto.PaymentDiscountResponseDto;
import com.uos.picobox.domain.payment.service.PaymentDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "07. 회원/비회원 - 결제", description = "결제 전 정보 저장, 결제 후 Confirm 요청 API (회원/게스트 모두 이용 가능)")
@RestController
@RequestMapping("/api/payment-discount")
@RequiredArgsConstructor
public class PaymentDiscountController {
    private final PaymentDiscountService paymentDiscountService;

    @Operation(summary = "할인 정보 조회", description = "할인 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "할인 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @GetMapping("/get")
    public ResponseEntity<?> findPaymentDiscount() {
        PaymentDiscountResponseDto[] response = paymentDiscountService.getPaymentDiscounts();
        return ResponseEntity.ok(response);
    }
}
