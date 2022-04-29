package com.atguigu.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author 无名氏
 * @date 2022/4/27
 * @Description: 跨域请求过滤器
 */
@Configuration
public class GulimallCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //添加允许的请求头
        corsConfiguration.addAllowedHeader("*");
        //添加允许的请求方式
        corsConfiguration.addAllowedMethod("*");
        //添加允许的请求来源
        corsConfiguration.addAllowedOrigin("*");
        //是否允许携带cookie进行跨域
        //设为false会丢失cookie信息
        corsConfiguration.setAllowCredentials(true);

        //CorsWebFilter需要传入CorsConfigurationSource接口类型的参数
        //UrlBasedCorsConfigurationSource是CorsConfigurationSource接口的实现类
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // "/**"表示任意路径
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }
}
