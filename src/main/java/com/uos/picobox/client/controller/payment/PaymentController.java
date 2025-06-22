package com.uos.picobox.client.controller.payment;

import com.uos.picobox.domain.payment.dto.BeforePaymentRequestDto;
import com.uos.picobox.domain.payment.dto.BeforePaymentResponseDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentRequestDto;
import com.uos.picobox.domain.payment.dto.ConfirmPaymentResponseDto;
import com.uos.picobox.domain.payment.service.PaymentService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @Operation(summary = "결제 전 정보 저장", description = "결제 전 결제 정보를 저장합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 정보 저장에 성공했습니다."),
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

    @Operation(summary = "결제 후 결제 Confirm", description = "결제 승인을 요청합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 승인에 성공했습니다."),
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

    @Operation(summary = "예매 결제 내역 조회", description = "예매에 대한 결제 내역을 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 내역 조회에 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "예매 정보 없음")
    })
    @GetMapping("/get")
    public ResponseEntity<?> findPaymentInfoByReservationId(
            @RequestParam
            @NotNull(message = "예약 ID는 필수입니다.")
            @Schema(description = "결제를 완료할 예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long reservationId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId
    ) {
        ConfirmPaymentResponseDto response = paymentService.findPaymentHistoryByReservationId(reservationId);
        return ResponseEntity.ok(response);
    }
}
