package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.common.to.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionSkusVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 无名氏
 * @date 2022/8/23
 * @Description:
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    /**
     * seckill:stock: + 商品随机码
     */
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.扫描需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.isOk()) {
            //上架商品
            List<SeckillSessionSkusVo> sessionSkusVos = r.getDataArray(SeckillSessionSkusVo.class);
            //缓存活动信息
            saveSessionInfos(sessionSkusVos);
            //缓存活动的关联商品信息
            saveSessionSkuInfos(sessionSkusVos);
        }
    }

    /**
     * 5、自定义受保护的资源
     * 1)、代码
     *      try(Entry entry = SphU. entry( "seckillSkus")){
     *      //业务逻辑
     *      }catch(Execption e){}
     * 2)、基于注解。
     *      @SentineLResource(vaLue = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
     * 无论是1, 2方式一定要配置被限流以后的默认返回.
     * url请求可以设置统一返回WebCallbackManager
     */
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus(BlockException e){
        log.error("getCurrentSeckillSkus被限流了");
        return null;
    }

    @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //try(需要释放的资源){}   ==> 用try-with-resources代替try-catch-finally
        try(Entry entry = SphU.entry("seckillSkus")) {
            long time = System.currentTimeMillis();
            Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys) {
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] split = replace.split("_");
                Long start = Long.parseLong(split[0]);
                Long end = Long.parseLong(split[1]);
                if (time >= start && time <= end) {
                    //这里的 -1相当于length-1，即最后一个元素。取出的结果为[0,length-1] 包含开始和最后的元素，即所有元素
                    List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                    BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

                    if (range != null) {
                        List<String> list = operations.multiGet(range);
                        if (!CollectionUtils.isEmpty(list)) {
                            List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                                SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(item.toString(), SeckillSkuRedisTo.class);
                                //秒杀开始后可以查看到随机码
                                //seckillSkuRedisTo.setRandomCode(null);
                                return seckillSkuRedisTo;
                            }).collect(Collectors.toList());
                            return collect;
                        }
                    }
                    //只要找到了在当前时间范围内的秒杀，不管range是否为null都退出循环(当然如果range不为null,直接就return了)
                    break;
                }
            }
        }catch (BlockException e){
            log.error("资源被限流:{}",e.getMessage());
        }
        return null;
    }

    /**
     * 查询指定sku的一个秒杀信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String s = hashOps.get(key);
                    if (s == null) {
                        return null;
                    }
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
                    long now = System.currentTimeMillis();
                    if (now < seckillSkuRedisTo.getStartTime() || now > seckillSkuRedisTo.getEndTime()) {
                        //不返回随机码
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberEntityTo memberEntityTo = LoginUserInterceptor.loginUser.get();

        //获取秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //4_1  (sessionId_skuId)
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);

        //校验合法性
        //1、校验开始时间和结束时间
        Long startTime = seckillSkuRedisTo.getStartTime();
        Long endTime = seckillSkuRedisTo.getEndTime();
        long now = System.currentTimeMillis();
        if (now < startTime || now > endTime) {
            return null;
        }
        //2、校验随机码、商品id、购买数量（我感觉商品id没必要校验）
        String randomCode = seckillSkuRedisTo.getRandomCode();
        String skuCode = seckillSkuRedisTo.getPromotionSessionId() + "_" + seckillSkuRedisTo.getSkuId();
        int limitNum = seckillSkuRedisTo.getSeckillLimit().intValue();
        if (!randomCode.equals(key) || !skuCode.equals(killId) || num > limitNum) {
            return null;
        }
        //3、校验该用户是否已经购买过，防止无限次购买(幂等性)  userId_sessionId_skuId
        String userKey = memberEntityTo.getId() + "_" + killId;
        long ttl = endTime - startTime;
        Boolean firstBuy = redisTemplate.opsForValue().setIfAbsent(userKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        //用户已经买过了
        if (firstBuy!=null && !firstBuy) {
            return null;
        }
        //占位成功，用户从未购买该商品
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
        //阻塞获取信号量，一直等待别人释放信号量（不能使用此方式获取信号量）
        //semaphore.acquire();
        //100毫秒内试一下，看是否能获取指定数量的信号量
        try {
            boolean b = semaphore.tryAcquire(num,100,TimeUnit.MILLISECONDS);
            //秒杀成功，快速生成订单,给mq发送一个消息
            if (b){
                String orderSn = IdWorker.getTimeId();
                SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                secKillOrderTo.setMemberId(memberEntityTo.getId());
                secKillOrderTo.setOrderSn(orderSn);
                secKillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                secKillOrderTo.setNum(num);
                secKillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                secKillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",secKillOrderTo);
                return orderSn;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionSkusVo> sessionSkusVos) {
        if (StringUtils.isEmpty(sessionSkusVos)) {
            return;
        }
        sessionSkusVos.forEach(session -> {
            long start = session.getStartTime().getTime();
            long end = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + start + "_" + end;
            //缓存活动信息
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey == null || !hasKey && CollectionUtils.isEmpty(session.getRelationSkus())) {
                List<String> values = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString())
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, values);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionSkusVo> sessionSkusVos) {
        if (StringUtils.isEmpty(sessionSkusVos)) {
            return;
        }
        sessionSkusVos.forEach(session -> {
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().forEach(seckillSkuRelationVo -> {
                        String skuKey = seckillSkuRelationVo.getPromotionSessionId().toString() + "_" + seckillSkuRelationVo.getSkuId().toString();
                        Boolean hasSkuKey = operations.hasKey(skuKey);
                        if (hasSkuKey == null || !hasSkuKey) {
                            SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                            BeanUtils.copyProperties(seckillSkuRelationVo, seckillSkuRedisTo);
                            R r = productFeignService.getSkuInfo(seckillSkuRelationVo.getSkuId());
                            if (r.isOk()) {
                                SeckillSkuRedisTo.SkuInfoVo skuInfoVo = r.get("skuInfo", SeckillSkuRedisTo.SkuInfoVo.class);
                                seckillSkuRedisTo.setSkuInfoVo(skuInfoVo);
                            }
                            //设置开始和结束时间
                            seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                            seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                            //设置随机码（只有秒杀开始的那一刻，才暴露随机码）（防止活动还没开始就准备好脚本，开始时直接抢购）
                            String token = UUID.randomUUID().toString().replace("-", "");
                            seckillSkuRedisTo.setRandomCode(token);
                            operations.put(skuKey, JSON.toJSONString(seckillSkuRedisTo));
                            //使用分布式信号量限流（只有上架了商品，才有库存信息）
                            //（但是我感觉这有问题，有可能2个活动都上架了该商品，这两个都应该设置不同的促销信息和库存，而这个库存是判断skuId存不存在
                            // 因此不同活动不能上架同一款商品，我觉得应该放skuId+活动时间/随机码，这样才能区分是哪个活动，才能让不同的活动上架同一款商品，保证每个活动的促销信息和库存不一样）
                            //信号量的key为`前缀+token`    value为商品的库存
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                            semaphore.trySetPermits(seckillSkuRelationVo.getSeckillCount().intValue());
                        }
                    }
            );
        });
    }
}
