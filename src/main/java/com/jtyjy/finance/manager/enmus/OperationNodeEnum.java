package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author liziyao
 * 操作节点 	部门 负责人、职能管理部门审核、财务销售组审核、财务负责人审批为OA审批节点，其他为预算系统节点；
 */
public enum OperationNodeEnum {
    SUBMITTED(1,"已提交"),

	DEPARTMENT_HEAD(2,"部门负责人"),
	FUNCTIONAL_DEPARTMENT(3,"职能管理部门审核"),
	FINANCIAL_SALES_TEAM(4,"财务销售组审核"),
    FINANCIAL_DIRECTOR(5,"财务负责人审批"),

    TAX_PREPARATION_CALCULATION_1(6,"税筹组计算1"),
    TAX_PREPARATION_CALCULATION_2(7,"税筹组计算2"),
    ACCOUNTING(8,"会计做账"),
    CASHIER_PAYMENT(9,"出纳付款"),
    VOUCHER_ENTRY(10,"凭证录入");

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
}
