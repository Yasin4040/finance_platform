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
	 * ????????????????????????????????????????????????
	 * 1.??????????????????????????????????????????????????????
	 * 2.??????????????????
	 * 3.???????????????????????????????????????
	 * 4.???????????????????????????????????????????????????
	 * 5.????????????????????????????????????????????????????????????????????????
	 * @param order
	 * @param details
	 * @param bunitid ???????????????
	 * @throws Exception 
	 */
	public void createSplitOrderTaskByOrder(BudgetReimbursementorder order,List<BudgetReimbursementorderDetail> details, long bunitid) throws Exception {
		if(details == null || details.size() == 0) {
			throw new Exception("?????????????????????????????????");
		}
		//1.??????????????????????????????????????????????????????
		this.fdDetailService.removeByOrderId(order.getId());
		this.removeByOrderId(order.getId());
		//???????????????????????????
		Set<Long> set = details.stream().map(BudgetReimbursementorderDetail :: getBunitid).collect(Collectors.toSet());
		set.add(bunitid);
		List<Long> bunits = new ArrayList<Long>(set);
		Map<Long, KVBean> accountsInfo = this.billingUnitService.getAccountsInfo(bunits);
		if(accountsInfo == null || accountsInfo.size() == 0) {
			throw new Exception("??????????????????????????????????????????");
		}
		//??????????????????
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
		//??????????????????
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
	 * ???????????????????????????
	 * @param orderId
	 */
	private void removeByOrderId(Long orderId) {
		QueryWrapper<BudgetReimbursementorderFdtask> wrapper = new QueryWrapper<BudgetReimbursementorderFdtask>();
		wrapper.eq("reimbursementid", orderId);
		this.remove(wrapper);
	}

	/**
	 * ????????????????????????????????????????????????
	 * @param orderId
	 * @param userName
	 * @return
	 */
	public List<BudgetReimbursementorderFdtaskDetail> getTaskDetailByOrderIdAndUserEmpno(Long orderId,String userName) {
		return this.fdDetailService.getTaskDetailByOrderIdAndUserEmpno(orderId,userName);
	}

	/**
	 * ??????????????????
	 * @param taskDetails
	 */
	public void updateTaskDetails(List<BudgetReimbursementorderFdtaskDetail> taskDetails) {
		this.fdDetailService.updateBatchById(taskDetails);
	}

	/**
	 * ????????????????????????
	 * @param orderId
	 * @return
	 */
	public boolean isFinishAllTask(Long orderId) {
		return this.fdDetailService.isFinishAllTask(orderId);
	}
	
}
