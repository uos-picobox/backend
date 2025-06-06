package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.actor.ActorRequestDto;
import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.entity.Actor;
import com.uos.picobox.domain.movie.repository.ActorRepository;
import com.uos.picobox.global.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActorService {

    private final ActorRepository actorRepository;
    private final S3Service s3Service; // S3Service 주입

    @Transactional
    public ActorResponseDto registerActor(ActorRequestDto actorRequestDto, MultipartFile profileImageFile) {
        LocalDate birthDate = actorRequestDto.getBirthDate();

        if (birthDate != null) {
            actorRepository.findByNameAndBirthDate(actorRequestDto.getName(), birthDate)
                    .ifPresent(a -> {
                        throw new IllegalArgumentException("이미 동일한 이름과 생년월일의 배우가 존재합니다: " + actorRequestDto.getName());
                    });
        }

        String profileS3Url = null;
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                profileS3Url = s3Service.upload(profileImageFile, "actor-profiles");
            } catch (IOException | SdkException e) {
                log.error("ActorService: 프로필 이미지 업로드 실패.", e);
                throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        Actor actor = Actor.builder()
                .name(actorRequestDto.getName())
                .birthDate(birthDate)
                .biography(actorRequestDto.getBiography())
                .profileImageUrl(profileS3Url)
                .build();
        Actor savedActor = actorRepository.save(actor);
        return new ActorResponseDto(savedActor);
    }

    @Transactional
    public ActorResponseDto editActor(Long actorId, ActorRequestDto actorRequestDto, MultipartFile profileImageFile) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));

        LocalDate newBirthDate = actorRequestDto.getBirthDate();

        boolean nameChanged = !actor.getName().equals(actorRequestDto.getName());
        boolean birthDateChanged = actor.getBirthDate() == null && newBirthDate != null || actor.getBirthDate() != null && !actor.getBirthDate().equals(newBirthDate);

        if ((nameChanged || birthDateChanged) && newBirthDate != null) {
            actorRepository.findByNameAndBirthDate(actorRequestDto.getName(), newBirthDate)
                    .filter(existingActor -> !existingActor.getId().equals(actorId))
                    .ifPresent(a -> {
                        throw new IllegalArgumentException("수정하려는 이름과 생년월일이 다른 배우와 중복됩니다.");
                    });
        }

        actor.updateDetails(
                actorRequestDto.getName(),
                newBirthDate,
                actorRequestDto.getBiography()
        );

        String currentProfileImageUrl = actor.getProfileImageUrl();
        String finalProfileS3Url = currentProfileImageUrl;

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            if (StringUtils.hasText(currentProfileImageUrl)) {
                try {
                    s3Service.delete(currentProfileImageUrl);
                } catch (SdkException e) {
                    log.error("ActorService: 프로필 이미지 삭제 실패. URL: " + currentProfileImageUrl, e);
                }
            }
            try {
                finalProfileS3Url = s3Service.upload(profileImageFile, "actor-profiles");
            } catch (IOException | SdkException e) {
                log.error("ActorService: 프로필 이미지 수정 업로드 실패.", e);
                throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
        actor.updateProfileImageUrl(finalProfileS3Url);

        actorRepository.save(actor);
        return new ActorResponseDto(actor);
    }

    /**
     * 특정 배우의 프로필 이미지만 업로드/변경 또는 삭제합니다.
     * 파일이 제공되면 업로드/변경하고, 파일이 제공되지 않으면 기존 이미지를 삭제하고 URL을 null로 설정합니다.
     */
    @Transactional
    public ActorResponseDto uploadOrUpdateActorProfileImage(Long actorId, MultipartFile profileImageFile) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));

        String newProfileS3Url = null;

        if (StringUtils.hasText(actor.getProfileImageUrl())) {
            try {
                s3Service.delete(actor.getProfileImageUrl());
            } catch (SdkException e) {
                log.error("ActorService: 프로필 이미지 삭제 실패. URL: " + actor.getProfileImageUrl(), e);
            }
        }

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                newProfileS3Url = s3Service.upload(profileImageFile, "actor-profiles");
            } catch (IOException | SdkException e) {
                log.error("ActorService: 프로필 이미지 전용 업로드 실패 (actorId: {}).", actorId, e);
                throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        actor.updateProfileImageUrl(newProfileS3Url);
        actorRepository.save(actor);
        return new ActorResponseDto(actor);
    }

    @Transactional
    public void removeActor(Long actorId, boolean force) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));

        if (!force) {
            List<String> associatedMovies = actorRepository.findMovieTitlesByActorId(actorId);
            if (!associatedMovies.isEmpty()) {
                String movies = String.join(", ", associatedMovies);
                throw new IllegalArgumentException(
                        String.format("해당 배우가 출연한 영화가 있어 삭제할 수 없습니다. (배우 ID: %d, 영화: [%s]). 강제 삭제를 원하시면 'force=true' 파라미터를 사용하세요.", actorId, movies)
                );
            }
        }

        if (StringUtils.hasText(actor.getProfileImageUrl())) {
            try {
                s3Service.delete(actor.getProfileImageUrl());
            } catch (SdkException e) {
                log.error("ActorService: 프로필 이미지 삭제 실패. URL: " + actor.getProfileImageUrl(), e);
            }
        }

        try {
            actorRepository.delete(actor);
            log.info("배우 정보가 성공적으로 삭제되었습니다. ID: {}, 강제삭제: {}", actorId, force);
        } catch (DataIntegrityViolationException e) {
            log.error("배우 삭제 중 예상치 못한 DataIntegrityViolationException 발생 (ID: {}): {}", actorId, e.getMessage());
            throw new IllegalStateException("배우 삭제 중 데이터 무결성 문제가 발생했습니다. (ID: " + actorId + ")", e);
        }
    }

    public List<ActorResponseDto> findAllActors() {
        return actorRepository.findAll().stream()
                .map(ActorResponseDto::new)
                .collect(Collectors.toList());
    }

    public ActorResponseDto findActorById(Long actorId) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));
        return new ActorResponseDto(actor);
    }
}