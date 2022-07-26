package com.jtyjy.finance.manager.enmus;

/**
 * 消息类型枚举
 * @author minzhq
 *
 */
public enum MsgTypeEnum {
	
	WARNING(1,"预警"),
	PUBLIC(2,"公示"),
	RESULT(3,"结果");
	
	private Integer code;
	private String msg;
	private MsgTypeEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public static String getValue(Integer code) {
		MsgTypeEnum[] payWayTypes = MsgTypeEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (MsgTypeEnum payWayType : payWayTypes){
                if (payWayType.code == code){
                    return payWayType.msg;
                }
            }
        }
        return null;
    }
	
	public static Integer getCode(String msg) {
		MsgTypeEnum[] payWayTypes = MsgTypeEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (MsgTypeEnum payWayType : payWayTypes){
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
