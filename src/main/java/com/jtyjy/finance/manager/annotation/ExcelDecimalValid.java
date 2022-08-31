package com.jtyjy.finance.manager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Excel导入必填校验注解
 * Created by ZiYao Lee on 2022/08/31.
 * Time: 23:52
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDecimalValid {
    int min();
    int max();
    String message() default "小数超过长度范围";
}
