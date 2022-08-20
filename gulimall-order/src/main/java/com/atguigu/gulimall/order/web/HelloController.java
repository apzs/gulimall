package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author 无名氏
 * @date 2022/8/12
 * @Description:
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     *
     * com.atguigu.gulimall.order.config.MyMQConfig类的listener方法监听消息
     * @return
     */
    @GetMapping("/test/createOrder")
    @ResponseBody
    public String createOrderTest(){
        //订单下单成功
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());
        //给MQ发消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return "已生成订单=>"+orderEntity.getOrderSn()+"，时间=>"+orderEntity.getModifyTime();
    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){

        return page;
    }
}
