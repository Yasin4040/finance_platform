package com.jtyjy.finance.manager.future;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.controller.extract.BudgetExtractController;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;

public class ExtractCalFutureTask implements Callable<Boolean>{
	private final static Logger LOGGER = LoggerFactory.getLogger(ExtractCalFutureTask.class);

	private SpringTools springTools;
	private RedisClient redisClient;
	private String extractBatch;
	private List<HrSalaryYearTaxUser> specialPersonNameList;
	
	
	public ExtractCalFutureTask(SpringTools springTools, RedisClient redisClient, String extractBatch,List<HrSalaryYearTaxUser> specialPersonNameList) {
		super();
		this.springTools = springTools;
		this.redisClient = redisClient;
		this.extractBatch = extractBatch;
		this.specialPersonNameList = specialPersonNameList;
	}

	@Override
	public Boolean call() throws Exception {
		if(StringUtils.isNotBlank(extractBatch) && specialPersonNameList !=null) {
			BudgetExtractsumService extractsumService = springTools.getBean(BudgetExtractsumService.class);
			try {
				extractsumService.calculate(extractBatch,specialPersonNameList,null);
			}catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(),e);
				return false;
			}finally{
				redisClient.delete(BudgetExtractController.EXTRACT_CALC_PREFIX+extractBatch);
			}
		}
		return null;
	}

}
