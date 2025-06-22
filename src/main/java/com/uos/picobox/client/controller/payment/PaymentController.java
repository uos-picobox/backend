package com.uos.picobox.client.controller.payment;

import com.uos.picobox.domain.payment.dto.BeforePaymentRequestDto;
import com.uos.picobox.domain.payment.dto.BeforePaymentResponseDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentRequestDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentResponseDto;
import com.uos.picobox.domain.payment.service.PaymentService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "07. 회원/비회원 - 결제", description = "결제 전 정보 저장, 결제 후 Confirm 요청 API (회원/게스트 모두 이용 가능)")
@RestController
@RequestMapping("/api/protected/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "결제 전 정보 저장", description = "결제 전 결제 정보를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "결제 정보 저장에 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (잘못된 가격 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "예매 또는 할인 정보 없음")
    })
    @PostMapping("/before")
    public ResponseEntity<?> saveBeforePaymentInfo(
            @Valid @RequestBody BeforePaymentRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId
            ) {
        BeforePaymentResponseDto response = paymentService.registerBeforePaymentInfo(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 후 결제 Confirm", description = "결제 승인을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "결제 승인에 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (잘못된 가격 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "예매 또는 할인 정보 없음")
    })
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication
    ) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        ConfirmPaymentResponseDto response = paymentService.confirmPayment(dto, sessionInfo);
        return ResponseEntity.ok(response);
    }
}
