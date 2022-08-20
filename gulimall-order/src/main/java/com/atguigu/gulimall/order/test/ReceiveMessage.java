package com.atguigu.gulimall.order.test;

import org.springframework.stereotype.Component;

/**
 * @author 无名氏
 * @date 2022/8/11
 * @Description: com.atguigu.gulimall.order.GulimallOrderApplicationTests.sendMessage2发送消息
 *  测试发放的控制台也有可能收到消息，测试方法的控制台也要看
 *  测试该类时，注释掉ReceiveMessage2类的方法
 */
@Component
public class ReceiveMessage {
    /**
     * 在需要监听队列的业务方法上标注@RabbitListener注解（该方法所在的类需要在容器中）
     * 参数可以写一下类型
     * 1、Message message:原生消息详细信息。头+体
     * 2、T<发送的消息的类型> OrderReturnReasonEntity content;
     * Channel channel: 当前传输数据的通道
     *
     * Queue:可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一一个收到此消息
     * 场景:
     * 1)、订单服务启动多个;同一个消息，只能有一个客户端收到
     * 2)、只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     */
    //@RabbitListener(queues = {"hello-java-queue"})
    //public void receiveMessage(Message message, OrderReturnReasonEntity entity, Channel channel) throws InterruptedException {
    //    //{"id":1,"name":"啊啊啊","sort":null,"status":null,"createTime":1660210035834}
    //    byte[] body = message.getBody();
    //    //消息的属性信息
    //    //[headers={__TypeId__=com.atguigu.gulimall.order.entity.OrderReturnReasonEntity}, contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=hello-java-exchange, receivedRoutingKey=hello.java, deliveryTag=1, consumerTag=amq.ctag-5sX1WFjvna_vui2BTkXVdg, consumerQueue=hello-java-queue]
    //    MessageProperties messageProperties = message.getMessageProperties();
    //    String contentType = messageProperties.getContentType();
    //    System.out.println("接收到消息："+entity);
    //    Thread.sleep(3000);
    //    System.out.println("消息处理完成：" + entity.getName());
    //}
}
