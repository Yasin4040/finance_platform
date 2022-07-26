package com.jtyjy.finance.manager.controller.authorfee.excel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.jtyjy.finance.manager.bean.BudgetAuthorfeesum;
import com.jtyjy.finance.manager.bean.BudgetMonthPeriod;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.bean.WbUser;
import com.klcwqy.easyexcel.anno.Location;
import com.klcwqy.easyexcel.anno.SheetInfo;

import lombok.Data;

/**
 * 稿费导入的表头
 * @author minzhq
 */
@SheetInfo(index = 0,startRow = 3,key = "detail")
@Data
public class ContributionFeeExcelHead {
	@Location(soleRow = 1, column = 1)
	@NotBlank(message = "届别不能为空！")
	private String yearName; //届别
	
	public ContributionFeeExcelHead(){}

	public ContributionFeeExcelHead(String yearName,  String contributionFeeNo, String unitName,  String contributionFeeMonth, String bxEmpno) {
		this.yearName = yearName;
		this.contributionFeeNo = contributionFeeNo;
		this.unitName = unitName;
		this.contributionFeeMonth = contributionFeeMonth;
		this.bxEmpno = bxEmpno;
	}

	@Location(soleRow = 1, column = 3)
	@NotBlank(message = "稿酬编号不能为空！")
	private String contributionFeeNo; //稿酬编号
	
	@Location(soleRow = 1, column = 5)
	@NotBlank(message = "提报部门不能为空！")
	private String unitName; //预算单位	
	
	@Location(soleRow = 1, column = 7)
	@NotBlank(message = "稿费月份不能为空！")//
	//@Pattern(regexp="^(20)\\d{2}((0[1-9])|(10|11|12))$",message="稿酬月份请填写正确的格式。例【202006】")
	private String contributionFeeMonth; //稿费月份 
	
	@Location(soleRow = 1, column = 9)
	@NotBlank(message = "报销人不能为空！")
	private String bxEmpno;  //报销人的工号
	
	private ContributionFeeExcelDetail instance;
	
	/*************************以下是其它字段********************************************/
	private BudgetYearPeriod yearPeriod;
	
	private BudgetMonthPeriod monthPeriod;
	
	private BudgetUnit unit;
	
	private BudgetAuthorfeesum feeSum;
	
	private WbUser bxUser;

}
