package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/23
 * @Description:
 */
public interface SeckillService {

    /**
     * 上架最近3天需要秒杀的商品
     */
    public void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);

}
