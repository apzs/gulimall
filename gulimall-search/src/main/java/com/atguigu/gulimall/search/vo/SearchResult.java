package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/7/22
 * @Description: 根据查询条件返回的结果
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 当前查询到的结果涉及到的所有品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果涉及到的所有分类
     */
    private List<CatalogVo> catalogs;

    /**
     *  当前查询到的结果涉及到的所有属性
     */
    private List<AttrVo> attrs;

    /**
     * 品牌vo
     */
    @Data
    public static class BrandVo{
        /**
         * 品牌id
         */
        private Long brandId;
        /**
         * 品牌名
         */
        private String brandName;
        /**
         * 品牌图片
         */
        private String brandImg;
    }

    /**
     * 分类vo
     */
    @Data
    public static class CatalogVo{
        /**
         * 分类id
         */
        private Long catalogId;
        /**
         * 分类名
         */
        private String catalogName;

    }

    /**
     * 属性vo
     */
    @Data
    public static class AttrVo{
        /**
         * 属性的id
         */
        private Long attrId;
        /**
         * 属性名
         */
        private String attrName;
        /**
         * 属性值
         */
        private List<String> attrValue;

    }
}
