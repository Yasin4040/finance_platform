package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetYearSubject;
import com.jtyjy.finance.manager.vo.BudgetYearSubjectVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetYearSubjectMapper extends BaseMapper<BudgetYearSubject> {

    /**
     * 查询年度预算科目
     *
     * @param budgetUnitId 单位Id
     * @return 年度预算科目
     */
    List<BudgetYearSubjectVO> listYearSubjectByUnitId(@Param("budgetUnitId") Long budgetUnitId);

    /**
     * 查询预算单位下各项费用金额
     *
     * @param budgetUnitId 预算单位Id
     * @return 费用统计
     */
    List<Map<String, String>> listTotalByUnitId(@Param("budgetUnitId") Long budgetUnitId);
}
