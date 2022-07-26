package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提成支付汇总表
 * @author minzhq
 * date 2021-12-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractPaySumExcelData {

	@ExcelProperty(value="提成类型")
	private String extractType;

	@ExcelProperty(value="部门")
	private String deptName;

	@ExcelProperty(value="届别")
	private String yearPeriod;

	@ExcelProperty(value="申请提成")
	private BigDecimal applyExtract = BigDecimal.ZERO;

	@ExcelProperty(value="提成个税")
	private BigDecimal extractTax = BigDecimal.ZERO;;

	@ExcelProperty(value="发票超额税金")
	private BigDecimal invoiceExcessTax = BigDecimal.ZERO;;

	@ExcelProperty(value="其它")
	private BigDecimal other = BigDecimal.ZERO;

	@ExcelProperty(value="法人公司发放")
	private BigDecimal extract = BigDecimal.ZERO;

	@ExcelProperty(value="备注")
	private String remark = "";
}
