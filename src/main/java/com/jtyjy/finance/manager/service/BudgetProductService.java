package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetProduct;
import com.jtyjy.finance.manager.mapper.BudgetProductMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品管理service
 * @author shubo
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProductService extends DefaultBaseService<BudgetProductMapper, BudgetProduct> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetProductMapper bpMapper;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_product"));
	}
	
	public Page<BudgetProduct> getProductInfo(Map<String, Object> conditionMap, Integer page, Integer rows){
	    Page<BudgetProduct> pageCond = new Page<>(page, rows);
	    List<BudgetProduct> retList = bpMapper.getProductPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
	    pageCond.setRecords(retList);
	    return pageCond;
	
	}

}
