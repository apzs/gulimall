package com.atguigu.gulimall.order.feign;

import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.to.ware.WareSkuLockTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RS;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/15
 * @Description:
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    public RS<List<SkuHasStockTo>> getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * 根据addrId获取运费 和 MemberAddressVo
     * @param addrId
     * @return
     */
    @GetMapping("/ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 下订单后。锁库存
     */
    @PostMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockTo wareSkuLockTo);
}
