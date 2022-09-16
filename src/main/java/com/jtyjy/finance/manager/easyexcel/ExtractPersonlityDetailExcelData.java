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
public class ExtractPersonlityDetailExcelData {

	@ExcelProperty("序号")
	private Integer orderNumber;

	@ExcelProperty("批次")
	private String batch;

	@ExcelProperty("部门")
	private String firstDept;

	@ExcelProperty("省区/大区")
	private String secondDept;

	@ExcelProperty("工号")
	@NotNull(message = "工号不能为空")
	private Integer empNo;

	@ExcelProperty("姓名")
	private String empName;

	@ExcelProperty("当期待发提成金额")
	private BigDecimal extract;

	@ExcelProperty("个体户名称/户名")
	@NotBlank(message = "户名不能为空")
	private String personlityName;

	@ExcelProperty("账户类型")
	private String userType;

	@ExcelProperty("累计交票")
	private BigDecimal receiptSum;

	@ExcelProperty("累计已发提成")
	private BigDecimal extractSum;

	@ExcelProperty("当期提成发放金额")
	private String curExtract;

	@ExcelProperty("累计已发工资")
	private BigDecimal salarySum;

	@ExcelProperty("当期工资发放金额")
	private String curSalary;

	@ExcelProperty("累计已发福利")
	private BigDecimal welfareSum;

	@ExcelProperty("当期福利费发放金额")
	private String curWelfare;

	@ExcelProperty("累计已发")
	private BigDecimal moneySum;

	@ExcelProperty("剩余票额")
	private String remainingInvoices;

	@ExcelProperty("剩余发放限额")
	private String remainingPayLimitMoney;

	@ExcelProperty("发放公司\n（公司全称）")
	@NotBlank(message = "发放公司不能为空")
	private String billingUnitName;

	@ExcelProperty("发放状态\n（正常/延期/调账）")
	@NotBlank(message = "发放状态不能为空")
	private String payStatus;

	@ExcelIgnore
	private String errMsg;

	public static ExtractPersonlityDetailExcelData transfer(IndividualEmployeeFiles individualEmployeeFiles){
		ExtractPersonlityDetailExcelData excelData = new ExtractPersonlityDetailExcelData();
		excelData.setBatch(individualEmployeeFiles.getBatchNo());
		excelData.setFirstDept(individualEmployeeFiles.getDepartmentFullName());
		excelData.setSecondDept(individualEmployeeFiles.getProvinceOrRegion());
		excelData.setEmpNo(individualEmployeeFiles.getEmployeeJobNum());
		excelData.setEmpName(individualEmployeeFiles.getEmployeeName());
		excelData.setPersonlityName(individualEmployeeFiles.getAccountName());
		excelData.setUserType(individualEmployeeFiles.getAccountType()==1?"个卡":"公户");
		return excelData;
	}
}
