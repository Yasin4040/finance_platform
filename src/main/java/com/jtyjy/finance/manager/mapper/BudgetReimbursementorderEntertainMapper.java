package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetReimbursementorderEntertain;
import com.jtyjy.finance.manager.easyexcel.EntertainSumExcelData;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderEntertainMapper extends BaseMapper<BudgetReimbursementorderEntertain> {
	
    /**
     * 招待报销汇总
     * @param bxType
     * @param yearId
     * @param monthId 
     * @return
     */
    List<EntertainSumExcelData> entertainSummaryByYear(Integer bxType, Long yearId, Long monthId);
}
