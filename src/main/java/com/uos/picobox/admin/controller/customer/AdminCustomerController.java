package com.uos.picobox.admin.controller.customer;

import com.uos.picobox.admin.dto.CustomerManagementResponseDto;
import com.uos.picobox.admin.dto.CustomerStatusUpdateRequestDto;
import com.uos.picobox.admin.service.AdminCustomerManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 11. 회원 정보 관리", description = "관리자용 회원 정보 조회 및 상태 관리 기능")
@Slf4j
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerManagementService adminCustomerManagementService;

    @Operation(summary = "전체 회원 목록 조회", description = "모든 회원을 조회합니다. 활성 상태별 필터링 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요")
    })
    @GetMapping
    public ResponseEntity<List<CustomerManagementResponseDto>> getAllCustomers(
            @Parameter(description = "정렬 기준 (registeredAt: 가입일순, name: 이름순, lastLoginAt: 최근 로그인순, points: 포인트순)", example = "registeredAt")
            @RequestParam(defaultValue = "registeredAt") String sort,
            @Parameter(description = "활성 상태 필터 (true: 활성 회원만, false: 정지 회원만, null: 전체)", example = "true")
            @RequestParam(required = false) Boolean isActive) {
        
        log.info("관리자 전체 회원 조회 요청: sort={}, isActive={}", sort, isActive);
        List<CustomerManagementResponseDto> customers = adminCustomerManagementService.getAllCustomers(sort, isActive);
        
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "회원 상세 정보 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerManagementResponseDto> getCustomerDetail(
            @Parameter(description = "회원 ID", required = true) @PathVariable Long customerId) {
        
        log.info("관리자 회원 상세 조회 요청: customerId={}", customerId);
        CustomerManagementResponseDto customer = adminCustomerManagementService.getCustomerDetail(customerId);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "회원 상태 변경", description = "회원의 활성/정지 상태를 변경합니다. isActive를 false로 설정하면 정지 처리됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PatchMapping("/{customerId}/status")
    public ResponseEntity<Void> updateCustomerStatus(
            @Parameter(description = "회원 ID", required = true) @PathVariable Long customerId,
            @Valid @RequestBody CustomerStatusUpdateRequestDto request) {
        
        log.info("관리자 회원 상태 변경 요청: customerId={}, isActive={}", customerId, request.getIsActive());
        adminCustomerManagementService.updateCustomerStatus(customerId, request);
        log.info("관리자 회원 상태 변경 완료: customerId={}", customerId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 ID로 회원 검색", description = "로그인 ID를 포함하는 회원들을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요")
    })
    @GetMapping("/search/login-id")
    public ResponseEntity<List<CustomerManagementResponseDto>> searchCustomersByLoginId(
            @Parameter(description = "검색할 로그인 ID", required = true) @RequestParam String loginId) {
        
        log.info("관리자 회원 로그인ID 검색 요청: loginId={}", loginId);
        List<CustomerManagementResponseDto> customers = adminCustomerManagementService.searchCustomersByLoginId(loginId);
        
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "이름으로 회원 검색", description = "이름을 포함하는 회원들을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요")
    })
    @GetMapping("/search/name")
    public ResponseEntity<List<CustomerManagementResponseDto>> searchCustomersByName(
            @Parameter(description = "검색할 이름", required = true) @RequestParam String name) {
        
        log.info("관리자 회원 이름 검색 요청: name={}", name);
        List<CustomerManagementResponseDto> customers = adminCustomerManagementService.searchCustomersByName(name);
        
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "이메일로 회원 검색", description = "이메일을 포함하는 회원들을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요")
    })
    @GetMapping("/search/email")
    public ResponseEntity<List<CustomerManagementResponseDto>> searchCustomersByEmail(
            @Parameter(description = "검색할 이메일", required = true) @RequestParam String email) {
        
        log.info("관리자 회원 이메일 검색 요청: email={}", email);
        List<CustomerManagementResponseDto> customers = adminCustomerManagementService.searchCustomersByEmail(email);
        
        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }
} 