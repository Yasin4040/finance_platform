package com.jtyjy.finance.manager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:int长度校验
 * Created by ZiYao Lee on 2022/08/31.
 * Time: 23:54
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelIntValid {
    int min();
    int max();
    String message() default "整数超过长度范围";
}
