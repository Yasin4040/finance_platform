package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成超额类型
 */
public enum ExtractExcessTypeEnum {
	NONE(-1,"无"),
	NOEXCESS(0,"未超额"),
	EXCESS_FINISHED(1,"超额且发完"),
	EXCESS_NOFINISHED(2,"超额未发完");
	
	public String value;
    public int type;
    
    ExtractExcessTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractExcessTypeEnum[] types = ExtractExcessTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractExcessTypeEnum lType : types){
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
    
    public static ExtractExcessTypeEnum getTypeEnume(int type){
    	ExtractExcessTypeEnum[] types = ExtractExcessTypeEnum.values();
        if (types != null && types.length > 0){
            for (ExtractExcessTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
