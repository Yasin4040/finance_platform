package com.jtyjy.finance.manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderFdtaskDetail;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderFdtaskDetailMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderFdtaskDetailService extends DefaultBaseService<BudgetReimbursementorderFdtaskDetailMapper, BudgetReimbursementorderFdtaskDetail> {

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_fdtask_detail"));
	}

	/**
	 * 根据报销单和当前登录人，查询当前登录人单位的分单详情
	 * @param id
	 * @param status 做账状态
	 * @return
	 */
	public List<BudgetReimbursementorderFdtaskDetail> getTaskByUserIdAndOrder(Long orderId,Integer status) {
		String userId = UserThreadLocal.get().getUserId();
		String sql = "select * from budget_reimbursementorder_fdtask_detail detail where detail.bunitid in(SELECT _bunit.id FROM budget_billing_unit _bunit WHERE _bunit.accountants IS NOT NULL AND  find_in_set('"+userId+"',_bunit.accountants)) and detail.accountstatus = "+status+" and detail.reimbursementid = "+orderId;
		List<BudgetReimbursementorderFdtaskDetail> list = this.jdbcTemplateService.query(sql, BudgetReimbursementorderFdtaskDetail.class);
		return list;
	}

	/**
	 * 按照报销单主键删除
	 * @param id
	 */
	public void removeByOrderId(Long orderId) {
		QueryWrapper<BudgetReimbursementorderFdtaskDetail> wrapper = new QueryWrapper<BudgetReimbursementorderFdtaskDetail>();
		wrapper.eq("reimbursementid", orderId);
		this.remove(wrapper);
	}

	/**
	 * 按照报销单主键和工号查询分单明细
	 * @param orderId
	 * @param userName
	 * @return
	 */
	public List<BudgetReimbursementorderFdtaskDetail> getTaskDetailByOrderIdAndUserEmpno(Long orderId,String userName) {
		QueryWrapper<BudgetReimbursementorderFdtaskDetail> wrapper = new QueryWrapper<BudgetReimbursementorderFdtaskDetail>();
		wrapper.eq("reimbursementid", orderId);
		wrapper.apply("concat(',',planaccounters,',') like concat('%',{0},'%')", userName);
		//wrapper.eq("receiver", userName);
		return this.list(wrapper);
	}

	/**
	 * 是否完成做账任务
	 * @param orderId
	 * @return
	 */
	public boolean isFinishAllTask(Long orderId) {
		QueryWrapper<BudgetReimbursementorderFdtaskDetail> wrapper = new QueryWrapper<BudgetReimbursementorderFdtaskDetail>();
		wrapper.eq("reimbursementid", orderId);
		wrapper.eq("accountstatus", 0);
		List<BudgetReimbursementorderFdtaskDetail> list = this.list(wrapper);
		return (list == null || list.size() == 0) ? true : false;
	}
	
}
