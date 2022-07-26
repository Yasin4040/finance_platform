package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentAddVO;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentVO;
import com.jtyjy.finance.manager.vo.BudgetSubjectAgentVO;
import com.jtyjy.finance.manager.vo.BxMonthAgentVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetMonthAgentMapper extends BaseMapper<BudgetMonthAgent> {

    /**
     * 获取受指定条件限制的月度动因主键
     *
     * @param ids        类似于 1,2,3,4
     * @param columnName
     * @return
     */
    List<Long> getControlAgentId(@Param("ids") String ids, @Param("columnName") String columnName);

    /**
     * 查询月度动因（普通）
     *
     * @param pageBean        分页
     * @param budgetUnitId    预算单位Id
     * @param budgetSubjectId 预算科目Id
     * @param monthId         月份Id
     * @param name            月度动因名称
     * @return 结果集
     */
    List<BudgetMonthAgentVO> monthAgentPage1(@Param("pageBean") Page<BudgetMonthAgentVO> pageBean,
                                             @Param("budgetUnitId") Long budgetUnitId,
                                             @Param("budgetSubjectId") Long budgetSubjectId,
                                             @Param("monthId") Integer monthId,
                                             @Param("name") String name);

    /**
     * 查询月度动因（产品）
     *
     * @param pageBean        分页
     * @param budgetUnitId    预算单位Id
     * @param budgetSubjectId 预算科目Id
     * @param monthId         月份Id
     * @param name            月度动因名称
     * @return 结果集
     */
    List<BudgetMonthAgentVO> monthAgentPage2(@Param("pageBean") Page<BudgetMonthAgentVO> pageBean,
                                             @Param("budgetUnitId") Long budgetUnitId,
                                             @Param("budgetSubjectId") Long budgetSubjectId,
                                             @Param("monthId") Integer monthId,
                                             @Param("name") String name);

    /**
     * 查询月度动因（分解）
     *
     * @param pageBean        分页
     * @param budgetSubjectId 预算科目Id
     * @param monthId         月份Id
     * @param name            月度动因名称
     * @return 结果集
     */
    List<BudgetMonthAgentVO> monthAgentPage3(@Param("pageBean") Page<BudgetMonthAgentVO> pageBean,
                                             @Param("budgetSubjectId") Long budgetSubjectId,
                                             @Param("monthId") Integer monthId,
                                             @Param("name") String name);

    /**
     * 查询年度动因下月份报销金额与划拨金额（已提交状态和已审核状态，且排除当前月份）
     *
     * @param yearAgentId 年度动因Id
     * @param monthId     月份Id
     * @return 报销金额与划拨金额
     */
    List<Map<String, BigDecimal>> listExecuteAndAllocateByExcludeMonthId(@Param("yearAgentId") Long yearAgentId, @Param("monthId") Long monthId);

    /**
     * 根据预算科目Id查询月度动因
     *
     * @param budgetUnitId 预算科目Id
     * @param monthId      月份Id
     * @param type         类型（1普通 2产品）
     * @return 结果集
     */
    List<BudgetMonthAgentVO> listMonthAgentByUnitId(@Param("budgetUnitId") Long budgetUnitId, @Param("monthId") Long monthId, @Param("type") Integer type);

    /**
     * 查询预算单位下所有月度分解动因
     *
     * @param budgetUnitId 预算科目Id
     * @param monthId      月份Id
     * @return 结果集
     */
    List<BudgetMonthAgentVO> listCostSplitMonthAgent(@Param("budgetUnitId") Long budgetUnitId, @Param("monthId") Long monthId);

    /**
     * 获取单位年度动因信息
     *
     * @param bean
     * @return
     */
    ReimbursementValidateMoney getUnitYearAgentInfo(@Param("bean") MonthAgentMoneyInfo bean);

    ReimbursementValidateMoney getUnitYearAgentInfoByYearAgentId(@Param("bean") MonthAgentMoneyInfo bean);

    /**
     * 获取单位月度科目信息
     *
     * @param bean
     * @return
     */
    ReimbursementValidateMoney getUnitMonthSubjectInfo(@Param("bean") MonthAgentMoneyInfo bean);

    /**
     * 查询月度科目动因
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetSubjectAgentVO> listSubjectMonthAgentByMap(@Param("pageBean") Page<BudgetSubjectAgentVO> pageBean, @Param("map") HashMap<String, Object> paramMap);

    /**
     * 根据年度动因Id查询已报销金额
     *
     * @param yearAgentId 年度动因Id
     * @return 结果集
     */
    List<BigDecimal> listReimMoneyByYearAgentId(@Param("yearAgentId") Long yearAgentId);

    /**
     * 根据年度动因Id查询已划拨金额
     *
     * @param yearAgentId 年度动因Id
     * @return 结果集
     */
    List<BigDecimal> listAllocatedMoneyByYearAgentId(@Param("yearAgentId") Long yearAgentId);

    /**
     * 查询月度追加动因信息
     *
     * @param monthAgentId 月度动因Id
     * @return 结果集
     */
    BudgetMonthAgentAddVO getMonthAgentInfo(@Param("monthAgentId") Long monthAgentId);

    /**
     * 获取可报销月度动因(项目借款生成报销)
     *
     * @param yearId       届别Id
     * @param budgetUnitId 预算单位Id
     * @param monthId      月份Id
     * @return 结果集
     */
    List<BxMonthAgentVO> listMonthAgentByBx(@Param("yearId") Long yearId,
                                            @Param("budgetUnitId") Long budgetUnitId,
                                            @Param("monthId") Long monthId);

    /**
     * 查询月度动因信息（固定资产）
     *
     */
    List<BudgetMonthAgent> getMonthAgentInfoAsset(@Param("map")HashMap<String, Object> paramMap);
}
