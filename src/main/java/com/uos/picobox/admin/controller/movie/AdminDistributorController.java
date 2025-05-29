package com.uos.picobox.admin.controller.movie;

import com.uos.picobox.domain.movie.dto.distributor.DistributorRequestDto;
import com.uos.picobox.domain.movie.dto.distributor.DistributorResponseDto;
import com.uos.picobox.domain.movie.service.DistributorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 03. 영화 배급사 관리", description = "영화 배급사 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/distributors")
@RequiredArgsConstructor
public class AdminDistributorController {

    private final DistributorService distributorService;

    @Operation(summary = "영화 배급사 등록", description = "새로운 영화 배급사를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배급사가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 배급사명)."),
    })
    @PostMapping("/create")
    public ResponseEntity<DistributorResponseDto> createDistributor(
            @Valid @RequestBody DistributorRequestDto requestDto) {
        DistributorResponseDto responseDto = distributorService.registerDistributor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 배급사 전체 목록 조회", description = "등록된 모든 영화 배급사 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배급사 목록이 성공적으로 조회되었습니다."),
    })
    @GetMapping("/get")
    public ResponseEntity<List<DistributorResponseDto>> getAllDistributors() {
        List<DistributorResponseDto> distributors = distributorService.findAllDistributors();
        return ResponseEntity.ok(distributors);
    }

    @Operation(summary = "특정 영화 배급사 조회", description = "ID로 특정 영화 배급사 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배급사 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 배급사를 찾을 수 없습니다."),
    })
    @GetMapping("/get/{distributorId}")
    public ResponseEntity<DistributorResponseDto> getDistributorById(
            @Parameter(description = "조회할 배급사 ID", required = true, example = "1")
            @PathVariable Long distributorId) {
        DistributorResponseDto distributor = distributorService.findDistributorById(distributorId);
        return ResponseEntity.ok(distributor);
    }

    @Operation(summary = "영화 배급사 수정", description = "기존 영화 배급사 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배급사 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 배급사명)."),
            @ApiResponse(responseCode = "404", description = "해당 배급사를 찾을 수 없습니다."),
    })
    @PutMapping("/update/{distributorId}")
    public ResponseEntity<DistributorResponseDto> updateDistributor(
            @Parameter(description = "수정할 배급사 ID", required = true, example = "1")
            @PathVariable Long distributorId,
            @Valid @RequestBody DistributorRequestDto requestDto) {
        DistributorResponseDto updatedDistributor = distributorService.editDistributor(distributorId, requestDto);
        return ResponseEntity.ok(updatedDistributor);
    }

    @Operation(summary = "영화 배급사 삭제", description = "영화 배급사 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "배급사 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 배급사를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 해당 배급사를 사용하는 영화가 있는 경우)."),
    })
    @DeleteMapping("/delete/{distributorId}")
    public ResponseEntity<Void> deleteDistributor(
            @Parameter(description = "삭제할 배급사 ID", required = true, example = "1")
            @PathVariable Long distributorId) {
        distributorService.removeDistributor(distributorId);
        return ResponseEntity.noContent().build();
    }
}