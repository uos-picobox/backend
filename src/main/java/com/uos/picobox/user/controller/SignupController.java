package com.uos.picobox.user.controller;

import com.uos.picobox.user.dto.AuthMailRequestDto;
import com.uos.picobox.user.dto.MailRequestDto;
import com.uos.picobox.user.dto.SignupRequestDto;
import com.uos.picobox.user.dto.SignupResponseDto;
import com.uos.picobox.global.service.EmailService;
import com.uos.picobox.user.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//"회원가입, 로그인 아이디 중복 검사, 이메일 중복 검사, 이메일 인증 코드 전송, 이메일 인증 코드 인증"
@Tag(name = "00. 회원 - 회원 가입", description = "회원가입 관련 API를 제공합니다.")

@RestController
@RequestMapping("/api/signup/customer")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;
    private final EmailService emailService;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 회원이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 회원 정보)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        if (!signupRequestDto.getPassword().equals(signupRequestDto.getRepeatPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        SignupResponseDto response = signupService.registerCustomer(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Validated
    @Operation(summary = "로그인ID 중복 검사", description = "로그인ID 중복 여부를 검사합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용할 수 있는 로그인ID입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 로그인ID)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/check/loginid")
    public ResponseEntity<?> checkLoginId(
            @RequestParam
            @NotBlank
            @Size(min = 1, max = 12, message = "1~12자 사이여야 합니다.")
            @Schema(description = "로그인 아이디", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
            String loginId) {
        boolean response = signupService.isLoginIdAvailable(loginId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Validated
    @Operation(summary = "이메일 중복 검사", description = "이메일 중복 여부를 검사합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용할 수 있는 이메일입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/check/email")
    public ResponseEntity<?> checkEmail(
            @RequestParam
            @NotBlank
            @Email(message = "이메일 형식이어야 합니다.")
            @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
            String email) {
        boolean response = signupService.isEmailAvailable(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "이메일 인증 코드 전송", description = "이메일 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 인증 메일을 전송했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/verify/email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody
            @Valid
            MailRequestDto mailRequestDto) throws MessagingException {
        if (!signupService.isEmailAvailable(mailRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.");
        }
        emailService.sendAuthMail(mailRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "이메일 인증 코드 인증", description = "이메일 인증 코드를 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "올바른 이메일 인증 코드입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 이메일, 인증 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/auth/email")
    public ResponseEntity<?> authEmail(
            @RequestBody
            @Valid
            AuthMailRequestDto authMailRequestDto) {
        if (!signupService.isEmailAvailable(authMailRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.");
        }
        emailService.checkAuthCode(authMailRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
