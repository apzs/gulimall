package com.atguigu.gulimall.seckill.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/8/23
 * @Description:
 */
@Data
public class SeckillSkuRedisTo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品秒杀随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    /**
     * 当前商品秒杀的开始时间
     */
    private Long startTime;

    /**
     * 当前商品秒杀的结束时间
     */
    private Long endTime;
    /**
     * sku的详细信息
     */
    private SkuInfoVo skuInfoVo;

    @Data
    public static class SkuInfoVo{
        private Long skuId;
        /**
         * spuId
         */
        private Long spuId;
        /**
         * sku名称
         */
        private String skuName;
        /**
         * sku介绍描述
         */
        private String skuDesc;
        /**
         * 所属分类id
         */
        private Long catalogId;
        /**
         * 品牌id
         */
        private Long brandId;
        /**
         * 默认图片
         */
        private String skuDefaultImg;
        /**
         * 标题
         */
        private String skuTitle;
        /**
         * 副标题
         */
        private String skuSubtitle;
        /**
         * 价格
         */
        private BigDecimal price;
        /**
         * 销量
         */
        private Long saleCount;
    }

}
