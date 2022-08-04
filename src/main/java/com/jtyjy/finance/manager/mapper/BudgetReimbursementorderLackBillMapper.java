package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderLackBill;
import com.jtyjy.finance.manager.dto.BudgetLackBillQueryDTO;
import com.jtyjy.finance.manager.vo.BudgetLackBillVO;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderLackBillMapper extends BaseMapper<BudgetReimbursementorderLackBill> {

    List<BudgetLackBillVO> getLackBillList(Page pageCond, BudgetLackBillQueryDTO params, String authSql);
}
