package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author liziyao
 * 查看状态 大区经理，业务经理
 */
public enum ViewStatusEnum {
    //业务经理为1个权重  大区经理权重为2;
    // 允许业务经理 1,关闭-1;
    //允许大区经理 2,关闭-2;
//
	NULL(0,"默认状态没有禁止 0"),
	ALL_CLOSE(-3,"关闭业务经理查看&&关闭大区经理查看  -1+-2"),
    ONLY_MANAGER(-1,"允许业务经理查看&&关闭大区经理查看  1+-2"),
    ONLY_BIG_MANAGER(1,"关闭业务经理查看&&允许大区经理查看 -1+2"),
    ALL_OPEN(3,"允许业务经理查看&&允许大区经理查看 1+2");


	public String desc;
    public int type;

    ViewStatusEnum(int type, String desc) {
        this.desc = desc;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	ViewStatusEnum[] types = ViewStatusEnum.values();
        if (types != null && types.length > 0){
            for (ViewStatusEnum lType : types){
                if (lType.type == type){
                    return lType.desc;
                }
            }
        }
        return null;
    }
    
    public int getType() {
		return type;
    }
    
    public static ViewStatusEnum getEnumByValue(String desc){
    	ViewStatusEnum[] types = ViewStatusEnum.values();
        if (types != null && types.length > 0){
            for (ViewStatusEnum lType : types){
                if (lType.desc.equals(desc)){
                    return lType;
                }
            }
        }
        return null;
    }
}
