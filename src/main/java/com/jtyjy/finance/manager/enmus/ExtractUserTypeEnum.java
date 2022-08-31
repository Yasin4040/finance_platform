package com.jtyjy.finance.manager.enmus;

/**
 * 消息类型枚举
 * @author minzhq
 *
 */
public enum ExtractUserTypeEnum {

	COMPANY_STAFF(1,"公司员工"),
	EXTERNAL_STAFF(2,"外部人员"),
	SELF_EMPLOYED_EMPLOYEES(3,"员工个体户");

	private Integer code;
	private String msg;
	private ExtractUserTypeEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public static String getValue(Integer code) {
		ExtractUserTypeEnum[] payWayTypes = ExtractUserTypeEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (ExtractUserTypeEnum payWayType : payWayTypes){
                if (payWayType.code == code){
                    return payWayType.msg;
                }
            }
        }
        return null;
    }
	
	public static Integer getCode(String msg) {
		ExtractUserTypeEnum[] payWayTypes = ExtractUserTypeEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (ExtractUserTypeEnum payWayType : payWayTypes){
                if (payWayType.msg.equals(msg)){
                    return payWayType.code;
                }
            }
        }
        return null;
    }

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
