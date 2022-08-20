package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author 无名氏
 * @date 2022/8/20
 * @Description:
 */
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    /**
     * com.atguigu.gulimall.order.web.HelloController类的createOrderTest方法发送消息
     * @param orderEntity
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException, InterruptedException {
        System.out.println("收到过期的订单信息，准备关闭订单=>"+orderEntity.getOrderSn()+"时间=>"+orderEntity.getModifyTime());
        try {
            //当前消息是否被第二次及以后(重新)派发过来了。
            Integer receivedDelay = message.getMessageProperties().getReceivedDelay();
            orderService.closeOrder(orderEntity);
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            e.printStackTrace();
        }
    }
}
