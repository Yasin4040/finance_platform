package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外审外包导出（稿费导出各中心报表时使用）
 * @author minzhq
 *
 */
@Data
@NoArgsConstructor
public class AuthorFeeDeptDetailWsWbExcelData {
	@ExcelProperty("外审外包费年度预算")
	private BigDecimal total;
	
	@ExcelProperty("动因名称")
	private String agentName;
	
	@ExcelProperty("实际已发放")
	private BigDecimal payTotal;
	
	@ExcelProperty("6月发放")
	private BigDecimal total6;
	
	@ExcelProperty("7月发放")
	private BigDecimal total7;
	
	@ExcelProperty("8月发放")
	private BigDecimal total8;
	
	@ExcelProperty("9月发放")
	private BigDecimal total9;
	
	@ExcelProperty("10月发放")
	private BigDecimal total10;
	
	@ExcelProperty("11月发放")
	private BigDecimal total11;
	
	@ExcelProperty("12月发放")
	private BigDecimal total12;
	
	@ExcelProperty("1月发放")
	private BigDecimal total1;
	
	@ExcelProperty("2月发放")
	private BigDecimal total2;
	
	@ExcelProperty("3月发放")
	private BigDecimal total3;
	
	@ExcelProperty("4月发放")
	private BigDecimal total4;
	
	@ExcelProperty("5月发放")
	private BigDecimal total5;
}
