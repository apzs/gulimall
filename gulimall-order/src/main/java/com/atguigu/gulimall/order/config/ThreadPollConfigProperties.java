package com.atguigu.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 无名氏
 * @date 2022/8/3
 * @Description:
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPollConfigProperties {

    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer keepAliveTime;
}
