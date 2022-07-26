package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 付款单类型
 */
public enum PaymoneyTypeEnum {
	REIMBURSEMENT_PAY(1,"报销付款"),
	EXTRACT_PAY(2,"提成付款"),
	LEND_PAY(3,"借款付款");
	
	public String value;
    public int type;
    
    PaymoneyTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	PaymoneyTypeEnum[] types = PaymoneyTypeEnum.values();
        if (types != null && types.length > 0){
            for (PaymoneyTypeEnum lType : types){
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
    
    public static PaymoneyTypeEnum getTypeEnume(int type){
    	PaymoneyTypeEnum[] types = PaymoneyTypeEnum.values();
        if (types != null && types.length > 0){
            for (PaymoneyTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
