package com.atguigu.gulimall.member.service;

import com.atguigu.common.to.Oauth2GiteeLoginTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 会员
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-18 22:06:32
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);


    void checkUsernameUnique(String username) throws UsernameExistException;

    void checkPhoneUnique(String phone) throws PhoneExistException;


    MemberEntity login(MemberLoginVo vo);


    MemberEntity giteeLogin(Oauth2GiteeLoginTo to);

}

