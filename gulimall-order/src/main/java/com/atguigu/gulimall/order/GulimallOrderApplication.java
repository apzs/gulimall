package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景; RabbitAutoConfiguration就会 自动生效
 * 2、给容器中自动配置了
 *    RabbitTemplate、AmqpAdmin、CachingConnectionFactory、 RabbitMessagingTemplate
 *    所有的属性都是spring.rabbitmq
 *    @ConfigurationProperties(prefix ="spring.rabbitmq")
 *    public class RabbitProperties
 * 3、给配置文件中配置spring.rabbitmq信息
 * 4、@EnableRabbit: @EnableXxxxx 开启功能
 * 5、监听消息:使用@RabbitListener; 必须有@EnableRabbit
 * @RabbitListener:类+方法上(监听哪些队列即可)
 * @RabbitHandler:标在方法上(重载区分不同的消息)
 */
//设置exposeProxy = true，对外暴露代理对象
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallOrderApplication.class, args);
	}

}
