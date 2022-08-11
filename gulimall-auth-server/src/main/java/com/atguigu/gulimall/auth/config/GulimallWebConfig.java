package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 无名氏
 * @date 2022/8/3
 * @Description:
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    /**
     * @GetMapping("/login.html")
     * public String loginPage(){
     *     return "login";
     * }
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
