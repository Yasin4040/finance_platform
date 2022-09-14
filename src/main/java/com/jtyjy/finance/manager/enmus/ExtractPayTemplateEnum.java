package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 提成支付模板
 */
public enum ExtractPayTemplateEnum {
    ZS_BATCH(1,"招行批量付款"),
	ZS_DF(2,"招行代发付款"),
	OLD(3,"老模板");

	public String value;
    public int type;

    ExtractPayTemplateEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ExtractPayTemplateEnum[] types = ExtractPayTemplateEnum.values();
        if (types != null && types.length > 0){
            for (ExtractPayTemplateEnum lType : types){
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
    
    public static ExtractPayTemplateEnum getEnumeByvalue(String value){
    	ExtractPayTemplateEnum[] types = ExtractPayTemplateEnum.values();
        if (types != null && types.length > 0){
            for (ExtractPayTemplateEnum lType : types){
                if (lType.value.equals(value)){
                    return lType;
                }
            }
        }
        return null;
    }
}
