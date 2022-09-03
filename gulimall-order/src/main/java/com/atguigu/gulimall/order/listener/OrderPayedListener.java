package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 无名氏
 * @date 2022/8/21
 * @Description:
 */
@Controller
public class OrderPayedListener {

    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 支付宝成功异步回调
     *
     * @param vo
     * @return
     */
    @PostMapping("/payed/notify")
    @ResponseBody
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) {
        //只要我们收到了支付宝给我们异步的通知，告诉我们订单支付成功。返回success, 支付宝就再也不通知
        try {
            boolean signVerified = checkSignVerified(request);
            //验证签名
            if (signVerified) {
                boolean result = orderService.handlePayResult(vo);
                if (result) {
                    return "success";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    private boolean checkSignVerified(HttpServletRequest request) throws AlipayApiException{
        //获取支付宝POST过来反馈信息
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String,String> params = new HashMap<>();
        requestParams.forEach((k,v)->{
            String value = StringUtils.collectionToDelimitedString(Arrays.asList(v), ",");
            //乱码解决，这段代码在出现乱码时使用
            //value = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            params.put(k,value);
        });
        //调用SDK验证签名
        return AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipayPublicKey(), alipayTemplate.getCharset(), alipayTemplate.getSignType());
    }
}
