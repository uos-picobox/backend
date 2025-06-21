package com.uos.picobox.global.utils;

import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SessionUtil {
    
    private final CacheManager cacheManager;
    private final CustomerRepository customerRepository;
    
    /**
     * 세션 ID로부터 로그인 ID를 조회합니다.
     * @param sessionId 세션 ID
     * @return 로그인 ID
     * @throws IllegalArgumentException 유효하지 않은 세션인 경우
     */
    public String getLoginIdFromSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("세션 ID가 제공되지 않았습니다.");
        }
        
        Cache cache = cacheManager.getCache("customerSession");
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(sessionId);
        
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 세션입니다.");
        }
        
        return (String) wrapper.get();
    }
    
    /**
     * 세션 ID로부터 고객 정보를 검증하고 고객 ID를 반환합니다.
     * @param sessionId 세션 ID
     * @return 고객 ID
     */
    public Long getCustomerIdFromSession(String sessionId) {
        String loginId = getLoginIdFromSession(sessionId);
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + loginId));
        return customer.getId();
    }
    
    /**
     * 세션 ID로부터 로그인 ID를 반환합니다.
     * @param sessionId 세션 ID
     * @return 로그인 ID
     */
    public String getCustomerLoginIdFromSession(String sessionId) {
        return getLoginIdFromSession(sessionId);
    }
} 