package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetYearPeriod;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetYearPeriodMapper extends BaseMapper<BudgetYearPeriod> {
	
    /**
     * 获取届别信息
     * @return
     */
    List<Map<String, Object>> getYearPeriod();
    
    /**
     * 根据届别id获得月份信息
     * @param yearId
     * @return
     */
    List<Map<String, Object>> getMonthPeriod(Long yearId);
    
    /**
     * 获取当前期间
     * @return
     */
    List<BudgetYearPeriod> getCurrentPeriod();
    
    /**
     * 获取最新的期间
     * @return
     */
    List<BudgetYearPeriod> getNewestPeriod();
}
