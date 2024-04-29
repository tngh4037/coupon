package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 로직
 */
@Service
public class KafkaApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;

    public KafkaApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository, CouponCreateProducer couponCreateProducer) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
    }

    public void apply(Long userId) {
        // 발급된 쿠폰의 갯수를 증가시키고,
        Long count = couponCountRepository.increment();

        // 발급된 쿠폰의 갯수가 발급 가능한 갯수를 초과하는 경우, 발급하지 않는다.
        if (count > 100) {
            return;
        }

        // 기존에 직접 쿠폰을 생성하던 로직을 삭제하고, couponCreateProducer 를 사용해서 topic에 userId를 전송하도록 변경
        // couponRepository.save(new Coupon(userId));
        couponCreateProducer.create(userId);
    }
}
