package com.jtyjy.finance.manager.yk;

/**
 * @author minzhq
 */
public class YkMsg {
	private String id;
	private String code;
	private String ip;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	@Override
	public String toString() {
		return String.format("YkMsg[%s,%s,%s]", id,code,ip);
	}
}
