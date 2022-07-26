package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 付款单状态
 */
public enum PaymoneyStatusEnum {
	WAIT_PAY(0,"等待付款"),
	RECEIVE_PAY(1,"接收付款"),
	PAYING(2,"正在付款"),
	PAYED(3,"已经付款");
	
	public String value;
    public int type;
    
    PaymoneyStatusEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	PaymoneyStatusEnum[] types = PaymoneyStatusEnum.values();
        if (types != null && types.length > 0){
            for (PaymoneyStatusEnum lType : types){
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
    
    public static PaymoneyStatusEnum getTypeEnume(int type){
    	PaymoneyStatusEnum[] types = PaymoneyStatusEnum.values();
        if (types != null && types.length > 0){
            for (PaymoneyStatusEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
