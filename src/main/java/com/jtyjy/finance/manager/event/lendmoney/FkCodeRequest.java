package com.jtyjy.finance.manager.event.lendmoney;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 付款扫码参数
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FkCodeRequest {

	private String requestId;
	private String empNo;
	
	/**
	 * 扫描信息
	 * @return
	 */
	public String getInfo() {
		return this.requestId + "-" + this.empNo;
	}
}
