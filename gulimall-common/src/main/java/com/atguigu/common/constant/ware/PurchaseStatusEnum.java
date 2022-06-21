package com.atguigu.common.constant.ware;

/**
 * @author 无名氏
 * @date 2022/6/14
 * @Description:
 */
public enum PurchaseStatusEnum {
    /**
     * 刚新建状态
     */
    CREATED(0,"新建"),
    /**
     * 已分配给采购员
     */
    ASSIGNED(1,"已分配"),
    /**
     * 采购员已领取
     */
    RECEIVE(2,"已领取"),
    /**
     * 采购员已完成采购
     */
    FINISHED(3,"已完成"),
    /**
     * 采购异常
     */
    HASERROR(4,"有异常");

    private int status;
    private String msg;

    PurchaseStatusEnum(int status, String msg) {
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
