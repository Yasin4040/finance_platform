package com.jtyjy.finance.manager.enmus;


/**
 * 报销类型枚举
 * @author User
 *
 */
public enum ReimbursementTypeEnmu {
	
	COMMON(1,"通用报销"),
	TRAVAL(2,"差旅报销"),
	TRAVALSUBSIDIES(4,"差旅补贴"),
	ENTERTAIN(3,"招待报销"),
	ENTERTAINSPREAD(5,"推广招待");
	
	private Integer code;
	private String message;
	private ReimbursementTypeEnmu(Integer code, String message) {
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

    public static String getValue(Integer code) {
        ReimbursementTypeEnmu[] bxTypeArr = ReimbursementTypeEnmu.values();
        if (bxTypeArr != null && bxTypeArr.length > 0) {
            for (ReimbursementTypeEnmu bxType : bxTypeArr) {
                if (bxType.code.equals(code)) {
                    return bxType.message;
                }
            }
        }
        return code.toString();
    }
}
