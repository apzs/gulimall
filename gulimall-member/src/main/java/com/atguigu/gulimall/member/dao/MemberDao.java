package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-18 22:06:32
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
