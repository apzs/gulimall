package com.atguigu.common.exception;

/**
 * @author 无名氏
 * @date 2022/5/7
 * @Description:
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 * 001：参数格式校验
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 * 15:用户
 * 20:库存
 */
public enum BizCodeException{
    /**
     * 系统未知异常
     */
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    /**
     * 参数格式校验失败
     */
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    /**
     * 同一手机号获取验证码频率太高
     */
    SMS_CODE_EXCEPTION(10002,"验证码获取频率太高，请稍后再试"),
    /**
     * 同一个接口QPS每秒发送的请求数过多
     */
    TOO_MANY_REQUEST(10003,"请求流量过大异常"),
    /**
     * 商品上架异常（向ElasticSearch里保存数据出错）
     */
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    /**
     * 用户名重复
     */
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    /**
     * 手机号重复
     */
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    /**
     * 登录的账号或密码错误 或 该用户不存在
     */
    ACCOUNT_PASSWORD_INVALID_EXCEPTION(15003,"账号或密码错误"),
    /**
     * 通过gitee登录失败
     */
    GITEE_LOGIN_EXCEPTION(15004,"通过gitee登录失败"),
    /**
     * 下订单锁库存，没有库存的异常
     */
    NO_STOCK_EXCEPTION(21000,"商品库存不足");


    private int code;
    private String msg;

    BizCodeException(int code, String msg) {
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
