package com.jtyjy.finance.manager.easyexcel;

import java.io.File;
import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出陈彩莲发放
 * @author minzhq
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractCCLPayExcelData {
	@ExcelProperty(value="工号")
	private String empNo;
	
	@ExcelProperty(value="业务经理")
	private String empName;
	
	@ExcelProperty(value="部门")
	private String fullDeptName;
	
	@ExcelProperty(value="银行")
	private String bankName;
	
	@ExcelProperty(value="银行账号")
	private String bankAccount;
	
	@ExcelProperty(value="发放金额")
	private BigDecimal paymoney;

	@ExcelProperty(value = "二维码")
	private File file;
}
