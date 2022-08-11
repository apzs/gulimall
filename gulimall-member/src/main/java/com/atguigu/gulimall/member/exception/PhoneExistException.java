package com.atguigu.gulimall.member.exception;

/**
 * @author 无名氏
 * @date 2022/8/5
 * @Description:
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号存在");
    }
}
