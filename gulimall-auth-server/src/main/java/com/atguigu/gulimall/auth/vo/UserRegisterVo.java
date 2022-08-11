package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author 无名氏
 * @date 2022/8/4
 * @Description: 注册表单
 */
@Data
public class UserRegisterVo {

    /**
     * 用户名
     */
    @NotNull(message = "用户名必须填写")
    @Length(min = 6,max = 18,message = "用户名必须是6-18位字符")
    private String username;
    /**
     * 密码
     */
    @NotNull(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码必须是6- 18位字符")
    private String password;
    /**
     * 手机号
     */
    @NotNull(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String phone;
    /**
     * 验证码
     */
    @NotNull(message = "验证码必须填写")
    private String code;
}
