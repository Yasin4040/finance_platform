package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 延期提成支付申请单状态
 */
public enum ExtractDelayStatusEnum {
	CALCULATION_COMPLETE(3,"未做账"),
	ACCOUNT(4,"做账完成"),
	PAY(5,"支付完成"),
	VOUCHER_ENTRY(6,"入账完成");


	public String value;
    public int type;

    ExtractDelayStatusEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractDelayStatusEnum[] types = ExtractDelayStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractDelayStatusEnum lType : types){
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
    
    public static ExtractDelayStatusEnum getTypeEnume(int type){
    	ExtractDelayStatusEnum[] types = ExtractDelayStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractDelayStatusEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
