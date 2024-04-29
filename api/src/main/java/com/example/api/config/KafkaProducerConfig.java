package com.example.api.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 토픽에 데이터를 전송하기 위한 producer 관련 설정
 */
@Configuration
public class KafkaProducerConfig {

    // producer 인스턴스를 생성하는데 필요한 설정값 정의
    // 스프링에서는 손쉽게 설정값들을 설정할 수 있도록 producerFactory 라는 인터페이스를 제공해준다.
    @Bean
    public ProducerFactory<String, Long> producerFactory() { // producerFactory 를 생성하기 위한 메서드
        Map<String, Object> config = new HashMap<>(); // 설정값들을 담아줄 map을 변수로 선언

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 서버 정보 추가
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // key serializer 클래스 정보 추가
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class); // value serializer 클래스 정보 추가

        return new DefaultKafkaProducerFactory<>(config);
    }

    // kafka topic 에 데이터를 전송하기 위해 사용할 kafkaTemplate 생성
    @Bean
    public KafkaTemplate<String, Long> kafkaTemplate() { // kafkaTemplate 을 빈으로 등록하기 위한 메서드 생성
        return new KafkaTemplate<>(producerFactory()); // KafkaTemplate 을 생성할 때, 위에서 만들었던 producerFactory 를 전달
    }

}