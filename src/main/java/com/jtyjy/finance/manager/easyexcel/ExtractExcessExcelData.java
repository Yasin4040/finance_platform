package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提成超额导出
 * @author minzhq
 * date 2021-05-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractExcessExcelData {
	@ExcelProperty(value="身份证号")
	private String idNumber;

	@ExcelProperty(value="是否公司员工")
	private String isCompanyEmp;
	
	@ExcelProperty(value="工号")
	private String empNo;
	
	@ExcelProperty(value="姓名")
	private String empName;

	@ExcelProperty(value="工资单位")
	private String billingUnitName;

	@ExcelProperty(value="超额提成")
	private BigDecimal excessMoney;

	@ExcelProperty(value="避税发放")
	private BigDecimal avoidTaxMoney = BigDecimal.ZERO;

	@ExcelProperty(value="外部户发放单位")
	private String outUnit;

	@ExcelProperty(value="外部户发放金额")
	private BigDecimal outUnitPayMoney = BigDecimal.ZERO;

	@ExcelProperty(value="错误明细")
	@ColumnWidth(30)
	private String errMsg;
}
