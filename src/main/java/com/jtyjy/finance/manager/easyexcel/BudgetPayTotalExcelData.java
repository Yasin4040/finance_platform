package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelIgnore;
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
public class BudgetPayTotalExcelData {

	@ExcelProperty("付款单位")
	private String bUnitName;

	@ExcelProperty("收款银行")
	private String bankName;

	@ExcelProperty("付款金额")
	private String payMoney;
}
