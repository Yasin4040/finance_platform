package com.jtyjy.finance.manager.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据加密（脱敏）工具类
 * @author shubo
 *
 */
public class DataEncryptionUtil {
    
    //银行账户：显示前六后四，范例：622848******4568
    public static String encryptBankAcct(String bankAcct) {
        if (StringUtils.isBlank(bankAcct)) {
            return "";
        }
        if (bankAcct.length() < 10 ) {
            return bankAcct;
        }
        return replaceBetween(bankAcct, 6, bankAcct.length() - 4, null);
    }
  
    //身份证号码：显示前六后四，范例：110601********2015
    public static String encryptIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        return replaceBetween(idCard, 6, idCard.length() - 4, null);
    }
 
    //手机：显示前三后四，范例：189****3684
    public static String encryptPhoneNo(String phoneNo) {
        if (StringUtils.isBlank(phoneNo)) {
            return "";
        }
        return replaceBetween(phoneNo, 3, phoneNo.length() - 4, null);
    }
    
    /**
     * 将字符串开始位置到结束位置之间的字符用指定字符替换
     * @param sourceStr 待处理字符串
     * @param begin 开始位置
     * @param end   结束位置
     * @param replacement 替换字符
     * @return 
     */
    private static String replaceBetween(String sourceStr, int begin, int end, String replacement) {
        if (sourceStr == null) {
            return "";
        }
        if (replacement == null) {
            replacement = "*";
        }
        int replaceLength = end - begin;
        if (StringUtils.isNotBlank(sourceStr) && replaceLength > 0) {
            StringBuilder sb = new StringBuilder(sourceStr);
            sb.replace(begin, end, StringUtils.repeat(replacement, replaceLength));
            return sb.toString();
        } else {
            return sourceStr;
        }
    }

}
