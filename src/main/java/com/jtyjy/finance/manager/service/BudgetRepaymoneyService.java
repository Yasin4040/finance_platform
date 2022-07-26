package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetArrears;
import com.jtyjy.finance.manager.bean.BudgetLendandrepaymoney;
import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.jtyjy.finance.manager.bean.BudgetRepaymoney;
import com.jtyjy.finance.manager.bean.BudgetRepaymoneyDetail;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetRepaymoneyMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.trade.DistributedNumber;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetRepaymoneyService extends DefaultBaseService<BudgetRepaymoneyMapper, BudgetRepaymoney> {

	private final TabChangeLogMapper loggerMapper;
	private final DistributedNumber distributedNumber;
	private final BudgetRepaymoneyDetailService repaymoneyDetailService;
	private final BudgetLendmoneyService lendmoneyService;
	private final BudgetArrearsService arrearsService;
	private final BudgetLendandrepaymoneyService lendandrepaymoneyService;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_repaymoney_new"));
	}
	
	/**
	 * 按照借款单还款
	 * @param lendMoney 借款单
	 * @param repayTypeId 还款对象id
	 * @param money 还款金额
	 * @param curlendmoney 当时借款金额
	 */
	public void payBack(BudgetLendmoney lendMoney,String repayTypeId, BigDecimal money,BigDecimal curlendmoney, String empId) {
		//本金未还 和 还款金额 比较 （大于还款金额）
		if(lendMoney.getLendmoney().add(lendMoney.getInterestmoney()).subtract(lendMoney.getRepaidmoney().add(lendMoney.getRepaidinterestmoney())).compareTo(money) < 0) {
			throw new RuntimeException("报销单生成付款单失败，【借款金额+利息】小于【还款金额+已还利息】金额");
		} 
		 //还款单
		BudgetRepaymoney budgetRepayMoney = JSON.parseObject(JSON.toJSONString(lendMoney), BudgetRepaymoney.class);
		budgetRepayMoney.setRepaytype(5);
		budgetRepayMoney.setRepaytypeid(repayTypeId);
		budgetRepayMoney.setCreatetime(new Date());
		budgetRepayMoney.setRepaydate(new Date());
		budgetRepayMoney.setEffectflag(false);
		budgetRepayMoney.setRepaymoney(money);
		budgetRepayMoney.setRepaymoneycode(this.distributedNumber.getRepayNum());
		if (StringUtils.isBlank(budgetRepayMoney.getEmpid())) {
		    budgetRepayMoney.setEmpid(empId);
		}
		this.save(budgetRepayMoney);
		//付款明细
		BudgetRepaymoneyDetail budgetRepayMoneyDetail = new BudgetRepaymoneyDetail();
		budgetRepayMoneyDetail.setRepaymoneyid(budgetRepayMoney.getId());
		budgetRepayMoneyDetail.setLendmoneyid(lendMoney.getId());
		//之前的金额
		budgetRepayMoneyDetail.setCurlendmoney(curlendmoney);
		budgetRepayMoneyDetail.setCreatetime(new Date());
		budgetRepayMoneyDetail.setRepaymoney(money);
		lendMoney.setRepaidmoney(lendMoney.getRepaidmoney().add(money));
		budgetRepayMoneyDetail.setInterestmoney(new BigDecimal(0));
		//现在的金额
		budgetRepayMoneyDetail.setNowlendmoney(budgetRepayMoneyDetail.getCurlendmoney().subtract(budgetRepayMoneyDetail.getRepaymoney()).subtract(budgetRepayMoneyDetail.getInterestmoney()));
		this.repaymoneyDetailService.save(budgetRepayMoneyDetail);
		this.repaymoney(budgetRepayMoney);
		this.lendmoneyService.updateById(lendMoney);
		//消除借款限制
		this.jdbcTemplateService.update("update budget_lendmoney_uselog_new set useflag=0 WHERE useflag=1 AND lendmoneyid="+lendMoney.getId());
	}
	
	/**
	 * 还款
	 * @param budgetRepayMoney
	 */
	public void repaymoney(BudgetRepaymoney budgetRepayMoney) {
		List<BudgetRepaymoney> repaymoneylist = this.jdbcTemplateService.query("select * from budget_repaymoney_new where empid='"+budgetRepayMoney.getEmpid()+"'", BudgetRepaymoney.class);
		if(repaymoneylist.size()==0){
			throw new RuntimeException("error!!!!!");
		}
		BudgetArrears arrears = this.arrearsService.getByEmpNo(budgetRepayMoney.getEmpno());
		BudgetLendandrepaymoney l_r_money = new BudgetLendandrepaymoney();
		if (arrears != null) {
			l_r_money.setEmpid(budgetRepayMoney.getEmpid());
			l_r_money.setEmpno(budgetRepayMoney.getEmpno());
			l_r_money.setEmpname(budgetRepayMoney.getEmpname());
			l_r_money.setRepaymoneyid(budgetRepayMoney.getId());
			l_r_money.setCurmoney(arrears.getArrearsmoeny()); //当前欠款
			budgetRepayMoney.setEffectflag(true);
			l_r_money.setMoney(budgetRepayMoney.getRepaymoney()); 
			l_r_money.setMoneytype(-1); // 还款
			l_r_money.setNowmoney(arrears.getArrearsmoeny().subtract(budgetRepayMoney.getRepaymoney()));
			l_r_money.setCreatetime(new Date());
			this.lendandrepaymoneyService.save(l_r_money);
			arrears.setArrearsmoeny(l_r_money.getNowmoney());
			arrears.setRepaymoney(arrears.getRepaymoney().add(l_r_money.getMoney()));
			this.arrearsService.updateById(arrears);
		}
		//当前还款员工还款总额(包括本次)
		BigDecimal totalrepaymoney = new BigDecimal(0);
		for(BudgetRepaymoney repaymoney:repaymoneylist) {
			totalrepaymoney = totalrepaymoney.add(repaymoney.getRepaymoney());
		}
		budgetRepayMoney.setNowrepaymoney(totalrepaymoney);
		this.saveOrUpdate(budgetRepayMoney);
	}
	
}
