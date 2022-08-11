package com.atguigu.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author 无名氏
 * @date 2022/8/7
 * @Description:
 */
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        //	We customize the name of the cookie to be JSESSIONID.
        serializer.setCookieName("GULIMALL_JSESSIONID");
        serializer.setDomainName("gulimall.com");
        ////We customize the path of the cookie to be / (rather than the default of the context root).
        //serializer.setCookiePath("/");
        ////If the regular expression matches, the first grouping is used as the domain.
        ////This means that a request to https://child.example.com sets the domain to example.com.
        ////However, a request to http://localhost:8080/ or https://192.168.1.100:8080/ leaves the cookie unset and,
        //// thus, still works in development without any changes being necessary for production.
        ////亲测不生效
        //serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }


}
