package com.atguigu.common.to.ware;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/17
 * @Description:
 */
@Data
public class WareLockStockResult {

    /**
     * 要锁定的sku的id
     */
    private Long skuId;

    /**
     * 锁定了的件数
     */
    private Integer num;

    /**
     * 是否锁定成功
     */
    private Boolean locked;
}
