package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author 无名氏
 * @date 2022/8/12
 * @Description:
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        //订单确认页
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        //下单:去创建订单，验令牌，验价格，锁库存...
        if (responseVo.getCode()==0){
            //下单成功来到支付选择页
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }else {
            //下单失败回到订单确认页重新确认订单信息
            String msg = "下单失败，";
            //成功为0,令牌验证失败为1,金额对比失败为2,锁定库存失败为3
            switch (responseVo.getCode()){
                case 1: msg+="订单信息过期，请刷新页面再提交"; break;
                case 2: msg+="订单商品发送变化，请刷新页面重新获取订单信息";break;
                case 3: msg+="库存锁定失败，商品库存不足";break;
                default: msg+="未知异常，请刷新重试";
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
