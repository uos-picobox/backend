package com.uos.picobox.admin.controller.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uos.picobox.domain.movie.dto.actor.ActorRequestDto;
import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.service.ActorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "관리자 - 02. 배우 관리", description = "배우 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/actors")
@RequiredArgsConstructor
public class AdminActorController {

    private final ActorService actorService;

    @Operation(summary = "배우 정보 및 프로필 이미지 동시 등록",
            description = "새로운 배우 정보(JSON 'actorDetails')와 프로필 이미지 파일('profileImage')을 한 번의 요청으로 등록합니다. (Content-Type: multipart/form-data)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배우 정보와 이미지가 성공적으로 등록되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 배우 등)."),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 파일 업로드 실패입니다.")
    })
    @PostMapping(value = "/create-with-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ActorResponseDto> createActorWithImage(
            @Parameter(name = "actorDetails", required = true,
                    description = """
                        배우 상세 정보 (JSON 형식)
                    
                        예시:
                        ```json
                        {
                          "name": "정우성",
                          "birthDate": "1972-04-22",
                          "biography": "대한민국 배우입니다."
                        }
                        ```
                        """,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorRequestDto.class)))
            @Valid @RequestPart("actorDetails") String actorDetailsJson,
            @Parameter(name = "profileImage", description = "프로필 이미지 파일 (선택 사항)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImageFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ActorRequestDto actorRequestDto = objectMapper.readValue(actorDetailsJson, ActorRequestDto.class);
        ActorResponseDto responseDto = actorService.registerActor(actorRequestDto, profileImageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "배우 정보 등록 (메타데이터 전용)",
            description = "새로운 배우 정보(JSON)만 시스템에 등록합니다. 프로필 이미지는 null로 설정되며, '/{actorId}/profile-image' API로 별도 업로드해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "배우 정보(메타데이터)가 성공적으로 등록되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 배우 등).")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ActorResponseDto> createActorMetadata(
            @Valid @RequestBody ActorRequestDto actorRequestDto) {
        ActorResponseDto responseDto = actorService.registerActor(actorRequestDto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "배우 정보 및 프로필 이미지 동시 수정",
            description = "기존 배우 정보(JSON 'actorDetails')를 수정하고, 프로필 이미지 파일('profileImage')도 함께 교체합니다. (Content-Type: multipart/form-data)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보와 이미지가 성공적으로 수정되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 배우 등)."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 파일 업로드 실패입니다.")
    })
    @PutMapping(value = "/{actorId}/update-with-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ActorResponseDto> updateActorWithImage(
            @Parameter(description = "수정할 배우 ID", required = true) @PathVariable Long actorId,
            @Parameter(name = "actorDetails", required = true, description = "수정할 배우 상세 정보 (JSON 형식)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorRequestDto.class)))
            @Valid @RequestPart("actorDetails") ActorRequestDto actorRequestDto,
            @Parameter(name = "profileImage", description = "새 프로필 이미지 파일 (선택 사항, 기존 이미지 교체)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImageFile) {
        ActorResponseDto updatedActor = actorService.editActor(actorId, actorRequestDto, profileImageFile);
        return ResponseEntity.ok(updatedActor);
    }

    @Operation(summary = "배우 정보 수정 (메타데이터 전용)",
            description = "기존 배우 정보(JSON)만 수정합니다. 프로필 이미지는 이 API로 변경되지 않으며, '/{actorId}/profile-image' API를 사용해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보(메타데이터)가 성공적으로 수정되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 배우 등)."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다.")
    })
    @PutMapping(value = "/{actorId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ActorResponseDto> updateActorMetadata(
            @Parameter(description = "수정할 배우 ID", required = true) @PathVariable Long actorId,
            @Valid @RequestBody ActorRequestDto actorRequestDto) {
        ActorResponseDto updatedActor = actorService.editActor(actorId, actorRequestDto, null);
        return ResponseEntity.ok(updatedActor);
    }

    @Operation(summary = "배우 프로필 이미지 업로드/변경/삭제 (이미지 전용)",
            description = "특정 배우의 프로필 이미지를 업로드하거나 변경합니다. 기존 이미지가 있으면 덮어쓰기 됩니다. 파일을 보내지 않으면 기존 이미지가 삭제되고 URL이 null로 설정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지가 성공적으로 업로드/변경/삭제되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "파일 처리 중 오류가 발생했습니다.")
    })
    @PutMapping(value = "/{actorId}/profile-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ActorResponseDto> uploadOrUpdateActorProfileImage(
            @Parameter(description = "프로필 이미지를 업로드/변경/삭제할 배우 ID", required = true) @PathVariable Long actorId,
            @Parameter(name = "profileImage", description = "프로필 이미지 파일. 파일을 보내지 않으면 기존 이미지가 삭제됩니다.",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImageFile) {
        ActorResponseDto actorResponseDto = actorService.uploadOrUpdateActorProfileImage(actorId, profileImageFile);
        return ResponseEntity.ok(actorResponseDto);
    }

    @Operation(summary = "배우 전체 목록 조회", description = "시스템에 등록된 모든 배우 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 목록이 성공적으로 조회되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class)))
    })
    @GetMapping("/get")
    public ResponseEntity<List<ActorResponseDto>> getAllActors() {
        List<ActorResponseDto> actors = actorService.findAllActors();
        return ResponseEntity.ok(actors);
    }

    @Operation(summary = "특정 배우 정보 조회", description = "ID로 특정 배우의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보가 성공적으로 조회되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다.")
    })
    @GetMapping("/get/{actorId}")
    public ResponseEntity<ActorResponseDto> getActorById(
            @Parameter(description = "조회할 배우 ID", required = true) @PathVariable Long actorId) {
        ActorResponseDto actor = actorService.findActorById(actorId);
        return ResponseEntity.ok(actor);
    }

    @Operation(summary = "배우 정보 삭제", description = "배우 정보를 삭제합니다. S3의 프로필 이미지도 함께 삭제됩니다. 'force=true' 쿼리 파라미터를 사용하여 출연 영화가 있어도 강제 삭제할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "배우 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 출연 영화가 있어 삭제 불가하나 강제 옵션 미사용)."),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다.")
    })
    @DeleteMapping("/delete/{actorId}")
    public ResponseEntity<Void> deleteActor(
            @Parameter(description = "삭제할 배우 ID", required = true) @PathVariable Long actorId,
            @Parameter(description = "강제 삭제 여부. 출연한 영화가 있어도 삭제하려면 true로 설정.", required = false)
            @RequestParam(defaultValue = "false") boolean force) {
        actorService.removeActor(actorId, force);
        return ResponseEntity.noContent().build();
    }
}