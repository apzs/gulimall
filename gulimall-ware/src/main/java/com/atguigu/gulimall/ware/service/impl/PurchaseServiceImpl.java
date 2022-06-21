package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.ware.PurchaseDetailStatusEnum;
import com.atguigu.common.constant.ware.PurchaseStatusEnum;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        LambdaQueryWrapper<PurchaseEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询状态为0(新建) 或 1(已分配) 的采购单
        lambdaQueryWrapper.eq(PurchaseEntity::getStatus, 0).or().eq(PurchaseEntity::getStatus, 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(PurchaseStatusEnum.CREATED.getStatus());
            //自动更新PurchaseEntity的更新时间
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
            //TODO 确认采购单状态是0，1才可以合并
        } else {
            //更新PurchaseEntity(采购单)的更新时间
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.ASSIGNED.getStatus());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        //合并采购需求，分派到指定采购单
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
    }

    /**
     * 采购员领取采购单
     *
     * @param purchaseIds 采购单id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void received(List<Long> purchaseIds) {
        //1、确认当前采购单的id是"新建"或者是"已分配"状态
        LambdaQueryWrapper<PurchaseEntity> purchaseQueryWrapper = new LambdaQueryWrapper<>();
        purchaseQueryWrapper.and(wrapper -> {
            wrapper.eq(PurchaseEntity::getStatus, PurchaseStatusEnum.CREATED.getStatus())
                    .or().eq(PurchaseEntity::getStatus, PurchaseStatusEnum.ASSIGNED.getStatus());
        }).in(PurchaseEntity::getId, purchaseIds);
        List<PurchaseEntity> purchaseEntities = this.list(purchaseQueryWrapper);

        //2、改变采购单状态(已使用注解在更新字段时自动更新updateTime)
        //TODO 设置采购人id，采购人名、联系方式
        List<PurchaseEntity> newPurchaseEntities = purchaseEntities.stream().map(purchaseEntity -> {
            purchaseEntity.setStatus(PurchaseStatusEnum.RECEIVE.getStatus());
            return purchaseEntity;
        }).collect(Collectors.toList());
        this.updateBatchById(newPurchaseEntities);

        //3、改变采购项状态
        PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
        purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.BUYING.getStatus());
        purchaseDetailService.updatePurchaseDetailBatchByPurchaseId(purchaseDetailEntity,newPurchaseEntities);
    }

    /**
     * 采购员完成采购
     * @param purchaseDoneVo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void donePurchase(PurchaseDoneVo purchaseDoneVo) {

        AtomicBoolean flag = new AtomicBoolean(true);
        //1、改变采购项状态
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDoneVo.getItems().stream().map(purchaseItemDone -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (purchaseItemDone.getStatus() == PurchaseDetailStatusEnum.HASERROR.getStatus()) {
                flag.set(false);
                purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.HASERROR.getStatus());
            } else if (purchaseItemDone.getStatus() == PurchaseDetailStatusEnum.FINISHED.getStatus()){
                purchaseDetailEntity.setStatus(PurchaseDetailStatusEnum.FINISHED.getStatus());
            }
            purchaseDetailEntity.setId(purchaseItemDone.getItemId());
            return purchaseDetailEntity;
        }).filter(purchaseDetailEntity->{
            return purchaseDetailEntity.getStatus()!=null;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        //2、改变采购单状态
        Long purchaseId = purchaseDoneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        Integer status = flag.get()?PurchaseStatusEnum.FINISHED.getStatus() : PurchaseStatusEnum.HASERROR.getStatus();
        purchaseEntity.setStatus(status);
        this.updateById(purchaseEntity);

        //3、将成功采购的进行入库
        List<Long> purchaseDetailIds = purchaseDoneVo.getItems().stream().filter(purchaseItemDone -> {
            return purchaseItemDone.getStatus() == PurchaseDetailStatusEnum.FINISHED.getStatus();
        }).map(PurchaseDoneVo.PurchaseItemDone::getItemId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(purchaseDetailIds)) {
            Collection<PurchaseDetailEntity> purchaseDetailList = purchaseDetailService.listByIds(purchaseDetailIds);
            wareSkuService.addOrUpdateStockBatchByskuIdAndwareId(purchaseDetailList);
        }
    }

}