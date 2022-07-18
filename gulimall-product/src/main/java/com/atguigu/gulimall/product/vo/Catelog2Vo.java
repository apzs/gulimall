package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/7/11
 * @Description: 二级分类vo
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {
    /**
     * 该二级分类对应的一级分类的id
     */
    private String catalog1Id;
    /**
     * 当前二级分类的id
     */
    private String id;
    /**
     * 当前二级分类的名字
     */
    private String name;
    /**
     * 该二级分类对应的所有三级分类
     */
    private List<Catelog3Vo> catalog3List;

    /**
     * 三级分类vo
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{
        /**
         * 该三级分类对应的二级分类
         */
        private String catalog2Id;
        /**
         * 该三级分类的id
         */
        private String id;
        /**
         * 该三级分类的名字
         */
        private String name;
    }

}
