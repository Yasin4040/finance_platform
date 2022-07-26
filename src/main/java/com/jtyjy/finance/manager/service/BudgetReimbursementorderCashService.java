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
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderCash;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderCashMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderCashService extends DefaultBaseService<BudgetReimbursementorderCashMapper, BudgetReimbursementorderCash> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_cash"));
	}

	public int saveByOrder(List<BudgetReimbursementorderCash> list, BudgetReimbursementorder order) {
        //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderCash> wrapper = new QueryWrapper<BudgetReimbursementorderCash>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
        if(null == list || list.isEmpty()) {
			return 0;
		}
		BudgetReimbursementorderCash.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}
	
	/**
	 * 按照订单主键查询
	 * @param orderId
	 * @return
	 */
	public List<BudgetReimbursementorderCash> getByOrderId(Long orderId){
		QueryWrapper<BudgetReimbursementorderCash> wrapper = new QueryWrapper<BudgetReimbursementorderCash>();
		wrapper.eq("reimbursementid", orderId);
		return this.list(wrapper);
	}
	
}
