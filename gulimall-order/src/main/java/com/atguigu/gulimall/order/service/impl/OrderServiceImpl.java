package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.to.ware.WareSkuLockTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RS;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WmsFeignService wmsFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberEntityTo memberEntityTo = LoginUserInterceptor.loginUser.get();
        System.out.println("主线程：" + Thread.currentThread().getId());
        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1、远程查询所有的收货地址列表
            System.out.println("getAddressFuture线程：" + Thread.currentThread().getId());
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderConfirmVo.MemberAddressVo> address = memberFeignService.getAddress(memberEntityTo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //2、远程查询购物车所有选中的购物项
            System.out.println("cartFuture线程：" + Thread.currentThread().getId());
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderConfirmVo.OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderConfirmVo.OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(OrderConfirmVo.OrderItemVo::getSkuId).collect(Collectors.toList());
            RS<List<SkuHasStockTo>> skuHasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuHasStockTo> data = skuHasStock.getData();
            if (!CollectionUtils.isEmpty(data)) {
                Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                orderConfirmVo.setStocks(stocks);
            }
        }, executor);
        //3、查询用户积分
        Integer integration = memberEntityTo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //orderConfirmVo.setIntegration(orderConfirmVo.getIntegration());
        orderConfirmVo.setPayPrice(orderConfirmVo.getPayPrice());
        orderConfirmVo.setTotal(orderConfirmVo.getTotal());
        //TODO 防重令牌
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityTo.getId();
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key, token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();
        return orderConfirmVo;
    }

    /**
     * rollbackFor: 哪些异常需要回滚
     * isolation:   事务的隔离级别(mysql默认为 REPEATABLE_READ 可重复读)
     * propagation: 事务的传播行为
     * @param vo
     * @return
     */
    //@GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        orderSubmitVoThreadLocal.set(vo);
        MemberEntityTo memberEntityTo = LoginUserInterceptor.loginUser.get();
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityTo.getId();

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        //下单:去创建订单，验令牌，验价格，锁库存...
        String orderToken = vo.getOrderToken();
        //验证并删除令牌[令牌的对比和删除必须保证原子性]
        //0:令牌失败  -  1:删除成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(key), orderToken);
        if (result == null || result == 0L) {
            //令牌验证失败
            response.setCode(1);
            return response;
        }
        //令牌验证成功
        //创建订单
        OrderCreateTo orderCreateTo = createOrder();
        //验价
        if (Math.abs(orderCreateTo.getPayPrice().subtract(vo.getPayPrice()).doubleValue()) < 0.01) {
            //保存订单
            this.saveOrder(orderCreateTo);
            //锁定库存
            WareSkuLockTo wareSkuLockTo = new WareSkuLockTo();
            wareSkuLockTo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
            List<WareSkuLockTo.OrderItemVo> orderItemVos = orderCreateTo.getOrderItems().stream().map(orderItemEntity -> {
                WareSkuLockTo.OrderItemVo orderItemVo = new WareSkuLockTo.OrderItemVo();
                orderItemVo.setSkuId(orderItemEntity.getSkuId());
                orderItemVo.setCount(orderItemEntity.getSkuQuantity());
                orderItemVo.setTitle(orderItemEntity.getSkuName());
                return orderItemVo;
            }).collect(Collectors.toList());
            wareSkuLockTo.setLocks(orderItemVos);
            R r = wmsFeignService.orderLockStock(wareSkuLockTo);
            if (r.getCode() == 0) {
                //锁定库存成功
                response.setCode(0);
                response.setOrder(orderCreateTo.getOrder());
                //int i = 10/0;
                //订单创建成功，发消息给RabbitMQ
                rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderCreateTo.getOrder());
                return response;
            } else {
                //锁定库存失败
                response.setCode(3);
                throw new RuntimeException("锁定库存失败");
                //return response;
            }
        } else {
            //金额对比失败
            response.setCode(2);
            return response;
        }
    }

    @Override
    public OrderEntity getOrderStatusByOrderSn(String orderSn) {
        LambdaQueryWrapper<OrderEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderEntity::getOrderSn,orderSn);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        OrderEntity orderEntity = this.getById(entity.getId());
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            OrderEntity update = new OrderEntity();
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            orderTo.setStatus(OrderStatusEnum.CANCLED.getCode());
            try {
                //TODO 保证消息一定会发送出去，每一个消息都可以做好日志记录(给数据库保存每一个消息的详细信息)。
                //定期扫描数据库将失败的消息再发送一遍;
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            }catch (Exception e){
                //将没法送成功的消息进行重试发送。
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存订单数据
     *
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity orderEntity = orderCreateTo.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        for (OrderItemEntity orderItem : orderCreateTo.getOrderItems()) {
            orderItemService.save(orderItem);
        }
    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        //构造订单
        OrderEntity order = buildOrder(orderSn);
        //构造订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        OrderEntity orderEntity = computePrice(order, orderItemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);

        orderCreateTo.setPayPrice(orderEntity.getPayAmount());
        orderCreateTo.setFare(orderEntity.getFreightAmount());
        return orderCreateTo;
    }

    /**
     * 订单的总额（订单项的实际应付价格相加，不包括运费）
     *
     * @param orderEntity
     * @param orderItemEntities
     * @return
     */
    private OrderEntity computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //优惠券
        BigDecimal coupon = new BigDecimal(0);
        //积分
        BigDecimal integration = new BigDecimal(0);
        //商品促销
        BigDecimal promotion = new BigDecimal(0);
        //订单项实际应付价格总和
        BigDecimal total = new BigDecimal(0);
        //获得的积分
        BigDecimal giftIntegration = new BigDecimal(0);
        //获得的成长值
        BigDecimal giftGrowth = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(orderItemEntities)) {
            for (OrderItemEntity orderItemEntity : orderItemEntities) {
                coupon = coupon.add(orderItemEntity.getCouponAmount());
                integration = integration.add(orderItemEntity.getIntegrationAmount());
                promotion = promotion.add(orderItemEntity.getPromotionAmount());
                total = total.add(orderItemEntity.getRealAmount());
                giftIntegration = giftIntegration.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));
                giftGrowth = giftGrowth.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));
            }
        }

        //设置积分信息
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        //订单项实际应付价格总和
        orderEntity.setTotalAmount(total);
        //应付总额 = 订单项实际应付价格总和 + 运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        //设置获得的积分和成长值信息
        orderEntity.setIntegration(giftIntegration.intValue());
        orderEntity.setGrowth(giftGrowth.intValue());
        return orderEntity;
    }

    /**
     * 构建订单数据
     *
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        //设置订单号
        orderEntity.setOrderSn(orderSn);
        MemberEntityTo memberEntityTo = LoginUserInterceptor.loginUser.get();
        orderEntity.setMemberId(memberEntityTo.getId());

        //获取收货地址信息
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        R r = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        Object data = r.get("data");
        String s = JSON.toJSONString(data);
        FareVo fareResp = JSON.parseObject(s, FareVo.class);
        //设置运费金额
        orderEntity.setFreightAmount(fareResp.getFare());
        FareVo.MemberAddressVo addressVo = fareResp.getMemberAddressVo();
        //设置城市
        orderEntity.setReceiverCity(addressVo.getCity());
        //设置详细地址
        orderEntity.setReceiverDetailAddress(addressVo.getDetailAddress());
        //设置收货人姓名
        orderEntity.setMemberUsername(addressVo.getName());
        //设置手机号
        orderEntity.setReceiverPhone(addressVo.getPhone());
        //设置邮政编码
        orderEntity.setReceiverPostCode(addressVo.getPostCode());
        //设置省份/直辖市
        orderEntity.setReceiverProvince(addressVo.getProvince());
        //设置区
        orderEntity.setReceiverRegion(addressVo.getRegion());

        //设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //自动确认时间
        orderEntity.setAutoConfirmDay(7);
        //删除状态（0代表未删除）
        orderEntity.setDeleteStatus(0);
        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项价格
        List<OrderConfirmVo.OrderItemVo> orderItemVos = cartFeignService.getCurrentUserCartItems();
        if (CollectionUtils.isEmpty(orderItemVos)) {
            return null;
        }

        return orderItemVos.stream().map(orderItemVo -> {
                    OrderItemEntity orderItemEntity = buildOrderItem(orderItemVo);
                    orderItemEntity.setOrderSn(orderSn);
                    return orderItemEntity;
                }
        ).collect(Collectors.toList());
    }

    /**
     * 构建某一个订单项数据
     *
     * @param orderItemVo
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderConfirmVo.OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1、订单信息:订单号 v
        //2、商品的SPU信息
        Long skuId = orderItemVo.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        Object data = r.get("data");
        String s = JSON.toJSONString(data);
        SpuInfoVo spuInfoVo = JSON.parseObject(s, SpuInfoVo.class);
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //3、商品的sku信息 v
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImage());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        String skuAttrVals = StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrVals);
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        //4、优惠信息[不做]

        //5、积分信息
        Integer total = orderItemVo.getPrice().intValue() * orderItemVo.getCount();
        //成长值
        orderItemEntity.setGiftGrowth(total);
        //积分
        orderItemEntity.setGiftIntegration(total);

        //6、订单项的价格信息
        //促销价格
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        //用完优惠券后的价格
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        //使用积分后的价格
        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        //当前订单项的实际价格(总价-优惠信息)
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realAmount = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }

}