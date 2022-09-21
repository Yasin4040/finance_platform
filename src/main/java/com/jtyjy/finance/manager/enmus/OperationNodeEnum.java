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
    FINANCIAL_SALES_TEAM_HEAD(5,"财务销售处长审核"),
    FINANCIAL_DIRECTOR(6,"财务负责人审核"),

    TAX_PREPARATION_CALCULATION_EMP(7,"税筹计算(员工)"),
    TAX_PREPARATION_CALCULATION_SELF(8,"税筹计算(个体户)"),
    ACCOUNTING(9,"会计做账"),
    CASHIER_PAYMENT(10,"出纳付款"),
    VOUCHER_ENTRY(11,"凭证录入"),

    SYSTEM_APPROVED(12,"系统审核通过"),

    TAX_RETURN(-1,"税务退回"),
    ACCOUNTING_RETURN(-2,"做账退回"),
    CASHIER_RETURN(-3,"出纳退回"),
    SYSTEM_RETURN(-4,"系统操作审核退回");

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
    public String getValue() {
        return value;
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
                if (lType.value.contains(desc)) {
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
