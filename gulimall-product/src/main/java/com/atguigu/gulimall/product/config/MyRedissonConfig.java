package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author 无名氏
 * @date 2022/7/16
 * @Description:
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        //1、创建配置
        Config config = new Config();
        //Redis url should start with redis:// or rediss:// (for SSL connection)
        //config.useSingleServer().setAddress("192.168.56.10:6379").setPassword("");
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        //2、根据Config创建出RedissonClient示例
        return Redisson.create(config);
    }

}
