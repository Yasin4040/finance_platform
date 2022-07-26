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
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderPayment;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderPaymentMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderPaymentService extends DefaultBaseService<BudgetReimbursementorderPaymentMapper, BudgetReimbursementorderPayment> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_payment"));
	}

	public int saveByOrder(List<BudgetReimbursementorderPayment> list, BudgetReimbursementorder order) {
	    //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderPayment> wrapper = new QueryWrapper<BudgetReimbursementorderPayment>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(list == null || list.size() == 0) {
			return 0;
		}
		BudgetReimbursementorderPayment.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderPayment> getByOrderId(Long id) {
		QueryWrapper<BudgetReimbursementorderPayment> wrapper = new QueryWrapper<BudgetReimbursementorderPayment>();
		wrapper.eq("reimbursementid", id);
		return this.list(wrapper);
		
	}
	
}
