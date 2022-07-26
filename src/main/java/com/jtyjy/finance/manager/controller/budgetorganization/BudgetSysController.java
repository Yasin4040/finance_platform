package com.jtyjy.finance.manager.controller.budgetorganization;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.future.StartBudgetFutureTask;
import com.jtyjy.finance.manager.service.BudgetSysService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "年度、月度预算启动" })
@RestController
@RequestMapping("/api/sys")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetSysController {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetSysController.class);
	
	@Autowired
	private BudgetSysService  sysService;
	
	@Autowired
	private SpringTools springTools;
	
	public final static String BUDGET_REDIS_PREFIX = "START_BUDGET_";
	
	@Autowired
	private RedisClient redisClient;
	
	@ApiOperation(value = "启动预算",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别管理中返回的yearmonthname", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/startBudget")
	public ResponseEntity startBudget(@RequestParam(name="query",required = true) String query) throws InterruptedException, ExecutionException {
		if(StringUtils.isBlank(query)) return ResponseEntity.error("缺少必传的参数");
		if(redisClient.exist(BUDGET_REDIS_PREFIX + query)) 
			return ResponseEntity.error("预算正在启动中，请不要重复操作！");
		redisClient.set(BUDGET_REDIS_PREFIX + query, "startBudgeting");
		//FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new StartBudgetFutureTask(query, this.springTools,redisClient));
        //ExecutorService executorService = Executors.newFixedThreadPool(1);
        //executorService.submit(futureTask);
		try{
			sysService.startBudget(query);
			return ResponseEntity.ok("启动成功");
		}catch (Exception e){
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}finally {
			redisClient.delete(BudgetSysController.BUDGET_REDIS_PREFIX+query);
		}
	}
}
