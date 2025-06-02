package com.uos.picobox.domain.ticket.service;

import com.uos.picobox.domain.price.repository.ScreeningSeatTypePriceRepository;
import com.uos.picobox.domain.ticket.dto.TicketTypeRequestDto;
import com.uos.picobox.domain.ticket.dto.TicketTypeResponseDto;
import com.uos.picobox.domain.ticket.entity.TicketType;
import com.uos.picobox.domain.ticket.repository.TicketTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final ScreeningSeatTypePriceRepository screeningSeatTypePriceRepository;

    @Transactional
    public TicketTypeResponseDto registerTicketType(TicketTypeRequestDto requestDto) {
        ticketTypeRepository.findByTypeName(requestDto.getTypeName())
                .ifPresent(tt -> {
                    throw new DataIntegrityViolationException("이미 존재하는 티켓 종류 이름입니다: " + requestDto.getTypeName());
                });

        TicketType ticketType = TicketType.builder()
                .typeName(requestDto.getTypeName())
                .description(requestDto.getDescription())
                .build();
        TicketType savedTicketType = ticketTypeRepository.save(ticketType);
        log.info("새로운 티켓 종류가 등록되었습니다: {}", savedTicketType.getTypeName());
        return new TicketTypeResponseDto(savedTicketType);
    }

    public List<TicketTypeResponseDto> findAllTicketTypes() {
        return ticketTypeRepository.findAll().stream()
                .map(TicketTypeResponseDto::new)
                .collect(Collectors.toList());
    }

    public TicketTypeResponseDto findTicketTypeById(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 티켓 종류를 찾을 수 없습니다: " + ticketTypeId));
        return new TicketTypeResponseDto(ticketType);
    }

    @Transactional
    public TicketTypeResponseDto editTicketType(Long ticketTypeId, TicketTypeRequestDto requestDto) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 티켓 종류를 찾을 수 없습니다: " + ticketTypeId));

        if (!ticketType.getTypeName().equals(requestDto.getTypeName())) {
            ticketTypeRepository.findByTypeName(requestDto.getTypeName())
                    .ifPresent(tt -> {
                        throw new DataIntegrityViolationException("이미 존재하는 티켓 종류 이름입니다: " + requestDto.getTypeName());
                    });
        }

        ticketType.updateDetails(requestDto.getTypeName(), requestDto.getDescription());
        log.info("티켓 종류 정보가 수정되었습니다: ID {}", ticketTypeId);
        return new TicketTypeResponseDto(ticketType);
    }

    @Transactional
    public void removeTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 티켓 종류를 찾을 수 없습니다: " + ticketTypeId));

        if (screeningSeatTypePriceRepository.existsByTicketTypeId(ticketTypeId)) {
            throw new IllegalStateException(
                    String.format("티켓 종류 '%s'(ID: %d)는 현재 가격 정책에서 사용 중이므로 삭제할 수 없습니다. 연결된 가격 설정을 먼저 삭제해주세요.",
                            ticketType.getTypeName(), ticketTypeId)
            );
        }
        ticketTypeRepository.delete(ticketType);
        log.info("티켓 종류가 삭제되었습니다: ID {}", ticketTypeId);
    }
}