package com.example.api.service;

import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 로직
 */
@Service
public class KafkaRedisAddApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;
    private final AppliedUserRepository appliedUserRepository;

    public KafkaRedisAddApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository, CouponCreateProducer couponCreateProducer, AppliedUserRepository appliedUserRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.appliedUserRepository = appliedUserRepository;
    }

    public void apply(Long userId) {
        Long apply = appliedUserRepository.add(userId);
        if (apply != 1) { // 이미 발급 요청을 했던 유저
            return;
        }

        // 발급된 쿠폰의 갯수를 증가시키고,
        Long count = couponCountRepository.increment();

        // 발급된 쿠폰의 갯수가 발급 가능한 갯수를 초과하는 경우, 발급하지 않는다.
        if (count > 100) {
            return;
        }

        // couponCreateProducer 를 사용해서 topic에 userId를 전송하도록 변경
        couponCreateProducer.create(userId);
    }
}
