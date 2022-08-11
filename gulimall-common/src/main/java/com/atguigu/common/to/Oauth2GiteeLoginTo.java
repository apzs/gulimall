package com.atguigu.common.to;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description:
 */
@Data
public class Oauth2GiteeLoginTo {
    private Long id;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;
    private String scope;
    private String avatarUrl;
    private Long createdAt;
    private String name;
}
