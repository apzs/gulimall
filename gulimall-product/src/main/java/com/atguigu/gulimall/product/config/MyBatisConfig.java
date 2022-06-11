package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 无名氏
 * @date 2022/5/10
 * @Description:
 * @EnableTransactionManagement ：开启事务功能
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyBatisConfig {

    /**
     * 引入分页插件
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //设置请求的页面大于最大页后操作，true调回到首页，false 继续请求 默认false
        paginationInterceptor.setOverflow(false);
        //设置最大单页限制数量，默认500条，-1 不受限制
        paginationInterceptor.setLimit(1000);

        return paginationInterceptor;
    }
}
