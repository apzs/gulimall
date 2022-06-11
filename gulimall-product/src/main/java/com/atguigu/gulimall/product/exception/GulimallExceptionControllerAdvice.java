package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeException;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 无名氏
 * @date 2022/5/7
 * @Description: 自定义异常捕获类
 *
 * @RestControllerAdvice =  @ControllerAdvice + @ResponseBody
 */

@Slf4j  //lombok日志
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    /**
     * 捕获异常
     * @ExceptionHandler: 捕获异常的类型
     * @param e 该异常类
     * @return  返回前端的结果
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题:{},异常类型:{}",e.getMessage(),e.getClass());
        Map<String, String> errMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((item)->{
            //实体类的字段名和对应的错误消息
            errMap.put(item.getField(),item.getDefaultMessage());
        });

        return R.error(BizCodeException.VALID_EXCEPTION).put("data",errMap);
    }

    /**
     * 所有前面没有被匹配的异常都会执行这个方法
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("异常:",throwable);
        return R.error(BizCodeException.UNKNOW_EXCEPTION);
    }
}
