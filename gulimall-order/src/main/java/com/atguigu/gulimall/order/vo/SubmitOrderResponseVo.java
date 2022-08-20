package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/16
 * @Description:
 */
@Data
public class SubmitOrderResponseVo {

    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 下单状态码(成功为0,令牌验证失败为1,金额对比失败为2,锁定库存失败为3)
     */
    private Integer code;
}
