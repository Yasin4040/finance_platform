package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 描述：<p>提成员工个体户明细</p>
 *
 * @author minzhq
 * @since 2022/8/31
 */
@Data
public class ExtractInitPersonlityDetailExcelData {


	@ExcelProperty("工号")
	private String empNo;

	@ExcelProperty("姓名")
	private String empName;

	@ExcelProperty("个体户名称/户名")
	private String personlityName;

	@ExcelProperty("累计交票")
	private BigDecimal receiptSum;

	@ExcelProperty("当期提成发放金额")
	private BigDecimal curExtract;

	@ExcelProperty("当期工资发放金额")
	private BigDecimal curSalary;

	@ExcelProperty("当期福利费发放金额")
	private BigDecimal curWelfare;

	@ExcelProperty("发放公司")
	private String billingUnitName;

	@ExcelProperty("发放状态\n（正常/延期/调账）")
	private String payStatus;

}
