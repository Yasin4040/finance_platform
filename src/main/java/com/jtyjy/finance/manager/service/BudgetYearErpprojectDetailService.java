package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetYearErpprojectDetail;
import com.jtyjy.finance.manager.mapper.BudgetYearErpprojectDetailMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
public class BudgetYearErpprojectDetailService extends DefaultBaseService<BudgetYearErpprojectDetailMapper, BudgetYearErpprojectDetail> {

	private final TabChangeLogMapper loggerMapper;

	public BudgetYearErpprojectDetailService(TabChangeLogMapper loggerMapper) {
		this.loggerMapper = loggerMapper;
	}

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_erpproject_detail"));
	}
	
}
