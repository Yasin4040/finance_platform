package com.jtyjy.finance.manager.event.bx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 报销扫码参数
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BxCodeRequest {

	private String orderId;
	private String empNo;
	private String version;
	
	/**
	 * 扫描信息
	 * @return
	 */
	public String getInfo() {
		return this.orderId + "-" + this.version + "-" + this.empNo;
	}
}
