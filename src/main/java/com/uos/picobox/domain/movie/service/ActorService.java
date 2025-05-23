package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.actor.ActorRequestDto;
import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.entity.Actor;
import com.uos.picobox.domain.movie.repository.ActorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * 배우 정보를 삭제합니다.
     * 이 배우가 출연한 영화가 있다면, 관련 영화 제목 목록과 함께 예외를 발생시킵니다.
     * 강제 삭제 파라미터(force)가 true이면, 관련 영화가 있어도 삭제를 진행합니다 (ON DELETE CASCADE).
     * @param actorId 삭제할 배우 ID
     * @param force 강제 삭제 여부 (true이면 출연 영화가 있어도 삭제)
     * @throws EntityNotFoundException 해당 ID의 배우가 없을 경우
     * @throws IllegalArgumentException 배우가 출연 영화가 있고 force=false일 경우
     * @throws DataIntegrityViolationException DB 레벨의 다른 무결성 제약 조건 위반 시
     */
    @Transactional
    public void removeActor(Long actorId, boolean force) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배우를 찾을 수 없습니다: " + actorId));

        List<String> associatedMovies = actorRepository.findMovieTitlesByActorId(actorId);

        if (!associatedMovies.isEmpty() && !force) {
            String movies = String.join(", ", associatedMovies);
            throw new IllegalArgumentException(
                    String.format("해당 배우가 출연한 영화가 있어 삭제할 수 없습니다. (배우 ID: %d, 영화: [%s]). 강제 삭제를 원하시면 'force=true' 파라미터를 사용하세요.", actorId, movies)
            );
        }

        // force가 true이거나, associatedMovies가 비어있으면 삭제 진행
        // ON DELETE CASCADE에 의해 MOVIE_CAST 테이블의 관련 레코드는 자동으로 삭제됩니다.
        try {
            actorRepository.delete(actor);
            log.info("배우 정보가 성공적으로 삭제되었습니다. ID: {}, 강제삭제: {}", actorId, force);
        } catch (DataIntegrityViolationException e) {
            log.error("배우 삭제 중 예상치 못한 DataIntegrityViolationException 발생 (ID: {}): {}", actorId, e.getMessage());
            throw new IllegalStateException("배우 삭제 중 데이터 무결성 문제가 발생했습니다. (ID: " + actorId + ")", e);
        }
    }
}