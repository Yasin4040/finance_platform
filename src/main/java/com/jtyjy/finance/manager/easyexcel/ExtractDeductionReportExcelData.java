package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import lombok.Data;

/**
 * 提成扣款的报表导出
 * @author minzhq
 * date 2021-04-20
 */
@Data
public class ExtractDeductionReportExcelData {
	@ExcelProperty(value="工号")
	@ColumnWidth(22)
    private String empno;
	
	@ExcelProperty("姓名")
    private String empname;
	
	@ExcelProperty("届别")
	private String period;
	
	@ExcelProperty("预算单位")
	private String unitname;
	
	@ExcelProperty("提成批次")
	private String extractmonth;
	
	@ExcelProperty("提成单号")
	private String code;
	
	@ExcelProperty("扣款项目")
	private String projectname;
	
	@ExcelProperty("扣款金额")
	private String repaymoney;
}
