package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderLackBill;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderLackBillMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderLackBillService extends DefaultBaseService<BudgetReimbursementorderLackBillMapper, BudgetReimbursementorderLackBill> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_LackBill"));
	}

	public int saveByOrder(List<BudgetReimbursementorderLackBill> list, BudgetReimbursementorder order) {
	    //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderLackBill> wrapper = new QueryWrapper<>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(list == null || list.size() == 0) {
			return 0;
		}
		BudgetReimbursementorderLackBill.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderLackBill> getByOrderId(Long id) {
		QueryWrapper<BudgetReimbursementorderLackBill> wrapper = new QueryWrapper<BudgetReimbursementorderLackBill>();
		wrapper.eq("reimbursementid", id);
		return this.list(wrapper);
		
	}
	
}
