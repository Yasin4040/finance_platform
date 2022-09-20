package com.jtyjy.finance.manager.enmus;

/**
 * 消息类型枚举
 * @author minzhq
 *
 */
public enum LogStatusEnum {

	COMPLETE(0,"已完成"),
	PASS(1,"通过"),
	REJECT(2,"退回"),
	OA_PASS(0,"批准"),
	OA_REJECT(3,"退回");

	private Integer code;
	private String msg;
	private LogStatusEnum(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public static String getValue(Integer code) {
		LogStatusEnum[] payWayTypes = LogStatusEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (LogStatusEnum payWayType : payWayTypes){
                if (payWayType.code == code){
                    return payWayType.msg;
                }
            }
        }
        return null;
    }
	
	public static Integer getCode(String msg) {
		LogStatusEnum[] payWayTypes = LogStatusEnum.values();
        if (payWayTypes != null && payWayTypes.length > 0){
            for (LogStatusEnum payWayType : payWayTypes){
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
