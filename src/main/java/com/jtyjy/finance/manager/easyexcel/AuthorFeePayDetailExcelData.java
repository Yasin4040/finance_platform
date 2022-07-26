package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

@Data
public class AuthorFeePayDetailExcelData {
	@ExcelProperty(value="收款账户")
	private String gatherbankaccount;
	@ExcelProperty(value="收款户名")
	private String gatherunit;
	@ExcelProperty(value="转账金额")
	private String copefee;
	@ExcelProperty(value="备注")
	private String remark;
	@ExcelProperty(value="收款银行")
	private String gatherbanktype;
	@ExcelProperty(value="收款银行支行")
	private String gatherbank;
	@ExcelProperty(value="收款省")
	private String province;
	@ExcelProperty(value="收款市")
	private String city;
	@ExcelProperty(value="转出账号")
	private String paybankaccount;
	@ExcelProperty(value="转账模式")
	private String mode;
}
