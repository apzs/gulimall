package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 无名氏
 * @date 2022/8/28
 * @Description:
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法被调用...getSkuSeckillInfo");
        return R.error(BizCodeException.TOO_MANY_REQUEST);
    }
}
