package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/23
 * @Description:
 */
@Data
public class SeckillSessionSkusVo {

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuRelationVo> relationSkus;

    @Data
    public static class SeckillSkuRelationVo{
        /**
         * id
         */
        private Long id;
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
    }
}
