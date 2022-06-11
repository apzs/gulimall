package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/6/1
 * @Description:
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
