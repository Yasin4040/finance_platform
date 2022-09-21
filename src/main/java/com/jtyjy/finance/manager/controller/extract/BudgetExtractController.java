package com.jtyjy.finance.manager.controller.extract;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.*;
import com.jtyjy.finance.manager.easyexcel.strategy.ExcelFillCellMergeStrategy;
import com.jtyjy.finance.manager.enmus.ExtractExcessTypeEnum;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractTypeEnum;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.lock.JedisLock;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxMarkDownMessage;
import com.jtyjy.weixin.message.QywxTextMsg;
import com.jtyjy.weixin.message.component.WxKVBean;
import com.klcwqy.easy.lock.LockThreadLocal;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = {"提成信息及提成扣款报表"})
@RestController
@RequestMapping("/api/extractInfo")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractController extends BaseController<BudgetExtractsum> {
	public final static String FEEPAY = "FEEPAY";
	public final static String TCIMPORT = "TCIMPORT";
	public final static String TCEXCESS = "TCEXCESS";

	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractController.class);

	@Autowired
	private BudgetExtractsumService extractsumService;

	@Autowired
	private MultipartResolver multipartResolver;

	@Autowired
	private BudgetExtractImportdetailService extractImportdetailService;

	@Autowired
	private BudgetExtractOuterpersonService extractOuterpersonService;

	@Autowired
	private WbUserService userService;

	@Autowired
	private HrService hrService;

	@Autowired
	private BudgetExtractpaydetailService payDetailService;

	@Autowired
	private BudgetExtractpaymentService paymentService;

	@Autowired
	private BudgetRepaymoneyDetailService repaymoneyDetailService;

	@Autowired
	private WbBanksService bankService;

	@Autowired
	private BudgetYearPeriodService yearPeriodService;

	@Autowired
	private CuratorFramework curatorFramework;

	@Autowired
	private BudgetPaymoneyService paymoneyService;

	private final static String IMPORT_TYPE = "tc";

	private final static String IMPORT_EXCESS_TYPE = "tc_excess";

	private final static String IMPORT_FEE_PAY = "tc_fee_pay";

	@Autowired
	private SpringTools springTools;

	public final static String EXTRACT_CALC_PREFIX = "EXTRACT_CALC_PREFIX_";
	@Autowired
	private RedisClient redisClient;

	@Autowired
	private RedisClient redis;

	@Autowired
	private MessageSender sender;

	@Value("${file.shareDir}")
	private String fileShareDir;

	@Value("${redis.file.key.expiretime}")
	private Integer expiretime;

	@ApiOperation(value = "获取提成扣款报表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "姓名/工号", name = "empNo", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractDeductionReport")
	public ResponseEntity<PageResult<ExtractDeductionDetailVO>> getExtractDeductionReport(@RequestParam(name = "empNo", required = false) String empNo,
	                                                                                      @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) {
		try {
			PageResult<ExtractDeductionDetailVO> pageList = extractsumService.getExtractDeductionReport(empNo, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "导出提成扣款报表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "姓名/工号", name = "empNo", dataType = "String", required = false)
	})
	@GetMapping("/exportExtractDeductionReport")
	public void exportExtractDeductionReport(@RequestParam(name = "empNo", required = false) String empNo, HttpServletResponse response) throws Exception {
		try {
			PageResult<ExtractDeductionDetailVO> pageList = extractsumService.getExtractDeductionReport(empNo, 1, Integer.MAX_VALUE);
			List<ExtractDeductionDetailVO> list = pageList.getList();
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("utf-8");
			String fileName = URLEncoder.encode("提成扣款报表", "UTF-8");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
			EasyExcel.write(response.getOutputStream(), ExtractDeductionReportExcelData.class).sheet("提成扣款报表").doWrite(list);
			//return ResponseEntity.ok("导出成功");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
			//return ResponseEntity.error(e.getMessage());	
		}
	}

	@ApiOperation(value = "获取提成批次导航树", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/getExtractPeriodNavigateTree")
	public ResponseEntity<List<ExtractPeriodNavigateTreeVO>> getExtractPeriodNavigateTree() {
		try {
			List<ExtractPeriodNavigateTreeVO> resultList = this.extractsumService.getExtractPeriodNavigateTree();
			return ResponseEntity.ok(resultList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "获取提成主数据列表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = false),
			@ApiImplicitParam(value = "单据状态", name = "status", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "提成单号", name = "code", dataType = "String", required = false),
			@ApiImplicitParam(value = "预算单位", name = "unitname", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractInfoList")
	public ResponseEntity<PageResult<ExtractInfoVO>> getExtractInfoList(@RequestParam(name = "query", required = true) String query,
	                                                                    @RequestParam(defaultValue = "1") Integer page,
	                                                                    @RequestParam(defaultValue = "20") Integer rows,
	                                                                    @RequestParam(name = "status", required = false) Integer status,
	                                                                    @RequestParam(name = "code", required = false) String code,
	                                                                    @RequestParam(name = "unitname", required = false) String unitname) {
		try {
			if (StringUtils.isBlank(query)) throw new RuntimeException("请先选择导航栏的一个参数。");
			Map<String, Object> params = new HashMap<>();
			params.put("query", query);
			params.put("status", status);
			params.put("code", code);
			params.put("unitname", unitname);
			PageResult<ExtractInfoVO> pageList = extractsumService.getExtractInfoList(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "下载提成导入模板", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downExtractImportTemplate")
	public void downExtractImportTemplate(HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplate.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成导入模板", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			List<Map<String, Object>> list = new ArrayList<>();
			workBook.fill(list, sheet);
			workBook.finish();

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "导入提成明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/importExtractDetail")
	public ResponseEntity importExtractDetail(@RequestParam(name = "file") MultipartFile file,@RequestParam(name = "batchNo",required = false) String batchNo, HttpServletResponse response, HttpServletRequest request) throws IOException {
		InputStream is = null;
		int headRows = 2; //表示表头有3行
		int colNum = 13; //列数
		EasyExcelImportListener extractListener = new EasyExcelImportListener(extractsumService, TCIMPORT, headRows ,colNum,batchNo);
		try {
			is = file.getInputStream();
			EasyExcel.read(is, extractListener).sheet(0).doReadSync();
		} catch (IOException e1) {
			e1.printStackTrace();
			LOGGER.error(e1.getMessage(), e1);
			return ResponseEntity.error(e1.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
				return ResponseEntity.error(e.getMessage());
			}
		}
		//表头错误明细（如果表头报错明细数据将不会校验）				
		List<String> headErrorMsg = extractListener.getHeadErrorMsg();
		//明细数据的错误明细
		Map<Integer, Map<Integer, String>> errorMap = extractListener.getErrorMap();
		//导入的所有的数据
		Map<Integer, Map<Integer, String>> allDataMap = extractListener.getAllDataMap();
		if (!headErrorMsg.isEmpty() || !errorMap.isEmpty()) {

			List<ExtractInfoExportExcelData> details = new ArrayList<>();
			//最终的表头数据
			Map<String, String> heads = new HashMap<>();
			//填充错误明细数据
			populateData(details, heads, allDataMap, headErrorMsg, errorMap);

			InputStream iss = null;
			try {
				iss = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplate.xlsx");
				String key = IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName();
				String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
				ExcelWriter workBook = EasyExcel.write(new File(errorFileName), ExtractInfoExportExcelData.class).withTemplate(iss).build();
				WriteSheet sheet = EasyExcel.writerSheet(0).build();
				sheet.setSheetName("提成导入错误明细");
				workBook.fill(heads, sheet);
				workBook.fill(details, sheet);
				workBook.finish();
				redis.set(key, errorFileName, expiretime);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (iss != null) iss.close();
			}
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");
		}
		return ResponseEntity.ok("导入成功");
	}

	@ApiOperation(value = "下载导入提成错误明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downImportExtractErrorDetail")
	public void downImportExtractErrorDetail(HttpServletResponse response, HttpServletRequest request) throws Exception {

		InputStream is = null;
		try {
			if (redis.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName()) == null) {
				throw new RuntimeException("没有提成错误明细可供下载。");
			}
			String errorFileName = redis.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
			is = new FileInputStream(errorFileName);
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成导入错误明细", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			workBook.finish();
			File file = new File(errorFileName);
			if (file.exists()) file.delete();
			redis.delete(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	/**
	 * 填充导入提成错误的数据
	 *
	 * @param details
	 * @param heads
	 * @param allDataMap
	 * @param headErrorMsg
	 * @param errorMap
	 */
	private void populateData(List<ExtractInfoExportExcelData> details,
	                          Map<String, String> heads, Map<Integer, Map<Integer, String>> allDataMap, List<String> headErrorMsg, Map<Integer, Map<Integer, String>> errorMap) {
		Map<Integer, String> headMap = allDataMap.get(1);
		String year = headMap.get(1); //届别
		String extractMonth = headMap.get(3); //提成期间
		String unitname = headMap.get(5); //预算单位
		if (!headErrorMsg.isEmpty()) {
			for (int i = 3; i <= allDataMap.size(); i++) {
				ExtractInfoExportExcelData ed = new ExtractInfoExportExcelData();
				Map<Integer, String> data = allDataMap.get(i);
				String isCompanyEmp = data.get(0); //是否公司员工
				ed.setIsCompanyEmp(isCompanyEmp);
				String empNo = data.get(1); //工号
				ed.setEmpNo(empNo);
				String empName = data.get(2); //姓名
				ed.setEmpName(empName);
				String sftc = data.get(3); //实发提成
				ed.setCopeextract(sftc);
				String zhs = data.get(4); //综合税
				ed.setConsotax(zhs);
				String tcPeriod = data.get(5); //提成届别
				ed.setExtractPeriod(tcPeriod);
				String isDebt = data.get(6); //是否坏账
				ed.setIsBadDebt(isDebt);
				String extractType = data.get(7);//提成类型
				ed.setExtractType(extractType);
				String shouldSendExtract = data.get(8);//应发提成
				ed.setShouldSendExtract(shouldSendExtract);
				String tax = data.get(9);//个税
				ed.setTax(tax);
				String taxReduction = data.get(10);//个税减免
				ed.setTaxReduction(taxReduction);
				String invoiceExcessTax = data.get(11);//发票超额税金
				ed.setInvoiceExcessTax(invoiceExcessTax);
				String invoiceExcessTaxReduction = data.get(12);//发票超额税金减免
				ed.setInvoiceExcessTaxReduction(invoiceExcessTaxReduction);
				ed.setErrMsg(headErrorMsg.stream().collect(Collectors.joining(",")));
				details.add(ed);
			}
		} else if (!errorMap.isEmpty()) {
			errorMap.forEach((i, data) -> {
				ExtractInfoExportExcelData ed = new ExtractInfoExportExcelData();
				String isCompanyEmp = data.get(0); //是否公司员工
				ed.setIsCompanyEmp(isCompanyEmp);
				String empNo = data.get(1); //工号
				ed.setEmpNo(empNo);
				String empName = data.get(2); //姓名
				ed.setEmpName(empName);
				String sftc = data.get(3); //实发提成
				ed.setCopeextract(sftc);
				String zhs = data.get(4); //综合税
				ed.setConsotax(zhs);
				String tcPeriod = data.get(5); //提成届别
				ed.setExtractPeriod(tcPeriod);
				String isDebt = data.get(6); //是否坏账
				ed.setIsBadDebt(isDebt);
				String extractType = data.get(7);//提成类型
				ed.setExtractType(extractType);
				String shouldSendExtract = data.get(8);//应发提成
				ed.setShouldSendExtract(shouldSendExtract);
				String tax = data.get(9);//个税
				ed.setTax(tax);
				String taxReduction = data.get(10);//个税减免
				ed.setTaxReduction(taxReduction);
				String invoiceExcessTax = data.get(11);//发票超额税金
				ed.setInvoiceExcessTax(invoiceExcessTax);
				String invoiceExcessTaxReduction = data.get(12);//发票超额税金减免
				ed.setInvoiceExcessTaxReduction(invoiceExcessTaxReduction);
				String errmsg = data.get(13);
				ed.setErrMsg(errmsg);
				details.add(ed);
			});
		}
		heads.put("yearPeriod", year);
		heads.put("extractMonth", extractMonth);
		heads.put("unitName", unitname);
	}


	@ApiOperation(value = "提交", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "ids", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/submit")
	//提交提成明细导入。
	public ResponseEntity submit(@RequestParam(name = "ids", required = true) String ids) throws Exception {
		try {
			String lockKey = "/finance-platform/extract/submit/" + ids;
			JedisLock jLock = new JedisLock(lockKey);
			// 加锁处理
			try {
				if (jLock.lock(10)) {
					this.extractsumService.submit(ids);
				}else{
					throw new RuntimeException("正在更新，请稍后！！！");
				}
			}
			catch (Exception e) {
				throw e;
			} finally {
				jLock.unlock();
			}
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "删除提成主数据", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "Long", required = true)
	})
	@GetMapping("/deleteExtractSum")
	public ResponseEntity deleteExtractSum(@RequestParam(name = "id", required = true) Long sumId) throws Exception {
		try {
			this.extractsumService.deleteExtractSum(sumId);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "获取导入明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成主表id", name = "id", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "提成届别", name = "yearid", dataType = "Long", required = false),
			@ApiImplicitParam(value = "公司员工(1是0否)", name = "iscompanyemp", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "坏账(1是0否)", name = "isbaddebt", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
			@ApiImplicitParam(value = "身份证号", name = "idnumber", dataType = "String", required = false)
	})
	@GetMapping("/getExtractImportDetails")
	public ResponseEntity<PageResult<ExtractImportDetailVO>> getExtractImportDetails(@RequestParam(name = "id", required = true) Long sumId,
	                                                                                 @RequestParam(defaultValue = "1") Integer page,
	                                                                                 @RequestParam(defaultValue = "20") Integer rows,
	                                                                                 @RequestParam(name = "yearid", required = false) Long yearid,
	                                                                                 @RequestParam(name = "iscompanyemp", required = false) Integer iscompanyemp,
	                                                                                 @RequestParam(name = "isbaddebt", required = false) Integer isbaddebt,
	                                                                                 @RequestParam(name = "empno", required = false) String empno,
	                                                                                 @RequestParam(name = "idnumber", required = false) String idnumber) {


		try {
			Map<String, Object> params = new HashMap<>();
			params.put("sumId", sumId);
			params.put("yearid", yearid);
			params.put("iscompanyemp", iscompanyemp);
			params.put("isbaddebt", isbaddebt);
			params.put("empno", empno);
			params.put("idnumber", idnumber);
			PageResult<ExtractImportDetailVO> pageList = extractsumService.getExtractImportDetails(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "获取提成明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成主表id", name = "id", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "公司员工(1是0否)", name = "iscompanyemp", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "超额类型", name = "excesstype", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "超额处理(1是0否)", name = "handleflag", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
			@ApiImplicitParam(value = "身份证号", name = "idnumber", dataType = "String", required = false)
	})
	@GetMapping("/getExtractDetails")
	public ResponseEntity<PageResult<BudgetExtractdetail>> getExtractDetails(@RequestParam(name = "id", required = true) Long sumId,
	                                                                         @RequestParam(defaultValue = "1") Integer page,
	                                                                         @RequestParam(defaultValue = "20") Integer rows,
	                                                                         @RequestParam(name = "excesstype", required = false) Integer excesstype,
	                                                                         @RequestParam(name = "iscompanyemp", required = false) Integer iscompanyemp,
	                                                                         @RequestParam(name = "handleflag", required = false) Integer handleflag,
	                                                                         @RequestParam(name = "empno", required = false) String empno,
	                                                                         @RequestParam(name = "idnumber", required = false) String idnumber) {


		try {
			Map<String, Object> params = new HashMap<>();
			params.put("sumId", sumId);
			params.put("excesstype", excesstype);
			params.put("iscompanyemp", iscompanyemp);
			params.put("handleflag", handleflag);
			params.put("empno", empno);
			params.put("idnumber", idnumber);
			PageResult<BudgetExtractdetail> pageList = extractsumService.getExtractDetails(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}


	@ApiOperation(value = "修改提成导入明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateExtractImportDetail")
	public ResponseEntity updateExtractImportDetail(@RequestBody @Valid BudgetExtractImportdetail entity, BindingResult bindingResult) {
		try {
			if (Objects.isNull(entity.getId())) throw new RuntimeException("缺少id必填字段");
			BudgetExtractImportdetail detail = this.extractImportdetailService.getById(entity.getId());
			BudgetExtractsum extractsum = this.extractsumService.getById(detail.getExtractsumid());
			if (extractsum.getStatus() > ExtractStatusEnum.DRAFT.getType())
				throw new RuntimeException("操作失败！只能修改退回和草稿状态的提成明细");
			BeanUtils.copyProperties(entity, detail);
			if (entity.getIscompanyemp()) {
				WbUser wbUser = userService.getById(entity.getEmpid());
				detail.setEmpno(wbUser.getUserName());
				detail.setEmpname(wbUser.getDisplayName());
				detail.setIdnumber(wbUser.getIdNumber());
			} else {
				BudgetExtractOuterperson outerperson = extractOuterpersonService.getById(entity.getEmpid());
				detail.setEmpno(outerperson.getEmpno());
				detail.setEmpname(outerperson.getName());
				detail.setIdnumber(outerperson.getIdnumber());
			}
			detail.setUpdatetime(new Date());
			extractImportdetailService.updateById(detail);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "删除提成导入明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成导入明细id", name = "id", dataType = "Long", required = true)
	})
	@PostMapping("/deleteExtractImportDetail")
	public ResponseEntity deleteExtractImportDetail(@RequestParam(name = "id", required = true) Long id) {
		try {
			BudgetExtractImportdetail detail = this.extractImportdetailService.getById(id);
			BudgetExtractsum extractsum = this.extractsumService.getById(detail.getExtractsumid());
			if (extractsum.getStatus() > ExtractStatusEnum.DRAFT.getType())
				throw new RuntimeException("操作失败！只能删除退回和草稿状态的提成明细");
			this.extractsumService.deleteExtractImportDetail(id);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "获取提成扣款明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "还款单id", name = "repaymoneyid", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "借款类型", name = "lendtype", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "借款单号", name = "lendmoneycode", dataType = "String", required = false)
	})
	@GetMapping("/getExtractWithholdDetails")
	public ResponseEntity<PageResult<ExtractWithholdDetailVO>> getExtractWithholdDetails(@RequestParam(name = "repaymoneyid", required = true) Long repaymoneyid,
	                                                                                     @RequestParam(defaultValue = "1") Integer page,
	                                                                                     @RequestParam(defaultValue = "20") Integer rows,
	                                                                                     @RequestParam(name = "lendtype", required = false) Integer lendtype,
	                                                                                     @RequestParam(name = "lendmoneycode", required = false) String lendmoneycode) {


		try {
			Map<String, Object> params = new HashMap<>();
			params.put("repaymoneyid", repaymoneyid);
			params.put("lendtype", lendtype);
			params.put("lendmoneycode", lendmoneycode);
			PageResult<ExtractWithholdDetailVO> pageResult = extractsumService.getExtractWithholdDetails(params, page, rows);
			return ResponseEntity.ok(pageResult);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}


	@ApiOperation(value = "获取提成发放日志", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件(query)", name = "query", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "公司员工(1是0否)", name = "iscompanyemp", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
			@ApiImplicitParam(value = "工资单位", name = "billingunitname", dataType = "String", required = false)
	})
	@GetMapping("/getExtractGrantLogDetails")
	public ResponseEntity<PageResult<BudgetExtractgrantlog>> getExtractGrantLogDetails(@RequestParam(name = "query", required = true) String query,
	                                                                                   @RequestParam(defaultValue = "1") Integer page,
	                                                                                   @RequestParam(defaultValue = "20") Integer rows,
	                                                                                   @RequestParam(name = "iscompanyemp", required = false) Integer iscompanyemp,
	                                                                                   @RequestParam(name = "empno", required = false) String empno,
	                                                                                   @RequestParam(name = "billingunitname", required = false) String billingunitname) {


		try {
			Map<String, Object> params = new HashMap<>();
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			params.put("extractmonth", query.split("-")[2]);
			if (iscompanyemp != null) params.put("iscompanyemp", iscompanyemp);
			if (StringUtils.isNotBlank(empno)) params.put("empno", empno);
			if (StringUtils.isNotBlank(billingunitname)) params.put("billingunitname", billingunitname);
			PageResult<BudgetExtractgrantlog> pageResult = extractsumService.getExtractGrantLogDetails(params, page, rows);
			return ResponseEntity.ok(pageResult);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "员工批次发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
	})
	@GetMapping("/getExtractBatchPayDetails")
	public ResponseEntity<PageResult<ExtractPayDetailVO>> getExtractBatchPayDetails(@RequestParam(name = "query", required = true) String query,
	                                                                           @RequestParam(defaultValue = "1") Integer page,
	                                                                           @RequestParam(defaultValue = "20") Integer rows,
	                                                                           @RequestParam(name = "empno", required = false) String empno) {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			Map<String, Object> params = new HashMap<>();
			params.put("extractmonth", query.split("-")[2]);
			if (StringUtils.isNotBlank(empno)) params.put("empno", empno);
			PageResult<ExtractPayDetailVO> pageResult = extractsumService.getExtractPayDetails(params, page, rows);
			return ResponseEntity.ok(pageResult);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "员工单号发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成id", name = "sumId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
	})
	@GetMapping("/getExtractPayDetails")
	public ResponseEntity<PageResult<ExtractPayDetailVO>> getExtractPayDetails(@RequestParam(name = "sumId", required = true) Long sumId,
	                                                                           @RequestParam(defaultValue = "1") Integer page,
	                                                                           @RequestParam(defaultValue = "20") Integer rows,
	                                                                           @RequestParam(name = "empno", required = false) String empno) {
		try {
			BudgetExtractsum extractsum = this.extractsumService.getById(sumId);
			Map<String, Object> params = new HashMap<>();
			params.put("extractmonth", extractsum.getExtractmonth());
			if (StringUtils.isNotBlank(empno)) params.put("empno", empno);
			params.put("sumId", sumId);
			PageResult<ExtractPayDetailVO> pageResult = extractsumService.getExtractPayDetails(params, page, rows);
			return ResponseEntity.ok(pageResult);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "获取提成计税明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "工号/姓名", name = "empno", dataType = "String", required = false),
	})
	@GetMapping("/getExtractCalTaxDetails")
	public ResponseEntity<PageResult<BudgetExtractpaydetail>> getExtractCalTaxDetails(@RequestParam(name = "query", required = true) String query,
	                                                                                  @RequestParam(defaultValue = "1") Integer page,
	                                                                                  @RequestParam(defaultValue = "20") Integer rows,
	                                                                                  @RequestParam(name = "empno", required = false) String empno) {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			Map<String, Object> params = new HashMap<>();
			params.put("extractmonth", query.split("-")[2]);
			if (StringUtils.isNotBlank(empno)) params.put("empno", empno);
			PageResult<BudgetExtractpaydetail> pageResult = extractsumService.getExtractCalTaxDetails(params, page, rows);
			return ResponseEntity.ok(pageResult);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "审核通过", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "ids", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/agree")
	public ResponseEntity agree(@RequestParam(name = "ids", required = true) String ids) throws Exception {
		try {
			this.extractsumService.agree(ids);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "退回", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "Long", required = true),
			@ApiImplicitParam(value = "退回意见", name = "remark", dataType = "String", required = true)
	})
	@GetMapping("/reject")
	public ResponseEntity reject(@RequestParam(name = "id", required = true) Long sumId,
	                             @RequestParam(name = "remark", required = true) String remark) throws Exception {
		try {
			long t1 = System.currentTimeMillis();
			this.extractsumService.reject(sumId, remark);
			long t2 = System.currentTimeMillis();
			return ResponseEntity.ok(t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "计算发放", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/calculate")
	public ResponseEntity calculate(@RequestParam(name = "query", required = true) String query, @RequestParam(name = "empno", required = false) String empno) throws Exception {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			//特殊人员名单
			List<HrSalaryYearTaxUser> specialPersonNameList = hrService.list();
			if (redisClient.exist(EXTRACT_CALC_PREFIX + extractBatch))
				return ResponseEntity.error("提成批次【" + extractBatch + "】正在计算中，请勿重复操作！");
			redisClient.set(EXTRACT_CALC_PREFIX + extractBatch, "startBudgeting");
//			FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new ExtractCalFutureTask( this.springTools,redisClient,extractBatch,specialPersonNameList));
//	        ExecutorService executorService = Executors.newFixedThreadPool(1);
//	        executorService.submit(futureTask);
			try {
				this.extractsumService.calculate(extractBatch, specialPersonNameList, null);
			} catch (Exception e) {
				throw e;
			} finally {
				redisClient.delete(BudgetExtractController.EXTRACT_CALC_PREFIX + extractBatch);
			}

//			String lockKey = "/finance-platform/extract/calculate/"+query;
//			ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, null);
//			try {
//				zookeeperShareLock.tryLock();
//				this.extractsumService.calculate(extractBatch,specialPersonNameList);
//			}catch(Exception e) {
//				throw e;
//			}finally {
//	            zookeeperShareLock.unLock();
//	        }
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	/**
	 * 获取工资信息
	 *
	 * @param salaryMonth
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/getSalary")
	@NoLoginAnno
	public Map<String, Object> getSalary(String salaryMonth) {
		Map<String, Object> result = new HashMap<>();
		try {
			Map<String, Object> map = hrService.getSalary(salaryMonth);
			result.put("data", map);
			result.put("code", 0);
			result.put("msg", "成功");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			result.put("data", new HashMap<>());
			result.put("code", 1);
			result.put("msg", e.getMessage());
		}
		return result;
	}


	@ApiOperation(value = "超额导出", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportExtractExcessDetail")
	public ResponseEntity<String> exportExtractExcessDetail(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			this.extractsumService.validateIsCanSetExcess(extractBatch);
			//当前批次的所有提成
			List<BudgetExtractdetail> curExtractDetails = this.extractsumService.getExtractExcessDetailByExtractmonth(extractBatch);
			if (curExtractDetails.isEmpty()) throw new RuntimeException("提成批次【" + extractBatch + "】无超额提成可处理!");
			List<String> empnoList = curExtractDetails.stream().map(e -> e.getEmpno()).distinct().collect(Collectors.toList());
			//获取人员与工资发位的映射关系
			Map<String, String> idnumber2billingUnitNameMap = this.extractsumService.getIdnumber2BillingUnitNameMap(extractBatch, empnoList);

			List<ExtractExcessExcelData> details = new ArrayList<>();
			curExtractDetails.stream().collect(Collectors.groupingBy(e -> e.getIdnumber())).forEach((idnumber, bedList) -> {
				ExtractExcessExcelData ed = new ExtractExcessExcelData();
				ed.setEmpNo(bedList.get(0).getEmpno());
				ed.setEmpName(bedList.get(0).getEmpname());
				ed.setIsCompanyEmp(bedList.get(0).getIscompanyemp() ? "是" : "否");
				ed.setIdNumber(idnumber);
				ed.setBillingUnitName(idnumber2billingUnitNameMap.get(idnumber));
				ed.setExcessMoney(bedList.get(0).getExcessmoney());
				ed.setAvoidTaxMoney(null);
				ed.setOutUnitPayMoney(null);
				details.add(ed);
			});
			is = this.getClass().getClassLoader().getResourceAsStream("template/extractExcessDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractBatch + "提成超额明细", response), ExtractExcessExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			workBook.fill(details, sheet);
			workBook.finish();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		} finally {
			if (is != null) is.close();
		}
		return ResponseEntity.ok();
	}


	@ApiOperation(value = "导出外部户发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportExtractOutUnitPayDetail")
	public void exportExtractOutUnitPayDetail(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		int length = query.split("-").length;
		if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");

		ClassPathResource resource = new ClassPathResource("template/exportExtractOutUnitPayDetail.xlsx");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InputStream is = null;
		XSSFWorkbook workbook = null;
		try {
			String extractBatch = query.split("-")[2];
			//外部户的发放明细
			Map<String,List<ExtractOutUnitPayExcelData>> detailMap = this.extractsumService.getExtractOutUnitPayDetails(extractBatch);
			List<String> outUnitNameList = detailMap.keySet().stream().collect(Collectors.toList());
			if(!outUnitNameList.isEmpty()){
				workbook = new XSSFWorkbook(resource.getInputStream());
				workbook.setSheetName(0, outUnitNameList.get(0));
				for (int i = 1; i < outUnitNameList.size(); i++) {
					workbook.cloneSheet(0, outUnitNameList.get(i));
				}
				workbook.write(bos);
				is = new ByteArrayInputStream(bos.toByteArray());
				ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractBatch + "员工外部户发放明细", response), ExtractOutUnitPayExcelData.class).withTemplate(is).build();
				detailMap.forEach((outUnitName,list)->{
					WriteSheet sheet = EasyExcel.writerSheet(outUnitNameList.indexOf(outUnitName)).build();
					workBook.fill(list, sheet);
				});
				workBook.finish();
			}else{
				throw new RuntimeException("没有员工外部户发放明细可导出！");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "导入提成费用金额", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@PostMapping("/importExtractFeePay")
	public ResponseEntity importExtractFeePay(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "query", required = true) String query, HttpServletResponse response, HttpServletRequest request) throws IOException {

		int length = query.split("-").length;
		if (length != 3) return ResponseEntity.error("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];

		/**
		 * 校验是否能够导入费用金额
		 */
		List<BudgetExtractsum> extractSumList = this.extractsumService.list(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0));
		long count = extractSumList.stream().filter(e -> e.getStatus() != ExtractStatusEnum.APPROVED.getType()).count();
		if (count > 0) return ResponseEntity.error("提成批次【" + extractBatch + "】不支持此操作!");
		InputStream is = null;
		int headRows = 0; //表示表头有1行
		EasyExcelImportListener extractListener = new EasyExcelImportListener(extractsumService, FEEPAY, headRows, 3, extractBatch, extractSumList);
		try {
			is = file.getInputStream();
			EasyExcel.read(is, extractListener).sheet(0).doReadSync();
		} catch (IOException e1) {
			e1.printStackTrace();
			LOGGER.error(e1.getMessage(), e1);
			return ResponseEntity.error(e1.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
				return ResponseEntity.error(e.getMessage());
			}
		}
		//明细数据的错误明细
		Map<Integer, Map<Integer, String>> errorMap = extractListener.getErrorMap();
		//导入的所有的数据
		Map<Integer, Map<Integer, String>> allDataMap = extractListener.getAllDataMap();
		if (!errorMap.isEmpty()) {
			List<ExtractFeePayExcelData> details = new ArrayList<>();
			allDataMap.forEach((i, data) -> {
				String empNo = data.get(0);
				String empName = data.get(1);
				String feeStr = data.get(2);

				ExtractFeePayExcelData ed = new ExtractFeePayExcelData();
				ed.setEmpNo(empNo);
				ed.setEmpName(empName);
				ed.setFeePay(feeStr);
				Map<Integer, String> errMap = (Map<Integer, String>) errorMap.get(i);
				if (errMap != null) {
					String errMsg = errMap.get(3);
					ed.setErrMsg(errMsg);
				}
				details.add(ed);
			});
			InputStream iss = null;
			try {
				String key = IMPORT_FEE_PAY + "_" + UserThreadLocal.get().getUserName();
				String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
				iss = this.getClass().getClassLoader().getResourceAsStream("template/importFeePay.xlsx");
				ExcelWriter workBook = EasyExcel.write(new File(errorFileName), ExtractFeePayExcelData.class).withTemplate(iss).build();
				WriteSheet sheet = EasyExcel.writerSheet(0).build();
				workBook.fill(details, sheet);
				workBook.finish();
				redis.set(key, errorFileName, expiretime);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (iss != null) iss.close();
			}
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");
		}
		return ResponseEntity.ok("导入成功");
	}


	@ApiOperation(value = "下载提成费用发放模板", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downExtractFeepayTemplate")
	public void downExtractFeepayTemplate(HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("template/importFeePay.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成费用发放导入模板", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			List<Map<String, Object>> list = new ArrayList<>();
			workBook.fill(list, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	@ApiOperation(value = "导入提成超额明细", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@PostMapping("/importExtractExcessDetail")
	public ResponseEntity importExtractExcessDetail(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "query", required = true) String query, HttpServletResponse response, HttpServletRequest request) throws IOException {

		int length = query.split("-").length;
		if (length != 3) return ResponseEntity.error("请先选择导航栏的一个批次！");
		String extractBatch = query.split("-")[2];

		/**
		 * 校验是否能够导入超额明细
		 */
		this.extractsumService.validateIsCanSetExcess(extractBatch);
		InputStream inputStream = file.getInputStream();
		try{
			List<ExtractExcessExcelData> details = this.extractsumService.importExtractExcessDetail(inputStream,extractBatch);
			List<ExtractExcessExcelData> errorDetails = details.stream().filter(e -> StringUtils.isNotBlank(e.getErrMsg())).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(errorDetails)){
				InputStream iss = null;
				try {
					String key = IMPORT_EXCESS_TYPE + "_" + UserThreadLocal.get().getUserName();
					String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
					iss = this.getClass().getClassLoader().getResourceAsStream("template/extractExcessDetail.xlsx");
					ExcelWriter workBook = EasyExcel.write(new File(errorFileName), ExtractExcessExcelData.class).withTemplate(iss).build();
					WriteSheet sheet = EasyExcel.writerSheet(0).build();
					workBook.fill(details, sheet);
					workBook.finish();
					redis.set(key, errorFileName, expiretime);
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error(e.getMessage(), e);
				} finally {
					if (iss != null) iss.close();
					if(inputStream!=null) inputStream.close();
				}
				return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");
			}
		}catch (Exception e){
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, e.getMessage());
		}finally {
			if(inputStream!=null) inputStream.close();
		}
		return ResponseEntity.ok("导入成功");
	}


	@ApiOperation(value = "下载导入提成费用发放错误明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downImportExtractFeePayErrorDetail")
	public ResponseEntity<String> downImportExtractFeePayErrorDetail(HttpServletResponse response, HttpServletRequest request) throws Exception {

		InputStream is = null;
		try {
			if (redis.get(IMPORT_FEE_PAY + "_" + UserThreadLocal.get().getUserName()) == null) {
				throw new RuntimeException("没有提成费用发放错误明细可供下载。");
			}
			String errorFileName = redis.get(IMPORT_FEE_PAY + "_" + UserThreadLocal.get().getUserName());
			is = new FileInputStream(errorFileName);
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成费用发放导入错误明细", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			workBook.finish();
			File file = new File(errorFileName);
			if (file.exists()) file.delete();
			redis.delete(IMPORT_FEE_PAY + "_" + UserThreadLocal.get().getUserName());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		} finally {
			if (is != null) is.close();
		}
		return ResponseEntity.ok();
	}

	@ApiOperation(value = "下载导入提成超额错误明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downImportExtractExcessErrorDetail")
	public ResponseEntity<String> downImportExtractExcessErrorDetail(HttpServletResponse response, HttpServletRequest request) throws Exception {

		InputStream is = null;
		try {
			if (redis.get(IMPORT_EXCESS_TYPE + "_" + UserThreadLocal.get().getUserName()) == null) {
				throw new RuntimeException("没有提成错误明细可供下载。");
			}
			String errorFileName = redis.get(IMPORT_EXCESS_TYPE + "_" + UserThreadLocal.get().getUserName());
			is = new FileInputStream(errorFileName);
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成超额导入错误明细", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			workBook.finish();
			File file = new File(errorFileName);
			if (file.exists()) file.delete();
			redis.delete(IMPORT_EXCESS_TYPE + "_" + UserThreadLocal.get().getUserName());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		} finally {
			if (is != null) is.close();
		}
		return ResponseEntity.ok();
	}

	@ApiOperation(value = "导出发放表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成主表id", name = "id", dataType = "Long", required = true)
	})
	@GetMapping("/exportExtractPaymentDetail")
	public void exportExtractPaymentDetail(@RequestParam(name = "id", required = true) Long id, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			//模板中的表头
			Map<String, Object> heads = new HashMap<>();

			BudgetExtractsum extractsum = this.extractsumService.getById(id);
			if (extractsum.getStatus() != ExtractStatusEnum.CALCULATION_COMPLETE.getType())
				throw new RuntimeException("导出失败！该批次提成还未计算完成");
			heads.put("yearPeriod", yearPeriodService.getById(extractsum.getYearid()).getPeriod());
			heads.put("extractMonth", extractsum.getExtractmonth());
			heads.put("unitName", extractsum.getDeptname());

			//模板中的明细
			List<ExtractPaymentExcelData> details = new ArrayList<>();
			extractsumService.exportExtractPaymentDetail(extractsum, details);
			//汇总数据
			BigDecimal realExtractSum = details.stream().filter(e -> e.getIsSum()).map(e -> e.getRealExtract()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal consotaxSum = details.stream().filter(e -> e.getIsSum()).map(e -> e.getConsotax()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal salaryUnitPayMoneySum = details.stream().filter(e -> e.getIsSum()).map(e -> e.getSalaryUnitPayMoney() == null ? BigDecimal.ZERO : e.getSalaryUnitPayMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal avoidUnitPayMoneySum = details.stream().filter(e -> e.getIsSum()).map(e -> e.getAvoidUnitPayMoney() == null ? BigDecimal.ZERO : e.getAvoidUnitPayMoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal feeSum = details.stream().filter(e -> e.getIsSum()).map(e -> e.getFee() == null ? BigDecimal.ZERO : e.getFee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			heads.put("realExtractSum", realExtractSum);
			heads.put("consotaxSum", consotaxSum);
			heads.put("salaryUnitPayMoneySum", salaryUnitPayMoneySum);
			heads.put("avoidUnitPayMoneySum", avoidUnitPayMoneySum);
			heads.put("feeSum", feeSum);

			FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			is = this.getClass().getClassLoader().getResourceAsStream("template/extractPaymentDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractsum.getExtractmonth() + "-" + extractsum.getDeptname() + "-提成发放表", response), ExtractPaymentExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("提成发放表");
			workBook.fill(heads, sheet);
			workBook.fill(details, fillConfig, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	@ApiOperation(value = "导出陈彩莲发放", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportExtractCCLPayDetail")
	public void exportExtractCCLPayDetail(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];

			List<BudgetExtractdetail> allExtractDetails = this.extractsumService.getExtractDetailByExtractmonth(extractBatch);
			/**
			 * 判断是否存在未设置超额的
			 */
			Long unHandleExtractCount = allExtractDetails.stream().filter(e -> e.getExcesstype() != null && e.getExcesstype().intValue() == ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType() &&
					e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0
					&& e.getHandleflag() != null && !e.getHandleflag()).count();
			if (unHandleExtractCount > 0) throw new RuntimeException("导出失败！提成批次【" + extractBatch + "】中存在记录未进行超额发放。");
			Map<String, Object> heads = new HashMap<>();
			heads.put("extractMonth", extractBatch);
			List<ExtractCCLPayExcelData> details = this.extractsumService.getCCLPayDetailList(extractBatch);
			is = this.getClass().getClassLoader().getResourceAsStream("template/extractCCLPayDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("陈彩莲提成发放表", response), ExtractCCLPayExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("陈彩莲提成发放");
			workBook.fill(heads, sheet);
			workBook.fill(details, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "导出提成报税表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportCurMonthExtractIncomeDetail")
	public void exportCurMonthExtractIncomeDetail(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			int length = query.split("-").length;
			if (length != 2) throw new RuntimeException("请先选择导航栏的一个月份！");
			BudgetYearPeriod yearPeriod = yearPeriodService.getById(Long.valueOf(query.split("-")[0]));
			//BudgetYearPeriod yearPeriod = yearPeriodService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", query.split("-")[0]));
			//当前选择的提成月份
			String extractMonth = this.extractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(query.split("-")[1]));
			/**
			 * 校验当前月的提成是否都已计算过
			 */
			List<BudgetExtractsum> extractSumList = this.extractsumService.list(new QueryWrapper<BudgetExtractsum>().likeRight("extractmonth", extractMonth).eq("deleteflag", 0).lt("status", ExtractStatusEnum.CALCULATION_COMPLETE.getType()));
			if (!extractSumList.isEmpty()) throw new RuntimeException("月份【" + extractMonth + "】存在未计算发放的提成。");

			/**
			 * 获取当年之前每个月所有的工资
			 */
			int curmonth = Integer.valueOf(extractMonth.substring(4, 6));
			int year = Integer.valueOf(extractMonth.substring(0, 4));
			Map<Integer, Map<String, Object>> lastSalaryMsgMap = new HashMap<>();
			for (int i = curmonth - 1; i > 0; i--) {
				String lastmonth = year + (i >= 10 ? i + "" : "0" + i);
				Map<String, Object> salaryMsg = this.hrService.getSalary(lastmonth);
				lastSalaryMsgMap.put(i, salaryMsg);
			}

			List<ExtractIncomeExcelData> details = this.extractsumService.getExtractIncomeDetails(extractMonth, lastSalaryMsgMap);

			//String path = ClassUtils.getDefaultClassLoader().getResource("template").getPath();
			//String templateName = "/extractIncomeDetail.xlsx";
			is = this.getClass().getClassLoader().getResourceAsStream("template/extractIncomeDetail.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractMonth + "提成报税表", response), ExtractIncomeExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName(extractMonth + "提成报税表");
			workBook.fill(details, sheet);
			workBook.finish();
			//return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
			//return ResponseEntity.error(e.getMessage());
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "查看提成批次二维码", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/showExtractQrcode")
	public ResponseEntity showExtractQrcode(@RequestParam(name = "query", required = true) String query) throws Exception {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			String qrcode = this.extractsumService.showExtractQrcode(extractBatch);
			return ResponseEntity.ok(qrcode);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "撤回计算结果", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/reset")
	public ResponseEntity reset(@RequestParam(name = "query", required = true) String query) throws Exception {
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			this.extractsumService.reset(extractBatch);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	//	@ApiOperation(value = "导出提成支付", httpMethod = "GET")
//	@ApiImplicitParams(value = {
//			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
//			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
//	})
	@ApiIgnore
	@GetMapping("/exportExtractPay")
	public void exportExtractPay(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		OutputStream outputStream = null;
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			/**
			 * 校验当前批次的提成是否都已计算过
			 */
			List<BudgetExtractsum> extractSumList = this.extractsumService.list(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0).lt("status", ExtractStatusEnum.CALCULATION_COMPLETE.getType()));
			if (!extractSumList.isEmpty()) throw new RuntimeException("批次【" + extractBatch + "】存在未计算发放的提成。");
			Map<String, Object> heads = extractsumService.getExtractPayExcelHead(extractBatch);
			List<ExtractPayApplyExcelData> details = extractsumService.getExtractExcelDetails(extractBatch, heads);
			FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			is = BudgetExtractController.class.getClassLoader().getResourceAsStream("template/extractPayApply.xlsx");
			ExcelFillCellMergeStrategy.rowMergeFlag = new HashMap<>();
			outputStream = EasyExcelUtil.getOutputStream(extractBatch + "提成支付表", response);
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(extractBatch + "提成支付表", response)).withTemplate(is).registerWriteHandler(new ExcelFillCellMergeStrategy(5, null, 1)).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("支付申请单");
			workBook.fill(heads, sheet);
			workBook.fill(details, fillConfig, sheet);
			workBook.finish();

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
			if (outputStream != null) outputStream.close();
		}
	}


	@ApiOperation(value = "提成支付申请单发放明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			//@ApiImplicitParam(value = "提成id", name = "extractPayApplyId", dataType = "Long", required = true)
			@ApiImplicitParam(value = "提成id", name = "extractSumId", dataType = "Long", required = true)
	})
	@GetMapping("/getExtractPayApplyPayDetail")
	public ResponseEntity<ExtractPayApplyPayDetailVO> getExtractPayApplyPayDetail(@RequestParam(name = "extractSumId", required = true) Long extractSumId) throws Exception {
		try {
			ExtractPayApplyPayDetailVO result = this.extractsumService.getExtractPayApplyPayDetail(extractSumId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "导出提成支付汇总", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportExtractPaySum")
	public void exportExtractPaySum(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		OutputStream outputStream = null;
		try {
			int length = query.split("-").length;
			if (length != 3) throw new RuntimeException("请先选择导航栏的一个批次！");
			String extractBatch = query.split("-")[2];
			/**
			 * 校验当前批次的提成是否都已计算过
			 */
			List<BudgetExtractsum> extractSumList = this.extractsumService.list(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0).lt("status", ExtractStatusEnum.CALCULATION_COMPLETE.getType()));
			if (!extractSumList.isEmpty()) throw new RuntimeException("批次【" + extractBatch + "】存在未计算发放的提成。");
			List<ExtractPaySumExcelData> extractPaySumExcelDetails = extractsumService.getExtractPaySumExcelDetails(extractBatch);
			is = BudgetExtractController.class.getClassLoader().getResourceAsStream("template/extractPaySum.xlsx");
			ExcelFillCellMergeStrategy.rowMergeFlag = new HashMap<>();
			outputStream = EasyExcelUtil.getOutputStream(extractBatch + "提成支付汇总表", response);
			int[] col = {1};
			ExcelWriter workBook = EasyExcel.write(outputStream).withTemplate(is).registerWriteHandler(new ExcelFillCellMergeStrategy(3, col, 2)).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("提成支付汇总表");
			workBook.fill(extractPaySumExcelDetails, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
			if (outputStream != null) outputStream.close();
		}
	}

	@ApiOperation(value = "签收", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成批次", name = "extractMonth", dataType = "String", required = true)
	})
	@GetMapping("/sign")
	public ResponseEntity sign(@RequestParam(name = "extractMonth", required = true) String extractMonth) throws Exception {
		try {
			this.extractsumService.sign(extractMonth);
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "签收列表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "提成批次", name = "extractMonth", dataType = "String", required = true)
	})
	@GetMapping("/getExtractSignList")
	public ResponseEntity<BudgetExtractSignMain> getExtractSignList(@RequestParam(name = "extractMonth", required = true) String extractMonth) throws Exception {
		try {
			BudgetExtractSignMain result = this.extractsumService.getExtractSignList(extractMonth);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		InputStream is = null;
		try {

			List<ExtractPaySumExcelData> details = new ArrayList<>();
			ExtractPaySumExcelData data = new ExtractPaySumExcelData();
			data.setExtractType(ExtractTypeEnum.RETURN.value);
			data.setDeptName("直营");
			data.setYearPeriod("20届");
			data.setApplyExtract(new BigDecimal("100"));
			data.setExtractTax(new BigDecimal("100.1"));
			data.setInvoiceExcessTax(new BigDecimal("1.2"));
			details.add(data);
			ExtractPaySumExcelData data1 = new ExtractPaySumExcelData();
			data1.setExtractType(ExtractTypeEnum.DRAFT.value);
			data1.setDeptName("传媒");
			data1.setYearPeriod("22届");
			data1.setApplyExtract(new BigDecimal("200"));
			data1.setExtractTax(new BigDecimal("200.1"));
			data1.setInvoiceExcessTax(new BigDecimal("2.2"));
			details.add(data1);
			ExtractPaySumExcelData data2 = new ExtractPaySumExcelData();
			data2.setExtractType(ExtractTypeEnum.DRAFT.value);
			data2.setDeptName("义教");
			data2.setYearPeriod("21届");
			data2.setApplyExtract(new BigDecimal("200"));
			data2.setExtractTax(new BigDecimal("200.1"));
			data2.setInvoiceExcessTax(new BigDecimal("2.2"));
			details.add(data1);
			ExtractPaySumExcelData data3 = new ExtractPaySumExcelData();
			data3.setExtractType(ExtractTypeEnum.VERIFYING.value);
			data3.setDeptName("直营");
			data3.setYearPeriod("21届");
			data3.setApplyExtract(new BigDecimal("200"));
			data3.setExtractTax(new BigDecimal("200.1"));
			data3.setInvoiceExcessTax(new BigDecimal("2.2"));
			details.add(data1);
			is = BudgetExtractController.class.getClassLoader().getResourceAsStream("template/extractPaySum.xlsx");
			ExcelFillCellMergeStrategy.rowMergeFlag = new HashMap<>();
			int[] col = {1};
			ExcelWriter workBook = EasyExcel.write(new File("D:\\exceltemplate\\b.xlsx")).withTemplate(is).registerWriteHandler(new ExcelFillCellMergeStrategy(3, col, 2)).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).registerWriteHandler(new ExcelFillCellMergeStrategy(3, col, 2)).build();
			sheet.setSheetName("提成支付汇总表");
			workBook.fill(details, sheet);
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (is != null) is.close();
		}
	}
}
