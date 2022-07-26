package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetProject;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.bean.BudgetProject;
import com.jtyjy.finance.manager.mapper.BudgetProjectMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

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
public class BudgetProjectService extends DefaultBaseService<BudgetProjectMapper, BudgetProject> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetProjectMapper bpMapper;
	
	private final BudgetYearPeriodService bypService;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_project_new"));
	}
	
	public Page<BudgetProject> queryByYear(Map<String, Object> conditionMap, Integer page, Integer rows){
	    QueryWrapper<BudgetProject> queryWrapper = new QueryWrapper<>();
        Page<BudgetProject> pageBean = new Page<BudgetProject>(page, rows);
        if (null == conditionMap.get("yearId")) {
            BudgetYearPeriod nowYear = this.bypService.getNowYearPeriod();
            conditionMap.put("yearId", nowYear.getId());
        }
        queryWrapper.eq("yearid", conditionMap.get("yearId"));
        if(null != conditionMap.get("projectno") && !"".equals(conditionMap.get("projectno"))) {
            queryWrapper.like("projectno", conditionMap.get("projectno"));
        }
        if(null != conditionMap.get("type")) {
            queryWrapper.eq("type", conditionMap.get("type"));
        }
        if(null != conditionMap.get("confirmflag")) {
            queryWrapper.eq("confirmflag", conditionMap.get("confirmflag"));
        }
        if(null != conditionMap.get("name") && !"".equals(conditionMap.get("name"))) {
            queryWrapper.like("name", conditionMap.get("name"));
        }
        if(null != conditionMap.get("unitId")) {
            queryWrapper.like("unitids", conditionMap.get("unitId"));
        }
        queryWrapper.orderByAsc("orderno");
        Page<BudgetProject> selectPage = this.bpMapper.selectPage(pageBean, queryWrapper);
        //List<BudgetProject> list = selectPage.getRecords();
        return selectPage;
	    
	}
}
