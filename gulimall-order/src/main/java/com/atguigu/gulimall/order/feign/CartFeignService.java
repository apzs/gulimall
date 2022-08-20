package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/15
 * @Description:
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderConfirmVo.OrderItemVo> getCurrentUserCartItems();
}
