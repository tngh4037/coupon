package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 발급 로직
 */
@Service
public class ApplyService {

    private final CouponRepository couponRepository;

    public ApplyService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public void apply(Long userId) {
        // 현재까지 발급된 쿠폰의 갯수를 가져온 후,
        long count = couponRepository.count(); // !! occurred race condition !!

        // 쿠폰의 갯수가 발급 가능한 갯수를 초과한 경우, 발급하지 않는다.
        if (count > 100) {
            return;
        }

        // 발급 가능한 경우, 쿠폰을 생성한다.
        couponRepository.save(new Coupon(userId));
    }
}

// [ 위 쿠폰 발급 로직의 문제점 ]
// - 동시 요청이 들어오는 경우, 레이스 컨디션이 발생한다. ( 레이스 컨디션 이란, 둘 이상의 쓰레드가 공유데이터에 엑세스를 하고, 동시에 작업을 하려고할 때 발생하는 문제이다. => 만약 싱글 스레드로 작업을 한다면 레이스 컨디션이 발생하지 않는다. )

// [ 레이스 컨디션 해결방법 ]
// 1) 자바에서 지원하는 synchronized 사용
// : but, 서버가 여러대인 경우 문제를 해결할 수 없어 적절하지 않다. (race condition 발생)
// 2) mysql lock 활용
// : but, 우리가 원하는 것은 쿠폰 갯수에 대한 정합성이다. 락을 활용하여 구현한다면, 발급된 쿠폰 갯수를 조회하는 것 부터 ~ 쿠폰을 생성할 때 까지 락을 걸어야 한다. 그렇게 된다면, 락을 거는 구간이 길어져서 성능에 불이익이 있을 수 있다. ex) 쿠폰을 저장(save)하는 로직이 2초가 걸린다면, 락은 2초 뒤에 풀리게 되므로, 이후 사용자들은 그만큼 기다려야 한다.
// 3) redis incr 활용
// : 이 프로젝트의 핵심은 쿠폰 갯수에 대한 정합성이다. 따라서, 쿠폰 갯수에 대한 관리만 하면된다.
// : redis 에는 incr 이라는 명령어가 있다. ( 레이스 컨디션을 해결하는 방법에는 여러가지가 있겠지만, 이 강의에서는 redis 를 활용하여 해결한다. )
