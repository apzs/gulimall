package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/5
 * @Description:
 */
@Data
public class UserLoginVo {

    /**
     * 登录的账号(邮箱/用户名/手机号)
     */
    private String loginAccount;
    /**
     * 密码
     */
    private String password;
}
