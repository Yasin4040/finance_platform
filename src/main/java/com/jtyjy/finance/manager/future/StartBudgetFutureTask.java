package com.jtyjy.finance.manager.future;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.controller.budgetorganization.BudgetSysController;
import com.jtyjy.finance.manager.service.BudgetSysService;

public class StartBudgetFutureTask implements Callable<Boolean>{

	private final static Logger LOGGER = LoggerFactory.getLogger(StartBudgetFutureTask.class);
	
	private String query;
	private SpringTools springTools;
	private RedisClient redisClient;
	
	public StartBudgetFutureTask(String query,SpringTools springTools,RedisClient redisClient) {
		this.query = query;
		this.springTools = springTools;
		this.redisClient = redisClient;
	}
	
	@Override
	public Boolean call() throws Exception {
		if(StringUtils.isNotBlank(query)) {
			BudgetSysService sysService = springTools.getBean(BudgetSysService.class);
			try {
				sysService.startBudget(query);
			}catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(),e);
				return false;
			}finally{
				redisClient.delete(BudgetSysController.BUDGET_REDIS_PREFIX+query);
			}
		}
		return true;
	}

}
