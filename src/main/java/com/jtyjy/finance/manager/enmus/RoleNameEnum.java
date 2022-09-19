package com.jtyjy.finance.manager.enmus;
/**
 * 
 * @author minzhq
 * 角色名称 枚举
 */
public enum RoleNameEnum {
	COMMERCIAL_COMMISSION(1,"商务提成"),
    BIG_MANAGER(2,"大区经理"),
    MANAGER(3,"业务经理"),
    TAX(4,"税筹计算"),
    ACCOUNT(5,"账务做账"),
    PAY(6,"出纳付款");

	public String value;
    public int type;

    RoleNameEnum(int type, String value) {
        this.value = value;
        this.type = type;
    }
    
    public static String getValue(int type) {
    	RoleNameEnum[] types = RoleNameEnum.values();
        if (types != null && types.length > 0){
            for (RoleNameEnum lType : types){
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
    public String getValue() {
        return value;
    }
    
    public static RoleNameEnum getTypeEnum(int type){
    	RoleNameEnum[] types = RoleNameEnum.values();
        if (types != null && types.length > 0){
            for (RoleNameEnum lType : types){
                if (lType.type == type){
                    return lType;
                }
            }
        }
        return null;
    }
}
