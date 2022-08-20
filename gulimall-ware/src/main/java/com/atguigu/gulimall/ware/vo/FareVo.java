package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/8/16
 * @Description:
 */
@Data
public class FareVo {

    private MemberAddressVo memberAddressVo;

    private BigDecimal fare;
}
