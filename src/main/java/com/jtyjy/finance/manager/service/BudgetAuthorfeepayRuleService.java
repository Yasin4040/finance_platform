package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRule;
import com.jtyjy.finance.manager.mapper.BudgetAuthorfeepayRuleMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAuthorfeepayRuleService extends DefaultBaseService<BudgetAuthorfeepayRuleMapper, BudgetAuthorfeepayRule> {

	private final TabChangeLogMapper loggerMapper;

	@Autowired
	private BudgetAuthorfeepayRuleMapper ruleMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_authorfeepay_rule"));
	}

    public void saveOne(BudgetAuthorfeepayRule bean) {
		bean.setCreatetime(new Date());
		save(bean);
    }

	public void updateOneById(BudgetAuthorfeepayRule bean) {
		bean.setUpdatetime(new Date());
		saveOrUpdate(bean);
	}
}
