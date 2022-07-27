package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/7/22
 * @Description: 封装可能的所有的查询条件
 *  catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1
 */
@Data
public class SearchParam {

    /**
     * 页面传过来的全文匹配的关键字
     * keyword=小米
     */
    private String keyword;

    /**
     * 三级分类的id
     * catalog3Id=225
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * 按照销量升序或降序     sort=saleCount_asc/desc
     * 按照sku价格升序或降序  sort=skuPrice_asc/desc
     * 按照热度评分升序或降序  sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 品牌的id(可以指定多个品牌)
     * brandId=1&brandId=2
     */
    private List<Long> brandId;

    /**
     * 是否有货(是否只显示有货)
     * hasStock=0/1
     */
    private Integer hasStock;

    /**
     * 价格区间
     * 价格在1~500之间  skuPrice=1_500
     * 价格不高于500    skuPrice=_500
     * 价格不低于1      skuPrice=1_
     */
    private String skuPrice;

    /**
     * 指定属性
     * attrs=1_安卓:其他&attrs=2_5寸:6寸
     *
     * 1号属性(系统)值为 `安卓`或`其他`
     * 2号属性(屏幕尺寸)值为 `5寸`或`6寸`
     */
    private List<String> attrs;

    /**
     * 页码
     * pageNum=1  from=0  to=5   size=5    [0,1,2,3,4]
     * pageNum=2  from=5  to=10  size=5    [5,6,7,8,9]
     *
     * from= (pageNum - 1)*size
     *  to = pageNum*size
     */
    private Integer pageNum;

}
