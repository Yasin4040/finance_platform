package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderAllocated;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderAllocatedMapper extends BaseMapper<BudgetReimbursementorderAllocated> {

    /**
     * 根据年度动因Id查询划拨表明细
     *
     * @param yearAgentId 年度动因Id
     * @return 结果集
     */
    List<BudgetReimbursementorderAllocated> listDetailByYearAgentId(Long yearAgentId);

    /**
     * 根据预算单位Id、预算科目Id、月份Id查询报销单明细
     *
     * @param unitId    预算单位Id
     * @param subjectId 预算科目Id
     * @param monthId   月份Id
     * @return 结果集
     */
    List<BudgetReimbursementorderAllocated> listDetailByMonthId(@Param("unitId") Long unitId, @Param("subjectId") Long subjectId, @Param("monthId") Long monthId);
}
