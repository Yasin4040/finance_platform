package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetCompanyMonthReport;
import com.jtyjy.finance.manager.mapper.BudgetCompanyMonthReportMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetCompanyMonthReportService extends DefaultBaseService<BudgetCompanyMonthReportMapper, BudgetCompanyMonthReport> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_company_month_report"));
	}
	
}
