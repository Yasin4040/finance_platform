package com.jtyjy.finance.manager.enmus;

/**
 * @author minzhq
 * 借款类型
 */
public enum LendTypeEnum {
    TEMPORARY_LEND(1, "临时借款"),
    OTHER_LEND(2, "其它借款"),
    DAILY_LEND(3, "日常借款"),
    PROJECT_PRE_CLAIM(4, "项目预领"),
    CONTRACT_LEND(5, "合同借款"),
    NOCONTRACT_LEND(6, "非合同借款"),
    PERSON_BORROWING(7, "个人借支"),
    PROJECT_BORROWING(8, "项目借支"),

    LEND_TYPE_11(11, "个人借款"),
    LEND_TYPE_12(12, "费用借款"),
    LEND_TYPE_13(13, "销售政策支持借款申请"),
    LEND_TYPE_14(14, "备用金借款"),
    LEND_TYPE_15(15, "合同借款"),
    LEND_TYPE_16(16, "非合同借款");

    public String value;
    public int type;

    LendTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }

    public static String getValue(int type) {
        LendTypeEnum[] types = LendTypeEnum.values();
        if (types != null && types.length > 0) {
            for (LendTypeEnum lType : types) {
                if (lType.type == type) {
                    return lType.value;
                }
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public static LendTypeEnum getTypeEnume(int type) {
        LendTypeEnum[] types = LendTypeEnum.values();
        if (types != null && types.length > 0) {
            for (LendTypeEnum lType : types) {
                if (lType.type == type) {
                    return lType;
                }
            }
        }
        return null;
    }
}
