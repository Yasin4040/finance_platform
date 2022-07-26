package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.BudgetExtractpayRule;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitAccountMapper;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitMapper;
import com.jtyjy.finance.manager.mapper.BudgetExtractpayRuleMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.vo.ExtractPayRuleVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractpayRuleService extends DefaultBaseService<BudgetExtractpayRuleMapper, BudgetExtractpayRule> {

	private final TabChangeLogMapper loggerMapper;
	
	@Autowired
	private BudgetExtractpayRuleMapper mapper;
	
	@Autowired
	private BudgetBillingUnitMapper unitMapper;
	
	@Autowired
	private BudgetBillingUnitAccountMapper unitAccountMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extractpay_rule"));
	}

	public PageResult<ExtractPayRuleVO> getExtractPayRuleList(Integer page, Integer rows, String ruleName) {
		Page<BudgetExtractpayRule> pageCond = new Page<BudgetExtractpayRule>(page,rows);
		pageCond = mapper.selectPage(pageCond, new QueryWrapper<BudgetExtractpayRule>().like(StringUtils.isNotBlank(ruleName),"name", ruleName).orderByDesc("createtime"));
		
		List<BudgetExtractpayRule> records = pageCond.getRecords();
		List<ExtractPayRuleVO> voList = records.stream().map(e->{
			ExtractPayRuleVO vo = new ExtractPayRuleVO();
			BeanUtils.copyProperties(e, vo);
			vo.setCreatetime(Constants.FULL_FORMAT.format(e.getCreatetime()));
			vo.setEffectdate(Constants.FORMAT_10.format(e.getEffectdate()));
			if(e.getPersonunitid()!=null) {
				//单位账户id
				BudgetBillingUnitAccount unitAccount = unitAccountMapper.selectById(e.getPersonunitid());
				BudgetBillingUnit billingUnit = unitMapper.selectById(unitAccount.getBillingunitid());
				vo.setPersonunitname(billingUnit.getName()+"-"+unitAccount.getBankaccount());
			}
			return vo;
		}).collect(Collectors.toList());		
		return PageResult.apply(pageCond.getTotal(), voList);
	}
	
	
}





