package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.utils.RS;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/7/7
 * @Description:
 */
@FeignClient("gulimall-ware")
public interface WareFelginService {

    @PostMapping("/ware/waresku/hasStock")
    public RS<List<SkuHasStockTo>> getSkuHasStock(List<Long> skuIds);
}
