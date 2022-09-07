package com.jtyjy.finance.manager.controller.extract;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.easyexcel.ExtractPersonalityPayDetailExcelData;
import com.jtyjy.finance.manager.easyexcel.ExtractPersonlityDetailExcelData;
import com.jtyjy.finance.manager.exception.MyException;
import com.jtyjy.finance.manager.service.BudgetExtractPersonalityPayService;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailQueryVO;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailVO;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/2
 */
@Api(tags = {"提成员工个体户发放界面接口"})
@RestController
@RequestMapping("/api/extractInfo")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractPersonalityPayController {

	@Autowired
	private BudgetExtractPersonalityPayService personalityPayService;

	@Autowired
	private BudgetExtractsumService extractsumService;

	@ApiOperation(value = "新增员工个体户发放明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addExtractPersonalityPayDetail")
	public ResponseEntity addExtractPersonalityPayDetail(@RequestBody @Validated ExtractPersonalityPayDetailVO entity) {
		try {
			String query = entity.getQuery();
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			entity.setExtractBatch(extractBatch);
			personalityPayService.addExtractPersonalityPayDetail(entity);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "修改员工个体户发放明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateExtractPersonalityPayDetail")
	public ResponseEntity updateExtractPersonalityPayDetail(@RequestBody @Validated ExtractPersonalityPayDetailVO entity) {
		try {
			if (entity.getId() == null) throw new RuntimeException("缺少id");
			String query = entity.getQuery();
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			entity.setExtractBatch(extractBatch);
			personalityPayService.updateExtractPersonalityPayDetail(entity);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "删除员工个体户发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "ids", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/deleteExtractPersonalityPayDetail")
	public ResponseEntity deleteExtractPersonalityPayDetail(@RequestParam(name = "ids") String ids) {
		try {
			personalityPayService.deleteExtractPersonalityPayDetail(ids);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "导出员工个体户批次发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportPersonalityPayDetail")
	public void exportPersonalityPayDetail(ExtractPersonalityPayDetailQueryVO params, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			if (StringUtils.isBlank(params.getQuery())) throw new RuntimeException("参数异常。");
			String query = params.getQuery();
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			List<ExtractPersonalityPayDetailExcelData> details = personalityPayService.getExtractPersonalityPayDetailVO(params, extractBatch);
			is = this.getClass().getClassLoader().getResourceAsStream("template/exportPersonlityPayDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractBatch + "员工个体户明细表", response), ExtractPersonalityPayDetailExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName(extractBatch + "员工个体户发放明细表");
			workBook.fill(details, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	@ApiOperation(value = "员工个体户单据发放明细", httpMethod = "GET")
	@GetMapping("/getExtractPersonalityPayDetails")
	public ResponseEntity<PageResult<ExtractPersonalityPayDetailVO>> getExtractPersonalityPayDetails(ExtractPersonalityPayDetailQueryVO params,
	                                                                                                 @RequestParam(defaultValue = "1") Integer page,
	                                                                                                 @RequestParam(defaultValue = "20") Integer rows) {
		if (params.getSumId() == null) return ResponseEntity.error("参数异常。");
		PageResult<ExtractPersonalityPayDetailVO> pageList = extractsumService.getExtractPersonalityPayDetailVO(params, page, rows, null);
		return ResponseEntity.ok(pageList);

	}

	@ApiOperation(value = "员工个体户批次发放明细", httpMethod = "GET")
	@GetMapping("/getExtractPersonalityBatchPayDetails")
	public ResponseEntity<PageResult<ExtractPersonalityPayDetailVO>> getExtractPersonalityBatchPayDetails(ExtractPersonalityPayDetailQueryVO params,
	                                                                                                      @RequestParam(defaultValue = "1") Integer page,
	                                                                                                      @RequestParam(defaultValue = "20") Integer rows) {

		if (StringUtils.isBlank(params.getQuery())) return ResponseEntity.error("参数异常。");
		String query = params.getQuery();
		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		PageResult<ExtractPersonalityPayDetailVO> pageList = extractsumService.getExtractPersonalityPayDetailVO(params, page, rows, extractBatch);
		return ResponseEntity.ok(pageList);

	}

	@ApiOperation(value = "导出员工个体户明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportPersonalityDetail")
	public void exportPersonlityDetail(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			List<ExtractPersonlityDetailExcelData> details = this.extractsumService.getExtractPersonlityDetail(extractBatch);
			is = this.getClass().getClassLoader().getResourceAsStream("template/exportPersonlitydetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractBatch + "员工个体户明细表", response), ExtractPersonlityDetailExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName(extractBatch + "员工个体户明细表");
			workBook.fill(details, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	@ApiOperation(value = "导入员工个体户发放明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@PostMapping("/importPersonalityPayDetail")
	public ResponseEntity importPersonalityPayDetail(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "query", required = true) String query, HttpServletResponse response, HttpServletRequest request) throws Exception {

		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		/**
		 * 校验是否能够导入员工个体户发放明细
		 */
		this.extractsumService.validateIsCanOperatePersonalityPayDetail(extractBatch);
		InputStream inputStream = file.getInputStream();
		try {
			this.extractsumService.importPersonalityPayDetail(inputStream, extractBatch);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, e.getMessage());
		} finally {
			if (inputStream != null) inputStream.close();
		}
		return ResponseEntity.ok("导入成功");
	}

	@ApiOperation(value = "撤回员工个体户导入", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/resetPersonalityPayDetailImport")
	public ResponseEntity resetPersonalityPayDetailImport(@RequestParam(name = "query", required = true) String query) throws Exception {

		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		/**
		 * 校验是否能够导入员工个体户发放明细
		 */
		try {
			this.extractsumService.validateIsCanOperatePersonalityPayDetail(extractBatch);
			this.personalityPayService.resetPersonalityPayDetailImport(extractBatch);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();
	}

	@ApiOperation(value = "员工个体户确认完成(注意响应的状态码，文档有解释)", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@ApiResponses({
			@ApiResponse(code = 0, message = "成功"),
			@ApiResponse(code = -1, message = "接口报错"),
			@ApiResponse(code = -9999, message = "二次确认信息")
	})
	@GetMapping("/ensureComplete")
	public ResponseEntity ensureComplete(@RequestParam(name = "query", required = true) String query) throws Exception {

		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		/**
		 * 校验是否能够导入员工个体户发放明细
		 */
		try {
			this.personalityPayService.ensureComplete(extractBatch, false);
		} catch (MyException e) {
			e.printStackTrace();
			return ResponseEntity.apply(StatusCodeEnmus.OTHER, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();
	}

	@ApiOperation(value = "员工个体户确认完成(第二次确认)", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/secondEnsureComplete")
	public ResponseEntity secondEnsureComplete(@RequestParam(name = "query", required = true) String query) throws Exception {

		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		try {
			this.personalityPayService.ensureComplete(extractBatch, true);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();
	}


	@ApiOperation(value = "取消确认", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/cancelEnsureComplete")
	public ResponseEntity cancelEnsureComplete(@RequestParam(name = "query", required = true) String query) throws Exception {

		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];
		try {
			this.personalityPayService.cancelEnsureComplete(extractBatch);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();
	}

	@ApiIgnore
	@PostMapping("/importInitPersonalityPayDetail")
	public ResponseEntity importInitPersonalityPayDetail(@RequestParam(name = "file") MultipartFile file, HttpServletResponse response, HttpServletRequest request) throws Exception {
		InputStream inputStream = file.getInputStream();
		try{
			this.personalityPayService.importInitPersonalityPayDetail(inputStream);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(inputStream!=null)inputStream.close();
		}
		return ResponseEntity.ok("导入成功");
	}

	@ApiIgnore
	@GetMapping("/downInitPersonalityPayDetailTemplate")
	public void downInitPersonalityPayDetailTemplate(HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("template/importPersonlityInitPayDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成导入员工个体户发放明细初始化模板", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			List<Map<String, Object>> list = new ArrayList<>();
			workBook.fill(list, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "消息通知", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/personalityNotice")
	public ResponseEntity<String> personalityNotice(@RequestParam(name = "query", required = true) String query) {

		if (StringUtils.isBlank(query)) return ResponseEntity.error("参数异常。");
		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];

		try {
			this.personalityPayService.personalityNotice(extractBatch);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();

	}


	@ApiOperation(value = "确认发放", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true),
			@ApiImplicitParam(value = "延期的员工个体户发放ids(多个用逗号分隔)", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/ensureSend")
	public ResponseEntity<String> ensureSend(@RequestParam(name = "query", required = true) String query,@RequestParam(name = "ids", required = true) String ids) {

		if (StringUtils.isBlank(query)) return ResponseEntity.error("参数异常。");
		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];

		try {
			this.personalityPayService.ensureSend(extractBatch,ids);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
		return ResponseEntity.ok();

	}
}