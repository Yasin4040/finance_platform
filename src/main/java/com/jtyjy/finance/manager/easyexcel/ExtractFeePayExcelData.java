package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提成费用发放
 * @author minzhq
 * date 2021-12-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractFeePayExcelData {
	
	@ExcelProperty(value="编号")
	private String empNo;
	
	@ExcelProperty(value="姓名")
	private String empName;

	@ExcelProperty(value="费用金额")
	private String feePay;
	
	@ExcelProperty(value="错误明细")
	@ColumnWidth(30)
	private String errMsg;
}
