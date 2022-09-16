package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Data
public class BudgetExtractZhDfPayExcelData {

	@ExcelProperty("账号")
	private String bankAccount;
	@ExcelProperty("户名")
	private String bankAccountName;
	@ExcelProperty("金额")
	private BigDecimal payMoney;
	@ExcelProperty("开户行")
	private String openBank;
	@ExcelProperty("开户地")
	private String city;
	@ExcelProperty("汇款备注")
	private String remark;

	public BudgetExtractZhDfPayExcelData(){
		remark = "JJ";
	}
	public BudgetExtractZhDfPayExcelData(boolean initFalse){
	}
}
