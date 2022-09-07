package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成员工个体户发放状态
 */
public enum ExtractPersonalityPayStatusEnum {
	COMMON(1,"正常"),
	TRANSFER(2,"调账"),
	DELAY(3,"延期");

	
	public String value;
    public int type;
    
    ExtractPersonalityPayStatusEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractPersonalityPayStatusEnum[] types = ExtractPersonalityPayStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractPersonalityPayStatusEnum lType : types){
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
    
    public static ExtractPersonalityPayStatusEnum getTypeEnume(int type){
    	ExtractPersonalityPayStatusEnum[] types = ExtractPersonalityPayStatusEnum.values();
        if (types != null && types.length > 0){
            for (ExtractPersonalityPayStatusEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }

	public static ExtractPersonalityPayStatusEnum getEnumeByValue(String value){
		ExtractPersonalityPayStatusEnum[] types = ExtractPersonalityPayStatusEnum.values();
		if (types != null && types.length > 0){
			for (ExtractPersonalityPayStatusEnum lType : types){
				if (lType.value.equals(value)){
					return lType;
				}
			}
		}
		return null;
	}
}
