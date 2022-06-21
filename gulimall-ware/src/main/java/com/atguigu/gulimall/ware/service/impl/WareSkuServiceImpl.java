package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;

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
        if (query==null || query.getSkuName()==null){
            //远程查询sku的名字；如果失败，整个事务不回滚
            //1、 自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚?高级
            try {
                R info = productFeignService.info(wareSkuEntity.getSkuId());
                if (info.getCode() == 0){
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


}