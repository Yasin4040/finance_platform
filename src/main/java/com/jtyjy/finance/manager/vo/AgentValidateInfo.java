package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报销动因校验信息
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentValidateInfo {

	private int index;
	private BigDecimal money;
	private BigDecimal bigMoney;
	private String errorInfo;
	
	public String getValidateInfo() {
		return errorInfo + "第"+index+"条记录校验失败，最大可报销金额为："+bigMoney+"，申请报销金额为："+money;
	}
}
