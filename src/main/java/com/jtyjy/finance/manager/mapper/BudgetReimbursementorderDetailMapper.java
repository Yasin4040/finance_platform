package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.vo.BxDetailVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderDetailMapper extends BaseMapper<BudgetReimbursementorderDetail> {

    /**
     * 根据年度动因Id查询报销单明细
     *
     * @param yearAgentId 年度动因Id
     * @return 结果集
     */
    List<BudgetReimbursementorderDetail> listDetailByYearAgentId(@Param("yearAgentId") Long yearAgentId);

    /**
     * 根据预算单位Id、预算科目Id、月份Id查询报销单明细
     *
     * @param unitId    预算单位Id
     * @param subjectId 预算科目Id
     * @param monthId   月份Id
     * @return 结果集
     */
    List<BudgetReimbursementorderDetail> listDetailByMonthId(@Param("unitId") Long unitId, @Param("subjectId") Long subjectId, @Param("monthId") Long monthId);

    /**
     * 报销明细分页查询
     * @param pageCond
     * @param conditionMap
     * @param authSql
     * @return
     */
    List<BxDetailVO> getBxDetailPageInfo(Page<BxDetailVO> pageCond, Map<String, Object> conditionMap, String authSql);

    List<BxDetailVO> getBxDetailNoPage(Map<String, Object> conditionMap, String authSql);

    List<BudgetReimbursementorderDetail> getByBxNum(@Param("bxNum") String bxNum);
}
