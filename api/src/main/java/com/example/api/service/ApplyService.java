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
        long count = couponRepository.count();

        // 쿠폰의 갯수가 발급 가능한 갯수를 초과한 경우, 발급하지 않는다.
        if (count > 100) {
            return;
        }

        // 발급 가능한 경우, 쿠폰을 생성한다.
        couponRepository.save(new Coupon(userId));
    }
}

// [ 위 쿠폰 발급 로직의 문제점 ]
// - 동시 요청이 들어오는 경우, 레이스 컨디션이 발생한다. ( 레이스 컨디션 이란, 둘 이상의 쓰레드가 공유데이터에 엑세스를 하고, 동시에 작업을 하려고할 때 발생하는 문제이다. )

// [ 레이스 컨디션 해결방법 ]
// - 레이스 컨디션을 해결하는 방법에는 여러가지가 있겠지만, 이 강의에서는 redis 를 활용하여 해결한다.
