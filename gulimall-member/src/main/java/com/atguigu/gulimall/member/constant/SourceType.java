package com.atguigu.gulimall.member.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author 无名氏
 * @date 2022/8/7
 * @Description:
 */
public enum SourceType {
    /**
     *
     */
    UN_KNOW(0,"未知方式"),
    /**
     * 用户通过本系统注册
     */
    REGISTER(1,"注册"),
    /**
     * 通过Gitte授权登录
     */
    GITEE_LOGIN(2,"gitee登录"),
    /**
     * 通过github授权登录
     */
    GITHUB_LOGIN(3,"github登录");

    /**
     * 根据sourceType的值向数据库中存储
     */
    @EnumValue
    private int sourceType;
    /**
     * JSON通过该值序列化（可用用在可以用在get方法或者属性字段上，一个类只能用一个，序列化只包含该值）
     */
    @JsonValue
    private String description;

    SourceType(int sourceType,String description) {
        this.sourceType = sourceType;
        this.description = description;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
