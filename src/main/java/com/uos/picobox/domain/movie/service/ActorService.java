package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.actor.ActorRequestDto;
import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.entity.Actor;
import com.uos.picobox.domain.movie.repository.ActorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActorService {

    private final ActorRepository actorRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 문자열 형식의 생년월일을 LocalDate 객체로 변환합니다.
     * @param birthDateStr yyyy-MM-dd 형식의 생년월일 문자열
     * @return 변환된 LocalDate 객체, 또는 입력 문자열이 비어있거나 null이면 null
     * @throws IllegalArgumentException 날짜 형식이 올바르지 않을 경우
     */
    private LocalDate parseBirthDate(String birthDateStr) {
        if (StringUtils.hasText(birthDateStr)) {
            try {
                return LocalDate.parse(birthDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다 (yyyy-MM-dd): " + birthDateStr, e);
            }
        }
        return null;
    }

    @Transactional
    public ActorResponseDto registerActor(ActorRequestDto actorRequestDto) {
        LocalDate birthDate = parseBirthDate(actorRequestDto.getBirthDate());

        if (birthDate != null) {
            actorRepository.findByNameAndBirthDate(actorRequestDto.getName(), birthDate)
                    .ifPresent(a -> {
                        throw new IllegalArgumentException("이미 동일한 이름과 생년월일의 배우가 존재합니다: " + actorRequestDto.getName());
                    });
        }

        Actor actor = Actor.builder()
                .name(actorRequestDto.getName())
                .birthDate(birthDate)
                .biography(actorRequestDto.getBiography())
                .build();
        Actor savedActor = actorRepository.save(actor);
        return new ActorResponseDto(savedActor);
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

    @Transactional
    public ActorResponseDto editActor(Long actorId, ActorRequestDto actorRequestDto) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));

        LocalDate newBirthDate = parseBirthDate(actorRequestDto.getBirthDate());

        boolean nameChanged = !actor.getName().equals(actorRequestDto.getName());
        boolean birthDateChanged = (actor.getBirthDate() == null && newBirthDate != null) ||
                (actor.getBirthDate() != null && !actor.getBirthDate().equals(newBirthDate));

        if ((nameChanged || birthDateChanged) && newBirthDate != null) {
            actorRepository.findByNameAndBirthDate(actorRequestDto.getName(), newBirthDate)
                    .filter(existingActor -> !existingActor.getId().equals(actorId))
                    .ifPresent(a -> {
                        throw new IllegalArgumentException("수정하려는 이름과 생년월일이 다른 배우와 중복됩니다.");
                    });
        }

        actor.updateDetails(actorRequestDto.getName(), newBirthDate, actorRequestDto.getBiography());
        return new ActorResponseDto(actor);
    }

    @Transactional
    public void removeActor(Long actorId) {
        if (!actorRepository.existsById(actorId)) {
            throw new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId);
        }
        actorRepository.deleteById(actorId);
    }
}