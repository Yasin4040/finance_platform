package com.jtyjy.finance.manager.utils;

import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.finance.manager.annotation.ExcelDecimalValid;
import com.jtyjy.finance.manager.annotation.ExcelIntValid;
import com.jtyjy.finance.manager.annotation.ExcelStrLengthValid;
import com.jtyjy.finance.manager.annotation.ExcelValid;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/31.
 * Time: 23:55
 */
public class ExcelImportValid {

    /**
     * Excel导入字段校验
     *
     * @param object 校验的JavaBean 其属性须有自定义注解
     * @author linmaosheng
     */
    public static void  valid(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        StringBuilder errMsg = new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);
            //属性的值
            Object fieldValue = null;
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                //BusinessException是我自定义的异常，这里需要改成你自己写的自定义异常
                //"参数验证错误"
                errMsg.append(field.getAnnotation(ExcelValid.class).message());
//                throw new BusinessException(field.getAnnotation(ExcelValid.class).message());
            }

            //是否包含必填校验注解
            boolean isExcelValid = field.isAnnotationPresent(ExcelValid.class);
            if (isExcelValid && Objects.isNull(fieldValue)) {
                errMsg.append("||");
                errMsg.append(field.getAnnotation(ExcelValid.class).message());
//                throw new BusinessException(field.getAnnotation(ExcelValid.class).message());
            }
            //是否包含字符串长度校验注解
            boolean isExcelStrValid = field.isAnnotationPresent(ExcelStrLengthValid.class);
            if (isExcelStrValid) {
                String cellStr = fieldValue.toString();
                int length = field.getAnnotation(ExcelStrLengthValid.class).length();
                if (StringUtils.isNotBlank(cellStr) && cellStr.length() > length) {
                    errMsg.append("||");
                    errMsg.append(field.getAnnotation(ExcelStrLengthValid.class).message());

//                    throw new BusinessException(field.getAnnotation(ExcelStrLengthValid.class).message());
                }
            }
            //是否包含int类型校验注解
            boolean isExcelIntValid = field.isAnnotationPresent(ExcelIntValid.class);
            if (isExcelIntValid) {
                if (fieldValue instanceof Integer) {
                    int cellInt = Integer.parseInt(fieldValue.toString());
                    int min = field.getAnnotation(ExcelIntValid.class).min();
                    int max = field.getAnnotation(ExcelIntValid.class).max();
                    if (cellInt < min || cellInt > max) {
                        errMsg.append("||");
                        errMsg.append(field.getAnnotation(ExcelIntValid.class).message());
//                        throw new BusinessException(field.getAnnotation(ExcelIntValid.class).message());
                    }
                }
            }
            //是否包含decimal类型注解
            boolean isExcelDecimalValid = field.isAnnotationPresent(ExcelDecimalValid.class);
            if (isExcelDecimalValid) {
                if (isBigDecimal(fieldValue.toString())) {
                    BigDecimal cellDecimal = new BigDecimal(fieldValue.toString());
                    BigDecimal min = new BigDecimal(field.getAnnotation(ExcelDecimalValid.class).min());
                    BigDecimal max = new BigDecimal(field.getAnnotation(ExcelDecimalValid.class).max());
                    if (cellDecimal.compareTo(min) < 0 || cellDecimal.compareTo(max) > 0) {
                        errMsg.append("||");
                        errMsg.append(field.getAnnotation(ExcelDecimalValid.class).message());
//                        throw new BusinessException(field.getAnnotation(ExcelDecimalValid.class).message());

                    }
                } else {
                    errMsg.append("||");
                    errMsg.append("不是小数数字类型");
//                    throw new BusinessException("不是小数数字类型");
                }
            }
        }
        if(StringUtils.isNotBlank(errMsg.toString())){
            throw new BusinessException(errMsg.toString());
        }
    }

    private static boolean isBigDecimal(String decimal) {
        try {
            BigDecimal bd = new BigDecimal(decimal);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
