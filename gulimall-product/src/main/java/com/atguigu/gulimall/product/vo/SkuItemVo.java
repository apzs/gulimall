package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/7/31
 * @Description: 商品详情页数据
 */
@Data
public class SkuItemVo {
    /**
     * pms_sku_info
     * sku基本信息获取
     */
    SkuInfoEntity info;

    /**
     * 是否有货
     */
    boolean hasStock = true;

    /**
     * pms_sku_images
     * sku的图片信息
     */
    List<SkuImagesEntity> images;

    /**
     * 获取spu的销售属性组合
     */
    List<SkuItemSaleAttrVo> saleAttr;

    /**
     * 获取spu的介绍(图片集)
     */
    SpuInfoDescEntity desp;

    /**
     * 获取spu的规格参数信息
     */
    List<SpuItemAttrGroupVo> groupAttrs;
    /**
     * 当前商品的秒杀优惠信息
     */
    SeckillInfoVo seckillInfo;

    /**
     * sku的销售属性(比如可以选择的 颜色、内存容量 等)
     */
    @Data
    public static class SkuItemSaleAttrVo{
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名
         */
        private String attrName;
        /**
         * 属性值
         */
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class AttrValueWithSkuIdVo{
        /**
         * 属性值(如翡冷翠、8GB+128GB、8GB+256GB等)
         */
        private String attrValue;
        /**
         * 具有该属性值的skuId集合
         */
        private String skuIds;
    }

    /**
     * 商品的基本属性分组
     * (基本信息:
     *    机身长度（mm） 150.5
     *    机身宽度（mm） 77.8
     *    机身厚度（mm） 8.2
     *    机身重量（g）  约186g（含电池）
     * )
     */
    @Data
    public static class SpuItemAttrGroupVo{
        /**
         * 组名
         */
        private String groupName;
        /**
         * 属性列表
         */
        private List<SpuBaseAttrVo> attrs;
    }

    /**
     * 商品详细信息里的基本属性
     */
    @Data
    public static class SpuBaseAttrVo{
        /**
         * 属性名
         */
        private String attrName;
        /**
         * 属性值
         */
        private String attrValue;
    }

}
