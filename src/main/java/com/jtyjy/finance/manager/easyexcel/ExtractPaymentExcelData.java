package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提成发放表
 * @author minzhq
 * date 2021-05-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractPaymentExcelData {
	
	@ExcelProperty(value="工号")
	private String empNo;
	
	@ExcelProperty(value="姓名")
	private String empName;
	
	@ExcelProperty(value="实发提成")
	private BigDecimal realExtract;
	
	@ExcelProperty(value="综合税")
	private BigDecimal consotax;
	
	@ExcelProperty(value={"扣款项目"})
	private String projectName;
	
	@ExcelProperty(value="扣款金额")
	private BigDecimal deductionMoney;
	
	@ExcelProperty(value="发放单位1")
	private String salaryUnitName;
	
	@ExcelProperty(value="金额")
	private BigDecimal salaryUnitPayMoney;
	
	@ExcelProperty(value="发放单位2")
	private String avoidUnitName;
	
	@ExcelProperty(value="金额")
	private BigDecimal avoidUnitPayMoney;
	
	@ExcelProperty(value="法人公司实发费用")
	private BigDecimal fee;
	
	@ExcelProperty(value="银行账号")
	private String bankAccount;
	
	@ExcelProperty(value="银行类型")
	private String bankName;
	
	@ExcelProperty(value="开户行")
	private String openBank;
	
	@ExcelProperty(value="省份")
	private String province;
	
	@ExcelProperty(value="城市")
	private String city;
	
	@ExcelIgnore
	private Boolean isSum = false;
	
}
