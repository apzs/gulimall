package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-17 23:11:05
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
