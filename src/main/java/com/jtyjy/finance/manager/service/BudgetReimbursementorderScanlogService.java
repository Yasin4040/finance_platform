package com.jtyjy.finance.manager.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderScanlog;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.constants.ReimbursementStepHelper;
import com.jtyjy.finance.manager.event.bx.BxCodeRequest;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderScanlogMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderScanlogService extends DefaultBaseService<BudgetReimbursementorderScanlogMapper, BudgetReimbursementorderScanlog> {

	private final TabChangeLogMapper loggerMapper;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_scanlog"));
	}

	/**
	 * 根据扫描结果保存扫描日志
	 * @param codeRequest
	 * @param order
	 * @param openPage
	 * @param scanResult 
	 */
	public void saveByScanOrder(BxCodeRequest codeRequest, BudgetReimbursementorder order, String step, String opt) {
		//保存扫描日志
		boolean openPage = ReimbursementStepHelper.RECEIVED.equals(opt) ? false : true;
		String scanResult = ReimbursementStepHelper.getStepMessage(order.getReimcode(), step, ReimbursementStepHelper.RECEIVED, "成功");
		BudgetReimbursementorderScanlog bean = new BudgetReimbursementorderScanlog();
		bean.setReimbursementid(order.getId());
		bean.setReimcode(order.getReimcode());
		bean.setScantime(new Date());
		bean.setScaner(UserThreadLocal.get().getUserName());
		bean.setScanername(UserThreadLocal.get().getDisplayName());
		bean.setScanflag(true);
		bean.setScantype(order.getCurscanstatus());
		bean.setOperateflag(openPage);
		bean.setScaninfo(codeRequest.getInfo());
		if(step.equals(ReimbursementStepHelper.ACCOUNTING_DO_BILL) && !"1".equals(opt)) {
        	//会计做账-结束流转
        	scanResult = "报销单【" + order.getReimcode() + "】会计做账--结束流转";
        	bean.setScantype(Integer.valueOf(ReimbursementStepHelper.END_PROCESS));
        }
		bean.setScanresult(scanResult);
		bean.setReceivestatus(Integer.parseInt(step));
		this.save(bean);
	}
}
