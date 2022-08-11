package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.entity.OauthGiteeEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 无名氏
 * @date 2022/8/7
 * @Description:
 */
public interface OauthGiteeService extends IService<OauthGiteeEntity> {
    /**
     * 根据gitee的Id查询MemberId
     * @param id
     * @return
     */
    public Long getMemberId(Long id);
}
