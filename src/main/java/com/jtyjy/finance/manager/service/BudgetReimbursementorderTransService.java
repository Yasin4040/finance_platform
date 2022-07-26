package com.jtyjy.finance.manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTrans;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderTransMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderTransService extends DefaultBaseService<BudgetReimbursementorderTransMapper, BudgetReimbursementorderTrans> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_trans"));
	}

	public int saveByOrder(List<BudgetReimbursementorderTrans> list, BudgetReimbursementorder order) {
	    //按照订单主键删除
        QueryWrapper<BudgetReimbursementorderTrans> wrapper = new QueryWrapper<BudgetReimbursementorderTrans>();
        wrapper.eq("reimbursementid", order.getId());
        this.remove(wrapper);
	    if(null == list || list.isEmpty()) {
			return 0;
		}
		BudgetReimbursementorderTrans.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}
	
	/**
	 * 按照订单主键查询
	 * @param orderId
	 * @return
	 */
	public List<BudgetReimbursementorderTrans> getByOrderId(Long orderId){
		Page<BudgetReimbursementorderTrans> pageCond = new Page<>(1, 200);
	    QueryWrapper<BudgetReimbursementorderTrans> wrapper = new QueryWrapper<BudgetReimbursementorderTrans>();
		wrapper.eq("reimbursementid", orderId);
		pageCond = this.page(pageCond, wrapper);
		return pageCond.getRecords();
	}
	
}
