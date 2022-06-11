package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/5/12
 * @Description:
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * 所属分类名   /手机/数码/手机
     */

    private String catelogName;
    /**
     * 所属分组名  主机
     */
    private String groupName;

    /**
     * 所属分类的完整路径
     */
    private Long[] catelogPath;
}
