package com.example.api.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * redis 명령어를 실행할 repository
 */
@Repository
public class CouponCountRepository {

    private final RedisTemplate<String, String> redisTemplate; // redis 명령어를 실행할 수 있어야 하므로 RedisTemplate 을 변수로 추가해준다.

    public CouponCountRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // redis 의 incr 명령어를 사용하기 위한 메서드
    public Long increment() {
        return redisTemplate
                .opsForValue()
                .increment("coupon_count");
    }
}
