package com.uos.picobox.admin.controller.movie;

import com.uos.picobox.domain.movie.dto.actor.ActorRequestDto;
import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.service.ActorService;
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

@Tag(name = "관리자 - 02. 배우 관리", description = "배우 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/actors")
@RequiredArgsConstructor
public class AdminActorController {

    private final ActorService actorService;

    @Operation(summary = "배우 정보 등록", description = "새로운 배우 정보를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배우 정보가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 배우 정보)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<ActorResponseDto> createActor(
            @Valid @RequestBody ActorRequestDto actorRequestDto) {
        ActorResponseDto responseDto = actorService.registerActor(actorRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "배우 전체 목록 조회", description = "시스템에 등록된 모든 배우 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<List<ActorResponseDto>> getAllActors() {
        List<ActorResponseDto> actors = actorService.findAllActors();
        return ResponseEntity.ok(actors);
    }

    @Operation(summary = "특정 배우 정보 조회", description = "ID로 특정 배우의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/get/{actorId}")
    public ResponseEntity<ActorResponseDto> getActorById(
            @Parameter(description = "조회할 배우 ID", required = true, example = "1")
            @PathVariable Long actorId) {
        ActorResponseDto actor = actorService.findActorById(actorId);
        return ResponseEntity.ok(actor);
    }

    @Operation(summary = "배우 정보 수정", description = "기존 배우 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 배우 정보)."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PutMapping("/update/{actorId}")
    public ResponseEntity<ActorResponseDto> updateActor(
            @Parameter(description = "수정할 배우 ID", required = true, example = "1")
            @PathVariable Long actorId,
            @Valid @RequestBody ActorRequestDto actorRequestDto) {
        ActorResponseDto updatedActor = actorService.editActor(actorId, actorRequestDto);
        return ResponseEntity.ok(updatedActor);
    }

    @Operation(summary = "배우 정보 삭제", description = "배우 정보를 시스템에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "배우 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 해당 배우가 출연한 영화가 있는 경우)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @DeleteMapping("/delete/{actorId}")
    public ResponseEntity<Void> deleteActor(
            @Parameter(description = "삭제할 배우 ID", required = true, example = "1")
            @PathVariable Long actorId) {
        actorService.removeActor(actorId);
        return ResponseEntity.noContent().build();
    }
}