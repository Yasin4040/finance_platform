package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetLendInterestRule;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.mapper.BudgetLendInterestRuleMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetLendInterestRuleService extends DefaultBaseService<BudgetLendInterestRuleMapper, BudgetLendInterestRule> {

    private final TabChangeLogMapper loggerMapper;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_lend_interest_rule_new"));
    }

}
