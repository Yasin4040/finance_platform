package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成类型
 */
public enum ExtractTypeEnum {
    //“扎账前提成”、“扎账后提成”、“期间提成”、“坏账明细”、“绩效奖提成”、“预提绩效奖”
//
	RETURN(1,"期间提成"),
	DRAFT(2,"扎账总提成"),
	VERIFYING(3,"扎账后提成"),
    APPROVED(4,"坏账明细"),
    PERFORMANCE_AWARD_COMMISSION(5,"绩效奖提成"),
    ACCRUED_PERFORMANCE_AWARD(6,"预提绩效奖"),
    COMMISSION_BEFORE_ACCOUNTS(7,"扎账前提成");


	public String value;
    public int type;

    ExtractTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractTypeEnum[] types = ExtractTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractTypeEnum lType : types){
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
    
    public static ExtractTypeEnum getEnumeByvalue(String value){
    	ExtractTypeEnum[] types = ExtractTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractTypeEnum lType : types){
                if (lType.value.equals(value)){
                    return lType;
                }
            }
        }
        return null;
    }
}
