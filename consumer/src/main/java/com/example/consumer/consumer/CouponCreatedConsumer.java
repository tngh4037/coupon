package com.example.consumer.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.FailedEvent;
import com.example.consumer.repository.CouponRepository;
import com.example.consumer.repository.FailedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 토픽에 전송된 데이터를 가져오기 위한 컨슈머 작업
 */
@Component
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;
    private final FailedEventRepository failedEventRepository;
    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    public CouponCreatedConsumer(CouponRepository couponRepository,
                                 FailedEventRepository failedEventRepository) {
        this.couponRepository = couponRepository;
        this.failedEventRepository = failedEventRepository;
    }

    // 데이터를 가져오기 위한 메서드
    @KafkaListener(topics = "coupon_create", groupId = "group_1")
    public void listener(Long userId) {
        try {
            couponRepository.save(new Coupon(userId));
        } catch (Exception e) {
            logger.error("failed to create coupon :: " + userId);
            failedEventRepository.save(new FailedEvent(userId)); // 이후 배치 프로그램 등에서 FailedEvent 에 쌓인 데이터들을 주기적으로 읽어서 쿠폰 발급
        }
    }
}

// kafka 를 사용하면 api에서 직접 쿠폰을 생성할 때에 비해서, 처리량을 조절할 수 있게된다.
// 처리량을 조절함에 따라서 DB에 부하를 줄일 수 있다는 장점이 있다.
// 다만, 쿠폰 생성까지 약간의 텀이 발생할 수 있는 단점은 있다.
