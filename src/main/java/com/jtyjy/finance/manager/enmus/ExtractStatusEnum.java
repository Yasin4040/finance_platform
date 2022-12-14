package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成状态
 */
public enum ExtractStatusEnum {
	REJECT(-2,"作废"),
	RETURN(-1,"已退回"),
	DRAFT(0,"草稿"),
	VERIFYING(1,"已提交"),
	APPROVED(2,"审核通过"),
	CALCULATION_COMPLETE(3,"计算完成"),
	ACCOUNT(4,"做账完成"),
	PAY(5,"支付完成"),
	VOUCHER_ENTRY(6,"入账完成");

	
	public String value;
    public int type;
    
    ExtractStatusEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractStatusEnum[] types = ExtractStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractStatusEnum lType : types){
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
    
    public static ExtractStatusEnum getTypeEnume(int type){
    	ExtractStatusEnum[] types = ExtractStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractStatusEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
