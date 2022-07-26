package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetUnitSubject;
import com.jtyjy.finance.manager.mapper.BudgetUnitSubjectMapper;
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
public class BudgetUnitSubjectService extends DefaultBaseService<BudgetUnitSubjectMapper, BudgetUnitSubject> {

	private final TabChangeLogMapper loggerMapper;

	private final BudgetUnitSubjectMapper mapper;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_unit_subject"));
	}

	/**
	 * 根据预算单位主键查询预算单位科目
	 * @param unitIds
	 * @return
	 */
	public List<BudgetUnitSubject> getBudgetUnitSubjectByUnitIds(List<Long> unitIds) {
		QueryWrapper<BudgetUnitSubject> wrapper = new QueryWrapper<BudgetUnitSubject>();
		wrapper.in("unitid", unitIds);
		return this.list(wrapper);
	}
	
	public List<Long> getUnitIdsBySubjectId(Long subjectId){
	    return this.mapper.getUnitIdsBySubjectId(subjectId);
	}
}

