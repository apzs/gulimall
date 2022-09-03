package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        String promotionSessionId = (String) params.get("promotionSessionId");
        LambdaQueryWrapper<SeckillSkuRelationEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.and(StringUtils.hasText(key),(l)->l.eq(SeckillSkuRelationEntity::getId,key)
                .or().eq(SeckillSkuRelationEntity::getPromotionId,key)
                .or().eq(SeckillSkuRelationEntity::getSkuId,key));
        lambdaQueryWrapper.eq(StringUtils.hasText(promotionSessionId),SeckillSkuRelationEntity::getPromotionSessionId,promotionSessionId);

        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

}