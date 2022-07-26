package com.jtyjy.finance.manager.constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 常量配置
 *
 * @author konglingcheng
 */
public class Constants {

    /**
     * 14位日期格式化对象
     */
    public static final FastDateFormat FORMAT_14 = FastDateFormat.getInstance("yyyyMMddHHmmss");
    public static final FastDateFormat FORMAT_12 = FastDateFormat.getInstance("yyyyMMddHHmm");
    public static final FastDateFormat FORMAT_8 = FastDateFormat.getInstance("yyyyMMdd");
    public static final FastDateFormat FORMAT_6 = FastDateFormat.getInstance("yyyyMM");
    public static final FastDateFormat FORMAT_10 = FastDateFormat.getInstance("yyyy-MM-dd");
    public static final FastDateFormat FORMAT2_10 = FastDateFormat.getInstance("yyyy/MM/dd");
    public static final FastDateFormat FULL_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    public static final FastDateFormat NOSS_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
    public static final FastDateFormat FULL_FORMAT2 = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss");

    /**
     * json对象互转
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * utf-8编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 企业微信验证码前缀
     */
    public static final String CHECK_CODE_PREFIX = "CHECK_CODE_";

    /**
     * 验证码失效时间
     */
    public static final int CHECK_CODE_EXPIRE = 180;

    /**
     * 支付类型
     */
    public interface PAY_TYPE {
        Integer CASH = 0;
        Integer TRANSFER = 1;
        Integer REIMBURSEMENT = 2;
    }

    /**
     * 获取审核状态描述
     */
    public static String getRequestStatus(Integer requestStatus) {
        switch (requestStatus) {
            case -1:
                return "退回";
            case 0:
                return "草稿";
            case 1:
                return "已提交";
            case 2:
                return "审核通过";
            default:
                return "未知";
        }
    }

}
