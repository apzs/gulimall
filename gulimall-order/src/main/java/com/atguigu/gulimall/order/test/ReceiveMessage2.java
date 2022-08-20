package com.atguigu.gulimall.order.test;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 无名氏
 * @date 2022/8/11
 * @Description: com.atguigu.gulimall.order.GulimallOrderApplicationTests.sendMessage3发送消息
 * 测试发放的控制台也有可能收到消息，测试方法的控制台也要看
 * 测试该类时，注释掉ReceiveMessage1类的方法
 */
@Component
@RabbitListener(queues = {"hello-java-queue"})
public class ReceiveMessage2 {

    @RabbitHandler
    public void receiveMessage1(Message message, OrderReturnReasonEntity entity, Channel channel) throws IOException {
        System.out.println("OrderReturnReasonEntity类消息处理完成：" + entity.getName());
        //channel信道内按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (deliveryTag % 2 == 0) {
            //签收消息
            //basicAck(long deliveryTag, boolean multiple是否批量确认收货，如果为false只签收当前消息)
            channel.basicAck(deliveryTag, false);
            System.out.println("签收了第" + deliveryTag + "个货物");
        } else {
            //拒收消息
            //requeue=false丢弃消息 requeue=true 消息发回服务器，服务器重新入队。
            //basicReject(long deliveryTag, boolean requeue)
            //channel.basicReject();

            //basicNack(long deliveryTag, boolean multiple, boolean requeue)
            channel.basicNack(deliveryTag, false, true);
            System.out.println("拒签了第"+deliveryTag+"个货物");
        }

    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity entity) {
        System.out.println("OrderEntity类消息处理完成：" + entity.getOrderSn());
    }
}
