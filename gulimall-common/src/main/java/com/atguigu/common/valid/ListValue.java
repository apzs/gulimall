package com.atguigu.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author 无名氏
 * @date 2022/5/7
 * @Description: 只能为列表内的值
 */
//生成文档
@Documented
//使用哪个校验器进行校验(如果不指定，需要在初始化的时候指定)
@Constraint(validatedBy = { ListValueConstraintValidator.class })
//注解可以标注在哪个位置
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
//校验时机，在运行时获取
@Retention(RUNTIME)
public @interface ListValue {
    /**
     * 校验出错以后，错误信息去哪取
     * 默认使用com.atguigu.common.valid.ListValue.message属性去
     * org/hibernate/validator/ValidationMessages.properties配置文件中去取
     *
     * 一般为 (注解全类名 + .message)
     * @return
     */
    String message() default "{com.atguigu.common.valid.ListValue.message}";
    /**
     * 支持分组校验
     * @return
     */
    Class<?>[] groups() default { };
    /**
     * 自定义负载信息
     * @return
     */
    Class<? extends Payload>[] payload() default { };
    /**
     * 可以为哪些值
     * @return
     */
    int[] vals() default { };
}
