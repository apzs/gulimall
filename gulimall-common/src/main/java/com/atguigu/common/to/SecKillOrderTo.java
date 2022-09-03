package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/8/28
 * @Description: 秒杀订单to(秒杀快速创建的订单)
 */
@Data
public class SecKillOrderTo {

    /**
     * 订单号
     */
    private String orderSn;
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
     * 秒杀的数量
     */
    private Integer num;
    /**
     * 会员id
     */
    private Long memberId;


}
