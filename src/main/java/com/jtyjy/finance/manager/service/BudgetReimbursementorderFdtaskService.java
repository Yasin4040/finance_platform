package com.jtyjy.finance.manager.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.KVBean;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderFdtask;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderFdtaskDetail;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderFdtaskMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderFdtaskService extends DefaultBaseService<BudgetReimbursementorderFdtaskMapper, BudgetReimbursementorderFdtask> {
	
	private final BudgetReimbursementorderFdtaskDetailService fdDetailService;
	
	private final BudgetBillingUnitService billingUnitService;

	private final TabChangeLogMapper loggerMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_fdtask"));
	}

	/**
	 * 根据报销单和报销详情创建分单任务
	 * 1.删除本报销单以前的分单任务和分单详情
	 * 2.创建分单任务
	 * 3.为每个有票单位创建分单详情
	 * 4.设置分单任务和分单详情的主开票单位
	 * 5.设置分单详情的计划做账会计为做账单位下的所有会计
	 * @param order
	 * @param details
	 * @param bunitid 主开票单位
	 * @throws Exception 
	 */
	public void createSplitOrderTaskByOrder(BudgetReimbursementorder order,List<BudgetReimbursementorderDetail> details, long bunitid) throws Exception {
		if(details == null || details.size() == 0) {
			throw new Exception("数据异常！报销明细为空");
		}
		//1.删除本报销单以前的分单任务和分单详情
		this.fdDetailService.removeByOrderId(order.getId());
		this.removeByOrderId(order.getId());
		//查询预算单位的会计
		Set<Long> set = details.stream().map(BudgetReimbursementorderDetail :: getBunitid).collect(Collectors.toSet());
		set.add(bunitid);
		List<Long> bunits = new ArrayList<Long>(set);
		Map<Long, KVBean> accountsInfo = this.billingUnitService.getAccountsInfo(bunits);
		if(accountsInfo == null || accountsInfo.size() == 0) {
			throw new Exception("未获取到开票单位下的会计信息");
		}
		//创建分单任务
		Map<Long, String> unitNameMap = new HashMap<Long, String>();
		details.forEach(ele -> unitNameMap.put(ele.getBunitid(), ele.getBunitname()));
		BudgetReimbursementorderFdtask task = new BudgetReimbursementorderFdtask();
		task.setBunitid(bunitid);
		task.setBunitname(unitNameMap.get(bunitid));
		task.setCreatetime(new Date());
		task.setFder(UserThreadLocal.get().getUserName());
		task.setFdername(UserThreadLocal.get().getDisplayName());
		task.setFdtime(new Date());
		task.setReimbursementid(order.getId());
		task.setReimcode(order.getReimcode());
		this.save(task);
		//创建分单详情
		BudgetReimbursementorderFdtaskDetail taskDetail = null;
		List<BudgetReimbursementorderFdtaskDetail > taskDetails = new ArrayList<BudgetReimbursementorderFdtaskDetail>(details.size());
		for (BudgetReimbursementorderDetail detail : details) {
			BudgetBillingUnit billingUnit = this.billingUnitService.getById(detail.getBunitid());
			if(!"1".equals(billingUnit.getBillingUnitType())) continue;
			taskDetail = BudgetReimbursementorderFdtaskDetail.createFromTask(task);
			taskDetail.setPlanbunitid(detail.getBunitid());
			taskDetail.setPlanbunitname(detail.getBunitname());
			taskDetail.setAccountstatus(false);
			taskDetail.setPlanaccounters(accountsInfo.get(detail.getBunitid()).getK().toString());
			taskDetail.setPlanaccounternames(accountsInfo.get(detail.getBunitid()).getV().toString());
			taskDetail.setTasktime(new Date());
			taskDetail.setTaskid(task.getId());
			taskDetails.add(taskDetail);
		}
		this.fdDetailService.saveBatch(taskDetails);
	}

	/**
	 * 按照报销单主键删除
	 * @param orderId
	 */
	private void removeByOrderId(Long orderId) {
		QueryWrapper<BudgetReimbursementorderFdtask> wrapper = new QueryWrapper<BudgetReimbursementorderFdtask>();
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
		return this.fdDetailService.getTaskDetailByOrderIdAndUserEmpno(orderId,userName);
	}

	/**
	 * 更新分单详情
	 * @param taskDetails
	 */
	public void updateTaskDetails(List<BudgetReimbursementorderFdtaskDetail> taskDetails) {
		this.fdDetailService.updateBatchById(taskDetails);
	}

	/**
	 * 是否完成做账任务
	 * @param orderId
	 * @return
	 */
	public boolean isFinishAllTask(Long orderId) {
		return this.fdDetailService.isFinishAllTask(orderId);
	}
	
}
