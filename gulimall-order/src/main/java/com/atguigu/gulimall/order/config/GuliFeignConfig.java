package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 无名氏
 * @date 2022/8/15
 * @Description:
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //拿到刚进来的这个请求(/toTrade)
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                //ServletRequestAttributes extends AbstractRequestAttributes
                //AbstractRequestAttributes implements RequestAttributes
                ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;

                System.out.println("RequestInterceptor线程："+Thread.currentThread().getId());
                if (attributes != null) {
                    //原本的 /toTrade 请求
                    HttpServletRequest request = attributes.getRequest();
                    //同步请求头数据，主要是Cookie
                    String cookie = request.getHeader("Cookie");
                    //为远程调用而构造的新请求
                    template.header("Cookie",cookie);
                    //template.
                }
            }
        };
    }

}
