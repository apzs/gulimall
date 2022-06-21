package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    /**
     * {
     *    page: 1,//当前页码
     *    limit: 10,//每页记录数
     *    sidx: 'id',//排序字段
     *    order: 'asc/desc',//排序方式
     *    key: '华为',//检索关键字
     *    status: 0,//状态
     *    wareId: 1,//仓库id
     * }
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        LambdaQueryWrapper<PurchaseDetailEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
            lambdaQueryWrapper.and(StringUtils.hasLength(key),wrapper -> {
                wrapper.eq(PurchaseDetailEntity::getSkuId, key).or().eq(PurchaseDetailEntity::getPurchaseId, key);
            });

        String status = (String) params.get("status");
        lambdaQueryWrapper.eq(StringUtils.hasLength(status),PurchaseDetailEntity::getStatus,status);

        String wareId = (String) params.get("wareId");
        lambdaQueryWrapper.eq(StringUtils.hasLength(wareId),PurchaseDetailEntity::getWareId,wareId);

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 更新采购需求(采购项)的 "采购单id"为指定采购id列表的字段
     * @param purchaseDetailEntity 需要修改的采购项字段
     * @param purchaseEntities  采购项的采购单id列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePurchaseDetailBatchByPurchaseId(PurchaseDetailEntity purchaseDetailEntity, List<PurchaseEntity> purchaseEntities) {
        List<Long> purchaseIds = purchaseEntities.stream().map(PurchaseEntity::getId).collect(Collectors.toList());

        LambdaQueryWrapper<PurchaseDetailEntity> purchaseDetailQueryWrapper = new LambdaQueryWrapper<>();
        purchaseDetailQueryWrapper.in(PurchaseDetailEntity::getPurchaseId,purchaseIds);

        this.update(purchaseDetailEntity,purchaseDetailQueryWrapper);
    }

}