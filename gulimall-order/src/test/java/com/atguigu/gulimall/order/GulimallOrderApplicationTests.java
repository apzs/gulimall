package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    /**
     * 创建交换机
     */
    @Test
    public void createExchange() {
        //声明交换机（durable：持久化）
        //DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", directExchange.getName());
    }

    /**
     * 创建队列
     */
    @Test
    public void createQueue() {
        //exclusive：排他(只能被声明的连接使用，只要一个连接连上该队列，其他连接就连不上该队列)
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Exchange[{}]创建成功", queue.getName());
    }

    /**
     * 创建绑定
     */
    @Test
    public void createBinding() {
        /*
         * String destination :目的地
         * DestinationType destinationType :目的地类型（交换机/队列）
         * String exchange    :交换机
         * String routingKey  :路由键
         * Map<String, Object> arguments 参数
         */
        //将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,
                "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-Binding");
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 发送String类型消息
     */
    @Test
    public void sendMessage() {
        //rabbitTemplate.send();
        //convertAndSend(String exchange, String routingKey, final Object object)
        String msg = "hello world";
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", msg);
        log.info("消息发送完成{}", msg);
    }

    /**
     * 发送其他对象类型消息
     * com.atguigu.gulimall.order.test.ReceiveMessage.receiveMessage接收消息
     */
    @Test
    public void sendMessage2() {
        for (int i = 0; i < 10; i++) {
            //发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
            OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
            entity.setId(1L);
            entity.setCreateTime(new Date());
            entity.setName("啊啊啊-->" + i);
            //rabbitTemplate.send();
            //convertAndSend(String exchange, String routingKey, final Object object)
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity);
            log.info("消息发送完成{}", entity);
        }
    }

    /**
     * 发送不同对象类型消息(模拟同一个队列发送不同对象 或 不同队列发送的对象不同)
     * 发送OrderReturnReasonEntity对象 ReceiveMessage2类的receiveMessage1方法接收该消息
     * 发送OrderEntity对象             ReceiveMessage2类的receiveMessage2方法接收该消息
     */
    @Test
    public void sendMessage3() {
        for (int i = 0; i < 10; i++) {
            String uuid = UUID.randomUUID().toString();
            if (i % 2 == 0) {
                //发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("啊啊啊-->" + i);
                //rabbitTemplate.send();
                //convertAndSend(String exchange, String routingKey, final Object object)
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData(uuid));
                log.info("消息发送完成{}", entity);
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData(uuid));
                log.info("消息发送完成{}", entity);
            }
        }
    }


    @Test
    public void sendMessage4() {
        for (int i = 0; i < 10; i++) {
            String uuid = UUID.randomUUID().toString();
            if (i % 2 == 0) {
                //发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("啊啊啊-->" + i);
                //rabbitTemplate.send();
                //convertAndSend(String exchange, String routingKey, final Object object)
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData(uuid));
                log.info("消息发送完成{}", entity);
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.javxxxxa", entity, new CorrelationData(uuid));
                log.info("消息发送完成{}", entity);
            }
        }
    }

    @Test
    public void sendMessage5() {
        for (int i = 1; i <= 5; i++) {
            String uuid = UUID.randomUUID().toString();
            //发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
            OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
            entity.setId(1L);
            entity.setCreateTime(new Date());
            entity.setName("发送第【" + i + "】个货物");
            //rabbitTemplate.send();
            //convertAndSend(String exchange, String routingKey, final Object object)
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity, new CorrelationData(uuid));
            log.info("消息发送完成{}", entity);

        }
    }


}
