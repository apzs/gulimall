package com.atguigu.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author 无名氏
 * @date 2022/8/11
 * @Description:
 */
@Configuration
public class MyRabbitConfig {

    /**
     * 消息转换器(转换为JSON数据)
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
