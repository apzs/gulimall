package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    /**
     * 在支付宝创建的应用的id
     */
    private String appId = "2021000117672941";

    /**
     * 商户私钥，您的PKCS8格式RSA2私钥
     */
    public String merchantPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCOxczcRlCWEDYJJw8tfQOUquwmIdF1i+unRRrsulAjfTr37vWwIodjplfWUUrbICLhA16x783BwZThAcOzx95QCWTRNkgOQvI2S3pRhsfq4Epca1l1HzemqZegFrXY8X/Wy0jfDc/BHs/dQfgexCaP5C9gA6Dk2ziFfJQ5coNqAthPR6LMuORdqhPF8nmLwzl7MUrK6cBf42sBiTTRJ9Gr4RuGTBmRfkt/TWHXG0a8muO7Q4q+2ssEI5LZZyy6XZbILhrlXhDcIMfH3T/XKnUHo0EG5GkkyM+WU/WSRia5vMe+FMwmFg8bcVSBuutKRo6liZyqk6HXWwhP6AWFweDfAgMBAAECggEAC1N3t/XACNeKYdl+Y75qHU4d47yFEE0kSaW6yBLTrq0Nk4Oaa/mhQe99Qlw6kv2OpskMpxL+AKPpupvYOZfec/z6yXoD51W6P0JnlrYZf/GBFKnTc+4hDRChHasPjAFEgfaodkFzPL/D/6pvE4m+AWY5t+Uxy1X0uuoyHAiQkkU/LC6ryeeknTEjXDq3kOw6fl616IMgDJgjjd8nRvb+J9tlp5WAk7mdTVQb1rFNZ3HUJjiSapO2Io+qxMm3FhlTiayK/a4qlSq7N41kALRqFR+ReAmTQ1vK3IFp0OsZx/4SS+ecz6V8A72RPlmWTNxF3X2LJykupe6GnF+a7QF3WQKBgQDA7aS9peK+iTMijvNvXKJJ+MQ4nXLNc5BeqkanyOoqMPcIOtulYKA9nLU0mRlnX06YXNPLTS0Fxp+GsmZRW8MsZUuncXZKPwCwfR4JND4/a21L0NcjDkpZzuw3xg7kbERmyHCb3kaT1Yfbnnmr/qK2LHHxFCu80y2oCLD1/N5j4wKBgQC9cpUolR0HNexaX7bQfgWJmhm+KavnXZFUbZSHUIMgUrUEuHMOQJ1mhTb34CYClWuhlgrfWCqCmtX3jCsP3mkYQOejQQhPiNKqmtS0PlqEnUMTajHxZd3T3MMQ8rnPRdhiC0Mma3IjhBLiUKzOHo6WWuNYb3C3zuQH2QE58yE31QKBgQCdibArOGkF4yzBPG+l8MOImb1I6bCT+Zja5DobyiEEYMVnkABJc7SFexAwqg+nbJjWK366lQN9ouoi6ExKhR7DgtebHDhWeknK0/AR3foyeTtfXGJAeuvVl/UlufHxLkNQlr31WTTShNuSRz7dZmlhTruf+zOX1e2DN82goGLMtwKBgH5+doe9YL+CysjhJeD6dFoXcd7eVjDK8hHMhSMAqD053DJFslUu4aolgrlpihepi08+Lw5IAUJpfjqm9c3HtKaEF+aSsqjgc8GEVkfvZmMr2jCpdKV5Xe9rdTfoyhZSzZNRb8nEbla2hiFkuq16C7zq/Of8qOeiFfIDfGUkKObtAoGAcxxT4QaALrMUU5slvW3L9l48OcCk5SKug0tH5fgLNmF9T56dDmhpeA5IQU2DrXRIIM5R1PrGLV5G4KmEMnglaycP5ogQ2oPoms71V9O74oIUMDtmOt69v3mzi5d/+NbZORPZwzCACYy56O9adbb2b/j2QBGW33bULcU4uPoDZrs=";
    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
     */
    private String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7uIc5ExPu5aTWWtvUNPfytqkxXTiqo0EvOqC5/zqhhrluSV5RabruMN6mAEvgA9LXfwGem8p9VOjTdGOUFsYT1T0gOhUgvrqVXOp5X3A5A9bLLajGoBWo06xMj/IpbwbQI8+jZhjPAUja5E5rh67jVaUqpaaoOMI4Y/4baPp5x4DF0Nh0VrEN61VV1Uh00Oyu5Mv6rel545xn5cjWsPsCnxVsaqA4kTrl5FaReb2Yc+DWBa55IAvbQkyUWdeL7amIWtqqGV32kgR5i2oY7NviB1ITrr+kyCqBDJNWxlN5pHU8iGxvvdhY1TevohAnsIv1PZ8YRP8QZtch4rzbL6ZswIDAQAB";
    /**
     * 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
     */
    private String notifyUrl;

    /**
     * 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 同步通知，支付成功，一般跳转到成功页
     */
    private String returnUrl;

    /**
     * 签名方式
     */
    private String signType = "RSA2";

    /**
     * 字符编码格式
     */
    private String charset = "utf-8";

    /**
     * 支付宝网关； https://openapi.alipaydev.com/gateway.do
     */
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {
        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                appId, merchantPrivateKey, "json",
                charset, alipayPublicKey, signType);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String outTradeNo = vo.getOut_trade_no();
        //付款金额，必填
        String totalAmount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
