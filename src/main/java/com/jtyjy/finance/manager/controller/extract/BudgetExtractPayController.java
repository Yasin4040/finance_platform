package com.jtyjy.finance.manager.controller.extract;

import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.service.BudgetExtractPayService;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Api(tags = {"提成支付"})
@RestController
@RequestMapping("/api/extractPay")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractPayController {
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractPayController.class);

	@Autowired
	private BudgetExtractPayService extractPayService;

	@ApiOperation(value = "提成付款单列表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractPayMoneyList")
	public ResponseEntity<PageResult<BudgetExtractPayResponseVO>> getExtractPayMoneyList(BudgetExtractPayQueryVO params,
																							  @RequestParam(defaultValue = "1") Integer page,
			                                                                                  @RequestParam(defaultValue = "20") Integer rows) {
		try {
			PageResult<BudgetExtractPayResponseVO> pageList = extractPayService.getExtractPayMoneyList(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

}
