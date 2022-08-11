package com.atguigu.gulimall.cart.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description: 购物车
 */
public class CartVo {
    /**
     * 商品项
     */
    private List<CartItemVo> items;
    /**
     * 商品数量（所有商品的count相加）
     */
    private Integer countNum;
    /**
     * 商品有几种类型（有几种不同的商品）
     */
    private Integer countType;
    /**
     * 商品总价（所有商品总价加起来）
     */
    private BigDecimal totalAmount;
    /**
     * 减免的价格
     */
    private BigDecimal reduce = BigDecimal.ZERO;

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int countNum = 0;
        if (!CollectionUtils.isEmpty(items)){
            for (CartItemVo item : items) {
                if (item.getCheck()){
                    countNum+=item.getCount();
                }
            }
        }
        return countNum;
    }

    public Integer getCountType() {
        int countType = 0;
        if (!CollectionUtils.isEmpty(items)){
            for (CartItemVo item : items) {
                if (item.getCheck()){
                    countType++;
                }
            }
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        //购物总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(items)){
            for (CartItemVo item : items) {
                if (item.getCheck()){
                    totalAmount = totalAmount.add(item.getPrice());
                }
            }
        }
        //减去优惠
        totalAmount = totalAmount.subtract(getReduce());
        return totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
