package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.vo.BudgetYearAgentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetYearAgentMapper extends BaseMapper<BudgetYearAgent> {

    /**
     * 获取年度动因列表（分页）
     *
     * @param pageBean        分页
     * @param budgetUnitId    预算单位Id
     * @param budgetSubjectId 预算科目Id
     * @param name            动因名称
     * @return 年度动因集合
     */
    List<BudgetYearAgentVO> yearAgentPage(@Param("pageBean") Page<BudgetYearAgentVO> pageBean,
                                          @Param("budgetUnitId") Long budgetUnitId,
                                          @Param("budgetSubjectId") Long budgetSubjectId,
                                          @Param("name") String name,@Param("category")String category);

    /**
     * 检测动因是否能删除
     *
     * @param yearAgentIds 需要删除的动因Ids
     * @return 不能删除的动因数量
     */
    Integer countNotDeleteByIds(@Param("list") List<Long> yearAgentIds);

    /**
     * 根据预算单位Id查询年度动因
     *
     * @param yearId       届别Id
     * @param budgetUnitId 预算单位Id
     * @return 年度动因集合
     */
    List<BudgetYearAgent> listYearAgentByUnitId(@Param("yearId") Long yearId, @Param("budgetUnitId") Long budgetUnitId);

    /**
     * 统计届别下是否存在科目
     *
     * @param yearId    届别Id
     * @param subjectId 预算科目Id
     * @return
     */
    Integer countYearSubject(@Param("yearId") Long yearId, @Param("subjectId") Long subjectId, @Param("productId") Long productId);

    /**
     * 根据月度动因Id获取年度动因
     *
     * @param monthAgentId 月度动因Id
     * @return 年度动因
     */
    BudgetYearAgent getYearAgentByMonthAgentId(@Param("monthAgentId") Long monthAgentId);
}
