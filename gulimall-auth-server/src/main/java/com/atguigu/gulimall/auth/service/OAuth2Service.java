package com.atguigu.gulimall.auth.service;

import com.atguigu.common.to.MemberEntityTo;

/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description:
 */
public interface OAuth2Service {

    MemberEntityTo giteeRegister(String code);
}
