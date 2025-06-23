package com.uos.picobox.admin.service;

import com.uos.picobox.admin.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDeleteService {

    private final AdminRepository adminRepository;

    @Transactional
    public void deleteAdminById(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new EntityNotFoundException("존재하지 않는 관리자 ID입니다.");
        }
        adminRepository.deleteById(adminId);
    }

    public Long findAdminIdByLoginId(String loginId) {
        return adminRepository.findIdByLoginId(loginId);
    }
}
