package com.uos.picobox.user.controller;

import com.uos.picobox.global.service.EmailService;
import com.uos.picobox.global.utils.PasswordUtils;
import com.uos.picobox.user.dto.*;
import com.uos.picobox.user.service.CustomerInfoService;
import com.uos.picobox.user.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "00. 회원 - 회원 정보", description = "회원 정보 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/customer/find")
@RequiredArgsConstructor
public class FindLoginInfoController {
    private final CustomerInfoService customerInfoService;
    private final SignupService signupService;
    private final EmailService emailService;
    private final PasswordUtils passwordUtils;

    @Operation(summary = "아이디 찾기 - 이메일 인증 코드 전송", description = "아이디 찾기를 위한 이메일 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 인증 메일을 전송했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/login-id/verify/email")
    public ResponseEntity<?> verifyEmailForFindLoginId(
            @RequestBody
            @Valid
            FindLoginIdRequestDto dto) throws MessagingException {
        if (!customerInfoService.existsEmailAndName(dto.getEmail(), dto.getName())) {
            throw new IllegalArgumentException("존재하지 않는 이메일, 이름 정보입니다.");
        }
        emailService.sendAuthMail(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "아이디 찾기 - 이메일 인증 코드 인증", description = "아이디 찾기를 위한 이메일 인증 코드를 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "올바른 이메일 인증 코드입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일, 인증 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/login-id/auth/email")
    public ResponseEntity<?> authEmailForFindLoginId(
            @RequestBody
            @Valid
            AuthMailRequestDto authMailRequestDto) {
        emailService.checkAuthCode(authMailRequestDto);
        AuthMailForLoginIdResponseDto response = customerInfoService.findLoginIdByEmail(authMailRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "비밀번호 재설정 - 이메일 인증 코드 전송", description = "비밀번호 재설정을 위한 이메일 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 인증 메일을 전송했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/password/verify/email")
    public ResponseEntity<?> verifyEmailForFindPassword(
            @RequestBody
            @Valid
            FindPasswordRequestDto dto) throws MessagingException {
        if (!customerInfoService.existsLoginIdAndEmail(dto.getLoginId(), dto.getEmail())) {
            throw new IllegalArgumentException("존재하지 않는 로그인ID, 이메일 정보입니다.");
        }
        emailService.sendAuthMail(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "비밀번호 재설정 - 이메일 인증 코드 인증", description = "비밀번호 재설정을 위한 이메일 인증 코드를 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "올바른 이메일 인증 코드입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일, 인증 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/password/auth/email")
    public ResponseEntity<?> authEmailForFindPassword(
            @RequestBody
            @Valid
            AuthMailRequestDto authMailRequestDto) {
        if (signupService.isEmailAvailable(authMailRequestDto.getEmail())) {
            throw new IllegalArgumentException("존재하지 않는 이메일 정보입니다.");
        }
        emailService.checkAuthCode(authMailRequestDto);
        AuthMailForPasswordResponseDto response = passwordUtils.setAuthCode(authMailRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "비밀번호 재설정 - 재설정 비밀번호 입력", description = "재설정할 비밀번호를 입력합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정이 성공적으로 처리되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일, 인증 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody
            @Valid
            ResetPasswordRequestDto dto) {
        ResetPasswordResponseDto response = customerInfoService.resetPassword(dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
