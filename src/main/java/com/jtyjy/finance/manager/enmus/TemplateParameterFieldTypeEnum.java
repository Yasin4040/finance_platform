package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 模板参数字段类型
 */
public enum TemplateParameterFieldTypeEnum {
	TEXT(1,"文本"),
	MONEY(2,"金钱"),
	PERCENT(3,"百分比");
	
	public String value;
    public int type;
    
    TemplateParameterFieldTypeEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	TemplateParameterFieldTypeEnum[] types = TemplateParameterFieldTypeEnum.values();
        if (types != null && types.length > 0){
            for (TemplateParameterFieldTypeEnum lType : types){
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
    
    public static TemplateParameterFieldTypeEnum getTypeEnume(int type){
    	TemplateParameterFieldTypeEnum[] types = TemplateParameterFieldTypeEnum.values();
        if (types != null && types.length > 0){
            for (TemplateParameterFieldTypeEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
