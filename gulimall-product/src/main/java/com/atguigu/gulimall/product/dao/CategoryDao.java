package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-17 18:19:58
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
