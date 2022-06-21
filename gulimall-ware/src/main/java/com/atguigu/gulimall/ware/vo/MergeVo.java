package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 无名氏
 * @date 2022/6/14
 * @Description:
 */
@Data
public class MergeVo {

    /**
     * 采购单id
     */
    private Long purchaseId;
    /**
     * 要合并的采购项集合
     */
    private List<Long> items;
}
