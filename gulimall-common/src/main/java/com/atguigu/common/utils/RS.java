/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import lombok.Data;

@Data
public class RS<T> {
    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private boolean success;

    private T data;


    public RS<T> ok(String msg) {
        return this.ok(0,msg);
    }

    public RS<T> ok() {
        return this.ok(0,"success");
    }

    public RS<T> ok(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.success = true;
        return this;
    }

    public RS<T> ok(T t) {
        this.data = t;
        return this;
    }

    public RS<T> setData(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public RS<T> error(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.success = false;
        return this;
    }


}
