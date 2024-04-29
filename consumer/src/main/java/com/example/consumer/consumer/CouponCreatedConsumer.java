package com.example.consumer.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.repository.CouponRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 토픽에 전송된 데이터를 가져오기 위한 컨슈머 작업
 */
@Component
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;

    public CouponCreatedConsumer(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // 데이터를 가져오기 위한 메서드
    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {
        couponRepository.save(new Coupon(userId));
    }
}

// kafka 를 사용하면 api에서 직접 쿠폰을 생성할 때에 비해서, 처리량을 조절할 수 있게된다.
// 처리량을 조절함에 따라서 DB에 부하를 줄일 수 있다는 장점이 있다.
// 다만, 쿠폰 생성까지 약간의 텀이 발생할 수 있는 단점은 있다.
