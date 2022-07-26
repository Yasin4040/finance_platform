package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 稿费税前报表
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorFeePreTaxSumExcelData {
	@ExcelProperty(value="研发部门")
	private String deptName;  //研发部门
	
	@ExcelProperty(value="合计")
	private BigDecimal sum; //合计
	
	@ExcelProperty(value="稿费")
	private BigDecimal gfMoney; //稿费
	
	@ExcelProperty(value="外省外包费")
	private BigDecimal wswbMoney; //外省外包费
	
	@ExcelProperty(value="待摊-稿费")
	private BigDecimal dtgfMoney; //待摊稿费
	
	@ExcelProperty(value="待摊-外省外包费")
	private BigDecimal dtwswbMoney; //待摊-外省外包费
	
	@ExcelProperty(value="扣税稿酬")
	private BigDecimal taxMoney; //扣税稿酬

	@ExcelProperty(value="不扣税稿酬")
	private BigDecimal noTaxMoney; //不扣税稿酬
	
}
