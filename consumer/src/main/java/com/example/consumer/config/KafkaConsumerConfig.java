package com.example.consumer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer 작업을 하기 위한 설정
 */
@Configuration
public class KafkaConsumerConfig {

    // consumer 인스턴스를 생성하는데 필요한 설정값 정의
    // 스프링에서는 손쉽게 설정값들을 설정할 수 있도록 consumerFactory 라는 인터페이스를 제공해준다.
    @Bean
    public ConsumerFactory<String, Long> consumerFactory() { // consumerFactory 를 생성하기 위한 메서드
        Map<String, Object> config = new HashMap<>(); // 설정값들을 담아줄 map을 변수로 선언

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 서버 정보 추가
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_1"); // group_id 정보 추가
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // key deserializer 클래스 정보 추가
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class); // value deserializer 클래스 정보 추가

        return new DefaultKafkaConsumerFactory<>(config);
    }

    // topic 으로 부터 메시지를 전달받기 위한 kafka listener 를 만드는 kafkaListenerContainerFactory 를 생성
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Long> kafkaListenerContainerFactory() { // kafkaListenerContainerFactory 를 생성하기 위한 메서드
        ConcurrentKafkaListenerContainerFactory<String, Long> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory()); // kafkaListenerContainerFactory 을 생성할 때, 위에서 만들었던 consumerFactory 를 전달
        return factory;
    }
}
