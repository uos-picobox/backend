package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.distributor.DistributorRequestDto;
import com.uos.picobox.domain.movie.dto.distributor.DistributorResponseDto;
import com.uos.picobox.domain.movie.entity.Distributor;
import com.uos.picobox.domain.movie.repository.DistributorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistributorService {

    private final DistributorRepository distributorRepository;

    @Transactional
    public DistributorResponseDto registerDistributor(DistributorRequestDto requestDto) {
        distributorRepository.findByName(requestDto.getName())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("이미 존재하는 배급사명입니다: " + requestDto.getName());
                });

        Distributor distributor = Distributor.builder()
                .name(requestDto.getName())
                .address(requestDto.getAddress())
                .phone(requestDto.getPhone())
                .build();
        Distributor savedDistributor = distributorRepository.save(distributor);
        return new DistributorResponseDto(savedDistributor);
    }

    public List<DistributorResponseDto> findAllDistributors() {
        return distributorRepository.findAll().stream()
                .map(DistributorResponseDto::new)
                .collect(Collectors.toList());
    }

    public DistributorResponseDto findDistributorById(Long distributorId) {
        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배급사를 찾을 수 없습니다: " + distributorId));
        return new DistributorResponseDto(distributor);
    }

    @Transactional
    public DistributorResponseDto editDistributor(Long distributorId, DistributorRequestDto requestDto) {
        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 배급사를 찾을 수 없습니다: " + distributorId));

        if (!distributor.getName().equals(requestDto.getName())) {
            distributorRepository.findByName(requestDto.getName())
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("이미 존재하는 배급사명입니다: " + requestDto.getName());
                    });
        }

        distributor.updateDetails(
                requestDto.getName(),
                requestDto.getAddress(),
                requestDto.getPhone()
        );
        return new DistributorResponseDto(distributor);
    }

    @Transactional
    public void removeDistributor(Long distributorId) {
        if (!distributorRepository.existsById(distributorId)) {
            throw new EntityNotFoundException("해당 ID의 배급사를 찾을 수 없습니다: " + distributorId);
        }
        distributorRepository.deleteById(distributorId);
    }
}