package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description: 购物车里的商品项
 */
@Data
public class CartItemVo {
    /**
     * sku的id
     */
    private Long skuId;
    /**
     * 是否选中（默认选中）
     */
    private Boolean check = true;
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
     * 计算总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return price==null ? BigDecimal.ZERO : price.multiply(new BigDecimal(count));
    }
}
