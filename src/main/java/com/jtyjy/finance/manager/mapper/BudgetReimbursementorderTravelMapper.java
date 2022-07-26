package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTravel;
import com.jtyjy.finance.manager.easyexcel.TravelSumExcelData;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderTravelMapper extends BaseMapper<BudgetReimbursementorderTravel> {
	
    /**
     * 差旅报销汇总
     * @param bxType
     * @param yearId
     * @param monthId 
     * @return
     */
    List<TravelSumExcelData> travelSummaryByYear(Integer bxType, Long yearId, Long monthId);
}
