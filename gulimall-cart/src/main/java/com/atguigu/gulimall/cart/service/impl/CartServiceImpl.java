package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.SkuInfoEntityTo;
import com.atguigu.common.to.UserInfoTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description:
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;

    private static final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        if (StringUtils.hasText(s)){
            CartItemVo cartItemVo = JSON.parseObject(s, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount()+num);
            //修改count后，重新计算totalPrice（总价）
            cartItemVo.setTotalPrice(cartItemVo.getTotalPrice());
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
            return cartItemVo;
        }

        CartItemVo cartItemVo = new CartItemVo();
        //购物车没有此商品
        //获取sku基本信息
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            R r = productFeignService.info(skuId);
            Object info = r.get("skuInfo");
            String jsonString = JSON.toJSONString(info);
            SkuInfoEntityTo infoEntityTo = JSON.parseObject(jsonString, SkuInfoEntityTo.class);
            //第一次添加，默认选中
            cartItemVo.setCheck(true);
            //第一次添加，数量都为1
            cartItemVo.setCount(num);
            cartItemVo.setImage(infoEntityTo.getSkuDefaultImg());
            cartItemVo.setTitle(infoEntityTo.getSkuTitle());
            cartItemVo.setSkuId(infoEntityTo.getSkuId());
            cartItemVo.setPrice(infoEntityTo.getPrice());
        },executor);

        //远程查询sku组合信息
        CompletableFuture<Void> getSkuSaleAttrValuesFuture = CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItemVo.setSkuAttr(skuSaleAttrValues);
        }, executor);
        CompletableFuture.allOf(skuInfoFuture,getSkuSaleAttrValuesFuture).get();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
        return cartItemVo;
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(s,CartItemVo.class);
    }

    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //已登录
            //先判断临时购物车有没有数据
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> tempCartItems = getCartItems(tempCartKey);
            if (!CollectionUtils.isEmpty(tempCartItems)){
                //临时购物车有数据(合并到用户账户中)
                for (CartItemVo tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //删除redis里临时购物车的数据
                stringRedisTemplate.delete(tempCartKey);
            }
            // gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }else {
            //没登录
            // gulimall:cart:6a642344-003e-4ac1-bea1-27260c5c75c3
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            cartVo.setItems(getCartItems(cartKey));

        }

        return cartVo;
    }

    /**
     * 修改购物车中商品的选中状态
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        //存放到redis
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        //存放到redis
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }


    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (!CollectionUtils.isEmpty(values)){
            List<CartItemVo> items = values.stream().map(obj -> {
                String str = (String) obj;
                 return JSON.parseObject(str, CartItemVo.class);
            }).collect(Collectors.toList());

            return items;
        }
        return null;
    }

    /**
     * 获取要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {
            // gulimall:cart:6a642344-003e-4ac1-bea1-27260c5c75c3
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //stringRedisTemplate.opsForHash().get(cartKey,"1");
        return stringRedisTemplate.boundHashOps(cartKey);
    }
}
