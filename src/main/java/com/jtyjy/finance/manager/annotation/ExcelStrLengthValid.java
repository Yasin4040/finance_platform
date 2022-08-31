package com.jtyjy.finance.manager.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Excel导入 字符串长度校验注解
 * Created by ZiYao Lee on 2022/08/31.
 * Time: 23:52
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelStrLengthValid {
    int length() default 0;
    String message() default "字段超长";
}
