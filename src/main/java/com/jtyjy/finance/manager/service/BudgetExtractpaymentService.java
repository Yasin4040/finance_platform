package com.jtyjy.finance.manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetExtractpaydetail;
import com.jtyjy.finance.manager.bean.BudgetExtractpayment;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetExtractpaymentMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractpaymentService extends DefaultBaseService<BudgetExtractpaymentMapper, BudgetExtractpayment> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extractpayment"));
	}

	/**
	 * 根据付款单主键查询付款详情
	 * @param paymoneyobjectid
	 * @return
	 */
	public BudgetExtractpaydetail getDetail(Long paymoneyobjectid) {
		String sql = " SELECT t.*  FROM budget_extractpaydetail t left join budget_extractpayment m on t.id = m.budgetextractpaydetailid where m.id = "+paymoneyobjectid;
		List<BudgetExtractpaydetail> list = this.jdbcTemplateService.query(sql, BudgetExtractpaydetail.class);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
}
