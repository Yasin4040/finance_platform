package com.jtyjy.finance.manager.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetExtractOuterperson;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitMapper;
import com.jtyjy.finance.manager.mapper.BudgetExtractOuterpersonMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractOuterpersonService extends DefaultBaseService<BudgetExtractOuterpersonMapper, BudgetExtractOuterperson> {

	private final TabChangeLogMapper loggerMapper;
	
	@Autowired
	private BudgetExtractOuterpersonMapper mapper;
	
	@Autowired
	private BudgetBillingUnitMapper billingUnitMapper;
	
	@Autowired
	private WbBanksMapper bankMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extract_outerperson"));
	}

	public PageResult<BudgetExtractOuterperson> getExtractOutPersonList(String name, Integer page, Integer rows) {
		Map<Long, BudgetBillingUnit> billingUnitMap = billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(e->e.getId(), e->e));
		Map<String, WbBanks> bankMap = bankMapper.selectList(null).stream().collect(Collectors.toMap(e->e.getSubBranchCode(), e->e));
		Page<BudgetExtractOuterperson> pageCond = new Page<BudgetExtractOuterperson>(page,rows);
		pageCond = mapper.selectPage(pageCond, new QueryWrapper<BudgetExtractOuterperson>().like(StringUtils.isNotBlank(name),"name", name));
		List<BudgetExtractOuterperson> records = pageCond.getRecords();
		records.forEach(e->{
			if(e.getBudgetbillingunitid()!=null)e.setBillingUnitName(billingUnitMap.get(e.getBudgetbillingunitid()).getName());
			if(StringUtils.isNotBlank(e.getBranchcode())) e.setOpenBank(bankMap.get(e.getBranchcode()).getSubBranchName());
		});
		return PageResult.apply(pageCond.getTotal(), pageCond.getRecords());
	}

	/**
	 * 按照身份证查询
	 * @param idNums
	 * @return
	 */
	public List<BudgetExtractOuterperson> getByIdNums(Set<String> idNums) {
		QueryWrapper<BudgetExtractOuterperson> wrapper = new QueryWrapper<BudgetExtractOuterperson>();
		wrapper.in("idnumber", idNums);
		return this.list(wrapper);
	}
	
}
