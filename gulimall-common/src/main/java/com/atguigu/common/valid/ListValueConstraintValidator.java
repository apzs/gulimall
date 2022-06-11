package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 无名氏
 * @date 2022/5/7
 * @Description:
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    /**
     * 初始化信息
     * 可以获取注解的详细信息
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //@ListValue注解中 vals 属性的值
        //值必须为这几个中的一个
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * 判断是否校验成功
     * @param value
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return set.contains(value);
    }
}
