package com.atguigu.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 无名氏
 * @date 2022/8/3
 * @Description: 线程池
 */
//@EnableConfigurationProperties(ThreadPollConfigProperties.class)
@Configuration
public class MyThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPollConfigProperties poll){
        return new ThreadPoolExecutor(poll.getCorePoolSize(),
                poll.getMaximumPoolSize(), poll.getKeepAliveTime(),
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new  ThreadPoolExecutor.AbortPolicy());
    }
}
