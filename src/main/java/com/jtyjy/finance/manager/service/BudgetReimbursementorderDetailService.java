package com.jtyjy.finance.manager.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.mapper.BudgetReimbursementorderDetailMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.vo.BxDetailVO;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetReimbursementorderDetailService extends DefaultBaseService<BudgetReimbursementorderDetailMapper, BudgetReimbursementorderDetail> {

	private final TabChangeLogMapper loggerMapper;

	private final BudgetReimbursementorderDetailMapper mapper;
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder_detail"));
	}

	/**
	 * 保存详情
	 * @param orderDetail
	 * @param order
	 * @return
	 * @throws Exception 
	 */
	public int saveByOrder(List<BudgetReimbursementorderDetail> list, BudgetReimbursementorder order) throws Exception {
		if(null == list || list.isEmpty()) {
			throw new Exception("报销详情不能为空！");
		}
		//按照订单主键删除
		QueryWrapper<BudgetReimbursementorderDetail> wrapper = new QueryWrapper<BudgetReimbursementorderDetail>();
		wrapper.eq("reimbursementid", order.getId());
		this.remove(wrapper);
		BudgetReimbursementorderDetail.setBase(list,order);
		this.saveBatch(list);
		return list.size();
	}

	public List<BudgetReimbursementorderDetail> getByOrderId(Long orderId) {
		QueryWrapper<BudgetReimbursementorderDetail> wrapper = new QueryWrapper<BudgetReimbursementorderDetail>();
		wrapper.eq("reimbursementid", orderId);
		//wrapper.eq("reimflag", 1);
		return this.list(wrapper);
	}
	
	/**
	 * 需要做账的明细
	 * @param id
	 * @return
	 */
	public List<BudgetReimbursementorderDetail> getDoBillDetail(Long orderId) {
		String sql = "SELECT _detail.* FROM budget_reimbursementorder_detail _detail INNER JOIN budget_billing_unit _billingunit ON _billingunit.id = _detail.bunitid WHERE  _detail.reimbursementid="+orderId;
		return this.jdbcTemplateService.query(sql, BudgetReimbursementorderDetail.class);
	}
	
	public Page<BxDetailVO> pageLike(Integer page, Integer rows, Map<String,Object> conditionMap) throws Exception {
        Page<BxDetailVO> pageCond = new Page<>(page, rows);
        List<BxDetailVO> retList = this.mapper.getBxDetailPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
	
	public List<BxDetailVO> noPage(Map<String,Object> conditionMap) throws Exception {
        List<BxDetailVO> retList = this.mapper.getBxDetailNoPage(conditionMap, JdbcSqlThreadLocal.get());
        return retList;
    }

	public List<BudgetReimbursementorderDetail> getByBxNum(String bxNum){
		List<BudgetReimbursementorderDetail> details = this.mapper.getByBxNum(bxNum);
		return details;
	}
}
