package com.jtyjy.finance.manager.utils;

import com.jtyjy.finance.manager.constants.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证身份证号码 身份证号码, 可以解析身份证号码的各个字段，
 * 以及验证身份证号码是否有效; 身份证号码构成：6位地址编码+8位生日+3位顺序码+1位校验码
 *
 * @Author 袁前兼
 * @Date 2021/3/12 11:58
 */
public class CheckIdCard {

    /**
     * 身份证的最小出生日期,1900年1月1日
     */
    private final static Date MINIMAL_BIRTH_DATE = new Date(-2209017600000L);

    private final static int NEW_CARD_NUMBER_LENGTH = 18;

    /**
     * 18位身份证中最后一位校验码
     */
    private final static char[] VERIFY_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 18位身份证中，各个数字的生成校验码时的权值
     */
    private final static int[] VERIFY_CODE_WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 验证身份证号是否正确
     *
     * @param cardNumber 身份证编号
     * @return 是否正确
     */
    public static boolean validate(String cardNumber) throws Exception {
        // 身份证号不能为空 且长度为18位
        if (StringUtils.isBlank(cardNumber) || NEW_CARD_NUMBER_LENGTH != cardNumber.length()) {
            throw new Exception("身份证编号填写错误");
        }
        boolean result = true;
        // 身份证号的前17位必须是阿拉伯数字
        for (int i = 0; result && i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            char ch = cardNumber.charAt(i);
            result = ch >= '0' && ch <= '9';
        }
        // 身份证号的第18位校验
//        result = (calculateVerifyCode(cardNumber) == cardNumber.charAt(NEW_CARD_NUMBER_LENGTH - 1));

        // 出生日期不能晚于当前时间，并且不能早于1900年
        try {
            String birthdayPart = cardNumber.substring(6, 14);

            Date birthDate = Constants.FORMAT_8.parse(birthdayPart);
            result = result && null != birthDate;
            result = result && birthDate.before(new Date());
            result = result && birthDate.after(MINIMAL_BIRTH_DATE);

            // 出生日期中的年、月、日必须正确,比如月份范围是[1,12], 日期范围是[1,31]，还需要校验闰年、大月、小月的情况时， 月份和日期相符合
            String realBirthdayPart = Constants.FORMAT_8.format(birthDate);
            result = result && (birthdayPart.equals(realBirthdayPart));
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 校验码（第十八位数）：
     * <p>
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和；
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2; 计算模 Y = mod(S, 11)< 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9
     * 8 7 6 5 4 3 2
     */
    private static char calculateVerifyCode(CharSequence cardNumber) {
        int sum = 0;
        for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            char ch = cardNumber.charAt(i);
            sum += ((ch - '0')) * VERIFY_CODE_WEIGHT[i];
        }
        return VERIFY_CODE[sum % 11];
    }

    /**
     * 获取身份证信息内容
     */
    public static Map<String, Object> getByIdentityCard(String cardCode) throws Exception {
        Map<String, Object> map = new HashMap<>(2);
        // 得到年份
        String year = cardCode.substring(6).substring(0, 4);
        // 得到月份
        String month = cardCode.substring(10).substring(0, 2);

        // 判断性别
        String sex = "男";
        if (Integer.parseInt(cardCode.substring(16).substring(0, 1)) % 2 == 0) {
            sex = "女";
        }
        // 得到当前的系统时间
        Date date = new Date();
        // 当前年份
        String currentYear = Constants.FORMAT_10.format(date).substring(0, 4);
        // 月份
        String currentMonth = Constants.FORMAT_10.format(date).substring(5, 7);
        // 当前月份大于用户出身的月份表示已过生日
        int age = Integer.parseInt(currentYear) - Integer.parseInt(year);
        if (Integer.parseInt(month) <= Integer.parseInt(currentMonth)) {
            age += 1;
        }
        map.put("sex", sex);
        map.put("age", age);
        return map;
    }

}