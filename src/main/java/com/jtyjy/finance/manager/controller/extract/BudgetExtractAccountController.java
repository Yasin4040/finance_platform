package com.jtyjy.finance.manager.controller.extract;

import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.dto.ExtractAccountDTO;
import com.jtyjy.finance.manager.service.BudgetExtractAccountService;
import com.jtyjy.finance.manager.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Api(tags = {"提成账务做账"})
@RestController
@RequestMapping("/api/extractAccount")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractAccountController {

	@Autowired
	private BudgetExtractAccountService accountService;

	@ApiOperation(value = "获取做账任务列表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractAccountTaskList")
	public ResponseEntity<PageResult<ExtractAccountTaskResponseVO>> getExtractAccountTaskList(ExtractAccountTaskQueryVO params,
	                                                                                          @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) {
		try {
			if(params.getIsHistory()==null) return ResponseEntity.error("缺少参数。");
			PageResult<ExtractAccountTaskResponseVO> pageList = accountService.getExtractAccountTaskList(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "查看明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "任务id", name = "taskId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "付款单位", name = "unitName", dataType = "String", required = false),
			@ApiImplicitParam(value = "个体户名称", name = "personalityName", dataType = "String", required = false),
			@ApiImplicitParam(value = "发放状态(0:否 1：是)", name = "payStatus", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractAccountTaskDetail")
	public ResponseEntity<PageResult<ExtractAccountTaskDetailVO>> getExtractAccountTaskDetail(
																							@RequestParam(value = "taskId")Long taskId,
																							@RequestParam(value = "unitName",required = false)String unitName,
																							@RequestParam(value = "personalityName",required = false)String personalityName,
																							@RequestParam(value = "payStatus",required = false)Integer payStatus,
	                                                                                          @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) {
		try {
			PageResult<ExtractAccountTaskDetailVO> pageList = accountService.getExtractAccountTaskDetail(taskId,unitName,personalityName,payStatus, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "延期支付申请单明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "延期支付申请单号", name = "delayPayApplyOrderNo", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractDelayPayApplyDetail")
	public ResponseEntity<ExtractDelayPayApplyVO> getExtractDelayPayApplyDetail(@RequestParam(value = "delayPayApplyOrderNo")String delayPayApplyOrderNo) {
		try {
			ExtractDelayPayApplyVO result = accountService.getExtractDelayPayApplyDetail(delayPayApplyOrderNo);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "获取做账单位列表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "任务单位id", name = "taskId", dataType = "Long", required = true)
	})
	@GetMapping("/getExtractTaskBillingUnitList")
	public ResponseEntity<List<ExtractBillingUnitVO>> getExtractTaskBillingUnitList(@RequestParam(value = "taskId")Long taskId) {
		try {
			List<ExtractBillingUnitVO> result = accountService.getExtractTaskBillingUnitList(taskId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "做账单位完成", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/account")
	public ResponseEntity<String> account(@RequestBody @Validated ExtractAccountDTO accountDTO) {
		try {
			accountService.account(accountDTO);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}
}
