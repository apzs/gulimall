package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 无名氏
 * @date 2022/8/12
 * @Description: 封装订单确认页数据的vo
 */
public class OrderConfirmVo {

    /**
     * 收货地址
     */
    @Getter @Setter
    List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    @Getter @Setter
    List<OrderItemVo> items;

    //发票信息...

    //优惠券...

    /**
     * 积分
     */
    @Getter @Setter
    Integer integration;

    /**
     * 订单总价格
     */
    BigDecimal total;

    /**
     * 应付总额（应付总额=订单价格-优惠券-积分-红包）
     */
    BigDecimal payPrice;

    /**
     * 令牌，防止重复提交
     */
    @Getter @Setter
    String orderToken;

    /**
     * 是否有库存
     * Long：skuId
     * Boolean：是否有库存
     */
    @Getter @Setter
    Map<Long,Boolean> stocks;

    public Integer getCount(){
        Integer count = 0;
        for (OrderItemVo item : items) {
            count+=item.count;
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (!CollectionUtils.isEmpty(items)){
            for (OrderItemVo item : items) {
                BigDecimal bigDecimal = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(bigDecimal);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }

    /**
     * 会员收货地址 ums_member_receive_address
     */
    @Data
    public static class MemberAddressVo{
        /**
         * id
         */
        private Long id;
        /**
         * member_id
         */
        private Long memberId;
        /**
         * 收货人姓名
         */
        private String name;
        /**
         * 电话
         */
        private String phone;
        /**
         * 邮政编码
         */
        private String postCode;
        /**
         * 省份/直辖市
         */
        private String province;
        /**
         * 城市
         */
        private String city;
        /**
         * 区
         */
        private String region;
        /**
         * 详细地址(街道)
         */
        private String detailAddress;
        /**
         * 省市区代码
         */
        private String areacode;
        /**
         * 是否默认
         */
        private Integer defaultStatus;
    }

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
         * //TODO 查询库存状态
         * 是否有货
         */
        //private boolean hasStock;
        /**
         * 货物重量
         */
        private BigDecimal weight;
    }


}
