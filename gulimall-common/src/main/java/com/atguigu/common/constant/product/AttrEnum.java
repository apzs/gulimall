package com.atguigu.common.constant.product;

/**
 * @author 无名氏
 * @date 2022/5/17
 * @Description:
 */
public enum AttrEnum {
    /**
     * 基本属性
     */
    ATTR_TYPE_BASE(1,"基本属性"),
    /**
     * 销售属性
     */
    ATTR_TYPES_SALE(0,"销售属性");

    private int code;
    private String msg;

    AttrEnum(int code, String msg) {
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
