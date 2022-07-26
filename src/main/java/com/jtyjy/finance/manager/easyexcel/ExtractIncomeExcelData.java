package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提成收入明细(提成报税时用)
 * @author minzhq
 * date 2021-05-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractIncomeExcelData {
	
	@ExcelProperty(value="工号")
	private String empNo;
	
	@ExcelProperty(value="姓名")
	private String empName;
	
	@ExcelProperty(value="法人公司")
	private String salaryUnitName;
	
	@ExcelProperty(value="本期收入")
	private BigDecimal curIncome;
	
	@ExcelProperty(value="社保")
	private BigDecimal fiveRiskOneFund;
	
	@ExcelProperty(value="本期个税")
	private BigDecimal curTax;
	
	@ExcelProperty(value="工资")
	private BigDecimal curMonthSalary;
	
	@ExcelProperty(value="提成")
	private BigDecimal curMonthExtract;
}
