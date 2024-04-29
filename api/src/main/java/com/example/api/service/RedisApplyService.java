package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 로직
 */
@Service
public class RedisApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    public RedisApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
    }

    public void apply(Long userId) {
        // 발급된 쿠폰의 갯수를 증가시키고,
        Long count = couponCountRepository.increment();

        // 발급된 쿠폰의 갯수가 발급 가능한 갯수를 초과하는 경우, 발급하지 않는다.
        if (count > 100) {
            return;
        }

        // 발급 가능한 경우, 쿠폰을 생성한다.
        couponRepository.save(new Coupon(userId));
    }
}

// [ redis 의 incr 명령어 ]
// - key에 대한 value를 1씩 증가시킨다. (이후 증가된 값을 리턴)
// - redis 는 싱글 스레드 기반으로 동작하여 race condition 을 해결할 수 있을 뿐만 아니라, incr 명령어는 성능도 굉장히 빠르다.
// - 이 명령어를 사용하여 "발급된 쿠폰의 갯수를 제어" 한다면 성능도 빠르며, 데이터 정합성도 지킬 수 있다.
// - 활용 예시 ex) 쿠폰을 생성하기 전에 redis 쿠폰 카운트를 1 증가시키고, 리턴값이 100보다 크다면, 이미 100개 이상이 발급되었다는 뜻이므로, 쿠폰 발급을 하지 않도록 적용.

// [ But, 문제점 ]
// - 현재 로직은 쿠폰 발급 요청이 들어오면 redis 를 활용해서 쿠폰의 발급 갯수를 가져온 후에 발급이 가능한 경우, RDB에 저장하는 방식이다.
// - 이 방식은 문제가 없어 보일 수 있으나, 발급하는 쿠폰의 갯수가 많아지면 많아질수록 RDB에 부하를 주게 된다.
// - 만약 사용하는 RDB 가 쿠폰 전용 DB가 아니라, 다양한 곳에서 사용하고 있었다면 다른 서비스까지 장애가 발생할 수 있다.
//   ㄴ ex) mysql 이 1분에 100개의 insert 가 가능하다고 가정해보자.
//   ㄴ 이 상태에서 10시에 1만개의 쿠폰 생성 요청이 들어오고, 10시 1분에 주문 생성 요청, 10시 2분에 회원 가입 요청이 들어오면 어떻게 될까?
//
//      ㄴ 1) 먼저, 1분에 100개씩 1만개를 생성하려면 100분이 걸린다. 그리고 10시 1분, 10시 2분에 들어온 요청은 100분 이후에 처리되게 된다. ( 만약 timeout이 없다면 느리게라도 모든 요청이 처리되겠지만, 대부분 서비스에는 timeout 옵션이 설정되어 있다. 그러므로 주문, 가입 요청 뿐만 아니라, 일부분의 쿠폰도 생성되지 않는 오류가 발생할 수 있다. )
//      ㄴ 2) 짧은 시간내에 많은 요청이 들어오게 된다면, DB 서버에 리소스를 많이 사용하게 되므로 부하가 발생하게 되고(RBD의 cpu 사용량이 높아짐), 이는 곧 서비스 지연이나 오류로 이어질 수 있다.

// => kafka 를 활용하면 이러한 문제를 해결할 수 있다. ( kafka 를 활용해서 쿠폰을 생성 )
// : producer 를 활용하여 쿠폰 생성을 요청한 유저의 id 를 토픽에 넣고, consumer 를 활용하여 유저의 id를 가져와서 쿠폰을 생성하도록 적용.