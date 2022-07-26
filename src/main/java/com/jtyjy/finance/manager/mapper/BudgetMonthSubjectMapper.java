package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetMonthSubject;
import com.jtyjy.finance.manager.vo.BudgetMonthSubjectVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetMonthSubjectMapper extends BaseMapper<BudgetMonthSubject> {

    /**
     * 根据预算单位查询月度科目预算
     *
     * @param budgetUnitId 预算单位Id
     * @param monthId      月份Id
     * @return 结果集
     */
    List<BudgetMonthSubjectVO> listMonthSubjectByUnitId(@Param("budgetUnitId") Long budgetUnitId, @Param("monthId") Long monthId);

    /**
     * 公司月度汇总
     *
     * @param yearId  届别Id
     * @param monthId 月份Id
     * @return 结果集
     */
    List<BudgetMonthSubjectVO> exportCompanyMonthAgentCollect(@Param("yearId") Long yearId, @Param("monthId") Long monthId);

    /**
     * 预算科目月度汇总
     *
     * @param subjectId 预算科目Id
     * @param monthId   月份Id
     * @return 结果集
     */
    List<BudgetMonthSubjectVO> listMonthSubjectBySubjectId(@Param("subjectId") Long subjectId, @Param("monthId") Long monthId);

    List<BudgetMonthSubjectVO> listMonthSubjectByUnitIdAndMonth(@Param("budgetUnitId") Long budgetUnitId,
                                                                @Param("monthIds") List<Long> monthIds,
                                                                @Param("monthId") Long monthId);
}
