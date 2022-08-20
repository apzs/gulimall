package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.enums.OrderStatusEnum;
import com.atguigu.common.mq.StockLockedTo;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.to.ware.WareSkuLockTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;

    /**
     * {
     * page: 1,//当前页码
     * limit: 10,//每页记录数
     * sidx: 'id',//排序字段
     * order: 'asc/desc',//排序方式
     * wareId: 123,//仓库id
     * skuId: 123//商品id
     * }
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        String skuId = (String) params.get("skuId");
        lambdaQueryWrapper.eq(StringUtils.hasLength(skuId), WareSkuEntity::getSkuId, skuId);

        String wareId = (String) params.get("wareId");
        lambdaQueryWrapper.eq(StringUtils.hasLength(wareId), WareSkuEntity::getWareId, wareId);

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOrUpdateStockBatchByskuIdAndwareId(Collection<PurchaseDetailEntity> purchaseDetailList) {
        purchaseDetailList.forEach(this::addOrUpdateStockByskuIdAndwareId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateStockByskuIdAndwareId(PurchaseDetailEntity purchaseDetailEntity) {
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        wareSkuEntity.setSkuId(purchaseDetailEntity.getSkuId());
        wareSkuEntity.setWareId(purchaseDetailEntity.getWareId());

        LambdaQueryWrapper<WareSkuEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WareSkuEntity::getSkuId, purchaseDetailEntity.getSkuId())
                .eq(WareSkuEntity::getWareId, purchaseDetailEntity.getWareId());
        WareSkuEntity query = wareSkuDao.selectOne(lambdaQueryWrapper);
        if (query == null || query.getSkuName() == null) {
            //远程查询sku的名字；如果失败，整个事务不回滚
            //1、 自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚?高级
            try {
                R info = productFeignService.info(wareSkuEntity.getSkuId());
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
            }
        }
        if (query == null) {
            wareSkuEntity.setStockLocked(0);
            wareSkuEntity.setStock(purchaseDetailEntity.getSkuNum());
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuEntity.setId(query.getId());
            wareSkuEntity.setStock(query.getStock() + purchaseDetailEntity.getSkuNum());
            wareSkuDao.updateById(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo vo = new SkuHasStockTo();
            //SELECT SUM (stock) FROM、 wms ware skui WHERE sku id=1
            Long count = this.baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }


    /**
     * 为订单锁定库存
     *
     * @param wareSkuLockTo
     * @return 库存解锁的场景
     * 1)、下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     * 2)、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存就要自动解锁。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean orderLockStock(WareSkuLockTo wareSkuLockTo) {

        //保存库存工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockTo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //按照下单的收货地址，找到一个就近仓库，锁定库存。
        //找到每个商品在哪个仓库都有库存
        List<WareSkuLockTo.OrderItemVo> locks = wareSkuLockTo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(orderItemVo -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = orderItemVo.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            //select ware_id from wms_ware_sku where sku_id = 1 and stock - stock_locked > 0
            List<Long> wareId = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareId);
            skuWareHasStock.setNum(orderItemVo.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            //没有库存
            if (CollectionUtils.isEmpty(wareIds)) {
                throw new NoStockException(skuId);
            }
            //锁定库存
            for (Long wareId : wareIds) {
                //成功返回1，失败返回0
                //update wms_ware_sku set stock_locked = stock_locked+2 where sku_id=1 and ware_id = 1 and stock - stock_locked>=2
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    //锁库存成功
                    skuStocked = true;
                    //保存工作单详情
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(skuId);
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailEntity.setWareId(wareId);
                    wareOrderTaskDetailEntity.setSkuNum(hasStock.getNum());
                    wareOrderTaskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //向RabbitMQ发送一条消息
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    //只发id不行，防止回滚以后找不到数据(我觉得只发id没问题)
                    StockLockedTo.StockDetailTo stockDetailTo = new StockLockedTo.StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {
                    //锁库存成功
                }
            }
            if (!skuStocked) {
                //当前商品没有库存了
                throw new NoStockException(skuId);
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {
        //工作单详情
        StockLockedTo.StockDetailTo detail = stockLockedTo.getDetail();
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detail.getId());
        if (wareOrderTaskDetailEntity == null) {
            //工作单详情里没有数据，无需解锁，确认收到消息
            return;
        }
        /**
         * 解锁库存
         * 1、没有订单：证明锁定库存后面的业务出问题了，这种情况需要解锁库存
         * 2、有订单：如果有订单需要判断订单状态，如果订单状态为`未支付`或`用户主动取消` 这时需要解锁库存
         */
        //库存工作单id
        Long wareOrderTaskId = stockLockedTo.getId();
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(wareOrderTaskId);
        R r = orderFeignService.getOrderStatus(wareOrderTaskEntity.getOrderSn());
        if (r.getCode() != 0) {
            //消息拒绝以后重新放到队列里面，让别人继续消费解锁。
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            throw new RuntimeException("获取订单状态异常");
        }
        Object data = r.get("data");
        OrderVo orderVo = null;
        if (data != null) {
            String s = JSON.toJSONString(data);
            orderVo = JSON.parseObject(s, OrderVo.class);
        }
        //没有订单或订单状态为待付款或取消 并且工作单的锁定状态为已锁定
        if ((data == null || OrderStatusEnum.CANCLED.getCode().equals(orderVo.getStatus()))
                && wareOrderTaskDetailEntity.getLockStatus() == 1) {
            this.unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。查订单状态新建状态，什么都不做就走了。
     * 导致卡顿的订单，永远不能解锁库存
     *
     * @param orderTo
     */
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> orderTaskDetailEntities = wareOrderTaskDetailService.getOrderTaskDetailsByTaskId(taskId);
        for (WareOrderTaskDetailEntity entity : orderTaskDetailEntities) {
            this.unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        //update wms_ware_sku set stock_locked = stock_locked - 1 where sku_id = 1 and ware_id = 1
        wareSkuDao.unLockStock(skuId, wareId, num);
        //更新库存工作单状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        //已解锁
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }


    /**
     * 判断哪些商品有库存
     */
    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }


}