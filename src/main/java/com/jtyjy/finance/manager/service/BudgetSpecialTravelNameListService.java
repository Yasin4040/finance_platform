package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetSpecialTravelNameList;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetSpecialTravelNameListMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
public class BudgetSpecialTravelNameListService extends DefaultBaseService<BudgetSpecialTravelNameListMapper, BudgetSpecialTravelNameList> {

	@Autowired
	private TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("BudgetSpecialTravelNameList"));
	}
}
