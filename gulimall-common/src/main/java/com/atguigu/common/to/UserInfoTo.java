package com.atguigu.common.to;

import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description:
 */
@Data
public class UserInfoTo {
    /**
     * 用户的id
     */
    private Long userId;
    /**
     * 用户的标识
     */
    private String userKey;
    /**
     * 是否有临时用户的cookie
     */
    private boolean hasTempUserCookie = false;
}
