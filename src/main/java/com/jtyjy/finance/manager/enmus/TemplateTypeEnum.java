package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 消息模板类型
 */
public enum TemplateTypeEnum {
	IMPORTANT_PERSON(1,"重点人员"),
	OTHER_PERSON(2,"其他人员"),
	NOLEND_OVERDUE(3,"无借款逾期"),
	GOON_LEND(4,"一次逾期再次借款");
	
	public String value;
    public int type;
    
    TemplateTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	TemplateTypeEnum[] types = TemplateTypeEnum.values();
        if (types != null && types.length > 0){
            for (TemplateTypeEnum lType : types){
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
    
    public static TemplateTypeEnum getTypeEnume(int type){
    	TemplateTypeEnum[] types = TemplateTypeEnum.values();
        if (types != null && types.length > 0){
            for (TemplateTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
