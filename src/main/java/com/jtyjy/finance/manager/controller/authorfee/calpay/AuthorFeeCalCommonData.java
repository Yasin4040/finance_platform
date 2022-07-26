package com.jtyjy.finance.manager.controller.authorfee.calpay;

import java.util.Map;

import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRuledetail;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeetaxRuledetail;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.WbBanks;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 稿费计税的通用数据 
 * @author minzhq
 *
 */
@Data
public class AuthorFeeCalCommonData {
	
	private Map<String, Object> salaryCompanyMap; //工资单位
	
	//生效的计税规则明细
	private List<BudgetAuthorfeetaxRuledetail> taxRuleDetailList = new ArrayList<>();
	//生效的发放规则明细
	private List<BudgetAuthorfeepayRuledetail> payRuleDetailList = new ArrayList<>();
	
	//配置的发放单位账户
	private BudgetBillingUnitAccount payUnitAccount;
	
	private Map<String,WbBanks> bankMap;
	
	//代收数据
	private ReplaceReceiveData replaceReceiveData; 
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class ReplaceReceiveData{
		private String code;
		private String openBank;
		private String bankAccount;
		private String unit;
	}
	
	public ReplaceReceiveData createReplaceReceiveData(String code,String openBank
				,String bankAccount,String unit) {
		return new ReplaceReceiveData(code,openBank,bankAccount,unit);
	}
	
	
	public PayUnit createPayUnit(Long payUnitid,Long payUnitAccountId,String payBank,
			String payBankAccount,String payUnitName) {
		return new PayUnit(payUnitid,payUnitAccountId,payBank,payBankAccount,payUnitName);
	}
	
	//默认发放账户（不计税[身份证不为空，纳税人识别号为空]或者（内部员工）付款单位与员工工资发放单位一致）
	private PayUnit defaultPayUnit;
	
	//[身份证为空，纳税人识别号不为空]
	private PayUnit noTaxPayUnit;

	//慧谷
	private PayUnit hg;

	//日出
	private PayUnit rc;

	//金太阳
	private PayUnit jty;
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class PayUnit{
		private Long payUnitid;
		private Long payUnitAccountId;
		private String payBank;
		private String payBankAccount;
		private String payUnitName;
	}
}
