package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractpayRule;
import com.jtyjy.finance.manager.bean.BudgetExtractquotaRule;
import com.jtyjy.finance.manager.mapper.BudgetExtractquotaRuleMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractquotaRuleService extends DefaultBaseService<BudgetExtractquotaRuleMapper, BudgetExtractquotaRule> {

	private final TabChangeLogMapper loggerMapper;
	
	@Autowired
	private BudgetExtractquotaRuleMapper mapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extractquota_rule"));
	}

	public PageResult<BudgetExtractquotaRule> getExtractQuotaRuleList(Integer page, Integer rows, String ruleName) {
		Page<BudgetExtractquotaRule> pageCond = new Page<BudgetExtractquotaRule>(page,rows);
		pageCond = mapper.selectPage(pageCond, new QueryWrapper<BudgetExtractquotaRule>().like(StringUtils.isNotBlank(ruleName),"name", ruleName).orderByDesc("createtime"));
		return PageResult.apply(pageCond.getTotal(), pageCond.getRecords());
	}
	
}
