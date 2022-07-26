package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetMonthAgentadd;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentAddVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetMonthAgentaddMapper extends BaseMapper<BudgetMonthAgentadd> {

    /**
     * 获取年度可追加动因
     *
     * @param budgetUnitId    预算单位Id
     * @param budgetSubjectId 预算科目Id
     * @param monthId         月份Id
     * @return 结果集
     */
    List<BudgetMonthAgent> listCanAddAgents(@Param("budgetUnitId") Long budgetUnitId,
                                            @Param("budgetSubjectId") Long budgetSubjectId,
                                            @Param("monthId") Long monthId);

    /**
     * 查询单个月度追加动因列表
     *
     * @param infoId 主表主键Id
     * @return 结果集
     */
    List<BudgetMonthAgentAddVO> listAddAgentByInfoId(@Param("infoId") Long infoId);

    /**
     * 已提交审核的追加金额
     *
     * @param monthAgentId 月度动因id
     * @return 结果集
     */
    List<BigDecimal> listLockMoneyByMonthAgentId(@Param("monthAgentId") Long monthAgentId);
}
