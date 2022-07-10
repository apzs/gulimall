package com.atguigu.common.constant.product;

/**
 * @author 无名氏
 * @date 2022/5/17
 * @Description: spu的状态
 */
public enum StatusEnum {
    /**
     * 新建商品
     */
    NEW_SPU(0,"新建商品"),
    /**
     * 上架商品
     */
    SPU_UP(1,"上架商品"),
    /**
     * 下架商品
     */
    SPU_DOWN(2,"下架商品");

    private int code;
    private String msg;

    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

