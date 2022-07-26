package com.jtyjy.finance.manager.dto.bxExcel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrickDetailDto {

	@Location(column = 0)
	@NotBlank(message = "借款单号不能为空")
	private String jkCode;
	
	@Location(column = 1)
    @NotBlank(message = "借款人名称不能为！")
	private String jkName;
	
	@Location(column = 2)
    @NotNull(message = "冲账不能为空")
	private Double czMoney;
	
	private BudgetLendmoney lendMoneyInfo;
	@Override
	public String toString() {
		return "StrickDetail [jkCode=" + jkCode + ", jkName=" + jkName + ", czMoney=" + czMoney + "]";
	}
}
