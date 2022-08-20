package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/16
 * @Description:
 */
@Data
public class OrderCreateTo {

    /**
     * 订单实体类
     */
    private OrderEntity order;

    /**
     * 订单项
     */
    private List<OrderItemEntity> orderItems;

    /**
     * 运费
     */
    private BigDecimal fare;

    /**
     * 订单计算的应付价格
     */
    private BigDecimal payPrice;
}
