package com.atguigu.gulimall.member.exception;

/**
 * @author 无名氏
 * @date 2022/8/5
 * @Description:
 */
public class UsernameExistException extends RuntimeException{
    public UsernameExistException(){
        super("用户名存在");
    }
}
