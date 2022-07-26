package com.jtyjy.finance.manager.enmus;

/**
 * Author: ldw
 * Description: 稿费状态枚举类
 * Date:  2021-04-25
 */
public enum AuthorFeeStatusEnum {

    STATUS_WITHDRAWL(-1, "已退回"),  //退回
    STATUS_UNAUDITED(0, "草稿"), //	未审核(草稿)
    STATUS_AUDITING(1, "审核中"), //审核中
    STATUS_AUDITED(2, "已审核"),//已审核
    STATUS_TAX_CALCULATED(3, "已计税"),//已计税
    STATUS_REIMBURSED(4, "已报销");        //已生成报销单


    public String value;
    public int type;

    AuthorFeeStatusEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }

    public static String getValue(int type) {
    	AuthorFeeStatusEnum[] types = AuthorFeeStatusEnum.values();
        if (types != null && types.length > 0){
            for (AuthorFeeStatusEnum lType : types){
                if (lType.type == type){
                    return lType.value;
                }
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public static AuthorFeeStatusEnum getTypeEnume(int type){
    	AuthorFeeStatusEnum[] types = AuthorFeeStatusEnum.values();
        if (types != null && types.length > 0){
            for (AuthorFeeStatusEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }


}
