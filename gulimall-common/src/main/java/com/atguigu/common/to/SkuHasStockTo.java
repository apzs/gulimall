package com.atguigu.common.to;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/7/6
 * @Description:
 */
@Data
public class SkuHasStockTo {

    private Long skuId;
    private Boolean hasStock;
}
