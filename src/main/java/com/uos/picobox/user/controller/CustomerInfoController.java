package com.uos.picobox.user.controller;

import com.uos.picobox.global.service.EmailService;
import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.*;
import com.uos.picobox.user.service.CustomerInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "00. 회원 - 회원 정보", description = "회원 정보 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/protected/customer")
@RequiredArgsConstructor
public class CustomerInfoController {
    private final CustomerInfoService customerInfoService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "내 정보 조회", description = "회원 자신의 정보를 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @GetMapping("/get")
    public ResponseEntity<?> getCustomerInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long customerId = sessionUtils.findCustomerIdByAuthentication(authentication);
        CustomerInfoResponseDto response = customerInfoService.findCustomerInfoById(customerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 정보 수정", description = "회원 자신의 정보를 수정합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateCustomerInfo(
            @Valid @RequestBody CustomerInfoRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId) {
        CustomerInfoResponseDto response = customerInfoService.updateCustomerInfo(dto);
        return ResponseEntity.ok(response);
    }
}
