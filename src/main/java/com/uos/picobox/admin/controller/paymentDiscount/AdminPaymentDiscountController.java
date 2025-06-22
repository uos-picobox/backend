package com.uos.picobox.admin.controller.paymentDiscount;

import com.uos.picobox.domain.payment.dto.EditPaymentDiscountRequestDto;
import com.uos.picobox.domain.payment.dto.PaymentDiscountRequestDto;
import com.uos.picobox.domain.payment.dto.PaymentDiscountResponseDto;
import com.uos.picobox.domain.payment.service.PaymentDiscountService;
import com.uos.picobox.domain.price.dto.PriceSettingRequestDto;
import com.uos.picobox.domain.price.dto.PriceSettingResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 11. 결제 할인 정보 관리", description = "결제 할인 정보 CUD API")
@RestController
@RequestMapping("/api/admin/payment-discount")
@RequiredArgsConstructor
public class AdminPaymentDiscountController {
    private final PaymentDiscountService paymentDiscountService;

    @Operation(summary = "할인 정보 등록", description = "할인 정보를 등록합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "할인 정보가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerPaymentDiscount(
            @Valid @RequestBody PaymentDiscountRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId
            ) {
        PaymentDiscountResponseDto response = paymentDiscountService.registerPaymentDiscount(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할인 정보 수정", description = "할인 정보를 수정합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "할인 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updatePaymentDiscount(
            @Valid @RequestBody EditPaymentDiscountRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId
    ) {
        PaymentDiscountResponseDto response = paymentDiscountService.updatePaymentDiscount(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할인 정보 삭제", description = "할인 정보를 삭제합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "할인 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @DeleteMapping("/delete/{paymentDiscountId}")
    public ResponseEntity<?> deletePaymentDiscount(
            @Parameter(description = "결제 할인 ID", required = true, example = "4") @PathVariable Long paymentDiscountId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId
    ) {
        paymentDiscountService.deletePaymentDiscount(paymentDiscountId);
        return ResponseEntity.ok().build();
    }
}
