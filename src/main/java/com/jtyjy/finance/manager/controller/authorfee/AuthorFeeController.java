package com.jtyjy.finance.manager.controller.authorfee;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeedetail;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeesum;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.cache.PersonCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExcelDetail;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExportExcelDetail;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeImportCommonData;
import com.jtyjy.finance.manager.easyexcel.AuthorFeeCalTaxDetailExcelData;
import com.jtyjy.finance.manager.easyexcel.AuthorFeePreTaxSumExcelData;
import com.jtyjy.finance.manager.easyexcel.EasyExcelImportListener;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.AuthorFeeDetailVO;
import com.jtyjy.finance.manager.vo.AuthorFeeMainVO;
import com.jtyjy.finance.manager.vo.AuthorFeePeriodNavigateTreeVO;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import com.klcwqy.easy.strategy.Strategy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = {"????????????"})
@RestController
@RequestMapping("/api/budgetAuthorfee")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class AuthorFeeController extends BaseController {

	private final static Logger LOGGER = LoggerFactory.getLogger(AuthorFeeController.class);
	public final static String AUTHOR_IMPORT = "AUTHOR_IMPORT";

	@Autowired
	private WbUserService userService;

	@Autowired
	private BudgetYearPeriodService yearService;

	@Autowired
	private BudgetMonthPeriodService monthService;

	@Autowired
	private BudgetUnitService unitService;

	@Autowired
	private WbDeptService deptService;

	@Autowired
	private WbPersonService personService;

	@Autowired
	private BudgetProductCategoryService productCategoryService;

	@Autowired
	private BudgetProductService productService;

	@Autowired
	private BudgetMonthAgentService monthAgentService;

	@Autowired
	private BudgetAuthorService authorService;

	@Autowired
	private BudgetAuthorfeesumService authorfeesumService;

	@Autowired
	private BudgetAuthorfeedetailService authorfeedetailService;

	@Autowired
	private HrService hrService;

	@Autowired
	private UserCache userCache;

	@Autowired
	private PersonCache personCache;

	@Autowired
	private DeptCache deptCache;

	private final static String IMPORT_TYPE = "gf";

	private final static String AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_ = "AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_";

	@Autowired
	private CuratorFramework curatorFramework;

	@Autowired
	private RedisClient redis;

	@Value("${file.shareDir}")
	private String fileShareDir;

	@Value("${redis.file.key.expiretime}")
	private Integer expiretime;

	@ApiOperation(value = "????????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downAuthorFeeImportTemplate")
	public void downAuthorFeeImportTemplate(HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("template/importContributionFee.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			List<Map<String, Object>> list = new ArrayList<>();
			workBook.fill(list, sheet);
			workBook.finish();

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (is != null) is.close();
		}
	}


	@ApiOperation(value = "??????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????id(??????????????????,??????)", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/exportDetail")
	public void exportDetail(HttpServletResponse response, HttpServletRequest request,
	                         @RequestParam(name = "id", required = true) String id) throws Exception {

		String[] strArr = id.split(",");
		BudgetAuthorfeesum firstAuthorfeesum = this.authorfeesumService.getById(strArr[0]);
		ClassPathResource resource = new ClassPathResource("template/exportContributionFee.xlsx");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		InputStream is = null;
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(resource.getInputStream());
			workbook.setSheetName(0, firstAuthorfeesum.getCode());
			for (int i = 1; i < strArr.length; i++) {
				BudgetAuthorfeesum sum = this.authorfeesumService.getById(strArr[i]);
				workbook.cloneSheet(0, sum.getCode());
			}
			workbook.write(bos);

			is = new ByteArrayInputStream(bos.toByteArray());

			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response), ContributionFeeExcelDetail.class).withTemplate(is).build();
			for (int i = 0; i < strArr.length; i++) {
				String s = strArr[i];
				BudgetAuthorfeesum authorfeesum = this.authorfeesumService.getById(s);
				if (authorfeesum == null) continue;
				//????????????
				Map<String, String> heads = new HashMap<>();
				heads.put("yearName", authorfeesum.getYearperiod());
				heads.put("contributionFeeNo", authorfeesum.getCode());
				heads.put("unitName", authorfeesum.getFeedeptname());
				heads.put("contributionFeeMonth", authorfeesum.getFeemonth());
				heads.put("bxEmpno", authorfeesum.getReimno());

				List<BudgetAuthorfeedetail> feeDetails = authorfeedetailService.list(new QueryWrapper<BudgetAuthorfeedetail>().eq("authorfeesumid", s));

				List<ContributionFeeExcelDetail> details = feeDetails.stream().map(detail -> {
					ContributionFeeExcelDetail d = new ContributionFeeExcelDetail();
					d.setSubjectName(detail.getReimbursesubject());
					d.setIsDecutionTax(detail.getTaxtype() ? "???" : "???");
					d.setProductForm(detail.getProducttype());
					d.setMonthAgentName(detail.getProductbgtcls());
					d.setSubject(detail.getSubject());
					d.setRemark(detail.getContext());
					d.setAuthorType(detail.getAuthortype() ? "????????????" : "????????????");
					d.setAuthorName(detail.getAuthorname());
					d.setAuthorIdnumber((null == detail.getAuthoridnumber() || StringUtils.isEmpty(detail.getAuthoridnumber())) ? detail.getTaxpayeridnumber() : detail.getAuthoridnumber());
					d.setManuscriptQuality(detail.getPaperquality());
					d.setPageNumber(detail.getPageorcopy());
					d.setContributionFeeStandard(detail.getFeestandard().toString());
					d.setContributionFee(detail.getCopefee().stripTrailingZeros().toPlainString());
					d.setTeacherEmpno(detail.getEmpno());
					d.setTeacherEmpname(detail.getEmpname());
					d.setContributionFeeUnitName(detail.getFeebdgdept());
					d.setAscriptionUnitName(detail.getBusinessgroup());
					d.setIsNeedTran(null == detail.getNeedzz() ? "???" : (detail.getNeedzz() ? "???" : "???"));
					return d;
				}).collect(Collectors.toList());
				WriteSheet sheet = EasyExcel.writerSheet(i).build();
				sheet.setSheetName(authorfeesum.getCode() + "????????????");
				workBook.fill(heads, sheet);
				workBook.fill(details, sheet);
			}
			workBook.finish();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (bos != null) bos.close();
			if (is != null) is.close();
			if(workbook!=null) workbook.close();
		}
	}



	@ApiOperation(value = "?????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????????????????(??????)", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportBatchContributionFee")
	public void exportBatchContributionFee(@RequestParam(name = "query", required = true) String query,HttpServletResponse response) throws Exception{

		String[] queryArr = query.split("-");

		BudgetYearPeriod yearPeriod = null;
		String name = "";
		String feeMonth = null;
		if(queryArr.length == 1){
			yearPeriod = yearService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
			name = queryArr[0];
		}else if(queryArr.length == 2){
			yearPeriod = yearService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
			//????????????  202105
			feeMonth = BudgetExtractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));
			name = feeMonth;
		}else{
			throw new RuntimeException("???????????????");
		}
		InputStream is = null;

		try {
			List<ContributionFeeExportExcelDetail> details = authorfeesumService.getBatchContributionFee(yearPeriod.getPeriod(),feeMonth);
			is = this.getClass().getClassLoader().getResourceAsStream("template/exportMonthContributionFee.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream(name+"???????????????", response), ContributionFeeExportExcelDetail.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("?????????"+feeMonth+"????????????");
			workBook.fill(details, sheet);
			workBook.finish();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}



	@ApiOperation(value = "??????????????????", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/importDetail")
	public ResponseEntity importAuthorFeeDetail(@RequestParam(name = "file") CommonsMultipartFile file, HttpServletResponse response, HttpServletRequest request) throws Exception {

		ContributionFeeImportCommonData commonData = new ContributionFeeImportCommonData();
		commonData.setUnitSubjects(this.authorfeesumService.getUnitSubjects());
		commonData.setProductCategories(productCategoryService.list(null));
		commonData.setProducts(productService.list(null));
		commonData.setAuthors(authorService.list(null));

		InputStream is = null;
		int headRows = 2; //???????????????3???
		int colNum = 18; //??????
		EasyExcelImportListener extractListener = new EasyExcelImportListener(authorfeesumService, AUTHOR_IMPORT, headRows, colNum, commonData);
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
		//?????????????????????????????????????????????????????????????????????
		List<String> headErrorMsg = extractListener.getHeadErrorMsg();
		if (!headErrorMsg.isEmpty()) {
			return ResponseEntity.error(headErrorMsg.stream().collect(Collectors.joining(",")));
		}
		//???????????????????????????
		Map<Integer, Map<Integer, String>> errorMap = extractListener.getErrorMap();
		//????????????????????????
		Map<Integer, Map<Integer, String>> allDataMap = extractListener.getAllDataMap();

		if (!errorMap.isEmpty()) {
			List<ContributionFeeExcelDetail> details = new ArrayList<>();
			Map<String, String> heads = new HashMap<>();
			populateData(details, heads, allDataMap, errorMap, headRows);

			InputStream iss = null;
			try {
				iss = this.getClass().getClassLoader().getResourceAsStream("template/importContributionFee.xlsx");
				String key = IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName();
				String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_????????????.xlsx";
				ExcelWriter workBook = EasyExcel.write(new File(errorFileName), ContributionFeeExcelDetail.class).withTemplate(iss).build();
				WriteSheet sheet = EasyExcel.writerSheet(0).build();
				sheet.setSheetName("????????????????????????");
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
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????");
		}
		return ResponseEntity.ok("????????????");
	}

	private void populateData(List<ContributionFeeExcelDetail> details, Map<String, String> heads, Map<Integer, Map<Integer, String>> allDataMap, Map<Integer, Map<Integer, String>> errorMap, int headRows) {
		Map<Integer, String> headMap = allDataMap.get(1);
		String year = headMap.get(1); //??????
		String contributionFeeNo = headMap.get(3); //????????????
		String unitname = headMap.get(5); //????????????
		String contributionFeeMonth = headMap.get(7); //????????????
		String bxEmpno = headMap.get(9); //??????????????????
		allDataMap.forEach((i, data) -> {
			if (i <= headRows) return;
			String subjectName = data.get(0); //????????????
			String isDecutionTax = data.get(1); //????????????
			String productForm = data.get(2); //????????????
			String monthAgentName = data.get(3); //????????????II???
			String subject = data.get(4); //??????
			String remark = data.get(5); //?????????????????????
			String authorType = data.get(6); //????????????
			String authorName = data.get(7);//??????
			String authorIdnumber = data.get(8);//????????????
			String manuscriptQuality = data.get(9);//????????????
			String pageNumber = data.get(10);//???/???/??????
			String contributionFeeStandard = data.get(11);//????????????
			String contributionFee = data.get(12);//????????????
			String teacherEmpno = data.get(13);//??????????????????
			String teacherEmpname = data.get(14);//??????????????????
			String contributionFeeUnitName = data.get(15);//??????????????????
			String ascriptionUnitName = data.get(16);//?????????????????????????????????
			String isNeedTran = data.get(17);//????????????(???/???)
			ContributionFeeExcelDetail detail = new ContributionFeeExcelDetail(subjectName, isDecutionTax, productForm, monthAgentName, subject, remark, authorType, authorName, authorIdnumber, manuscriptQuality, pageNumber, contributionFeeStandard, contributionFee, teacherEmpno, teacherEmpname, contributionFeeUnitName, ascriptionUnitName, isNeedTran);
			if (errorMap.get(i) != null) {
				detail.setErrMsg(errorMap.get(i).get(18));
			}
			details.add(detail);
		});
		heads.put("yearName", year);
		heads.put("contributionFeeNo", contributionFeeNo);
		heads.put("unitName", unitname);
		heads.put("contributionFeeMonth", contributionFeeMonth);
		heads.put("bxEmpno", bxEmpno);
	}


//	@ApiOperation(value = "??????????????????", httpMethod = "POST")
//	@ApiImplicitParams(value = {
//			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
//	})
//	@PostMapping("/importDetail")
//	public ResponseEntity importAuthorFeeDetail(@RequestParam(name = "file") CommonsMultipartFile file, HttpServletResponse response, HttpServletRequest request) throws Exception {
//
//		//file.getName();
//		String extension = EasyExcelUtil.getFileExtension(file.getFileItem().getName());
//		if (!"XLSX".equals(extension.toUpperCase())) return ResponseEntity.error("???????????????xlsx??????????????????");
//		//if(redis.exist(AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_+UserThreadLocal.get().getUserName())){
//		//    return ResponseEntity.error("????????????????????????????????????????????????????????????");
//		//}
//		//redis.set(AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_+UserThreadLocal.get().getUserName(),AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_+UserThreadLocal.get().getUserName());
//		Object[] obj = FileTools.commonsMultipartFile2File(file, this.fileShareDir);
//
//		ContributionFeeImportCommonData commonData = new ContributionFeeImportCommonData();
//		commonData.setYearPeriods(yearService.list(null));
//		commonData.setUnitList(unitService.list(null));
//		commonData.setMonthPeriods(monthService.list(null));
//		commonData.setUserList(userCache.USERID_MAP.values().stream().collect(Collectors.toList()));
//		commonData.setDeptList(deptCache.DEPT_MAP.values().stream().collect(Collectors.toList()));
//		commonData.setPersonList(personCache.EMPNO_USER_MAP.values().stream().collect(Collectors.toList()));
//		commonData.setUnitSubjects(this.authorfeesumService.getUnitSubjects());
//		commonData.setProductCategories(productCategoryService.list(null));
//		commonData.setProducts(productService.list(null));
//		commonData.setMonthAgentList(monthAgentService.list(null));
//		commonData.setAuthors(authorService.list(null));
//		commonData.setAuthorfeesums(authorfeesumService.list(null));
//		/**
//		 * obj[0] ?????????????????????????????????
//		 * obj[1] ?????????????????????
//		 */
//		ImportHelper helper = new ImportHelper(obj[0].toString());
//		ContributionFeeImportExcelPostProcessor processor = new ContributionFeeImportExcelPostProcessor();
//		AuthorFeeAfterEndPostProcessor endPostProcessor = new AuthorFeeAfterEndPostProcessor();
//		processor.setAfterEndPostProcessor(endPostProcessor);
//		processor.setCommonData(commonData);
//		processor.setFeeSumService(this.authorfeesumService);
//		List<Sheet> sheets = helper.loadSheet();
//		helper.addProcessors(processor);
//		helper.addProcessors(endPostProcessor);
//		Map<String, Object> headMap  = helper.doImport(sheets, ContributionFeeExcelHead.class);
//		if(!helper.getErrorFile()) {
//			//??????????????????
//			try {
//				authorfeesumService.saveFeeDetails(headMap);
//			}catch(Exception e) {
//				e.printStackTrace();
//				return ResponseEntity.error(e.getMessage());
//			}finally {
//				helper.end(true);
//				File file2 = (File)obj[1];
//				if(file2.exists())file2.delete();
//			}
//			//redis.delete(AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_+UserThreadLocal.get().getUserName());
//			return ResponseEntity.ok("????????????");
//		}else {
//			helper.end(true);
//			File file2 = (File)obj[1];
//			if(file2.exists())file2.delete();
//			String fileExtension = EasyExcelUtil.getFileExtension(obj[0].toString());
//			String fileNameNotExtension = EasyExcelUtil.getFileNameNotExtension(obj[0].toString());
//			String errorFileName = fileNameNotExtension+"_????????????."+fileExtension;
//			redis.set(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName(), errorFileName,expiretime);
//			//redis.delete(AUTHOR_FEE_REDIS_IMPORT_KEY_TYPE_+UserThreadLocal.get().getUserName());
//			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"?????????????????????,?????????????????????");
//		}
//	}

	@ApiOperation(value = "??????????????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downAuthorFeeImportErrorDetail")
	public void downAuthorFeeImportErrorDetail(HttpServletResponse response) throws Exception {
		FileInputStream is = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		File file = null;
		try {
			if (redis.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName()) == null) {
				throw new RuntimeException("???????????????????????????????????????");
			}
			String errorFileName = redis.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
			String fileName = "????????????????????????";
			fileName = URLEncoder.encode(fileName, "UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("content-Type", "application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
			byte[] buffer = new byte[1024];
			file = new File(errorFileName);
			is = new FileInputStream(file);
			bis = new BufferedInputStream(is);
			os = response.getOutputStream();
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = bis.read(buffer);
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
			if (bis != null) bis.close();
			if (os != null) os.close();
			if (file.exists()) file.delete();
			redis.delete(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
		}
	}

	@ApiOperation(value = "???????????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????????????????(??????)", name = "query", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????", name = "status", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "????????????", name = "code", dataType = "String", required = false),
			@ApiImplicitParam(value = "????????????", name = "feedeptname", dataType = "String", required = false)
	})
	@GetMapping("/getAuthorSumList")
	public ResponseEntity<List<AuthorFeeMainVO>> getAuthorSumList(@RequestParam(name = "query", required = true) String query,
	                                                              @RequestParam(name = "status", required = false) Integer status,
	                                                              @RequestParam(name = "code", required = false) String code,
	                                                              @RequestParam(name = "feedeptname", required = false) String feeDeptName) {
		try {
			if (StringUtils.isBlank(query)) throw new RuntimeException("????????????????????????????????????");
			Map<String, Object> params = new HashMap<>();
			params.put("query", query);
			params.put("status", status);
			params.put("code", code);
			params.put("feeDeptName", feeDeptName);
			List<AuthorFeeMainVO> resultList = authorfeesumService.getAuthorSumList(params);
			return ResponseEntity.ok(resultList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "?????????????????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/getAuthorFeePeriodNavigateTree")
	public ResponseEntity<List<AuthorFeePeriodNavigateTreeVO>> getAuthorFeePeriodNavigateTree() {
		try {
			List<AuthorFeePeriodNavigateTreeVO> resultList = this.authorfeesumService.getAuthorFeePeriodNavigateTree();
			return ResponseEntity.ok(resultList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "???????????????????????????", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "???????????????id(???,??????)", name = "ids", dataType = "String", required = true)
	})
	@PostMapping("/batchDeleteAuthorFee")
	public ResponseEntity batchDeleteAuthorFee(@RequestParam(name = "ids", required = true) String ids) {
		try {
			authorfeesumService.batchDeleteAuthorFee(ids);
			return ResponseEntity.ok("????????????");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "???????????????????????????", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "???????????????id(???,??????)", name = "ids", dataType = "String", required = true)
	})
	@PostMapping("/batchSubmitAuthorFee")
	public ResponseEntity batchSubmitAuthorFee(@RequestParam(name = "ids", required = true) String ids) {
		try {
			authorfeesumService.batchSubmitAuthorFee(ids);
			return ResponseEntity.ok("????????????");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "???????????????????????????", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "???????????????id(???,??????)", name = "ids", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????-1????????? 2 ?????????", name = "status", dataType = "Integer", required = true),
			@ApiImplicitParam(value = "????????????", name = "remark", dataType = "String", required = false)
	})
	@PostMapping("/batchVerifyAuthorFee")
	public ResponseEntity batchVerifyAuthorFee(@RequestParam(name = "ids", required = true) String ids,
	                                           @RequestParam(name = "status", required = true) Integer status,
	                                           @RequestParam(name = "remark", required = false) String remark) {
		try {
			authorfeesumService.batchVerifyAuthorFee(ids, status, remark);
			return ResponseEntity.ok("????????????");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "??????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????id", name = "feeSumId", dataType = "Long", required = false),
			@ApiImplicitParam(value = "??????????????????0,???1???", name = "taxType", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "????????????II???", name = "agentName", dataType = "String", required = false),
			@ApiImplicitParam(value = "????????????(?????????????????????)", name = "authorType", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "??????", name = "author", dataType = "String", required = false),
			@ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getAuthorDetailList")
	public ResponseEntity<PageResult<AuthorFeeDetailVO>> getAuthorDetailList(@RequestParam(name = "feeSumId", required = true) Long feeSumId,
	                                                                         @RequestParam(name = "taxType", required = false) Integer taxType,
	                                                                         @RequestParam(name = "agentName", required = false) String agentName,
	                                                                         @RequestParam(name = "authorType", required = false) Integer authorType,
	                                                                         @RequestParam(name = "author", required = false) String author,
	                                                                         @RequestParam(defaultValue = "1") Integer page,
	                                                                         @RequestParam(defaultValue = "20") Integer rows) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("feeSumId", feeSumId);
			params.put("taxType", taxType);
			params.put("agentName", agentName);
			params.put("authorType", authorType);
			params.put("author", author);
			PageResult<AuthorFeeDetailVO> pageList = authorfeesumService.getAuthorDetailList(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "??????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????????????????(??????)", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/calculateTax")
	public ResponseEntity calculateTax(@RequestParam(name = "query", required = true) String query) {
		try {
			String[] queryArr = query.split("-");
			if (queryArr.length != 2) throw new RuntimeException("?????????????????????????????????????????????");
			BudgetYearPeriod yearPeriod = yearService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
			//????????????  202105
			String feeMonth = BudgetExtractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));
			String lockKey = "/finance-platform/budgetAuthorfee/calculateTax/" + feeMonth;
			ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, new Strategy() {
				@Override
				public void run(Object o) throws Exception {
					throw new RuntimeException("???????????????" + feeMonth + "??????????????????,??????????????????");
				}
			});
			try {
				zookeeperShareLock.tryLock();
				//??????????????????
				Map<String, Object> salaryCompanyMap = hrService.getSalaryCompanyByMonth(feeMonth);
				authorfeesumService.calculateTax(feeMonth, salaryCompanyMap);
			} catch (Exception e) {
				throw e;
			} finally {
				zookeeperShareLock.unLock();
			}

			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "??????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????????????????(??????)", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/resetCalculateTax")
	public ResponseEntity resetCalculateTax(@RequestParam(name = "query", required = true) String query) {
		try {
			String[] queryArr = query.split("-");
			if (queryArr.length != 2) throw new RuntimeException("????????????????????????????????????????????????");
			BudgetYearPeriod yearPeriod = yearService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
			//????????????  202105
			String feeMonth = BudgetExtractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));
			String lockKey = "/finance-platform/budgetAuthorfee/resetCalculateTax/" + feeMonth;
			ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, new Strategy() {
				@Override
				public void run(Object o) throws Exception {
					throw new RuntimeException("???????????????" + feeMonth + "????????????????????????,??????????????????");
				}
			});
			try {
				zookeeperShareLock.tryLock();
				authorfeesumService.resetCalculateTax(feeMonth);
			} catch (Exception e) {
				throw e;
			} finally {
				zookeeperShareLock.unLock();
			}
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "???????????????", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "???????????????id(???,??????)", name = "ids", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????????????????(??????)", name = "query", dataType = "String", required = true)
	})
	@PostMapping("/generateAuthorFeeReim")
	public ResponseEntity generateAuthorFeeReim(@RequestParam(name = "ids", required = true) String ids,
	                                            @RequestParam(name = "query") String query) {
		try {
			String[] queryArr = query.split("-");
			if (queryArr.length != 2) throw new RuntimeException("??????????????????????????????????????????");
			String lockKey = "/finance-platform/budgetAuthorfee/generateAuthorFeeReim/" + ids;
			ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, o -> {
				throw new RuntimeException("?????????????????????,?????????????????????");
			});
			try {
				zookeeperShareLock.tryLock();
				authorfeesumService.generateAuthorFeeReim(queryArr, ids);
			} catch (Exception e) {
				throw e;
			} finally {
				zookeeperShareLock.unLock();
			}
			return ResponseEntity.ok("????????????");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	@ApiOperation(value = "??????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????ids????????????,?????????", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/exportAuthorFeeSumList")
	public void exportExtractDeductionReport(@RequestParam(name = "ids", required = true) String ids, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			if (StringUtils.isBlank(ids)) throw new RuntimeException("??????????????????ids");
			List<BudgetAuthorfeesum> feeSumList = authorfeesumService.listByIds(Arrays.asList(ids.split(",")));
			String yearperiod = feeSumList.get(0).getYearperiod();//??????
			String month = feeSumList.get(0).getMonthid().toString();//??????
			//??????????????????
			Map<String, String> heads = new HashMap<>();
			heads.put("yearperiod", yearperiod);
			heads.put("month", month);

			List<AuthorFeePreTaxSumExcelData> details = feeSumList.stream().map(e -> {
				AuthorFeePreTaxSumExcelData ed = new AuthorFeePreTaxSumExcelData();
				ed.setDeptName(e.getFeedeptname());
				ed.setSum(e.getNoneedtaxtotal().add(e.getNeedtaxtotal()));
				ed.setGfMoney(e.getContributionfee());
				ed.setWswbMoney(e.getExternalauditfee());
				ed.setDtgfMoney(e.getContributionfeenext());
				ed.setDtwswbMoney(e.getExternalauditfeenext());
				ed.setTaxMoney(e.getNeedtaxtotal());
				ed.setNoTaxMoney(e.getNoneedtaxtotal());
				return ed;
			}).collect(Collectors.toList());
			is = this.getClass().getClassLoader().getResourceAsStream("template/exportAuthorFeePreTaxSum.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("???????????????", response), AuthorFeePreTaxSumExcelData.class).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();
			sheet.setSheetName("???????????????");
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


	@ApiOperation(value = "????????????????????????", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????id(???????????????????????????)", name = "reportid", dataType = "Long", required = false),
			@ApiImplicitParam(value = "??????id", name = "yearid", dataType = "Long", required = false),
			@ApiImplicitParam(value = "????????????(?????????)", name = "feemonth", dataType = "String", required = false),
			@ApiImplicitParam(value = "??????", name = "authorname", dataType = "String", required = false),
			@ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getAuthorFeeCalTaxDetailList")
	public ResponseEntity<PageResult<AuthorFeeCalTaxDetailExcelData>> getAuthorFeeCalTaxDetailList(@RequestParam(name = "reportid", required = false) Long reportid,
	                                                                                               @RequestParam(name = "yearid", required = false) Long yearid,
	                                                                                               @RequestParam(name = "feemonth", required = false) String feemonth,
	                                                                                               @RequestParam(name = "authorname", required = false) String authorname,
	                                                                                               @RequestParam(defaultValue = "1") Integer page,
	                                                                                               @RequestParam(defaultValue = "20") Integer rows) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("reportid", reportid);
			params.put("yearid", yearid);
			params.put("feemonth", feemonth);
			params.put("authorname", authorname);
			PageResult<AuthorFeeCalTaxDetailExcelData> pageList = authorfeesumService.getAuthorFeeCalTaxDetailList(params, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}


	public static void main(String[] args) throws Exception {
//    	ImportHelper helper = new ImportHelper("D:/exceltemplate/importContributionFee.xlsx");
//    	helper.addProcessors(new ContributionFeeImportExcelPostProcessor());
//    	List<Sheet> sheets = helper.loadSheet();
//    	Map<String, Object> headMap = helper.doImport(sheets, ContributionFeeExcelHead.class);
//    	helper.getErrorFile();
//    	helper.end(false);
//    	System.out.println(headMap);

		//Map<String,Object> map = new HashMap<>();
		//System.out.println(map.containsKey("a"));
		//System.out.println("a|b|c|d".split("\\|").length);
		//URL url = ClassUtils.getDefaultClassLoader().getResource("fileTemp");
		//System.out.println(url);
		//String path = ClassUtils.getDefaultClassLoader().getResource("fileTemp").getPath();
		//System.out.println(path);
		//String fileExtension = EasyExcelUtil.getFileExtension("D:/exceltemplate/importContributionFee.xlsx");
		//int index = "D:/exceltemplate/importContributionFee.xlsx".lastIndexOf(".");
		//System.out.println("D:/exceltemplate/importContributionFee.xlsx".lastIndexOf("."));
		//System.out.println("D:/exceltemplate/importContributionFee.xlsx".substring(0,index));
		File file = new File("D:/exceltemplate/temp\\1625129252425_????????????.xlsx");
		// "D:/exceltemplate/temp\1625129252425_????????????.xlsx";
		System.out.println(file.exists());
	}


}
