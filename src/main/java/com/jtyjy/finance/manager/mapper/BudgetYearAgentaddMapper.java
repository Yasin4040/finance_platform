package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.bean.BudgetYearAgentadd;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetYearAgentaddMapper extends BaseMapper<BudgetYearAgentadd> {

    /**
     * 获取年度可追加动因
     *
     * @param budgetUnitId    预算单位Id
     * @param budgetSubjectId 预算科目Id
     * @return 结果集
     */
    List<BudgetYearAgent> listCanAddAgents(@Param("budgetUnitId") Long budgetUnitId, @Param("budgetSubjectId") Long budgetSubjectId);

}
