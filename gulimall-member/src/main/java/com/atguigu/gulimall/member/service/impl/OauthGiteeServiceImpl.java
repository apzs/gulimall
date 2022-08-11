package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.dao.OauthGiteeDao;
import com.atguigu.gulimall.member.entity.OauthGiteeEntity;
import com.atguigu.gulimall.member.service.OauthGiteeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author 无名氏
 * @date 2022/8/7
 * @Description:
 */
@Service
public class OauthGiteeServiceImpl extends ServiceImpl<OauthGiteeDao, OauthGiteeEntity> implements OauthGiteeService {
    @Override
    public Long getMemberId(Long id) {
        LambdaQueryWrapper<OauthGiteeEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OauthGiteeEntity::getId, id).select(OauthGiteeEntity::getMemberId);
        OauthGiteeEntity oauthGiteeEntity = this.baseMapper.selectOne(lambdaQueryWrapper);
        if (oauthGiteeEntity!=null){
            return oauthGiteeEntity.getMemberId();
        }
        return null;
    }
}
