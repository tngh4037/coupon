package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class KafkaApplyServiceTest {

    @Autowired
    private KafkaApplyService kafkaApplyService;

    @Autowired
    private KafkaRedisAddApplyService kafkaRedisAddApplyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void 한번만응모() {
        kafkaApplyService.apply(1L);

        long count = couponRepository.count();

        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void 여러명응모() throws InterruptedException {
        int threadCount = 1000; // 동시에 여러 요청을 보내기 위해 멀티쓰레드를 사용한다. ( 여기서는 1000개의 요청을 보낸다. )
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 멀티쓰레드를 이용할 것이기 떄문에 ExecutorService 를 사용한다. ( ExecutorService 는 병렬 작업을 간단하게 할 수 있게 도와주는 자바의 API이다. )
        CountDownLatch latch = new CountDownLatch(threadCount);// 모든 요청이 끝날때까지 기다려야 하므로 CountDownLatch 를 사용한다. ( CountDownLatch 는 다른 쓰레드에서 수행하는 작업을 기다리도록 도와주는 클래스이다. )

        for (int i = 0; i < 1000; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    kafkaApplyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // consumer 가 데이터를 모두 수신해서 쿠폰을 생성하는 시간이, 테스트케이스가 종료되는 시간보다는 느리므로, 여유시간을 주었다.
        Thread.sleep(10000);

        // 모든 요청이 완료되면 생성된 쿠폰의 갯수를 확인한다.
        long count = couponRepository.count();

        // 우리가 기대하는 동작은 100 개의 쿠폰이 생성되는 것이다.
        Assertions.assertThat(count).isEqualTo(100); // Success ( Expected: 100L, Actual: 100L )
    }

    @Test
    void 한명당_한개의쿠폰만_발급() throws InterruptedException {
        int threadCount = 1000; // 동시에 여러 요청을 보내기 위해 멀티쓰레드를 사용한다. ( 여기서는 1000개의 요청을 보낸다. )
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 멀티쓰레드를 이용할 것이기 떄문에 ExecutorService 를 사용한다. ( ExecutorService 는 병렬 작업을 간단하게 할 수 있게 도와주는 자바의 API이다. )
        CountDownLatch latch = new CountDownLatch(threadCount);// 모든 요청이 끝날때까지 기다려야 하므로 CountDownLatch 를 사용한다. ( CountDownLatch 는 다른 쓰레드에서 수행하는 작업을 기다리도록 도와주는 클래스이다. )

        for (int i = 0; i < 1000; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    kafkaRedisAddApplyService.apply(1L); // 1이라는 유저가 1000번의 요청을 보낸다. (결과적으로 쿠폰은 1개만 발급되어야 한다.)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // consumer 가 데이터를 모두 수신해서 쿠폰을 생성하는 시간이, 테스트케이스가 종료되는 시간보다는 느리므로, 여유시간을 주었다.
        Thread.sleep(10000);

        // 모든 요청이 완료되면 생성된 쿠폰의 갯수를 확인한다.
        long count = couponRepository.count();

        // 우리가 기대하는 동작은 100 개의 쿠폰이 생성되는 것이다.
        Assertions.assertThat(count).isEqualTo(1); // Success ( Expected: 100L, Actual: 100L )
    }

}