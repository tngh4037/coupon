package com.example.api.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * KafkaTemplate 을 사용해서 topic에 데이터를 전송할 producer
 */
@Component
public class CouponCreateProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public CouponCreateProducer(KafkaTemplate<String, Long> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(Long userId) { // topic 에 userId 를 전달
        kafkaTemplate.send("coupon_create", userId);
    }
}
