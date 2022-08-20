package com.atguigu.common.to.ware;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/17
 * @Description: 下订单后锁库存
 */
@Data
public class WareSkuLockTo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁的库存
     */
    private List<OrderItemVo> locks;

    /**
     * 订单项（某一个具体商品）
     */
    @Data
    public static class OrderItemVo{
        /**
         * sku的id
         */
        private Long skuId;
        /**
         * 商品的标题
         */
        private String title;
        /**
         * 商品的图片
         */
        private String image;
        /**
         * sku的属性（选中的 颜色、内存容量 等）
         */
        private List<String> skuAttr;
        /**
         * 商品的价格
         */
        private BigDecimal price;
        /**
         * 商品的数量
         */
        private Integer count;
        /**
         * 总价(商品价格*商品数量)
         */
        private BigDecimal totalPrice;
        /**
         * 货物重量
         */
        private BigDecimal weight;
    }
}
