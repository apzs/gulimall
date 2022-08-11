package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description:
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){

        cartService.checkItem(skuId,check);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){

        cartService.changeItemCount(skuId,num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 浏览器有一个cookie; user-key; 标识用户身份，一个月后过期;
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份;
     * 浏览器以后保存，每次访问都会带上这个cookie;
     * 登录: session有用户
     * 没登录:按照cookie里面带来user-key来做。
     * 第一次:如果没有临时用户，帮忙创建一个临时用户。
     * <p>
     * 去登录页的请求
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //从ThreadLocal里得到用户信息
        //UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //System.out.println(userInfoTo);
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart",cartVo);
        return "cartList";
    }

    /**
     * 添加到购物车
     * RedirectAttributes.addFlashAttribute();将数据放在session里面可以在页面取出，但是只能取一次
     * RedirectAttributes.adAttribute("skuId",skuId);将数据放在url后面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes attributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){
        //重定向到成功页面。再次查询购物车数据即可
        CartItemVo item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }
}
