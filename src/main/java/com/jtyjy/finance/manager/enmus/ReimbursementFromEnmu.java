package com.jtyjy.finance.manager.enmus;

/**
 * 报销单来源枚举
 * @author User
 *
 */
public enum ReimbursementFromEnmu {
	
	COMMON(0,"普通报销单"),
	PAYMENT(1,"稿费"),
	COMMISSION(2,"提成"),
	SALARY(3,"工资"),
	PROJECT(4,"项目预领"),
	FIXED_ASSET(5,"固定资产");
	
	private Integer code;
	private String message;
	private ReimbursementFromEnmu(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
