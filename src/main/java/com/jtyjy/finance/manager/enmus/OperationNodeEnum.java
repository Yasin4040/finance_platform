package com.jtyjy.finance.manager.enmus;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author liziyao
 * 操作节点 	部门 负责人、职能管理部门审核、财务销售组审核、财务负责人审批为OA审批节点，其他为预算系统节点；
 */
public enum OperationNodeEnum {
    SUBMITTED(1,"单据提交"),

	DEPARTMENT_HEAD(2,"部门负责人审核"),
	FUNCTIONAL_DEPARTMENT(3,"职能管理部门审核"),
	FINANCIAL_SALES_TEAM(4,"财务销售组审核"),
    FINANCIAL_SALES_TEAM_HEAD(4,"财务销售处长审核"),
    FINANCIAL_DIRECTOR(6,"财务负责人审核"),

    TAX_PREPARATION_CALCULATION_1(7,"税筹组计算1"),
    TAX_PREPARATION_CALCULATION_2(8,"税筹组计算2"),
    ACCOUNTING(9,"会计做账"),
    CASHIER_PAYMENT(10,"出纳付款"),
    VOUCHER_ENTRY(11,"凭证录入");

    private final String value;
    private final int type;

    OperationNodeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	OperationNodeEnum[] types = OperationNodeEnum.values();
        if (types.length > 0){
            for (OperationNodeEnum lType : types){
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
    
    public static OperationNodeEnum getTypeEnum(int type){
    	OperationNodeEnum[] types = OperationNodeEnum.values();
        if (types.length > 0){
            for (OperationNodeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
    public static OperationNodeEnum getTypeEnumByDesc(String desc){
        OperationNodeEnum[] types = OperationNodeEnum.values();
        if (types.length > 0){
            for (OperationNodeEnum lType : types){
                if (lType.value == desc){
                    return lType;
                }
            }
        }
        return null;
    }
    public static List<String> getValues() {
        List<String> list = new ArrayList<>();
        OperationNodeEnum[] types = OperationNodeEnum.values();
        if (types.length > 0){
            for (OperationNodeEnum lType : types){
                list.add(lType.value);
            }
        }
        return list;
    }
}
