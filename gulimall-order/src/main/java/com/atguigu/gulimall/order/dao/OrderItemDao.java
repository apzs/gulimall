package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-18 22:19:37
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
