package com.jtyjy.finance.manager.controller.extract.pay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;

import lombok.Data;

/**
 * 提成发放通用数据
 * @author minzhq
 */
@Data
public class ExtractPayCommonData {
	private List<BudgetExtractsum> curBatchExtractSumList;
	
	private String curExtractBatch;
	
	private BudgetBillingUnit outerBillingUnit; //外部人员发放单位
	
	private BudgetBillingUnitAccount outerBillingUnitAccount; //外部人员发放单位账号
	
	private WbBanks outerWbBank; //外部人员发放单位账号银行信息
		
	private BudgetBillingUnit quiterBillingUnit; //离职人员发放单位
	
	private BudgetBillingUnitAccount quiterBillingUnitAccount; //离职人员发放单位账号
	
	private WbBanks quiterWbBank; //离职人员发放单位账号银行信息
	
	private Map<String, Object> salaryMsg; //工资信息
	
	private List<BudgetExtractpayRule> payRuleList; //生效的发放规则
	
	private List<BudgetExtractquotaRule> quotaruleList; //生效的限额规则
	
	private List<BudgetBillingUnit> allBillingUnitList; //所有的开票单位
	
	//key 为电子银联号
	private Map<String,WbBanks> banksMap; //银行信息
	
	//key 为开票单位的id
	private Map<Long,List<BudgetBillingUnitAccount>> billingAccountMap = new HashMap<>();
	
	private List<BudgetBillingUnitAccount> billingAccountList = new ArrayList<>();
	
	//key 为限额规则主表的id
	private Map<Long,List<BudgetExtractquotaRuledetail>> quotaRuleDetailMap = new HashMap<>(); //限额规则明细
	
	// key 为code_pname
	private Map<String,BudgetBankAccount> bankAccountMap;
	
	//key为工号
	private Map<String,WbUser> userMap;
	
	//外部人员map key为身份证号
	private Map<String,BudgetExtractOuterperson> outPersonMap;
	
	private Boolean isDeduction;  //是否允许冲借款
	//员工的借款列表
	private Map<String,List<BudgetLendmoney>> empno2LendmoneyMap;
	
	//key 明细id
	private Map<Long,Integer> payOrderMap = new HashMap<>(); //发放顺序
	
	//特殊人员名单
	List<HrSalaryYearTaxUser> specialPersonNameList;
	//当前年的开始提成批次
	private String curYearStartExtractBatch;
	//当前年的结束提成批次
	private String curYearEndExtractBatch ;
	
	private BigDecimal outerThreshold; //外部人员起征点
	
	private BigDecimal outerOrinalExtraTax; //外部人员标准额外税
	
	//根据身份证获取每个人每个工资发位的计税明细。
	private Map<String,Map<Long,List<BudgetExtractpaydetail>>> agoPayDetailMap;
	
	private List<ExtractEmpCalDataDetail> extractCalDataDetailList = new ArrayList<>();
	
	private List<BudgetExtractpayment> paymentList = new ArrayList<>();

	private Map<String, List<BudgetExtractFeePayDetailBeforeCal>> feePayEmpMap = new HashMap<>();
	/**
	 * 外部人员要使用
	 * @return
	 */
	public static List<Map> getTaxList() {
		List<Map> taxList = new ArrayList<>();
		Map m = new HashMap();
		m.put("quickcal","0");
		m.put("taxrate","3");
		taxList.add(m);
		Map m1 = new HashMap();
		m1.put("quickcal","210");
		m1.put("taxrate","10");
		taxList.add(m1);
		Map m2 = new HashMap();
		m2.put("quickcal","1410");
		m2.put("taxrate","20");
		taxList.add(m2);
		Map m3 = new HashMap();
		m3.put("quickcal","2660");
		m3.put("taxrate","25");
		taxList.add(m3);
		Map m4 = new HashMap();
		m4.put("quickcal","4410");
		m4.put("taxrate","30");
		taxList.add(m4);
		Map m5 = new HashMap();
		m5.put("quickcal","7160");
		m5.put("taxrate","35");
		taxList.add(m5);
		Map m6 = new HashMap();
		m6.put("quickcal","15160");
		m6.put("taxrate","45");
		taxList.add(m6);
		return taxList;
	}
}
