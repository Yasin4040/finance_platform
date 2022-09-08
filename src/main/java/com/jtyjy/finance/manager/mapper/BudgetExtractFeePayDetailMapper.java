package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractFeePayDetailBeforeCal;
import com.jtyjy.finance.manager.query.commission.FeeQuery;
import org.apache.ibatis.annotations.Param;

/**
 * @author Admin
 */
public interface BudgetExtractFeePayDetailMapper extends BaseMapper<BudgetExtractFeePayDetailBeforeCal> {
    Page<BudgetExtractFeePayDetailBeforeCal> selectFeePage(Page<Object> page,@Param("query") FeeQuery query);
}
