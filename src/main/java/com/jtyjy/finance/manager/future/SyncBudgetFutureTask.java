package com.jtyjy.finance.manager.future;

import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.controller.budgetorganization.BudgetMonthSubjectController;
import com.jtyjy.finance.manager.controller.budgetorganization.BudgetYearSubjectController;
import com.jtyjy.finance.manager.service.BudgetMonthSubjectService;
import com.jtyjy.finance.manager.service.BudgetYearSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SyncBudgetFutureTask implements Callable<Boolean> {
    private final static Logger LOGGER = LoggerFactory.getLogger(SyncBudgetFutureTask.class);

    private Long unitId;
    private Long monthId;
    private SpringTools springTools;
    private RedisClient redisClient;

    public SyncBudgetFutureTask(Long unitId, Long monthId, SpringTools springTools, RedisClient redisClient) {
        super();
        this.unitId = unitId;
        this.monthId = monthId;
        this.springTools = springTools;
        this.redisClient = redisClient;
    }

    @Override
    public Boolean call() throws Exception {
        if (monthId == null && unitId != null) {
            BudgetYearSubjectService budgetYearSubjectService = springTools.getBean(BudgetYearSubjectService.class);
            try {
                budgetYearSubjectService.syncYearAgentData(unitId);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
                return false;
            } finally {
                redisClient.delete(BudgetYearSubjectController.SYNC_BUDGET_REDIS_PREFIX + unitId);
            }
        } else if (monthId != null && unitId != null) {
            BudgetMonthSubjectService budgetMonthSubjectService = springTools.getBean(BudgetMonthSubjectService.class);
            try {
                budgetMonthSubjectService.syncMonthAgentData(unitId, monthId);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
                return false;
            } finally {
                redisClient.delete(BudgetMonthSubjectController.SYNC_MONTH_BUDGET_REDIS_PREFIX + unitId + "_" + monthId);
            }
        }
        return true;
    }
}
