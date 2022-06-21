package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author  无名氏
 * @date  2022/6/16
 * @Description:
 */
@FeignClient("gulimall-gateway")
@Service
public interface ProductFeignService {

    /**
     * 1)、让所有请求过网关;
     *    1、@FeignClient( "gulimall-gateway"):给gulimall-gateway所在的机器发请求
     *    2、/api/product/skuinfo/info/{skuId}
     * 2)、直接让后台指定服务处理
     *    1、@FeignClient( "gulimall-product")
     *    2、/product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/api/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
