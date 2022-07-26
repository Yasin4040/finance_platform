package com.jtyjy.finance.manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderAllocated;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderAllocatedMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderAllocatedService extends DefaultBaseService<BudgetReimbursementorderAllocatedMapper, BudgetReimbursementorderAllocated> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_allocated"));
	}

	public int saveByOrder(List<BudgetReimbursementorderAllocated> list, BudgetReimbursementorder order) {
        //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderAllocated> wrapper = new QueryWrapper<BudgetReimbursementorderAllocated>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(null == list || list.isEmpty()) {
			return 0;
		}
		BudgetReimbursementorderAllocated.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderAllocated> getByOrderId(Long orderId) {
		QueryWrapper<BudgetReimbursementorderAllocated> wrapper = new QueryWrapper<BudgetReimbursementorderAllocated>();
		wrapper.eq("reimbursementid", orderId);
		return this.list(wrapper);
	}
	
}
