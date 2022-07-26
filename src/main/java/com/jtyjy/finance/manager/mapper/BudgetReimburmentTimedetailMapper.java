package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetReimburmentTimedetail;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetReimburmentTimedetailMapper extends BaseMapper<BudgetReimburmentTimedetail> {
	
    List<BudgetReimburmentTimedetail> getTimeDetail(Long yearId, Long monthId, String authSql);
}
