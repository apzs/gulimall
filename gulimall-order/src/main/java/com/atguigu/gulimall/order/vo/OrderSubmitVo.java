package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/8/16
 * @Description: 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {

    /**
     * 收货地址的id
     */
    private Long addrId;
    /**
     * 支付方式（在线支付/货到付款）
     */
    private Integer payType;

    //再去购物车中查询商品，不用页面提交商品信息
    //积分、优惠、发票

    /**
     * 防重令牌
     */
    private String orderToken;

    /**
     * 页面提交的应付价格（如果提交订单后判断的应付价格和页面提交过来的价格不一样，给予用户提示）
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;
    ///用户相关信息，直接去session取出登录的用户

}
