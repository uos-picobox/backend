package com.uos.picobox.user.controller;

import com.uos.picobox.user.dto.SignupRequestDto;
import com.uos.picobox.user.dto.SignupResponseDto;
import com.uos.picobox.user.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 가입", description = "회원가입, 로그인 아이디 중복 검사, 이메일 중복 검사, 이메일 인증 코드 전송, 이메일 인증 코드 인증, 아이디/비밀번호 찾기, 비회원 로그인, 로그아웃")
@RestController
@RequestMapping("/api/user/signup")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 회원이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 유효성 검사 실패, 중복된 회원 정보)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        if (!signupRequestDto.getPassword().equals(signupRequestDto.getRepeatPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        SignupResponseDto response = signupService.registerCustomer(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
