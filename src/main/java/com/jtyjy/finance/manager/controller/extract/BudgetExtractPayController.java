package com.jtyjy.finance.manager.controller.extract;

import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.dto.ExtractPayCompleteDTO;
import com.jtyjy.finance.manager.dto.ExtractPreparePayDTO;
import com.jtyjy.finance.manager.enmus.ExtractPayTemplateEnum;
import com.jtyjy.finance.manager.service.BudgetExtractPayService;
import com.jtyjy.finance.manager.service.CommonService;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

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

	@Autowired
	private CommonService commonService;

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

	@ApiOperation(value = "提成准备付款", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/extractPreparePay")
	public ResponseEntity<String> extractPreparePay(@RequestBody @Validated ExtractPreparePayDTO extractPreparePayDTO) {

		try{
			this.extractPayService.extractPreparePay(extractPreparePayDTO);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok("操作成功！");

	}

	@ApiOperation(value = "提成准备付款(准备付款成功后调)", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/exportPay")
	public void exportPay(@Validated ExtractPreparePayDTO extractPreparePayDTO, HttpServletResponse response) throws Exception {

		if(extractPreparePayDTO.getPayTemplateType() == ExtractPayTemplateEnum.OLD.type){
			commonService.exportPreparePay(extractPreparePayDTO.getPayMoneyIds().stream().map(e->e.toString()).collect(Collectors.joining(",")), null,response);
		}else {
			commonService.exportOtherPreparePay(extractPreparePayDTO.getPayTemplateType(),extractPreparePayDTO.getPayMoneyIds(),response);
		}
	}



	@ApiOperation(value = "提成付款完成", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/paySuccess")
	public ResponseEntity<String> paySuccess(@RequestBody @Validated ExtractPayCompleteDTO extractPayCompleteDTO) {

		try{
			this.extractPayService.paySuccess(extractPayCompleteDTO);
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok("操作成功！");

	}

	@ApiOperation(value = "出纳退回", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/payReject")
	public ResponseEntity<String> payReject(@RequestParam(name = "query", required = true) String query) {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			extractPayService.payReject(extractBatch);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

}
