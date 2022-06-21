package com.atguigu.common.constant.ware;

/**
 * @author 无名氏
 * @date 2022/6/14
 * @Description:
 */
public enum PurchaseDetailStatusEnum {
    /**
     * 刚新建状态
     */
    CREATED(0,"新建"),
    /**
     * 已分配给采购员
     */
    ASSIGNED(1,"已分配"),
    /**
     * 采购员正在采购
     */
    BUYING(2,"正在采购"),
    /**
     * 采购员已完成采购
     */
    FINISHED(3,"已完成"),
    /**
     * 采购员采购失败
     */
    HASERROR(4,"采购失败");

    private int status;
    private String msg;

    PurchaseDetailStatusEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
