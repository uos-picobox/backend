package com.uos.picobox.global.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentUtils {
    private final CacheManager cacheManager;

    public void saveAmount(String orderId, Integer amount) {
        log.info("Saving OrderId" + orderId);
        Cache cache = cacheManager.getCache("payment");
        Objects.requireNonNull(cache).put(orderId, amount);
    }

    public boolean compareAmount(String orderId, Integer amount) {
        log.info("Request OrderId" + orderId);
        Cache cache = cacheManager.getCache("payment");
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(orderId);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("잘못된 orderId이거나 이미 만료된 주문입니다.");
        }
        Object raw = wrapper.get();
        if (raw instanceof Integer ) {
            return ((Integer) raw).equals(amount);
        }
        else {
            throw new IllegalArgumentException("결제 전 amount 정보를 찾을 수 없습니다.");
        }
    }

    public void evictPayment(String orderId) {
        Cache cache = cacheManager.getCache("payment");
        Objects.requireNonNull(cache).evict(orderId);
    }
}
