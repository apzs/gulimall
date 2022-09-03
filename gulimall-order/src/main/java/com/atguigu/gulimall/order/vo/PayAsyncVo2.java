package com.atguigu.gulimall.order.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/22
 * @Description:
 */
@ToString
@Data
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PayAsyncVo2 {

    private Date gmtCreate;
    private String charset;
    private Date gmtPayment;
    private Date notifyTime;
    private List<String> subject;
    private List<String> sign;
    /**
     * 支付者的id
     */
    @JsonAlias("buyer_id")
    private List<String> buyerId;
    /**
     * 订单的信息
     */
    private List<String> body;
    /**
     * 支付金额
     */
    private List<String> invoiceAmount;
    private List<String> version;
    /**
     * 通知id
     */
    private List<String> notifyId;
    private List<String> fundBillList;
    /**
     * 通知类型； trade_status_sync
     */
    private List<String> notifyType;
    /**
     * 订单号
     */
    private List<String> outTradeNo;
    /**
     * 支付的总额
     */
    private List<String> totalAmount;
    /**
     * 交易状态  TRADE_SUCCESS
     */
    private List<String> tradeStatus;
    /**
     * 流水号
     */
    private List<String> tradeNo;
    private List<String> authAppId;
    /**
     * 商家收到的款
     */
    private List<String> receiptAmount;
    private List<String> pointAmount;
    /**
     * 应用id
     */
    private List<String> appId;
    /**
     * 最终支付的金额
     */
    private List<String> buyerPayAmount;
    /**
     * 签名类型
     */
    private List<String> signType;
    /**
     * 商家的id
     */
    private List<String> sellerId;
}