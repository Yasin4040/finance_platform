package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提成导入
 * @author minzhq
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractInfoExportExcelData{
	
	@ExcelProperty(value={"是否公司员工(是/否)"})
	@ColumnWidth(30)
	private String isCompanyEmp;
	
	@ExcelProperty(value="工号")
	private String empNo;
	
	@ExcelProperty(value="姓名")
	private String empName;
	
	@ColumnWidth(30)
	@ExcelProperty(value="实发提成")
	private String copeextract;
	
	@ExcelProperty(value="综合税")
	@ColumnWidth(30)
	private String consotax;
	
	@ExcelProperty(value="提成届别")
	@ColumnWidth(30)
	private String extractPeriod;
	
	@ExcelProperty(value="是否坏账(是/否)")
	@ColumnWidth(30)
	private String isBadDebt;

	@ExcelProperty(value="提成类型")
	@ColumnWidth(30)
	String extractType;
	@ExcelProperty(value="应发提成")
	@ColumnWidth(30)
	String shouldSendExtract;
	@ExcelProperty(value="个税")
	@ColumnWidth(30)
	String tax;
	@ExcelProperty(value="个税减免")
	@ColumnWidth(30)
	String taxReduction;
	@ExcelProperty(value="发票超额税金")
	@ColumnWidth(30)
	String invoiceExcessTax;
	@ExcelProperty(value="发票超额税金减免")
	@ColumnWidth(30)
	String invoiceExcessTaxReduction;
	@ExcelProperty(value="错误明细")
	@ColumnWidth(30)
	private String errMsg;
}
