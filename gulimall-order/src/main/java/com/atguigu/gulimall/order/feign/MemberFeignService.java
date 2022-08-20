package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/12
 * @Description:
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<OrderConfirmVo.MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
