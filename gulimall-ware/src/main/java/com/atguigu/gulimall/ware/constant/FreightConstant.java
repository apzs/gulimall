package com.atguigu.gulimall.ware.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author 无名氏
 * @date 2022/8/16
 * @Description: 用户需要支付的运费
 */
@Data
@Component
@ConfigurationProperties(prefix = "gulimall.freight")
public class FreightConstant {

    /**
     * 本地运费
     */
    private BigDecimal localFreight = new BigDecimal("8");

    /**
     * 外地运费
     */
    private BigDecimal outlandFreight = new BigDecimal("12");
}
