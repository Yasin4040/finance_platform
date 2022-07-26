package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeepayRuledetail;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.mapper.BudgetAuthorfeepayRuledetailMapper;
import com.jtyjy.finance.manager.mapper.BudgetBillingUnitAccountMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAuthorfeepayRuledetailService extends DefaultBaseService<BudgetAuthorfeepayRuledetailMapper, BudgetAuthorfeepayRuledetail> {

	@Autowired
	private BudgetAuthorfeepayRuledetailMapper ruledetailMapper;



	private final TabChangeLogMapper loggerMapper;



	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_authorfeepay_ruledetail"));
	}

    public void saveOne(BudgetAuthorfeepayRuledetail bean) {
		//需要将开票单位信息保存到现在的数据库中


	    save(bean);
    }
}
