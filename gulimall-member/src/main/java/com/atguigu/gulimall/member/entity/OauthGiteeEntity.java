package com.atguigu.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description:
 */
@Data
@TableName("oauth_gitee")
public class OauthGiteeEntity {
    @TableId(value = "id",type = IdType.INPUT)
    private long id;
    private long memberId;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;
    private String scope;
    private long createdAt;
    private String avatarUrl;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDate createdTime;
}
