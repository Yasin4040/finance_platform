package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成任务类型
 */
public enum ExtractTaskTypeEnum {
	COMMON(1,"提成支付申请单"),
	DELAY(3,"延期支付申请单");

	public String value;
    public int type;

    ExtractTaskTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractTaskTypeEnum[] types = ExtractTaskTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractTaskTypeEnum lType : types){
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
    
    public static ExtractTaskTypeEnum getTypeEnume(int type){
    	ExtractTaskTypeEnum[] types = ExtractTaskTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractTaskTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
