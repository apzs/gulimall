package com.atguigu.gulimall.order.test;

import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 无名氏
 * @date 2022/8/19
 * @Description:
 * 1、引入spring-boot-starter-aop依赖
 * 2、@EnableAspectJAutoProxy(exposeProxy = true)  exposeProxy = true对外暴露代理对象
 * 3、AspectJTest orderService = (AspectJTest) AopContext.currentProxy();
 */
@Component
public class AspectJTest {

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED,timeout = 30)
    public void a(){
        System.out.println("执行a方法");
        AspectJTest orderService = (AspectJTest) AopContext.currentProxy();
        orderService.b();
        orderService.c();

    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED,timeout = 2)
    public void b(){
        System.out.println("执行b方法");
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW,timeout = 20)
    public void c(){
        System.out.println("执行c方法");
    }
}
