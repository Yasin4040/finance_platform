package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成状态
 */
public enum PayBatchTypeEnum {
	OTHER(0,"其它"),
	BX(1,"报销"),
	EXTRACT(2,"提成"),
	CASH(3,"现金"),
	PROJECT(4,"项目付款");


	public String value;
    public int type;

    PayBatchTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	PayBatchTypeEnum[] types = PayBatchTypeEnum.values();
        if (types != null && types.length > 0){
            for (PayBatchTypeEnum lType : types){
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
    
    public static PayBatchTypeEnum getTypeEnume(int type){
    	PayBatchTypeEnum[] types = PayBatchTypeEnum.values();
        if (types != null && types.length > 0){
            for (PayBatchTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
