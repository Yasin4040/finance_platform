package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetMonthStartup;
import org.apache.ibatis.annotations.Param;

/**
 * @author Admin
 */
public interface BudgetMonthStartupMapper extends BaseMapper<BudgetMonthStartup> {

    /**
     * 获取当前月启动预算
     *
     * @param yearId 届别Id
     * @return 结果集
     */
    BudgetMonthStartup getCurrentMonthStartUp(@Param("yearId") Long yearId);
}
