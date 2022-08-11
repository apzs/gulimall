package com.atguigu.gulimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description:
 */
@ConfigurationProperties(prefix = "oauth2.gitee")
@Component
@Data
public class Oauth2FormGitee {

    private String clientId;
    private String redirectUri;
    private String clientSecret;
}
