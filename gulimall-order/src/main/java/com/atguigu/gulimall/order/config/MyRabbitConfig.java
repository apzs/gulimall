package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * @author 无名氏
 * @date 2022/8/11
 * @Description:
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 消息转换器(转换为JSON数据)
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 在构造器执行之后执行
     * 定制RabbitTemplate
     * 服务收到消息就回调
     *    1、spring.rabbitmq.publisher-confirms=true
     *    2、设置确认回调
     * 消息正确抵达队列进行回调
     *    1、 #开启发送端消息抵达队列的确认(默认false)
     *       spring.rabbitmq.publisher-returns=true
     *       #只要抵达队列，以异步发送优先回调我们这个returnConfirm(当然也可以不设置，默认false)
     *       spring.rabbitmq.template.mandatory=true
     *    2、设置确认回调ReturnCallback
     * 3、消费端确认(保证每个消息被正确消费，此时才可以broker删除这个消息)。
     *     #消费方签收消息
     *     #将简单的恢复模式调为手动模式 (默认auto:自动回复)
     *     spring.rabbitmq.listener.simple.acknowledge-mode=manual
     *    1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *      问题：我们收到很多消息，自动回复给服务器ack,只有一个消息处理成功，宕机了。发生消息丢失
     *      消费者手动确认模式。只要我们没有明确告诉MQ，货物被签收。没有Ack,消息就一直是unacked状态。即使Consumer宕机。
     *                  消息不会丢失，会重新变为Ready，下一次有新的Consumer连接进来就发给他
     *    2、如何签收:
     *      channel.basicAck(deliveryTag, false);签收;业务成功完成就应该签收
     *      channel.basicNack(deliveryTag,false, true);拒签;业务失败,拒签
     */
    @PostConstruct
    public void initRabbitTemplate(){
        //设置消息抵达消息代理的回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *只要消息抵达Broker就ack=true
             * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一-id)
             * @param ack             消息是否成功收到
             * @param cause           失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /**
                 * 1、做好消息确认机制(publisher, consumer [手动ack] )
                 * 2、每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送一遍
                 */
                //消息发送到`Broker`了
                System.out.println("confirm...CorrelationData==>["+correlationData+"]ack==>["+ack+"]cause==>["+cause+"]");
            }
        });

        //消息抵达队列的回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message    投递失败的消息详细信息
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   当时这个消息发给哪个交换机
             * @param routingKey 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错误了。修改数据库当前消息的状态->错误。
                System.out.println("message = " + message + ", replyCode = " + replyCode +
                        ", replyText = " + replyText + ", exchange = " + exchange + ", routingKey = " + routingKey);
            }
        });
    }



}
