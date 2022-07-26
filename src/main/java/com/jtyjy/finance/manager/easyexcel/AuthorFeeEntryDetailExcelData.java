package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

/**
 * 稿费入账明细
 * @author minzhq
 */
@Data
public class AuthorFeeEntryDetailExcelData {
	
	@ExcelProperty(value="稿酬支付单位")
	private String payUnitName;
	
	@ExcelProperty(value="税前稿酬")
	private BigDecimal preTaxMoney;
	
	@ExcelProperty(value="出版社上缴税务")
	private BigDecimal cbssjTax;
	
	@ExcelProperty(value="公司上缴税务")
	private BigDecimal companySjTax;
	
	@ExcelProperty(value="公司留存")
	private BigDecimal gslc;
	
	@ExcelProperty(value="税后稿酬")
	private BigDecimal afterTaxMoney;
}
