package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 无名氏
 * @date 2022/8/19
 * @Description: 消息队列配置类
 * 若RabbitMQ里没有，容器中的Binding， Queue, Exchange 都会自动创建
 * RabbitMQ只要有。@Bean声明属性发生变化也不会覆盖
 */
@Configuration
public class MyMQConfig {

    /**
     * 给订单加上过期时间
     * x-dead-letter-exchange: order-event-exchange
     * x-dead-letter-routing-key: order.release.order
     * x-message-ttl: 60000
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", TimeUnit.MINUTES.toMillis(1));
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    /**
     * 释放订单
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        //TopicExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        //Binding(String destination, DestinationType destinationType, String exchange, String routingKey,Map<String, Object> arguments)
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange","order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange","order.release.order",null);
    }

    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange","order.release.other.#",null);
    }

    @Bean
    public Queue orderSeckillOrderQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete
        return new Queue("order.seckill.order.queue",true,false,false);
    }

    @Bean
    public Binding orderSeckillOrderBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,Map<String, Object> arguments
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.seckill.order", null);
    }

}
