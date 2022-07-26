package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 消息模板类别
 */
public enum TemplateCategoryEnum {
	BACK_MONEY(1,"回款"),
	LEDN(2,"借款");
	
	public String value;
    public int type;
    
    TemplateCategoryEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	TemplateCategoryEnum[] types = TemplateCategoryEnum.values();
        if (types != null && types.length > 0){
            for (TemplateCategoryEnum lType : types){
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
    
    public static TemplateCategoryEnum getTypeEnume(int type){
    	TemplateCategoryEnum[] types = TemplateCategoryEnum.values();
        if (types != null && types.length > 0){
            for (TemplateCategoryEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
