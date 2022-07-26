package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.mapper.WbDeptMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
public class WbDeptService extends DefaultBaseService<WbDeptMapper, WbDept> {

	private final TabChangeLogMapper loggerMapper;
	
	private final WbDeptMapper wdMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		//DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("wb_dept"));
	}
	
	public List<WbDept> getDeptInfo(String deptName) {
	    return this.wdMapper.getDeptInfo(deptName);
	}
}
