package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetYearPeriod;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author Admin
 */
public interface BudgetYearPeriodMapper extends BaseMapper<BudgetYearPeriod> {
	
    /**
     * 获取届别信息
     * @return Map<String, Object>
     */
    List<Map<String, Object>> getYearPeriod();
    
    /**
     * 根据届别id获得月份信息
     * @param yearId yearId
     * @return Map<String, Object>
     */
    List<Map<String, Object>> getMonthPeriod(@Param("yearId") Long yearId);
    
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

    @Cacheable(value = "yearCache",key = "#yearId",unless = "#result == null")
    String getNameById(Long yearId);
}
