package com.jtyjy.finance.manager.controller.extract;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.dto.ExtractPreparePayDTO;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhBatchPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhDfPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetPayTotalExcelData;
import com.jtyjy.finance.manager.enmus.ExtractPayTemplateEnum;
import com.jtyjy.finance.manager.service.BudgetExtractPayService;
import com.jtyjy.finance.manager.service.BudgetReimbursementorderService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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
	private BudgetReimbursementorderService budgetReimbursementorderService;

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

	@ApiOperation(value = "提成准备付款(准备付款成功后调)", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/exportPay")
	public void exportPay(@RequestBody @Validated ExtractPreparePayDTO extractPreparePayDTO, HttpServletResponse response) throws Exception {


		if(extractPreparePayDTO.getPayTemplateType() == ExtractPayTemplateEnum.OLD.type){
			budgetReimbursementorderService.exportPreparePay(extractPreparePayDTO.getPayMoneyIds().stream().map(e->e.toString()).collect(Collectors.joining(",")), null,response);
		}else {
			ClassPathResource resource = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStream is = null;
			try{
				List<Map<String,Object>> extractPayBatchDetailList = null;
				if(extractPreparePayDTO.getPayTemplateType() == ExtractPayTemplateEnum.ZS_BATCH.type){
					resource = new ClassPathResource("template/zhbatchpay.xlsx");
					extractPayBatchDetailList = this.extractPayService.getExtractPayBatchDetailList(extractPreparePayDTO.getPayMoneyIds(),ExtractPayTemplateEnum.ZS_BATCH.type);

					if(!CollectionUtils.isEmpty(extractPayBatchDetailList)){
						Map<String, Object> totalMap = extractPayBatchDetailList.get(0);
						XSSFWorkbook workbook = new XSSFWorkbook(resource.getInputStream());
						Map<String, Object> detailMap = extractPayBatchDetailList.get(1);
						List<String> nameList = detailMap.keySet().stream().collect(Collectors.toList());
						detailMap.forEach((k,v)->{
							workbook.cloneSheet(1, k);
						});
						workbook.write(bos);
						is = new ByteArrayInputStream(bos.toByteArray());
						ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成付款明细表", response), BudgetExtractZhBatchPayExcelData.class).withTemplate(is).build();

						WriteSheet sheet = EasyExcel.writerSheet(0).build();
						workBook.fill((List<BudgetPayTotalExcelData>)totalMap.get("付款明细汇总表"),sheet);
						detailMap.forEach((outUnitName,obj)->{
							WriteSheet sheet1 = EasyExcel.writerSheet(nameList.indexOf(outUnitName)+1).build();
							workBook.fill((List<BudgetExtractZhBatchPayExcelData>)obj, sheet1);
						});
						workBook.finish();
					}
				}else if(extractPreparePayDTO.getPayTemplateType() == ExtractPayTemplateEnum.ZS_DF.type){
					resource = new ClassPathResource("template/zhdfpay.xlsx");
					extractPayBatchDetailList = this.extractPayService.getExtractPayBatchDetailList(extractPreparePayDTO.getPayMoneyIds(),ExtractPayTemplateEnum.ZS_DF.type);

					if(!CollectionUtils.isEmpty(extractPayBatchDetailList)){
						Map<String, Object> totalMap = extractPayBatchDetailList.get(0);
						XSSFWorkbook workbook = new XSSFWorkbook(resource.getInputStream());
						Map<String, Object> detailMap = extractPayBatchDetailList.get(1);
						List<String> nameList = detailMap.keySet().stream().collect(Collectors.toList());
						detailMap.forEach((k,v)->{
							workbook.cloneSheet(1, k);
						});
						workbook.write(bos);
						is = new ByteArrayInputStream(bos.toByteArray());
						ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成付款明细表", response), BudgetExtractZhDfPayExcelData.class).withTemplate(is).build();

						WriteSheet sheet = EasyExcel.writerSheet(0).build();
						workBook.fill((List<BudgetPayTotalExcelData>)totalMap.get("付款明细汇总表"),sheet);
						detailMap.forEach((outUnitName,obj)->{
							WriteSheet sheet1 = EasyExcel.writerSheet(nameList.indexOf(outUnitName)+1).build();
							workBook.fill((List<BudgetExtractZhDfPayExcelData>)obj, sheet1);
						});
						workBook.finish();
					}
				}
			}catch (Exception e){
				LOGGER.error(e.getMessage(), e);
				throw e;
			}finally {
				if (is != null) is.close();
			}
		}
	}

}
