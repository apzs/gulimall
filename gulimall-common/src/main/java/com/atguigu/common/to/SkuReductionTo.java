package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/6/1
 * @Description:
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    /**
     * 满几件打几折
     * countStatus: 是否可以叠加优惠
     */
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    /**
     * 满多少减多少
     * priceStatus：是否可以叠加优惠
     */
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    /**
     * 会员价格
     */
    private List<MemberPrice> memberPrice;

    @Data
    public static class MemberPrice {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
