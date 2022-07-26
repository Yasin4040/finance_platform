package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 稿费计税明细
 * @author minzhq
 *
 */
@Data
public class AuthorFeeCalTaxDetailExcelData {
	@ExcelProperty("届别")
	@ApiModelProperty(value="届别")
	private String yearperiod;
	
	@ExcelProperty("稿费月份")
	@ApiModelProperty(value="稿费月份")
	private String feemonth;
	
	@ExcelProperty("报销科目")
	@ApiModelProperty(value="报销科目")
	private String reimbursesubject;
	
	@ExcelProperty("作者")
	@ApiModelProperty(value="作者")
	private String authorname;
	
	@ExcelProperty("身份证号")
	@ApiModelProperty(value="身份证号")
	private String authoridnumber;
	
	@ExcelProperty("纳税人识别号")
	@ApiModelProperty(value="纳税人识别号")
	private String taxpayeridnumber;
	
	@ExcelProperty("应发稿费")
	@ApiModelProperty(value="应发稿费")
	private BigDecimal copefee;
	
	@ExcelProperty("个税")
	@ApiModelProperty(value="个税")
	private BigDecimal tax;
	
	@ExcelProperty("实发稿费")
	@ApiModelProperty(value="实发稿费")
	private BigDecimal realfee;
	
	@ExcelProperty("收款方")
	@ApiModelProperty(value="收款方")
	private String gatherunit;
	
	@ExcelProperty("收款账户")
	@ApiModelProperty(value="收款账户")
	private String gatherbankaccount;
	
	@ExcelProperty("收款银行")
	@ApiModelProperty(value="收款银行")
	private String gatherbank;
	
	@ExcelProperty("收款省")
	@ApiModelProperty(value="收款省")
	private String gatherbankProvince;
	
	@ExcelProperty("收款市")
	@ApiModelProperty(value="收款市")
	private String gatherbankCity;
	
	@ExcelProperty("发放单位")
	@ApiModelProperty(value="发放单位")
	private String payunit;
	
	@ExcelProperty("发放账户")
	@ApiModelProperty(value="发放账户")
	private String payBankAccount;
	
	@ExcelProperty("发放银行")
	@ApiModelProperty(value="发放银行")
	private String payBank;
	
	@ExcelProperty("工资单位")
	@ApiModelProperty(value="工资单位")
	private String salaryunit;
	
	@ExcelProperty("稿费所属部门")
	@ApiModelProperty(value="稿费所属部门")
	private String feebdgdept;
	
	@ApiModelProperty(value="创建时间")
	@ExcelIgnore
	private String createtime;

	@ApiModelProperty(value="id")
	@ExcelIgnore
	private Long id;
}
