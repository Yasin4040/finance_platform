package com.jtyjy.finance.manager.controller.extract.pay;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.jtyjy.finance.manager.bean.BudgetBankAccount;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.BudgetExtractOuterperson;
import com.jtyjy.finance.manager.bean.BudgetExtractdetail;
import com.jtyjy.finance.manager.bean.BudgetExtractpayRule;
import com.jtyjy.finance.manager.bean.BudgetExtractpaydetail;
import com.jtyjy.finance.manager.bean.BudgetExtractquotaRule;
import com.jtyjy.finance.manager.bean.WbBanks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提成人员计算数据明细
 * @author minzhq
 */
@Data
@NoArgsConstructor
public class ExtractEmpCalDataDetail {

	private List<BudgetExtractdetail> extractDetails;
	
	private Boolean iscompanyemp;
	
	private Boolean isSepcialPerson = false; //是否是特殊人员

	private String empid;
	
	private String empno;
	
	private String empname;
	
	private String idnumber;
	
	private BigDecimal copeextract; //应发提成
	
	private BigDecimal realExtract; //实发提成（扣了借款的）

	//费用发放（计算之前）
	private BigDecimal feePay = BigDecimal.ZERO;
	
	private BigDecimal consotax; //综合税
	
	private Map<String,Object> salaryMap; //工资明细
	
	private BudgetBillingUnit billingUnit; // 发放单位

	private BudgetBillingUnitAccount unitAccount; //单位账户

	private WbBanks unitAccountBank; //单位账户银行信息
	
	private BudgetBillingUnit avoidBillingUnit; // 发放单位

	private BudgetBillingUnitAccount avoidUnitAccount; //单位账户

	private WbBanks avoidUnitAccountBank; //单位账户银行信息

	private BudgetBankAccount personAccount; //个人账号
	
	private OuterPersonAccountData outerPersonAccount;

	private WbBanks personAccountBank; //个人账号银行信息

	private Boolean isQuit = false; //是否是离职员工
	
	private BudgetExtractpayRule payRule;//发放规则
	
	private BudgetExtractquotaRule quotaRule; //限额规则
	
	private List<BudgetExtractpaydetail> curBillUnitPayDetailList; //当前工资发位下的计税明细
	
	private List<BudgetExtractOuterperson> refOuterPersonList;//关联的外部人员名单
	
	private BigDecimal finalIncorporatedCompanyPayedExtract = BigDecimal.ZERO; //最终法人公司发放金额
	
	private BigDecimal avoidTaxMoney = BigDecimal.ZERO;//最终陈彩莲发放
	
	private String curExtractDetailIds;
	
	public OuterPersonAccountData createOuterPersonAccountData(String bankAccount,String branchcode,String bankName,String subBranchName){
		return new OuterPersonAccountData(bankAccount,branchcode,bankName,subBranchName);
	}
	
	//public void getOuterPersonB
	/**
	 * 外部人员账户数据
	 */
	@Data
	@AllArgsConstructor
	public class OuterPersonAccountData{
		public String bankAccount;
		public String branchcode;
		public String bankName;
		public String subBranchName;
	}
}


