package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.iamxiongx.util.message.exception.BusinessException;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.core.tools.HttpClientTool;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.BankCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.controller.extract.BudgetExtractController;
import com.jtyjy.finance.manager.controller.extract.pay.ExtractEmpCalDataDetail;
import com.jtyjy.finance.manager.controller.extract.pay.ExtractPayCommonData;
import com.jtyjy.finance.manager.easyexcel.*;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.utils.QRCodeUtil;
import com.jtyjy.finance.manager.vo.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author minzhq
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractsumService extends DefaultBaseService<BudgetExtractsumMapper, BudgetExtractsum> implements ImportBaseInterface {

	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractsumService.class);

	private static String EXTRACT_PAY_DM_TYPE = "EXTRACTPAY";

	private static String EXTRACT_CAL_DM_TYPE = "EXTRACTCAL";

	private static String EXTRACT_ALL_VERIFY = "EXTRACT_ALL_VERIFY";

	private static String ISDEDUCTION = "ISDEDUCTION";

	private static String QUIT_PAYUNIT = "QUIT_PAYUNIT";

	private static String OUTER_PAYUNIT = "OUTER_PAYUNIT";

	private static String OUTERTHRESHOLD = "OUTERTHRESHOLD";

	private static String OUTEREXTRATAX = "OUTEREXTRATAX";

	private static String EXTRACTVERIFY = "EXTRACTVERIFY";

	private static String QRCODE_PREFIX = "TC";
	Map<String,String> exists = new HashMap<>();
	@Autowired
	private BudgetExtractsumMapper budgetExtractsumMapper;
	@Autowired
	private IndividualEmployeeFilesService individualService;
	@Autowired
	private CommonService commonService;
	@Autowired
	private BudgetExtractCommissionApplicationService applicationService;
	@Autowired
	private BudgetReimbursementorderService reimbursementorderService;
	@Autowired
	private BudgetExtractsumService sumService;
	@Autowired
	private BudgetExtractCommissionApplicationBudgetDetailsService budgetDetailsService;
	@Autowired
	private BudgetExtractCommissionApplicationLogService applicationLogService;

	@Autowired
	private TabChangeLogMapper loggerMapper;

	@Autowired
	private BudgetUnitMapper unitMapper;

	@Autowired
	private BudgetYearPeriodMapper yearMapper;

	@Autowired
	private BudgetMonthPeriodMapper monthMapper;

	@Autowired
	private BudgetExtractOuterpersonMapper outPersonMapper;
	@Autowired
	private DistributedNumber distributedNumber;

	@Autowired
	private BudgetLendmoneyUselogMapper lendmoneyUselogMapper;

	@Autowired
	private BudgetExtractImportdetailMapper extractImportDetailMapper;

	@Autowired
	private BudgetExtractImportdetailService extractImportDetailService;

	@Autowired
	private BudgetExtractdetailMapper extractDetailMapper;

	@Autowired
	private BudgetExtractdetailService extractDetailService;

	@Autowired
	private BudgetExtractArrearsMapper extractArrearsMapper;

	@Autowired
	private BudgetExtractgrantlogMapper extractgrantlogMapper;

	@Autowired
	private BudgetExtractpayRuleMapper payRuleMapper;

	@Autowired
	private BudgetExtractquotaRuleMapper quotaRuleMapper;

	@Autowired
	private BudgetExtractquotaRuledetailMapper quotaRuleDetailMapper;

	@Autowired
	private MessageSender sender;

	@Autowired
	private TabDmMapper dmMapper;


	@Autowired
	private BudgetBillingUnitMapper billingUnitMapper;

	@Autowired
	private BudgetBillingUnitAccountMapper billingUnitAccountMapper;

	@Autowired
	private BudgetExtractpaydetailMapper payDetailMapper;

	@Autowired
	private BudgetExtractpaymentMapper paymentMapper;

	@Autowired
	private BudgetPaymoneyMapper paymoneyMapper;

	@Autowired
	private BudgetPaymoneycodeMapper codeMapper;

	@Autowired
	private BudgetExtractgrantlogMapper grantLogMapper;

	@Autowired
	private BudgetBankAccountMapper bankAccountMapper;

	@Autowired
	private BudgetLendmoneyMapper lendmoneyMapper;

	@Autowired
	private BudgetRepaymoneyMapper repaymoneyMapper;

	@Autowired
	private BudgetRepaymoneyDetailMapper repaymoneyDetailMapper;

	@Autowired
	private BudgetArrearsMapper arrearsMapper;

	@Autowired
	private BudgetLendandrepaymoneyMapper lendandrepaymoneyMapper;

	@Autowired
	private BudgetExtractFeePayDetailMapper extractFeePayDetailMapper;

	@Autowired
	private BudgetExtractQrcodeMapper extractQrcodeMapper;

	@Autowired
	private BudgetExtractSignLogMapper signLogMapper;

	@Autowired
	private BudgetExtractCommissionApplicationLogService logService;

	@Autowired
	private BudgetExtractCommissionApplicationMapper applicationMapper;

	@Autowired
	private BudgetExtractDelayApplicationMapper delayApplicationMapper;

	@Value("${extract.qrcode.url}")
	private String extract_qrcode_url;

	private static final String QRCODE_FORMAT = ".png";

	@Value("${file.shareDir}")
	private String fileShareDir;

	@Autowired
	private BankCache bankCache;

	@Autowired
	private UserCache userCache;

	@Autowired
	private BudgetExtractpaymentOuterUnitService outerUnitService;

	@Autowired
	private BudgetExtractPerPayDetailService perPayDetailService;

	@Autowired
	private BudgetExtractPersonalityPayDetailMapper personalityPayDetailMapper;

	@Autowired
	private BudgetExtractPersonalityPayDetailService personalityPayDetailService;

	@Autowired
	private BudgetExtractTaxHandleRecordService taxHandleRecordService;

	@Autowired
	private BudgetExtractTaxHandleRecordMapper taxHandleRecordMapper;

	@Autowired
	private IndividualEmployeeFilesMapper individualEmployeeFilesMapper;

	@Autowired
	private IndividualEmployeeTicketReceiptInfoMapper individualEmployeeTicketReceiptInfoMapper;

	@Autowired
	private BudgetExtractAccountTaskMapper accountTaskMapper;

	@Autowired
	private BudgetExtractAccountTaskService accountTaskService;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extractsum"));
	}

	public PageResult<ExtractDeductionDetailVO> getExtractDeductionReport(String empNo, Integer page, Integer rows) {

		//??????????????????
		List<Integer> lendTypeList = Stream.of(
				LendTypeEnum.OTHER_LEND.getType(),
				LendTypeEnum.PROJECT_PRE_CLAIM.getType(),
				LendTypeEnum.PERSON_BORROWING.getType(),
				LendTypeEnum.PROJECT_BORROWING.getType())
				.collect(Collectors.toList());
		Page<ExtractDeductionDetailVO> pageCond = new Page<ExtractDeductionDetailVO>(page, rows);
		List<ExtractDeductionDetailVO> list = budgetExtractsumMapper.getExtractDeductionReport(pageCond, empNo, lendTypeList);
		return PageResult.apply(pageCond.getTotal(), list);
	}

	/**
	 * ???????????????????????????
	 *
	 * @return
	 */
	public List<ExtractPeriodNavigateTreeVO> getExtractPeriodNavigateTree() {
		//??????????????????
		List<ExtractPeriodNavigateTreeVO> yearList = yearMapper.selectList(new QueryWrapper<BudgetYearPeriod>().orderByDesc("code"))
				.stream().map(e -> {
					ExtractPeriodNavigateTreeVO vo = new ExtractPeriodNavigateTreeVO();
					vo.setQuery(e.getId().toString());
					vo.setYearCode(e.getCode());
					vo.setParentId("0");
					vo.setText(e.getPeriod());
					vo.setLevel(1);
					vo.setCurYearFlag(e.getCurrentflag());
					return vo;
				}).collect(Collectors.toList());
		List<BudgetMonthPeriod> monthList = monthMapper.selectList(new QueryWrapper<BudgetMonthPeriod>().orderByAsc("orderno"));
		//??????????????????????????????
		QueryWrapper<BudgetExtractsum> wrapper = new QueryWrapper<>();
		wrapper.groupBy("extractmonth").select("extractmonth");
		List<Map<String, Object>> extractMonths = this.budgetExtractsumMapper.selectMaps(wrapper);
		//????????????????????????
		yearList.stream().forEach(year -> {
			List<ExtractPeriodNavigateTreeVO> details = monthList.stream().map(e -> {
				ExtractPeriodNavigateTreeVO detailvo = new ExtractPeriodNavigateTreeVO();
				detailvo.setQuery(year.getQuery().concat("-").concat(e.getCode().toString()));
				detailvo.setParentId(year.getQuery());
				detailvo.setText(e.getPeriod());
				detailvo.setLevel(2);
				detailvo.setCurYearFlag(year.isCurYearFlag());
				//??????????????????????????????
				createMonthExtractChild(extractMonths, detailvo, Integer.parseInt(year.getYearCode()), Integer.parseInt(e.getCode()));
				return detailvo;
			}).collect(Collectors.toList());
			year.setChildren(details);
		});

		return yearList;
	}

	/**
	 * ????????????????????????
	 *
	 * @param extractMonths
	 * @param monthMap
	 * @param monthCode
	 * @param yearCode
	 */
	private void createMonthExtractChild(List<Map<String, Object>> extractMonths, ExtractPeriodNavigateTreeVO monthMap, Integer yearCode, Integer monthCode) {
		String curExtractMonth = getExtractMonth(yearCode, monthCode);
		List<ExtractPeriodNavigateTreeVO> curExtractMonthList = extractMonths.stream().filter(e -> e.get("extractmonth").toString().startsWith(curExtractMonth)).map(e -> {
			ExtractPeriodNavigateTreeVO detailvo = new ExtractPeriodNavigateTreeVO();
			detailvo.setQuery(monthMap.getQuery() + "-" + e.get("extractmonth").toString());
			detailvo.setParentId(monthMap.getQuery());
			detailvo.setText(e.get("extractmonth").toString());
			detailvo.setLevel(3);
			return detailvo;
		}).collect(Collectors.toList());
		monthMap.setChildren(curExtractMonthList);
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param yearCode
	 * @param monthCode
	 * @return
	 */
	public static String getExtractMonth(Integer yearCode, Integer monthCode) {
		String year = "";
		String month = "";
		if (monthCode >= 6) {
			year = yearCode - 1 + "";
			month = monthCode >= 10 ? monthCode.toString() : "0" + monthCode;
		} else {
			year = yearCode + "";
			month = "0" + monthCode;
		}
		return year + month;
	}

	public PageResult<ExtractInfoVO> getExtractInfoList(Map<String, Object> params, Integer page, Integer rows) {
		/**
		 * query   ???-???-????????????
		 */
		if (params.containsKey("query")) {
			String query = params.get("query").toString();
			String[] queryArr = query.split("-");
			if (queryArr.length == 1) params.put("yearid", queryArr[0]);
			else if (queryArr.length == 2) {
				BudgetYearPeriod yearPeriod = yearMapper.selectById(Long.valueOf(queryArr[0]));
				String extractMonth = getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));
				params.put("queryKey", extractMonth);
			} else if (queryArr.length == 3) {
				params.put("queryKey", queryArr[2]);
			}
		}
		Page<ExtractInfoVO> pageCond = new Page<ExtractInfoVO>(page, rows);
		List<ExtractInfoVO> list = this.budgetExtractsumMapper.getExtractInfoList(pageCond, params);
		list.stream().forEach(e -> {
			e.setStatusName(ExtractStatusEnum.getValue(e.getStatus()));
		});
		return PageResult.apply(pageCond.getTotal(), list);
	}

	private BudgetYearPeriod getPeriodByName(String name) {
		BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", name));
		return yearPeriod;
	}

	private BudgetUnit getBudgetUnitByYearAndName(Long yearId, String unitname) {
		BudgetUnit budgetUnit = unitMapper.selectOne(new LambdaQueryWrapper<BudgetUnit>().eq(BudgetUnit::getName, unitname).eq(BudgetUnit::getYearid, yearId));
		return budgetUnit;
	}

	private void validateImportTableHead(Map<Integer, String> data) {
		//??????
		String year = data.get(1); //??????
		String extractMonth = data.get(5); //????????????
		String unitname = data.get(9); //????????????

		if (StringUtils.isBlank(year)) throw new RuntimeException("??????????????????");
		if (StringUtils.isBlank(extractMonth)) throw new RuntimeException("????????????????????????");
		if (StringUtils.isBlank(unitname)) throw new RuntimeException("????????????????????????");
		BudgetYearPeriod yearPeriod = getPeriodByName(year);
		if (Objects.isNull(yearPeriod)) throw new RuntimeException("?????????" + year + "????????????");
		BudgetUnit budgetUnit = getBudgetUnitByYearAndName(yearPeriod.getId(), unitname);
		if (Objects.isNull(budgetUnit)) throw new RuntimeException("?????????" + year + "???????????????????????????" + unitname + "???");

		if (StringUtils.isEmpty(extractMonth)) {
			throw new RuntimeException("????????????????????????!");
		} else if (!extractMonth.matches("^2\\d{3}(0[1-9]|1[0-2])\\d{2}$")) {
			throw new RuntimeException("????????????????????????????????????!");
		} else {
			String extractBatch = extractMonth.substring(0, 6);
			List<String> periodMonthList = getPeriodMonthList(Integer.valueOf(yearPeriod.getCode()));
			if (!periodMonthList.contains(extractBatch))
				throw new RuntimeException("??????????????????" + yearPeriod.getPeriod() + "????????????????????????????????????????????????" + yearPeriod.getCode() + "???");
		}

		/**
		 * ??????????????????????????????????????????????????????????????????????????????????????????
		 * ??????????????????????????????
		 */
//		String maxextractmonth = extractMonth.substring(0, 4) + "1299";
//		Integer count = this.budgetExtractsumMapper.selectCount(
//				new QueryWrapper<BudgetExtractsum>().eq("deleteflag", 0)
//						.gt("extractmonth", Integer.valueOf(extractMonth))
//						.le("extractmonth", Integer.valueOf(maxextractmonth))
//						.gt("status", ExtractStatusEnum.APPROVED.getType()));
//		if (count > 0) throw new RuntimeException("???????????????????????????????????????????????????????????????????????????");
		BudgetExtractTaxHandleRecord recordServiceOne = taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractMonth));
		if (recordServiceOne != null) {
			if(recordServiceOne.getIsCalComplete()||recordServiceOne.getIsSetExcessComplete()||recordServiceOne.getIsPersonalityComplete()){
				throw new BusinessException("?????????????????????????????????");
			}
		}
	}

	private List<String> getPeriodMonthList(Integer code) {
		List<String> monthList = new ArrayList<>();
		monthList.add((code - 1) + "06");
		monthList.add((code - 1) + "07");
		monthList.add((code - 1) + "08");
		monthList.add((code - 1) + "09");
		monthList.add((code - 1) + "10");
		monthList.add((code - 1) + "11");
		monthList.add((code - 1) + "12");
		monthList.add(code + "01");
		monthList.add((code) + "02");
		monthList.add((code) + "03");
		monthList.add((code) + "04");
		monthList.add((code) + "05");
		return monthList;
	}

	@Override
	public Object validate(Integer row, Map<Integer, String> data, String importType, Object head, Object... params) {
		if (BudgetExtractController.TCIMPORT.equals(importType)) {
			//???2???
			if (row == 1) {
				//???????????????
				exists.clear();
				//?????????????????????
				validateImportTableHead(data);
				//???5???
			} else if (row >= 4) {
				//??????????????????
				validateImportTableDetails(data);
			}
		} else if (BudgetExtractController.TCEXCESS.equals(importType)) {
			/**
			 * ??????????????????
			 */
			if (row > 0) {
				String extractBatch = params[0].toString();
				//validateImportExcessDetails(data);
			}
		} else if (BudgetExtractController.FEEPAY.equals(importType)) {
			/**
			 * ??????????????????
			 */
			if (row > 0) {
				String extractBatch = params[0].toString();
				List<BudgetExtractsum> extractSumList = (List<BudgetExtractsum>) params[1];
				List<Long> extractSumIds = extractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
				validateImportFeePayDetails(data, extractSumIds, extractBatch);
			}
		}
		return head;
	}

	private void validateImportFeePayDetails(Map<Integer, String> data, List<Long> extractSumIds, String extractBatch) {
		String empNo = data.get(0);
		String empName = data.get(1);
		String feeStr = data.get(2);
		if (StringUtils.isBlank(empNo)) throw new RuntimeException("??????????????????");
		if (StringUtils.isBlank(empName)) throw new RuntimeException("??????????????????");
		if (StringUtils.isNotBlank(feeStr)) {
			BigDecimal fee = BigDecimal.ZERO;
			try {
				fee = new BigDecimal(feeStr);
			} catch (Exception e) {
				throw new RuntimeException("????????????????????????");
			}
			List<BudgetExtractFeePayDetailBeforeCal> budgetExtractFeePayDetailBeforeCals = extractFeePayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>().eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractBatch).eq(BudgetExtractFeePayDetailBeforeCal::getEmpNo, empNo));
			List<BudgetExtractdetail> extractdetails = new ArrayList<>();
			if (!CollectionUtils.isEmpty(extractSumIds)) {
				extractdetails = extractDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractdetail>().in(BudgetExtractdetail::getExtractsumid, extractSumIds).eq(BudgetExtractdetail::getEmpno, empNo));
			}
			if (CollectionUtils.isEmpty(extractdetails)) return;
			BigDecimal extract = extractdetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal feePay = budgetExtractFeePayDetailBeforeCals.stream().map(BudgetExtractFeePayDetailBeforeCal::getFeePay).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (extract.subtract(fee).subtract(feePay).compareTo(BigDecimal.ZERO) < 0)
				throw new RuntimeException(empName + "(" + empNo + ")?????????????????????????????????????????????????????????");
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @param data
	 */
//	private void validateImportExcessDetails(Map<Integer, String> data) {
//		String idNumber = data.get(0);
//		String isCompanyEmp = data.get(1);
//		String empNo = data.get(2);
//		String empName = data.get(3);
//		String billingUnitName = data.get(4);
//		String excessMoney = data.get(5);
//		String fee = data.get(6);
//		String avoidTaxMoney = data.get(7);
//		if (StringUtils.isBlank(idNumber)) throw new RuntimeException("????????????????????????");
//		if (StringUtils.isBlank(empNo)) throw new RuntimeException("??????????????????");
//		if (StringUtils.isBlank(empName)) throw new RuntimeException("??????????????????");
//
//		if (StringUtils.isBlank(fee) && StringUtils.isBlank(avoidTaxMoney))
//			throw new RuntimeException("???????????????????????????????????????????????????");
//
//		if (StringUtils.isNotBlank(fee)) {
//			try {
//				new BigDecimal(fee);
//			} catch (Exception e) {
//				throw new RuntimeException("??????????????????????????????");
//			}
//		}
//		if (StringUtils.isNotBlank(avoidTaxMoney)) {
//			try {
//				new BigDecimal(avoidTaxMoney);
//			} catch (Exception e) {
//				throw new RuntimeException("????????????????????????");
//			}
//		}
//	}
	private WbUser getUserByEmpno(String empNo) {
		//WbUser user = userMapper.selectOne(new LambdaQueryWrapper<WbUser>().eq(WbUser::getUserName, empNo));
		return userCache.getUserByEmpNo(empNo);
		//return user;
	}

	private BudgetExtractOuterperson getExtractOuterpersonByEmpnoAndEmpname(String empNo, String empName) {
		return outPersonMapper.selectOne(new LambdaQueryWrapper<BudgetExtractOuterperson>()
				.eq(BudgetExtractOuterperson::getEmpno, empNo).eq(BudgetExtractOuterperson::getName, empName));
	}

	private void validateImportTableDetails(Map<Integer, String> data) {
		//????????????
		String businessType = data.get(0); //????????????
		String empNo = data.get(1); //??????
		String empName = data.get(2); //??????
		String isDebt = data.get(3); //????????????   index = 3,????????????4???

		String extractType = data.get(4);//????????????
		String tcPeriod = data.get(5); //???????????? ???6???
		if (exists.get(businessType+empNo)==null) {
			exists.put(businessType+empNo,empName);
		}else if(exists.get(businessType+empNo).equals(empName)){
			throw new RuntimeException(businessType+","+empNo+","+empName+"??????????????????");
		}

//		String sftc = data.get(42);//???????????? ?????????????????????
		String sftc = data.get(43);//????????????
		String zhs = data.get(21); //?????????

		if (StringUtils.isBlank(businessType)) {
			throw new RuntimeException("??????????????????????????????!");
		}
		ExtractUserTypeEnum enumByValue = ExtractUserTypeEnum.getEnumByValue(businessType);
		if(enumByValue==null){
			throw new RuntimeException("???????????? ??????????????????????????????????????????????????????!");
		}
		switch (enumByValue){
			case COMPANY_STAFF:
				WbUser user = getUserByEmpno(empNo);
				if (user == null) {
					throw new RuntimeException("?????????" + empNo + "????????????!");
				} else {
					if (!empName.equals(user.getDisplayName())) {
						throw new RuntimeException("????????????????????????!??????????????????" + user.getDisplayName() + "???");
					}
				}
				break;
			case EXTERNAL_STAFF:
				BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
				if (outerPerson == null) throw new RuntimeException("???????????????" + empNo + "," + empName + "????????????!");
				if (outerPerson.getStopflag()) throw new RuntimeException("???????????????" + empNo + "," + empName + "???????????????!");
				break;
			case SELF_EMPLOYED_EMPLOYEES:
				user = getUserByEmpno(empNo);
				if (user == null) {
					throw new RuntimeException("?????????" + empNo + "????????????!");
				} else {
					if (!empName.equals(user.getDisplayName())) {
						throw new RuntimeException("????????????????????????!??????????????????" + user.getDisplayName() + "???");
					}
				}
				List<IndividualEmployeeFiles> employeeFilesList = individualService.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, empNo).list();
				if (CollectionUtils.isEmpty(employeeFilesList)) throw new RuntimeException("????????????" + empNo + "," + empName + "????????????!");
				if (employeeFilesList.stream().allMatch(x->x.getStatus()==2)) throw new RuntimeException("????????????" + empNo + "," + empName + "???????????????!");
				break;
				//??????????????????
			default:
				throw new RuntimeException("???????????? ??????????????????????????????????????????????????????!");
		}


		if (StringUtils.isBlank(empNo)) {
			throw new RuntimeException("??????????????????!");
		}
		if (StringUtils.isBlank(empName)) {
			throw new RuntimeException("??????????????????!");
		}

//		if (StringUtils.isBlank(sftc)) {
//			throw new RuntimeException("????????????????????????!");
//		} else {
//			BigDecimal tc = BigDecimal.ZERO;
//			try {
//				tc = new BigDecimal(sftc);
//			} catch (Exception e) {
//				throw new RuntimeException("???????????????????????????!");
//			}
//			if (tc.compareTo(BigDecimal.ZERO) != 0 && !NumberUtil.isInteger(trimZero(sftc))) {
//				throw new RuntimeException("????????????????????????????????????????????????????????????!");
//			}
//			if (tc.compareTo(BigDecimal.ZERO) < 0) {
//				throw new RuntimeException("????????????????????????0!");
//			}
//		}
//		if (StringUtils.isBlank(zhs)) {
//			throw new RuntimeException("?????????????????????!");
//		} else {
//			try {
//				new BigDecimal(zhs);
//			} catch (Exception e) {
//				throw new RuntimeException("????????????????????????!");
//			}
//		}
		if (StringUtils.isBlank(tcPeriod)) throw new RuntimeException("??????????????????????????????!");
		BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
		if (null == yearPeriod) throw new RuntimeException("?????????????????????" + tcPeriod + "????????????!");

		if (StringUtils.isBlank(isDebt)) throw new RuntimeException("????????????????????????!");
		if (!("???".equals(isDebt) || "???".equals(isDebt))) throw new RuntimeException("???????????????????????????!");

		//2021-12?????????
		if (StringUtils.isBlank(extractType)) throw new RuntimeException("????????????????????????!");
		if (ExtractTypeEnum.getEnumeByvalue(extractType) == null)
			throw new RuntimeException("?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
	}


	@Override
	public void saveData(Map<Integer, Map<Integer, String>> successMap, String importType
			, Map<Integer, Map<Integer, String>> errorMap, List<String> headErrorMsg, Object head, List<Object> details, Object... params) {
		if (BudgetExtractController.TCIMPORT.equals(importType)) {
			List<Map<Integer, Map<Integer, String>>> errorDatas = new ArrayList<>();
			BudgetExtractsum extractsum = null;

			if (!errorMap.isEmpty()) return;

			//??????????????????--????????????????????????
			Map<Integer, String> headMap = successMap.get(1);
			try {
				extractsum =  saveExtractSum(headMap);
			} catch (Exception e) {
				e.printStackTrace();
				headErrorMsg.add(e.getMessage());
				//???????????????????????????return
				return;
			}
			if (Objects.nonNull(extractsum)) {
				/**
				 * ??????????????????
				 */
				List<Integer> errorKeyList = new ArrayList<>();
				String badDebt = "??????";
				for (int i = 4; i <= successMap.size(); i++) {
					Map<Integer, String> detailMap = successMap.get(i);
					if (detailMap == null) continue;
					try {
						//???5?????????4???
						//@ApiModelProperty(value = "???????????? 0???1???")
						//"???".equals(isDebt) ? true : false
//						badDebt = successMap.get(3).get(6).equals("???")?"??????":"??????";
						badDebt = successMap.get(4).get(3).equals("???")?"??????":"??????";
						//??????????????????
						applicationService.saveExtractImportDetails(detailMap, extractsum);
//						saveExtractImportDetails(detailMap, extractsum);
					} catch (Exception e) {
						e.printStackTrace();
						detailMap.put(detailMap.size(), e.getMessage());
						errorMap.put(i, detailMap);
						errorKeyList.add(i);
					}
				}
				//???????????????????????????
				//??????+????????????+????????????+????????????+?????????/?????????
				applicationService.saveEntity(extractsum,badDebt,params);


				errorKeyList.stream().forEach(e -> successMap.remove(e));
				//????????????
				Integer personNum = this.extractImportDetailMapper.selectCount(new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid, extractsum.getId()));
				extractsum.setExtractnum(personNum);
				this.budgetExtractsumMapper.updateById(extractsum);
			}
		} else if (BudgetExtractController.TCEXCESS.equals(importType)) {
//			String curExtractBatch = params[0].toString();
//			List<BudgetExtractsum> sums = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", curExtractBatch).eq("deleteflag", 0));
//			if (sums.isEmpty()) refturn;
//			List<Long> sumIds = sums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
//			List<BudgetExtractdetail> extractDetailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", sumIds).eq("deleteflag", 0));
//			//key ???????????????
//			Map<String, BudgetExtractpaydetail> payDetailMap = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", curExtractBatch)).stream().collect(Collectors.toMap(BudgetExtractpaydetail::getIdnumber, e -> e, (e1, e2) -> e1));
//			//??????????????????
//			for (int i = 1; i <= successMap.size(); i++) {
//				Map<Integer, String> data = successMap.get(i);
//				try {
//					setExcessPay(data, extractDetailList, payDetailMap);
//				} catch (Exception e) {
//					e.printStackTrace();
//					data.put(data.size(), e.getMessage());
//					errorMap.put(i, data);
//				}
//			}
			//generatePaymoneyOrderIfExcessHandleOver(curExtractBatch, sums, 2, null);
		} else if (BudgetExtractController.FEEPAY.equals(importType)) {

			if (!errorMap.isEmpty()) return;
			/**
			 * ??????????????????
			 */
			String curExtractBatch = params[0].toString();
			for (int i = 1; i <= successMap.size(); i++) {
				Map<Integer, String> data = successMap.get(i);
				try {
					setFeePay(data, curExtractBatch);
				} catch (Exception e) {
					e.printStackTrace();
					data.put(data.size(), e.getMessage());
					errorMap.put(i, data);
				}
			}
		}
	}

	private void setFeePay(Map<Integer, String> data, String curExtractBatch) {
		BudgetExtractFeePayDetailBeforeCal detail = new BudgetExtractFeePayDetailBeforeCal();
		String empNo = data.get(0);
		String empName = data.get(1);
		String feeStr = data.get(2);
		detail.setEmpNo(empNo);
		detail.setEmpName(empName);
		detail.setCreateTime(new Date());
		String empno = LoginThreadLocal.get().getEmpno();
		String empname = LoginThreadLocal.get().getEmpname();
		detail.setCreatorName(empno + "(" + empname + ")");
		detail.setFeePay(StringUtils.isBlank(feeStr) ? BigDecimal.ZERO : new BigDecimal(feeStr));
		detail.setExtractMonth(curExtractBatch);
		extractFeePayDetailMapper.insert(detail);
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param sums
	 * @param type              1.??????  2 ??????
	 * @param extractDetailList
	 * @param payDetailMap
	 * @param paymentMap
	 */
	private void generatePaymoneyOrderIfExcessHandleOver(String curExtractBatch, List<BudgetExtractsum> sums, int type, String empno) {
		List<Long> sumIds = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", curExtractBatch).eq("deleteflag", 0)).stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		if (sumIds.isEmpty()) return;
		List<BudgetExtractdetail> extractDetailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", sumIds).eq("deleteflag", 0));

		long count1 = extractDetailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0).count();
		long count2 = extractDetailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0 && e.getHandleflag()).count();
		if (count1 == count2) {
			//??????????????????
			QueryWrapper<BudgetExtractpaydetail> wrapper = new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", curExtractBatch);
			if (StringUtils.isNotBlank(empno)) wrapper.eq("empno", empno);
			List<BudgetExtractpaydetail> payDetailList = this.payDetailMapper.selectList(wrapper);
			Map<String, BudgetExtractpaydetail> payDetailMap = payDetailList.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getIdnumber, e -> e, (e1, e2) -> e1));
			Map<Long, BudgetExtractpayment> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", payDetailList.stream().map(e -> e.getId()).collect(Collectors.toList()))).stream().collect(Collectors.toMap(BudgetExtractpayment::getBudgetextractpaydetailid, e -> e, (e1, e2) -> e1));
			List<BudgetPaymoney> paymoneyList = new ArrayList<>();
			extractDetailList.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getIdnumber)).forEach((idnumber, curExtractDetails) -> {
				//????????????????????????
				BudgetExtractpaydetail extractpaydetail = payDetailMap.get(idnumber);
				if (extractpaydetail == null) return;
				if (StringUtils.isNotBlank(empno)) {
					if (!empno.equals(extractpaydetail.getEmpno())) return;
				}
				BudgetExtractpayment extractpayment = paymentMap.get(extractpaydetail.getId());
				String code = this.budgetExtractsumMapper.selectById(curExtractDetails.get(0).getExtractsumid()).getCode();
				//???????????????
				if (extractpayment.getPaymoney1() != null && extractpayment.getPaymoney1().compareTo(BigDecimal.ZERO) > 0) {
					BudgetPaymoney paymoney = new BudgetPaymoney();
					paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
					paymoney.setPaymoneyobjectcode(code);
					paymoney.setPaymoneyobjectid(extractpayment.getId());
					paymoney.setPaymoney(NumberUtil.subZeroAndDot(extractpayment.getPaymoney1()));
					paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
					paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
					paymoney.setCreatetime(new Date());
					paymoney.setBunitname(extractpayment.getBunitname1());
					paymoney.setBunitbankaccount(extractpayment.getBunitbankaccount1());
					paymoney.setBunitaccountbranchcode(extractpayment.getBunitaccountbranchcode1());
					paymoney.setBunitaccountbranchname(extractpayment.getBunitaccountbranchname1());
					paymoney.setBankaccountname(extractpayment.getBankaccountname());
					paymoney.setBankaccount(extractpayment.getBankaccount());
					paymoney.setBankaccountbranchcode(extractpayment.getBankaccountbranchcode());
					paymoney.setBankaccountbranchname(extractpayment.getBankaccountbranchname());
					paymoney.setOpenbank(extractpayment.getBankaccountopenbank());
					paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
					paymoneyList.add(paymoney);
				}
				if (extractpayment.getPayfee() != null && extractpayment.getPayfee().compareTo(BigDecimal.ZERO) > 0) {
					BudgetPaymoney paymoney = new BudgetPaymoney();
					paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
					paymoney.setPaymoneyobjectcode(code);
					paymoney.setPaymoneyobjectid(extractpayment.getId());
					paymoney.setPaymoney(NumberUtil.subZeroAndDot(extractpayment.getPayfee()));
					paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
					paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
					paymoney.setCreatetime(new Date());
					paymoney.setBunitname(extractpayment.getBunitname1());
					paymoney.setBunitbankaccount(extractpayment.getBunitbankaccount1());
					paymoney.setBunitaccountbranchcode(extractpayment.getBunitaccountbranchcode1());
					paymoney.setBunitaccountbranchname(extractpayment.getBunitaccountbranchname1());
					paymoney.setBankaccountname(extractpayment.getBankaccountname());
					paymoney.setBankaccount(extractpayment.getBankaccount());
					paymoney.setBankaccountbranchcode(extractpayment.getBankaccountbranchcode());
					paymoney.setBankaccountbranchname(extractpayment.getBankaccountbranchname());
					paymoney.setOpenbank(extractpayment.getBankaccountopenbank());
					paymoney.setRemark("??????????????????????????????");
					paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
					paymoneyList.add(paymoney);
				}
				if (extractpayment.getPaymoney2() != null && extractpayment.getPaymoney2().compareTo(BigDecimal.ZERO) > 0) {
					//??????????????????
					BudgetPaymoney paymoney = new BudgetPaymoney();
					paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
					;
					paymoney.setPaymoneyobjectcode(code);
					paymoney.setPaymoneyobjectid(extractpayment.getId());
					paymoney.setPaymoney(NumberUtil.subZeroAndDot(extractpayment.getPaymoney2()));
					paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
					paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
					paymoney.setCreatetime(new Date());
					paymoney.setBunitname(extractpayment.getBunitname2());
					paymoney.setBunitbankaccount(extractpayment.getBunitbankaccount2());
					paymoney.setBunitaccountbranchcode(extractpayment.getBunitaccountbranchcode2());
					paymoney.setBunitaccountbranchname(extractpayment.getBunitaccountbranchname2());
					paymoney.setBankaccountname(extractpayment.getBankaccountname());
					paymoney.setBankaccount(extractpayment.getBankaccount());
					paymoney.setBankaccountbranchcode(extractpayment.getBankaccountbranchcode());
					paymoney.setBankaccountbranchname(extractpayment.getBankaccountbranchname());
					paymoney.setOpenbank(extractpayment.getBankaccountopenbank());
					paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
					paymoneyList.add(paymoney);
				}

			});
			if (!paymoneyList.isEmpty()) paymoneyService.saveBatch(paymoneyList);

			if (StringUtils.isBlank(empno)) {
				String extractImportors = sums.stream().map(e -> e.getCreator()).distinct().collect(Collectors.joining("|"));
				try {
					sender.sendQywxMsgSyn(new QywxTextMsg(extractImportors, null, null, 0, "???????????????" + curExtractBatch + "???????????????,??????????????????!", null));
					if (type == 1) {
						TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
						if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
							sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "????????????????????????????????????!", null));
					} else if (type == 2) {
						TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
						if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
							sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "???????????????????????????!!", null));
					}

				} catch (Exception e) {
				}
			}

		} else {
			if (StringUtils.isBlank(empno)) {
				try {
					TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
					if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
						sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "????????????,???????????????????????????????????????!", null));
				} catch (Exception e) {
				}
			}

		}

	}

	@Autowired
	private BudgetPaymoneyService paymoneyService;

	/**
	 * ??????????????????
	 *
	 * @param data
	 * @param extractDetailList
	 * @param payDetailMap
	 * @param paymentList
	 */
	private void setExcessPay(List<ExtractExcessExcelData> dataList, List<BudgetExtractdetail> extractDetailList, Map<String, BudgetExtractpaydetail> payDetailMap) {
		ExtractExcessExcelData data = dataList.get(0);
		String idNumber = data.getIdNumber();
		String isCompanyEmp = data.getIsCompanyEmp();
		String empNo = data.getEmpNo();
		String empName = data.getEmpName();
		String billingUnitName = data.getBillingUnitName();
		BigDecimal excessMoney = data.getExcessMoney();
		BigDecimal avoidTaxMoney = dataList.stream().map(ExtractExcessExcelData::getAvoidTaxMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		String outUnit = data.getOutUnit();
		BigDecimal outUnitPayMoney = dataList.stream().map(ExtractExcessExcelData::getOutUnitPayMoney).reduce(BigDecimal.ZERO, BigDecimal::add);

		List<BudgetExtractdetail> curExtractDetails = extractDetailList.stream().filter(e -> e.getIdnumber().equals(idNumber)).collect(Collectors.toList());
		if (curExtractDetails.isEmpty()) return;
		BudgetExtractdetail extractdetail = curExtractDetails.get(0);
		if (extractdetail.getHandleflag() || extractdetail.getExcessmoney() == null || extractdetail.getExcessmoney().compareTo(BigDecimal.ZERO) <= 0)
			return;
		//???????????????
		BigDecimal avoidtaxpay = avoidTaxMoney;
		BudgetExtractpaydetail extractpaydetail = payDetailMap.get(idNumber);
		if (Objects.isNull(extractpaydetail)) return;

		extractpaydetail.setTotalaviodtax(extractpaydetail.getAviodtax().add(avoidtaxpay));
		extractpaydetail.setIncorporatedcompanyfee(BigDecimal.ZERO);
		this.payDetailMapper.updateById(extractpaydetail);

		BudgetExtractpayment extractpayment = this.paymentMapper.selectOne(new QueryWrapper<BudgetExtractpayment>().eq("budgetextractpaydetailid", extractpaydetail.getId()));
		extractpayment.setPayfee(BigDecimal.ZERO);
		extractpayment.setPaymoney2(extractpayment.getPaymoney2().add(avoidtaxpay));
		this.paymentMapper.updateById(extractpayment);
		if (avoidtaxpay.compareTo(BigDecimal.ZERO) > 0) {
			BudgetExtractgrantlog log = new BudgetExtractgrantlog();
			log.setIdnumber(idNumber);
			log.setExtractmonth(extractpaydetail.getExtractmonth());
			log.setIscompanyemp(extractdetail.getIscompanyemp());
			log.setEmpno(extractdetail.getEmpno());
			log.setExcessmoney(BigDecimal.ZERO);
			log.setAlreadygrantmoney(extractpaydetail.getIncorporatedcompanylj());
			log.setEmpname(extractdetail.getEmpname());
			log.setBillingunitid(extractpayment.getBunitid2());
			log.setBillingunitname(extractpayment.getBunitname2());
			log.setShouldgrantextract(avoidtaxpay);
			log.setCouldgrantextract(avoidtaxpay);
			log.setOrderno(999);
			log.setCratetime(new Date());
			log.setExcessgrantflag(true);
			this.extractgrantlogMapper.insert(log);
		}
		if (outUnitPayMoney.compareTo(BigDecimal.ZERO) > 0) {
			List<BudgetExtractpaymentOuterUnit> outUnitList = new ArrayList<>();
			dataList.stream().filter(e -> StringUtils.isNotBlank(e.getOutUnit()) && e.getOutUnitPayMoney().compareTo(BigDecimal.ZERO) > 0).forEach(e -> {
				BudgetBillingUnit budgetBillingUnit = this.billingUnitMapper.selectOne(new LambdaQueryWrapper<BudgetBillingUnit>().eq(BudgetBillingUnit::getName, e.getOutUnit()).eq(BudgetBillingUnit::getOwnFlag, 1));
				BudgetExtractgrantlog log = new BudgetExtractgrantlog();
				log.setIdnumber(idNumber);
				log.setExtractmonth(extractpaydetail.getExtractmonth());
				log.setIscompanyemp(extractdetail.getIscompanyemp());
				log.setEmpno(extractdetail.getEmpno());
				log.setExcessmoney(BigDecimal.ZERO);
				log.setAlreadygrantmoney(extractpaydetail.getIncorporatedcompanylj());
				log.setEmpname(extractdetail.getEmpname());
				log.setBillingunitid(budgetBillingUnit.getId());
				log.setBillingunitname(budgetBillingUnit.getName());
				log.setShouldgrantextract(e.getOutUnitPayMoney());
				log.setCouldgrantextract(e.getOutUnitPayMoney());
				log.setOrderno(999);
				log.setCratetime(new Date());
				log.setExcessgrantflag(true);
				this.extractgrantlogMapper.insert(log);

				BudgetExtractpaymentOuterUnit budgetExtractpaymentOuterUnit = new BudgetExtractpaymentOuterUnit();
				budgetExtractpaymentOuterUnit.setExtractMonth(extractpaydetail.getExtractmonth());
				budgetExtractpaymentOuterUnit.setExtractPaymentId(extractpayment.getId());
				budgetExtractpaymentOuterUnit.setBillingUnitId(budgetBillingUnit.getId());
				budgetExtractpaymentOuterUnit.setBillingUnitName(budgetBillingUnit.getName());
				List<BudgetBillingUnitAccount> billingUnitAccounts = this.billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().eq(BudgetBillingUnitAccount::getBillingunitid, budgetBillingUnit.getId()).eq(BudgetBillingUnitAccount::getStopflag, 0).orderByDesc(BudgetBillingUnitAccount::getOrderno));
				BudgetBillingUnitAccount budgetBillingUnitAccount = billingUnitAccounts.get(0);
				budgetExtractpaymentOuterUnit.setUnitBankAccount(budgetBillingUnitAccount.getBankaccount());
				budgetExtractpaymentOuterUnit.setBranchcode(budgetBillingUnitAccount.getBranchcode());

				WbBanks bank = bankCache.getBankByBranchCode(budgetBillingUnitAccount.getBranchcode());
				budgetExtractpaymentOuterUnit.setBankName(bank.getBankName());
				budgetExtractpaymentOuterUnit.setOpenBank(bank.getSubBranchName());
				budgetExtractpaymentOuterUnit.setPayMoney(e.getOutUnitPayMoney());
				outUnitList.add(budgetExtractpaymentOuterUnit);
			});
			if (!CollectionUtils.isEmpty(outUnitList)) outerUnitService.saveBatch(outUnitList);
		}

		for (BudgetExtractdetail bbed : curExtractDetails) {
			bbed.setHandleflag(true);
		}
		this.extractDetailService.updateBatchById(curExtractDetails);
	}

	private void saveExtractImportDetails(Map<Integer, String> data, BudgetExtractsum extractsum) {
		String isCompanyEmp = data.get(0); //??????????????????
		//????????????????????????


		String empNo = data.get(1); //??????
		String empName = data.get(2); //??????
		String sftc = data.get(3); //????????????
		String zhs = data.get(4); //?????????
		String tcPeriod = data.get(5); //????????????
		String isDebt = data.get(6); //????????????

		String extractType = data.get(7);//????????????
		String shouldSendExtract = data.get(8);//????????????
		String tax = data.get(9);//??????
		String taxReduction = data.get(10);//????????????
		String invoiceExcessTax = data.get(11);//??????????????????
		String invoiceExcessTaxReduction = data.get(12);//????????????????????????


		BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
		//??????????????????
		BudgetExtractImportdetail extractImportdetail = null;
		if (Objects.nonNull(extractImportdetail)) {
			extractImportdetail.setConsotax(new BigDecimal(zhs)); // ?????????
			extractImportdetail.setCopeextract(new BigDecimal(sftc));// ????????????
			extractImportdetail.setUpdatetime(new Date());
			extractImportDetailMapper.updateById(extractImportdetail);
		} else {
			extractImportdetail = new BudgetExtractImportdetail();
			extractImportdetail.setId(null);
			extractImportdetail.setExtractsumid(extractsum.getId());
			//?????? ????????????
			setUserTypeValue(isCompanyEmp, empNo, empName, extractImportdetail);

//			if ("???".equals(isCompanyEmp)) {
//				WbUser user = getUserByEmpno(empNo);
//				extractImportdetail.setEmpid(user.getUserId());
//				extractImportdetail.setIdnumber(user.getIdNumber());
//			} else {
//				BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
//				extractImportdetail.setEmpid(outerPerson.getId().toString());
//				extractImportdetail.setIdnumber(outerPerson.getIdnumber());
//			}



			extractImportdetail.setEmpno(empNo);
			extractImportdetail.setEmpname(empName);
			extractImportdetail.setConsotax(new BigDecimal(zhs)); // ?????????
			extractImportdetail.setCopeextract(new BigDecimal(sftc));// ????????????
			extractImportdetail.setCreatetime(new Date());
			extractImportdetail.setUpdatetime(extractImportdetail.getCreatetime());
			extractImportdetail.setIscompanyemp("???".equals(isCompanyEmp) ? true : false);
			extractImportdetail.setYearid(yearPeriod.getId());
			extractImportdetail.setIsbaddebt("???".equals(isDebt) ? true : false);
			extractImportdetail.setExtractType(extractType);
			extractImportdetail.setShouldSendExtract(new BigDecimal(shouldSendExtract));
			extractImportdetail.setTax(StringUtils.isBlank(tax) ? BigDecimal.ZERO : new BigDecimal(tax));
			extractImportdetail.setTaxReduction(StringUtils.isBlank(taxReduction) ? BigDecimal.ZERO : new BigDecimal(taxReduction));
			extractImportdetail.setInvoiceExcessTax(StringUtils.isBlank(invoiceExcessTax) ? BigDecimal.ZERO : new BigDecimal(invoiceExcessTax));
			extractImportdetail.setInvoiceExcessTaxReduction(StringUtils.isBlank(invoiceExcessTaxReduction) ? BigDecimal.ZERO : new BigDecimal(invoiceExcessTaxReduction));
			extractImportDetailMapper.insert(extractImportdetail);
		}

	}

	private void setUserTypeValue(String isCompanyEmp, String empNo, String empName, BudgetExtractImportdetail extractImportdetail) {
		switch (ExtractUserTypeEnum.valueOf(isCompanyEmp)) {
			case COMPANY_STAFF:
				WbUser user = getUserByEmpno(empNo);
				extractImportdetail.setEmpid(user.getUserId());
				extractImportdetail.setIdnumber(user.getIdNumber());
				extractImportdetail.setBusinessType(ExtractUserTypeEnum.COMPANY_STAFF.getCode());
				break;
			case EXTERNAL_STAFF:
				BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
				extractImportdetail.setEmpid(outerPerson.getId().toString());
				extractImportdetail.setIdnumber(outerPerson.getIdnumber());
				extractImportdetail.setBusinessType(ExtractUserTypeEnum.EXTERNAL_STAFF.getCode());
				break;
			case SELF_EMPLOYED_EMPLOYEES:
				//todo ?????????
				WbUser user2 = getUserByEmpno(empNo);
				extractImportdetail.setEmpid(user2.getUserId());
				extractImportdetail.setIdnumber(user2.getIdNumber());
				extractImportdetail.setBusinessType(ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode());
//				extractImportdetail.setIndividualEmployeeId(employeeFiles.getId());
			break;
			default:
				break;
		}
	}

	private BudgetExtractsum saveExtractSum(Map<Integer, String> data) {

		String year = data.get(1); //??????   ???2???
		String extractMonth = data.get(5); //????????????   ???6???
		String unitname = data.get(9); //????????????  ???10???
		BudgetYearPeriod yearPeriod = getPeriodByName(year);
		BudgetUnit unit = getBudgetUnitByYearAndName(yearPeriod.getId(), unitname);
		BudgetExtractsum bes = new BudgetExtractsum();
		Date currentDate = new Date();
		bes.setStatus(0);
		bes.setDeptid(unit.getId().toString());
		bes.setYearid(yearPeriod.getId());
		bes.setDeptname(unit.getName());
		bes.setExtractmonth(extractMonth);
		bes.setReimbursementflag(0);
		if (extractMonth.substring(0, 6).endsWith("01")) {
			bes.setSalarymonth((Integer.valueOf(extractMonth.substring(0, 4)) - 1) + "12");
		} else {
			bes.setSalarymonth((Integer.valueOf(extractMonth.substring(0, 6)).intValue() - 1) + "");
		}
		bes.setExtractnum(0);
		bes.setCreatetime(currentDate);
		bes.setUpdatetime(currentDate);
		bes.setCreator(UserThreadLocal.get().getUserName());
		bes.setCreateorname(UserThreadLocal.get().getDisplayName());
		bes.setCode(distributedNumber.getExtractNum());
		this.budgetExtractsumMapper.insert(bes);
		return bes;
	}

	/**
	 * ??????
	 * 1.????????????????????????????????????
	 *
	 * @param sumId
	 */
	@Transactional(rollbackFor = Exception.class)
	public void submit(String ids) {

		Optional.ofNullable(ids).orElseThrow(() -> new RuntimeException("???????????????"));
		List<BudgetExtractsum> budgetExtractsums = this.budgetExtractsumMapper.selectBatchIds(Arrays.asList(ids.split(",")));

		/**
		 * ????????????
		 * ?????????????????????????????????????????????????????????????????????
		 */
		extractImportDetailMapper.clearExtractDetail(ids);
		//??????????????????
		extractDetailMapper.delete(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", Arrays.asList(ids.split(","))));

		List<BudgetExtractdetail> bedList = new ArrayList<>();
		List<BudgetExtractImportdetail> allimportDetails = new ArrayList<>();
		budgetExtractsums.forEach(extractsum -> {
			//??????
			// 1?????????????????????????????????????????????????????????????????????????????????,?????????????????????????????????
			// 2???????????????????????????????????????????????????????????????????????????????????????????????????
			applicationService.validateApplication(extractsum);
			//??????????????????????????????????????????
			if (extractsum.getStatus()!=ExtractStatusEnum.DRAFT.getType()){
				throw new RuntimeException("????????????????????????????????????????????????" + extractsum.getCode() + "??????????????????!");
			}
			//??????
			combine(extractsum.getId(), allimportDetails);
			extractsum.setStatus(ExtractStatusEnum.VERIFYING.getType());

			//????????? ??????????????????
			Optional<BudgetExtractCommissionApplication> applicationOptional =
					applicationService.lambdaQuery().eq(BudgetExtractCommissionApplication::getExtractSumId, extractsum.getId()).last("limit 1").oneOpt();
			if (applicationOptional.isPresent()) {
				BudgetExtractCommissionApplication application = applicationOptional.get();
				//???????????????????????????????????????
				applicationService.generateReimbursement(application,extractsum);
				//????????????  1 ?????????
				application.setStatus(ExtractStatusEnum.VERIFYING.getType());
				application.setUpdateTime(new Date());
				application.setUpdateBy(UserThreadLocal.getEmpNo());
				//????????????

				applicationLogService.saveLog(application.getId(),OperationNodeEnum.SUBMITTED, LogStatusEnum.COMPLETE);
				//uploadOA ??????OA
				try {
					applicationService.uploadOA(application);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("??????OA????????????");
				}
				applicationService.updateById(application);
			}
		});
		if (!allimportDetails.isEmpty()) this.extractImportDetailService.updateBatchById(allimportDetails);
		this.updateBatchById(budgetExtractsums);
		budgetExtractsums.forEach(extractsum -> {
			try {
//				TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACTVERIFY).eq("dm", extractsum.getDeptname()));
//				if (dm != null && StringUtils.isNotBlank(dm.getDmValue())) {
//					sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + extractsum.getCode() + "?????????????????????????????????????????????????????????", null));
//				}
				//????????????
				//????????????????????????????????????	??????????????????????????????????????????
				String deptId = extractsum.getDeptid();
				BudgetUnit budgetUnit = unitMapper.selectById(deptId);
				String accounting = budgetUnit.getAccounting();
				//userId,???????????????????????? ??????
				String[] userIdSplit = accounting.split(",");
				List<String> userNos = new ArrayList<>();
				for (int i = 0; i < userIdSplit.length; i++) {
					String empNo = UserCache.getUserByUserId(userIdSplit[0]).getUserName();
					userNos.add(empNo);
				}
				String toUsers = "";
				if (this.isTest()) {
					toUsers = this.getTestNotice();
				}else{
					toUsers=String.join("|", userNos);
				}
				if (StringUtils.isNotBlank(toUsers)) {
					String yearName = yearMapper.getNameById(extractsum.getYearid());
					Integer month = Integer.valueOf(extractsum.getExtractmonth().substring(4, 6));
					Integer batchNo = Integer.valueOf(extractsum.getExtractmonth().substring(6));

					String codeMsg = MessageFormat.format("{0}???{1}??????{2}???{3}???????????????????????????????????????", yearName, month, batchNo, extractsum.getCode());
					sender.sendQywxMsgSyn(new QywxTextMsg(toUsers, null, null, 0, codeMsg, null));
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	private void combine(Long sumId, List<BudgetExtractImportdetail> allimportDetails) {
		//?????????????????????????????????
		List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper
				.selectList(new QueryWrapper<BudgetExtractImportdetail>().eq("extractsumid", sumId));
		if (!CollectionUtils.isEmpty(importDetails)) {
			importDetails.stream().collect(Collectors.groupingBy(x->x.getBusinessType()+"-"+x.getIdnumber())).forEach((typeIdnumber, importdetailByEmpnoList) -> {
				BudgetExtractdetail bed = new BudgetExtractdetail();
				bed.setExtractsumid(sumId);
				BudgetExtractImportdetail beid = importdetailByEmpnoList.get(0);
				bed.setEmpid(beid.getEmpid());
				bed.setEmpno(beid.getEmpno());
				bed.setIdnumber(beid.getIdnumber());
				bed.setEmpname(beid.getEmpname());
				BigDecimal copeextract = importdetailByEmpnoList.stream().map(e -> e.getCopeextract()).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal consotax = importdetailByEmpnoList.stream().map(e -> e.getConsotax()).reduce(BigDecimal.ZERO, BigDecimal::add);
				bed.setCopeextract(copeextract);
				bed.setConsotax(consotax);
				bed.setCreatetime(new Date());
				bed.setUpdatetime(bed.getCreatetime());
				bed.setDeleteflag(0);
				bed.setIscompanyemp(beid.getIscompanyemp());
				bed.setBusinessType(beid.getBusinessType());
				bed.setExcesstype(-1);
				bed.setExcessmoney(BigDecimal.ZERO);
				extractDetailMapper.insert(bed);
				importdetailByEmpnoList.stream().forEach(importdetail -> {
					importdetail.setExtractdetailid(bed.getId());
					importdetail.setUpdatetime(new Date());
				});
			});
			allimportDetails.addAll(importDetails);
		} else {
			throw new RuntimeException("??????????????????????????????????????????????????????");
		}
	}


	/**
	 * ??????
	 *
	 * @param sumId
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteExtractSum(Long sumId) {
		//???????????? ?????????????????????  ???????????? ??????????????????
		//????????????
		//1?????????????????????
		//2????????? ?????????????????????
		//3????????????????????????
		//4?????????????????????
		//5?????????????????????
		//6?????????????????????


		BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(sumId);
		if(extractsum==null){
			throw new RuntimeException("??????????????????!");
		}

		if (extractsum.getStatus() != ExtractStatusEnum.DRAFT.getType())
			throw new RuntimeException("????????????!??????????????????????????????????????????");
		Optional<BudgetExtractCommissionApplication> applicationOptional = applicationService.lambdaQuery().eq(BudgetExtractCommissionApplication::getExtractSumId, sumId).last("limit 1").oneOpt();
		if (!applicationOptional.isPresent()) {
			throw new RuntimeException("??????????????????");
		}
		BudgetExtractCommissionApplication application = applicationOptional.get();
		Long reimbursementId = application.getReimbursementId();
		BudgetReimbursementorder reimbursementorder = reimbursementorderService.getById(reimbursementId);
		if (reimbursementorder!=null) {
			Integer reuqeststatus = reimbursementorder.getReuqeststatus();
			//???????????????-1????????????0????????????1??????????????????????????????2???????????????
			if (reuqeststatus == ExtractStatusEnum.APPROVED.getType()) {
				throw new RuntimeException("??????????????????????????????????????????????????????");
			}
			//4??????????????????
			reimbursementorderService.removeById(reimbursementorder.getId());
		}
		//1???????????????
//		if (extractsum.getStatus() > ExtractStatusEnum.VERIFYING.getType())
//			throw new RuntimeException("????????????!????????????????????????");
		extractImportDetailMapper.delete(new QueryWrapper<BudgetExtractImportdetail>().eq("extractsumid", sumId));
		//2????????? ????????????  ????????????1
		extractDetailMapper.delete(new QueryWrapper<BudgetExtractdetail>().eq("extractsumid", sumId));
		//3???????????????
		budgetExtractsumMapper.deleteById(sumId);
			//0?????????-1 ?????? ?????? -2 ?????????
		//application.getStatus()== ExtractStatusEnum.RETURN.getType()
			if (application.getStatus()== ExtractStatusEnum.DRAFT.getType()) {
				//5??????????????????
				applicationService.removeById(application.getId());
				//6???????????????
				budgetDetailsService.remove(new QueryWrapper<BudgetExtractCommissionApplicationBudgetDetails>()
						.lambda().eq(BudgetExtractCommissionApplicationBudgetDetails::getApplicationId, application.getId()));
				//7???????????????
				applicationLogService.remove(new QueryWrapper<BudgetExtractCommissionApplicationLog>()
						.lambda().eq(BudgetExtractCommissionApplicationLog::getApplicationId, application.getId()));
				//????????????
			}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<ExtractImportDetailVO> getExtractImportDetails(Map<String, Object> params, Integer page, Integer rows) {
		Page<ExtractImportDetailVO> pageCond = new Page<ExtractImportDetailVO>(page, rows);
		List<ExtractImportDetailVO> list = this.extractImportDetailMapper.getExtractImportDetails(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), list);
	}

	/**
	 * ????????????????????????
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<BudgetExtractdetail> getExtractDetails(Map<String, Object> params, Integer page, Integer rows) {

		Long sumId = Long.valueOf(params.get("sumId").toString());
		BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(sumId);
		Page<BudgetExtractdetail> pageCond = new Page<BudgetExtractdetail>(page, rows);
		QueryWrapper<BudgetExtractdetail> wrapper = new QueryWrapper<BudgetExtractdetail>();
		wrapper.eq("extractsumid", sumId);
		wrapper.eq("deleteflag", 0);
		if (params.get("excesstype") != null) wrapper.eq("excesstype", params.get("excesstype"));
		if (params.get("iscompanyemp") != null) wrapper.eq("iscompanyemp", params.get("iscompanyemp"));
		if (params.get("handleflag") != null) wrapper.eq("handleflag", params.get("handleflag"));
		if (params.get("idnumber") != null && StringUtils.isNotBlank(params.get("idnumber").toString()))
			wrapper.like("idnumber", params.get("idnumber"));
		if (params.get("empno") != null && StringUtils.isNotBlank(params.get("empno").toString())) {
			wrapper.and(qw -> {
				qw.like("empno", params.get("empno"))
						.or().like("empname", params.get("empno"));
			});
		}
		pageCond = this.extractDetailMapper.selectPage(pageCond, wrapper);

		List<BudgetExtractdetail> records = pageCond.getRecords();
		records.forEach(e -> {
			e.setExtractmonth(extractsum.getExtractmonth());
		});
		long total = pageCond.getTotal();
		return PageResult.apply(total, records);
	}

	/**
	 * ????????????????????????
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<ExtractWithholdDetailVO> getExtractWithholdDetails(Map<String, Object> params, Integer page,
	                                                                     Integer rows) {
		Page<ExtractWithholdDetailVO> pageCond = new Page<ExtractWithholdDetailVO>(page, rows);
		List<ExtractWithholdDetailVO> list = this.budgetExtractsumMapper.getExtractWithholdDetails(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), list);
	}

	/**
	 * ????????????????????????
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<BudgetExtractgrantlog> getExtractGrantLogDetails(Map<String, Object> params, Integer page,
	                                                                   Integer rows) {
		Page<BudgetExtractgrantlog> pageCond = new Page<BudgetExtractgrantlog>(page, rows);
		QueryWrapper<BudgetExtractgrantlog> queryWrapper = new QueryWrapper<BudgetExtractgrantlog>().eq("extractmonth", params.get("extractmonth").toString());
		if (params.containsKey("iscompanyemp")) queryWrapper.eq("iscompanyemp", params.get("iscompanyemp"));
		if (params.containsKey("billingunitname")) queryWrapper.like("billingunitname", params.get("billingunitname"));
		if (params.containsKey("empno")) {
			queryWrapper.and(qw -> {
				qw.like("empno", params.get("empno"))
						.or().like("empname", params.get("empno"));
			});
		}
		pageCond = extractgrantlogMapper.selectPage(pageCond, queryWrapper);
		return PageResult.apply(pageCond.getTotal(), pageCond.getRecords());
	}

	/**
	 * ????????????????????????
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<ExtractPayDetailVO> getExtractPayDetails(Map<String, Object> params, Integer page, Integer rows) {


		if(params.get("sumId")==null){
			Page<ExtractPayDetailVO> pageCond = new Page<ExtractPayDetailVO>(page, rows);
			List<ExtractPayDetailVO> resultList = getPayDetailsByCondition(pageCond, params);
			resultList.forEach(e->{
				List<BudgetExtractdetail> budgetExtractdetails = extractDetailMapper.selectBatchIds(Arrays.asList(e.getExtractdetailids().split(",")));
				e.setRealextract(budgetExtractdetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO,BigDecimal::add));
				e.setRepaymoney(budgetExtractdetails.stream().filter(e1->e1.getWithholdmoney()!=null).map(BudgetExtractdetail::getWithholdmoney).reduce(BigDecimal.ZERO,BigDecimal::add));
				e.setConsotax(budgetExtractdetails.stream().map(BudgetExtractdetail::getConsotax).reduce(BigDecimal.ZERO,BigDecimal::add));
			});
			return PageResult.apply(pageCond.getTotal(), resultList);
		}else{
			List<ExtractPayDetailVO> resultList = getPayDetailsByCondition(null, params);
			Long sumId = (Long) params.get("sumId");
			resultList = resultList.stream().peek(e -> {
				List<BudgetExtractdetail> budgetExtractdetails = extractDetailMapper.selectBatchIds(Arrays.asList(e.getExtractdetailids().split(",")));
				e.setRealextract(budgetExtractdetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO,BigDecimal::add));
				e.setRepaymoney(budgetExtractdetails.stream().filter(e1->e1.getWithholdmoney()!=null).map(BudgetExtractdetail::getWithholdmoney).reduce(BigDecimal.ZERO,BigDecimal::add));
				e.setConsotax(budgetExtractdetails.stream().map(BudgetExtractdetail::getConsotax).reduce(BigDecimal.ZERO,BigDecimal::add));
				splitOrder(e, sumId);
			}).filter(e->e.getIsSelf()).collect(Collectors.toList());

			List<ExtractPayDetailVO> list = resultList.stream().skip((page - 1) * rows).limit(rows).collect(Collectors.toList());
			return PageResult.apply(resultList.size(), list);
		}
	}

	private void splitOrder(ExtractPayDetailVO payDetailVO, Long sumId) {
		List<String> extractdetailids = Arrays.asList(payDetailVO.getExtractdetailids().split(","));
		List<BudgetExtractdetail> budgetExtractdetails = this.extractDetailMapper.selectBatchIds(extractdetailids);

		BudgetExtractdetail extractDetail = null;
		String[] detailIds = payDetailVO.getExtractdetailids().split(",");
		int extractSize = detailIds.length;
		int index = -1;

		for (int i = 0; i < extractSize; i++) {
			BudgetExtractdetail budgetExtractdetail = budgetExtractdetails.get(i);
			if (budgetExtractdetail.getExtractsumid().equals(sumId)) {
				index = i;
				extractDetail = budgetExtractdetail;
				break;
			}
		}
		if(index == -1){
			//?????????
			payDetailVO.setIsSelf(false);
			return;
		}

		BigDecimal totalCopeextract = budgetExtractdetails.stream().map(e -> e.getCopeextract()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal percent = BigDecimal.ZERO;
		if (totalCopeextract.compareTo(BigDecimal.ZERO) == 0) {
		} else {
			percent = extractDetail.getCopeextract().divide(totalCopeextract, 20, BigDecimal.ROUND_HALF_UP);
		}
		BigDecimal billingPaymoney = getPercentMoney(payDetailVO.getBillingPaymoney(), totalCopeextract, index, extractSize, percent, detailIds, budgetExtractdetails, BigDecimal.ROUND_HALF_UP);
		payDetailVO.setBillingPaymoney(billingPaymoney);
		BigDecimal avoidBillingPaymoney = getPercentMoney(payDetailVO.getAvoidBillingPaymoney(), totalCopeextract, index, extractSize, percent, detailIds, budgetExtractdetails, BigDecimal.ROUND_HALF_UP);
		payDetailVO.setAvoidBillingPaymoney(avoidBillingPaymoney);
		BigDecimal beforeCalFee = getPercentMoney(payDetailVO.getBeforeCalFee(), totalCopeextract, index, extractSize, percent, detailIds, budgetExtractdetails, BigDecimal.ROUND_HALF_UP);
		payDetailVO.setBeforeCalFee(beforeCalFee);
		BigDecimal outUnitPayMoney = getPercentMoney(payDetailVO.getOutUnitPayMoney(), totalCopeextract, index, extractSize, percent, detailIds, budgetExtractdetails, BigDecimal.ROUND_HALF_UP);
		payDetailVO.setOutUnitPayMoney(outUnitPayMoney);
	}

	private List<ExtractPayDetailVO> getPayDetailsByCondition(Page<ExtractPayDetailVO> pageCond, Map<String, Object> params) {
		return this.budgetExtractsumMapper.getExtractPayDetails(pageCond, params);
	}

	/**
	 * ????????????
	 *
	 * @param sumId
	 */
	public void agree(String ids) {

		Optional.ofNullable(ids).orElseThrow(() -> new RuntimeException("???????????????"));
		List<BudgetExtractsum> budgetExtractsums = this.budgetExtractsumMapper.selectBatchIds(Arrays.asList(ids.split(",")));

		budgetExtractsums.forEach(extractsum -> {
			//????????????????????? ?????????????????? todo
//			applicationLogService.dealHandleRecord(extractsum.getId());
			if (extractsum.getStatus() != ExtractStatusEnum.VERIFYING.getType())
				throw new RuntimeException("????????????!?????????????????????????????????");
			extractsum.setStatus(ExtractStatusEnum.APPROVED.getType());
			extractsum.setVerifytime(new Date());
			WbUser curUser = UserThreadLocal.get();
			extractsum.setVerifyorname(curUser.getDisplayName());
			extractsum.setVerifyorid(curUser.getUserId());
			this.updateById(extractsum);
			Optional<BudgetExtractCommissionApplication> applicationOptional = applicationService.getApplicationBySumId(String.valueOf(extractsum.getId()));
			if (applicationOptional.isPresent()) {
				applicationLogService.saveLog(applicationOptional.get().getId(), OperationNodeEnum.SYSTEM_APPROVED, LogStatusEnum.COMPLETE);
			}
			/**
			 * ??????????????????????????????????????????
			 * ???????????????????????????????????????	????????????????????????????????????	XX???XX???XX????????????????????????????????????????????????????????????????????????
			 */
			this.doMsgTask(extractsum.getExtractmonth(),ExtractStatusEnum.APPROVED, String.valueOf(extractsum.getId()));
		});
		this.updateBatchById(budgetExtractsums);
		/**
		 * ??????????????????????????????????????????
		 * ???????????????????????????????????????	????????????????????????????????????	XX???XX???XX????????????????????????????????????????????????????????????????????????
		 */

//
//		List<BudgetExtractsum> totalExtractSums = budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", budgetExtractsums.get(0).getExtractmonth()));
//		long approvedcount = totalExtractSums.stream().filter(e -> ExtractStatusEnum.APPROVED.getType() == e.getStatus().intValue()).count();
//		if (totalExtractSums.size() == approvedcount) {
//			try {
//				TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", EXTRACT_ALL_VERIFY));
//				sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + budgetExtractsums.get(0).getExtractmonth() + "?????????????????????????????????????????????????????????????????????", null));
//			} catch (Exception e) {
//			}
//		}
	}
	public void doMsgTask(String extractMonth,  ExtractStatusEnum statusEnum,String sumId) {
		BudgetExtractsum extractSum =  this.baseMapper.selectById(sumId);
		List<BudgetExtractsum> extractSumList =  this.baseMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractMonth).eq("deleteflag", 0));
		//??????????????????????????????????????????????????????
		long count = extractSumList.stream().filter(x->!x.getId().toString().equals(sumId)).filter(x -> !Objects.equals(x.getStatus(), statusEnum.getType())).count();
		if(count>0){
			return;
		}
		String yearName = yearMapper.getNameById(extractSum.getYearid());
		Integer month = Integer.valueOf(extractMonth.substring(4, 6));
		Integer batchNo = Integer.valueOf(extractMonth.substring(6));
		String code =  MessageFormat.format("{0}???{1}??????{2}???{3}???",yearName,month,batchNo,extractSum.getCode());
		switch (statusEnum){
			case APPROVED:
				String toUsers = "";
				if (count==0) {
					//XX???XX???XX???
					if (this.isTest()) {
						toUsers = this.getTestNotice();
					}else{
						List<String> empNoList = commonService.getEmpNoListByRoleNames(RoleNameEnum.TAX.getValue());
						toUsers=String.join("|", empNoList);
					}
					if(StringUtils.isNotBlank(toUsers))
						sender.sendQywxMsg(new QywxTextMsg(toUsers, null, null, 0,code+"?????????????????????????????????????????????????????????????????????" ,null));
				}
				break;
			default:
				break;
		}
	}
	/**
	 * ??????
	 *
	 * @param sumId
	 * @param remark
	 */
	public void reject(Long sumId, String remark) {
		BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(sumId);
		if (extractsum.getStatus() != ExtractStatusEnum.VERIFYING.getType())
			throw new RuntimeException("????????????!?????????????????????????????????");
		extractsum.setStatus(ExtractStatusEnum.RETURN.getType());
		extractsum.setVerifytime(new Date());
		WbUser curUser = UserThreadLocal.get();
		extractsum.setVerifyorname(curUser.getDisplayName());
		extractsum.setVerifyorid(curUser.getUserName());
		extractsum.setRemark(remark);
		this.budgetExtractsumMapper.updateById(extractsum);
		//??????????????? TODO ????????????

		Optional<BudgetExtractCommissionApplication> applicationOptional = applicationService.getApplicationBySumId(String.valueOf(sumId));
		if (applicationOptional.isPresent()) {
			BudgetExtractCommissionApplication application = applicationOptional.get();
			applicationLogService.saveLog(application.getId(),OperationNodeEnum.SYSTEM_RETURN,LogStatusEnum.REJECT);
			if (application.getReimbursementId()!=null) {
				BudgetReimbursementorder reimbursementorder = reimbursementorderService.getById(application.getReimbursementId());
				reimbursementorderService.removeById(reimbursementorder.getId());
			}
		}

		try {
			sender.sendQywxMsgSyn(new QywxTextMsg(extractsum.getCreator(), null, null, 0, "???????????????" + extractsum.getCode() + "?????????????????????????????????" + remark, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ????????????
	 *
	 * @param extractBatch          ????????????
	 * @param specialPersonNameList ??????????????????
	 * @throws Exception
	 */
	public void calculate(String extractBatch, List<HrSalaryYearTaxUser> specialPersonNameList, String empno) throws Exception {
		//????????????????????????????????????
		List<BudgetExtractsum> curBatchExtractSumList = this.budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch).eq(BudgetExtractsum::getDeleteflag, 0).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
		/**
		 * ??????????????????????????????
		 */
		if (StringUtils.isBlank(empno)) validateIsCanCalculate(extractBatch, curBatchExtractSumList);

		BudgetExtractTaxHandleRecord handleRecord = getExtractTaxHandleRecord(extractBatch);
		Boolean isReCalculate = false;
		if (Objects.nonNull(handleRecord)) {
			isReCalculate = handleRecord.getIsCalComplete();
		}

		if (isReCalculate) {
			//???????????? ????????????????????????
			clearaCalculatedData(extractBatch, empno);
		}
		List<Long> curExtractSumIdList = curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//?????????????????????????????????(?????????????????????)
		List<BudgetExtractdetail> curBatchExtractDetailList = this.extractDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractdetail>()
				.eq(BudgetExtractdetail::getDeleteflag, 0)
				.in(BudgetExtractdetail::getExtractsumid, curExtractSumIdList)
				.ne(BudgetExtractdetail::getBusinessType,ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode()));
		if (!curBatchExtractDetailList.isEmpty()){
			/**
			 * ?????????????????????????????????????????????????????????????????????????????????????????????
			 */
			ExtractPayCommonData extractPayCommonData = populateCommonData(extractBatch, curBatchExtractDetailList);
			extractPayCommonData.setCurBatchExtractSumList(curBatchExtractSumList);
			extractPayCommonData.setSpecialPersonNameList(specialPersonNameList);
			//????????????
			doCalculate(extractPayCommonData, curBatchExtractDetailList, isReCalculate, empno);
			invokeExtractCalEndPostProcessor(handleRecord, extractBatch, curBatchExtractSumList, 1);
		}else{
			List<BudgetExtractdetail> individualExtractDetailList = curBatchExtractDetailList.stream().filter(e -> ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode().equals(e.getBusinessType())).collect(Collectors.toList());
			if (handleRecord == null) {
				handleRecord = new BudgetExtractTaxHandleRecord();
				handleRecord.setExtractMonth(extractBatch);
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(true);
				handleRecord.setIsPersonalityComplete(false);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.save(handleRecord);
			}else{
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(true);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.updateById(handleRecord);
			}

			BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(extractBatch);
			if(extractTaxHandleRecord!=null && extractTaxHandleRecord.getIsCalComplete() && extractTaxHandleRecord.getIsPersonalityComplete() && extractTaxHandleRecord.getIsSetExcessComplete()){
				generateExtractStepLog(curExtractSumIdList, OperationNodeEnum.TAX_PREPARATION_CALCULATION_EMP, "???"+OperationNodeEnum.TAX_PREPARATION_CALCULATION_EMP.getValue() + "?????????", LogStatusEnum.COMPLETE.getCode());
				taxGroupSuccess(extractBatch);
			}
		}
	}

	public BudgetExtractTaxHandleRecord getExtractTaxHandleRecord(String extractBatch) {
		return taxHandleRecordService.getOne(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractBatch));
	}

	public boolean isTest(){
		TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "is_test"));
		return dm != null && StringUtils.isNotBlank(dm.getDmValue()) && "1".equals(dm.getDmValue());
	}
	public String getTestNotice(){
		TabDm dm1 = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "EXTRACTCAL").eq("dm", "test_notice"));
		return dm1.getDmValue();
	}

	private void invokeExtractCalEndPostProcessor(BudgetExtractTaxHandleRecord handleRecord, String curExtractBatch, List<BudgetExtractsum> curBatchExtractSumList, Integer type) {

		List<Long> sumIds = curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		if (sumIds.isEmpty()) return;
		List<BudgetExtractdetail> curBatchExtractDetailList = this.extractDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractdetail>().in(BudgetExtractdetail::getExtractsumid, sumIds).eq(BudgetExtractdetail::getDeleteflag, 0));
		List<BudgetExtractdetail> extractDetailList = curBatchExtractDetailList.stream().filter(e -> !ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode().equals(e.getBusinessType())).collect(Collectors.toList());
		List<BudgetExtractdetail> individualExtractDetailList = curBatchExtractDetailList.stream().filter(e -> ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode().equals(e.getBusinessType())).collect(Collectors.toList());

		long count1 = extractDetailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0).count();
		long count2 = extractDetailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0 && e.getHandleflag()).count();
		if (count1 == count2) {
			//??????????????????
			if (handleRecord == null) {
				handleRecord = new BudgetExtractTaxHandleRecord();
				handleRecord.setExtractMonth(curExtractBatch);
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(true);
				handleRecord.setIsPersonalityComplete(false);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.save(handleRecord);
			} else {
//				LambdaUpdateWrapper<BudgetExtractTaxHandleRecord> updateWrapper = new LambdaUpdateWrapper<>();
//				updateWrapper.eq(BudgetExtractTaxHandleRecord::getExtractMonth,curExtractBatch);
//				updateWrapper.eq(BudgetExtractTaxHandleRecord::getIsSetExcessComplete,handleRecord.getIsSetExcessComplete());
//				updateWrapper.set(BudgetExtractTaxHandleRecord::getIsSetExcessComplete,1);
//				int count = taxHandleRecordMapper.update(new BudgetExtractTaxHandleRecord(), updateWrapper);
//				if(count != 1){
//					throw new RuntimeException("????????????????????????");
//				}
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(true);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.updateById(handleRecord);
			}
			generateExtractStepLog(sumIds, OperationNodeEnum.TAX_PREPARATION_CALCULATION_EMP, "???"+OperationNodeEnum.TAX_PREPARATION_CALCULATION_EMP.getValue() + "?????????", LogStatusEnum.COMPLETE.getCode());
			taxGroupSuccess(curExtractBatch);

			if (type == 1) {
				try {
					TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
					if (dm != null && StringUtils.isNotBlank(dm.getDmValue())) {
						sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "????????????????????????????????????!", null));
					}
				} catch (Exception e) {
				}
			} else if (type == 2) {
				try {
					TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
					if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
						sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "???????????????????????????!", null));

				} catch (Exception e) {
				}
			}
		} else {
			if (handleRecord == null) {
				handleRecord = new BudgetExtractTaxHandleRecord();
				handleRecord.setExtractMonth(curExtractBatch);
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(false);
				handleRecord.setIsPersonalityComplete(false);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.save(handleRecord);
			} else {
				handleRecord.setIsCalComplete(true);
				handleRecord.setIsSetExcessComplete(false);
				if (CollectionUtils.isEmpty(individualExtractDetailList)) {
					handleRecord.setIsPersonalityComplete(true);
				}
				taxHandleRecordService.updateById(handleRecord);
			}
			try {
				TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
				if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
					sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "???????????????" + curExtractBatch + "????????????,???????????????????????????????????????!", null));
			} catch (Exception e) {
			}
		}

	}

	public void generateExtractStepLog(List<Long> sumIds, OperationNodeEnum nodeEnum, String remark, Integer status) {
		List<BudgetExtractCommissionApplication> applicationList = applicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractCommissionApplication>().in(BudgetExtractCommissionApplication::getExtractSumId, sumIds).eq(BudgetExtractCommissionApplication::getStatus, 1));
		List<BudgetExtractCommissionApplicationLog> logList = applicationList.stream().map(e -> {
			BudgetExtractCommissionApplicationLog extractLog = new BudgetExtractCommissionApplicationLog();
			extractLog.setNode(nodeEnum.getType());
			extractLog.setApplicationId(e.getId());
			extractLog.setCreateTime(new Date());
			extractLog.setCreateBy(UserThreadLocal.getEmpNo());
			extractLog.setCreatorName(UserThreadLocal.getEmpName());
			extractLog.setStatusName(LogStatusEnum.getValue(status));
			extractLog.setStatus(status);
			extractLog.setRemarks(remark);
			return extractLog;
		}).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(logList)) {
			logService.saveBatch(logList);
		}
	}


	/**
	 * ????????????
	 *
	 * @param extractPayCommonData
	 * @param curBatchExtractDetailList
	 */
	private void doCalculate(ExtractPayCommonData extractPayCommonData,
	                         List<BudgetExtractdetail> curBatchExtractDetailList, Boolean isReCalculate, String empno) throws Exception {
		List<String> errorMsg = new ArrayList<>();
		//?????????????????????
		curBatchExtractDetailList.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getIdnumber)).forEach((idnumber, extractDetails) -> {
			try {
				int size = extractDetails.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getEmpno)).size();
				if (size > 1) throw new RuntimeException("?????????????????????????????????????????????" + idnumber + "???????????????????????????");
				if (StringUtils.isNotBlank(empno)) {
					if (!empno.equals(extractDetails.get(0).getEmpno())) return;
				}
				//??????????????????
				ExtractEmpCalDataDetail curEmpCalData = packageExtractEmpCalData(extractDetails, extractPayCommonData);
				curEmpCalData.setExtractDetails(extractDetails);
				if (curEmpCalData.getIscompanyemp() && curEmpCalData.getIsQuit()) {
					/**
					 * ??????????????????
					 */
					handleQuitEmp(curEmpCalData, extractPayCommonData.getCurExtractBatch());
					return;
				}

				/**
				 * ????????????????????????????????????<=0 ?????? ????????????0????????????
				 */
				if (curEmpCalData.getCopeextract().compareTo(BigDecimal.ZERO) <= 0 && curEmpCalData.getConsotax().compareTo(BigDecimal.ZERO) == 0) {
					for (BudgetExtractdetail detail : curEmpCalData.getExtractDetails()) {
						detail.setExcessmoney(BigDecimal.ZERO);
						detail.setExcesstype(ExtractExcessTypeEnum.NOEXCESS.getType());
						detail.setHandleflag(false);
					}
					this.extractDetailService.updateBatchById(curEmpCalData.getExtractDetails());
					return;
				}

				if (!isReCalculate) {
					//???????????????
					//if (extractPayCommonData.getIsDeduction()) {
					//	extractDeduction(curEmpCalData.getExtractDetails(), extractPayCommonData.getEmpno2LendmoneyMap());
					//} else {
					for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
						bed.setRealextract(bed.getCopeextract().subtract(bed.getWithholdmoney() == null ? BigDecimal.ZERO : bed.getWithholdmoney()));
						this.extractDetailMapper.updateById(bed);
					}
					//}

				} else {
					for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
						bed.setRealextract(bed.getCopeextract().subtract(bed.getWithholdmoney() == null ? BigDecimal.ZERO : bed.getWithholdmoney()));
						this.extractDetailMapper.updateById(bed);
					}
				}
				//??????????????????
				BigDecimal realextract = curEmpCalData.getExtractDetails().stream().map(BudgetExtractdetail::getRealextract).reduce(BigDecimal.ZERO, BigDecimal::add);
				curEmpCalData.setRealExtract(realextract.subtract(curEmpCalData.getFeePay()));

				if (curEmpCalData.getRealExtract().compareTo(BigDecimal.ZERO) < 0)
					throw new RuntimeException(curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + ")????????????????????????!");
				if (curEmpCalData.getRealExtract().compareTo(BigDecimal.ZERO) == 0) return;
				/**
				 * ??????????????????
				 */
				BudgetExtractpaydetail curEmpPaydetail = calSplitCompany(curEmpCalData, extractPayCommonData.getCurExtractBatch(), extractPayCommonData.getOuterThreshold(), extractPayCommonData.getOuterOrinalExtraTax());

				List<BudgetExtractquotaRuledetail> quotaruledetailList = extractPayCommonData.getQuotaRuleDetailMap().get(curEmpCalData.getQuotaRule().getId()).stream().filter(e -> e.getMinsalary().compareTo(curEmpPaydetail.getSalary()) <= 0 && e.getMaxsalary().compareTo(curEmpPaydetail.getSalary()) > 0).collect(Collectors.toList());
				if (quotaruledetailList.isEmpty())
					throw new RuntimeException("???????????????????????????" + curEmpCalData.getBillingUnit().getName() + "?????????????????????!????????????:" + curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + "," + curEmpPaydetail.getSalary() + ")");
				if (quotaruledetailList.size() > 1)
					throw new RuntimeException("???????????????" + curEmpCalData.getBillingUnit().getName() + "???????????????????????????????????????!");
				//??????????????????
				BigDecimal quotamoney = quotaruledetailList.get(0).getQuotamoney();
				/**
				 * ????????????
				 */
				Long detailId = extractDetails.get(0).getId();
				extractPayCommonData.getPayOrderMap().put(detailId, 0);
				quotaPay(curEmpPaydetail, curEmpCalData, quotamoney, extractPayCommonData, detailId, isReCalculate);
				curEmpPaydetail.setAviodtax(BigDecimal.ZERO);
				curEmpPaydetail.setTotalaviodtax(BigDecimal.ZERO);
				payDetailMapper.updateById(curEmpPaydetail);
			} catch (Exception e) {
				e.printStackTrace();
				errorMsg.add(e.getMessage());
			}
		});
		if (!errorMsg.isEmpty()) {
			throw new RuntimeException(errorMsg.stream().collect(Collectors.joining("<br>")));
		}
		//???????????????????????????
		//generateExtractBatchCode(extractPayCommonData.getCurExtractBatch());


	}

	/**
	 * ???????????????????????????
	 *
	 * @param curExtractBatch ????????????
	 */
	private void generateExtractBatchCode(String curExtractBatch) throws Exception {
		BudgetExtractQrcode code = extractQrcodeMapper.selectOne(new LambdaQueryWrapper<BudgetExtractQrcode>().eq(BudgetExtractQrcode::getExtractMonth, curExtractBatch));
		if (Objects.nonNull(code)) return;
		BudgetExtractQrcode qrcode = new BudgetExtractQrcode();
		qrcode.setCreateTime(new Date());
		qrcode.setExtractMonth(curExtractBatch);
		String qrcodebase64str = QRCodeUtil.createBase64Qrcode(this.extract_qrcode_url + curExtractBatch, this.fileShareDir + File.separator + QRCODE_PREFIX + curExtractBatch + QRCODE_FORMAT);
		qrcode.setQrcode(qrcodebase64str);
		extractQrcodeMapper.insert(qrcode);
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 * @param curBatchExtractDetailList
	 * @param paymentList
	 * @param calDataList
	 * @param extractImportors
	 */
//	private void generatePaymoneyOrderIfExcessHandleOver(List<BudgetExtractdetail> curBatchExtractDetailList, List<BudgetExtractpayment> paymentList, List<ExtractEmpCalDataDetail> calDataList, String extractImportors) {
//		String extractmonth = "";
//		long count = curBatchExtractDetailList.stream().filter(e->e.getExcessmoney()!=null && e.getExcessmoney().compareTo(BigDecimal.ZERO)>0).count();
//		long handleCount = curBatchExtractDetailList.stream().filter(e->e.getExcessmoney()!=null && e.getExcessmoney().compareTo(BigDecimal.ZERO)>0 && e.getHandleflag()).count();
//		if(count == handleCount) {
//			/**
//			 * ??????????????????????????????
//			 */
//			List<BudgetPaymoney> paymoneyList = new ArrayList<>();
//			for(BudgetExtractpayment payment : paymentList) {
//				String detailId = payment.getExtractdetailids().split(",")[0];
//				BudgetExtractdetail extractdetail = curBatchExtractDetailList.stream().filter(e->e.getId().toString().equals(detailId)).findFirst().orElse(null);
//				ExtractEmpCalDataDetail empCalDataDetail = calDataList.stream().filter(e->e.getIdnumber().equals(extractdetail.getIdnumber())).findFirst().orElse(null);
//				BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(extractdetail.getExtractsumid());
//				extractmonth = extractsum.getExtractmonth();
//				//???????????????
//				createPaymoney(empCalDataDetail,payment,extractsum.getCode(),paymoneyList);
//			}
//			if(!paymoneyList.isEmpty()) this.paymoneyMapper.batchSavePaymoney(paymoneyList);
//			/**
//			 * ???????????????????????????
//			 */
//			try {
//				sender.sendQywxMsgSyn(new QywxTextMsg(extractImportors, null, null, 0, "???????????????"+extractmonth+"???????????????,??????????????????!", null));
//			}catch(Exception e) {}
//		}else {
//			try {
//				sender.sendQywxMsgSyn(new QywxTextMsg("17474", null, null, 0, "???????????????"+extractmonth+"????????????,?????????????????????????????????!", null));
//			}catch(Exception e) {}
//		}
//	}

	/**
	 * ???????????????
	 *
	 * @param empCalDataDetail
	 * @param payment
	 * @param code
	 * @param paymoneyList
	 */
	private void createPaymoney(ExtractEmpCalDataDetail empCalDataDetail, BudgetExtractpayment payment, String code, List<BudgetPaymoney> paymoneyList) {
		if (payment.getPaymoney1() != null && payment.getPaymoney1().compareTo(BigDecimal.ZERO) > 0) {
			BudgetPaymoney paymoney = new BudgetPaymoney();
			paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
			paymoney.setPaymoneyobjectcode(code);
			paymoney.setPaymoneyobjectid(payment.getId());
			paymoney.setPaymoney(NumberUtil.subZeroAndDot(payment.getPaymoney1()));
			paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
			paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
			paymoney.setCreatetime(new Date());
			paymoney.setBunitname(payment.getBunitname1());
			paymoney.setBunitbankaccount(payment.getBunitbankaccount1());
			paymoney.setBunitaccountbranchcode(payment.getBunitaccountbranchcode1());
			paymoney.setBunitaccountbranchname(payment.getBunitaccountbranchname1());
			paymoney.setBankaccountname(empCalDataDetail.getPersonAccount().getAccountname());
			paymoney.setBankaccount(empCalDataDetail.getPersonAccount().getBankaccount());
			paymoney.setBankaccountbranchcode(empCalDataDetail.getPersonAccountBank().getSubBranchCode());
			paymoney.setBankaccountbranchname(empCalDataDetail.getPersonAccountBank().getBankName());
			paymoney.setOpenbank(empCalDataDetail.getPersonAccountBank().getSubBranchName());
			paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
			paymoneyList.add(paymoney);
		}
		if (payment.getPayfee() != null && payment.getPayfee().compareTo(BigDecimal.ZERO) > 0) {
			BudgetPaymoney paymoney = new BudgetPaymoney();
			paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
			paymoney.setPaymoneyobjectcode(code);
			paymoney.setPaymoneyobjectid(payment.getId());
			paymoney.setPaymoney(NumberUtil.subZeroAndDot(payment.getPayfee()));
			paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
			paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
			paymoney.setCreatetime(new Date());
			paymoney.setBunitname(payment.getBunitname1());
			paymoney.setBunitbankaccount(payment.getBunitbankaccount1());
			paymoney.setBunitaccountbranchcode(payment.getBunitaccountbranchcode1());
			paymoney.setBunitaccountbranchname(payment.getBunitaccountbranchname1());
			paymoney.setBankaccountname(empCalDataDetail.getPersonAccount().getAccountname());
			paymoney.setBankaccount(empCalDataDetail.getPersonAccount().getBankaccount());
			paymoney.setBankaccountbranchcode(empCalDataDetail.getPersonAccountBank().getSubBranchCode());
			paymoney.setBankaccountbranchname(empCalDataDetail.getPersonAccountBank().getBankName());
			paymoney.setOpenbank(empCalDataDetail.getPersonAccountBank().getSubBranchName());
			paymoney.setRemark("??????????????????????????????");
			paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
			paymoneyList.add(paymoney);
		}
		if (payment.getPaymoney2() != null && payment.getPaymoney2().compareTo(BigDecimal.ZERO) > 0) {
			//??????????????????
			BudgetPaymoney paymoney = new BudgetPaymoney();
			paymoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.getType());
			;
			paymoney.setPaymoneyobjectid(payment.getId());
			paymoney.setPaymoney(NumberUtil.subZeroAndDot(payment.getPaymoney2()));
			paymoney.setPaytype(Constants.PAY_TYPE.TRANSFER);
			paymoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
			paymoney.setCreatetime(new Date());
			paymoney.setBunitname(payment.getBunitname2());
			paymoney.setBunitbankaccount(payment.getBunitbankaccount2());
			paymoney.setBunitaccountbranchcode(payment.getBunitaccountbranchcode2());
			paymoney.setBunitaccountbranchname(payment.getBunitaccountbranchname2());
			paymoney.setBankaccountname(empCalDataDetail.getPersonAccount().getAccountname());
			paymoney.setBankaccount(empCalDataDetail.getPersonAccount().getBankaccount());
			paymoney.setBankaccountbranchcode(empCalDataDetail.getPersonAccountBank().getSubBranchCode());
			paymoney.setBankaccountbranchname(empCalDataDetail.getPersonAccountBank().getBankName());
			paymoney.setOpenbank(empCalDataDetail.getPersonAccountBank().getSubBranchName());
			paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
			paymoneyList.add(paymoney);
		}
	}

	/**
	 * ????????????????????????
	 * ???????????????????????????????????????
	 *
	 * @param curEmpPaydetail
	 * @param curEmpCalData
	 * @param quotamoney
	 * @param extractPayCommonData
	 * @param detailId
	 * @param isReCalculate
	 */
	private void quotaPay(BudgetExtractpaydetail curEmpPaydetail, ExtractEmpCalDataDetail curEmpCalData, BigDecimal quotamoney, ExtractPayCommonData extractPayCommonData, Long detailId, Boolean isReCalculate) {
		String curDetailIds = curEmpCalData.getExtractDetails().stream().map(e -> e.getId().toString()).collect(Collectors.joining(","));
		curEmpCalData.setCurExtractDetailIds(curDetailIds);
		/**
		 * ????????????????????????????????????????????????
		 */
		BigDecimal incorporatedcompanyPayedExtract = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		//????????????????????????????????????
		BigDecimal curIncorporatedCompanyPayExtract = curEmpPaydetail.getIncorporatedcompany() == null ? BigDecimal.ZERO : curEmpPaydetail.getIncorporatedcompany();
		if (curIncorporatedCompanyPayExtract.compareTo(BigDecimal.ZERO) > 0 && curIncorporatedCompanyPayExtract.add(incorporatedcompanyPayedExtract).compareTo(quotamoney) > 0) {
			/**
			 * ????????????
			 */
			//tempmoney ????????????????????? ??????????????????>?????????????????????????????????????????????
			BigDecimal tempmoney = incorporatedcompanyPayedExtract.compareTo(quotamoney) == 1 ? quotamoney : incorporatedcompanyPayedExtract;
			//????????????????????????
			BigDecimal outerPayTotal = curIncorporatedCompanyPayExtract.add(tempmoney).subtract(quotamoney);
			createGrantLog(curEmpCalData, extractPayCommonData, quotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract.subtract(outerPayTotal), detailId);
			curEmpPaydetail.setIncorporatedcompany(curIncorporatedCompanyPayExtract.subtract(outerPayTotal));

			//???????????????????????????
			List<BigDecimal> payextractList = new ArrayList<>();
			payextractList.add(outerPayTotal);
			//???????????????????????????
			//List<BudgetExtractOuterperson> refOuterPersonList = curEmpCalData.getRefOuterPersonList();
			/**
			 * ?????????????????????
			 */
			List<BudgetExtractOuterperson> refOuterPersonList = Lists.newArrayList();
			for (int i = 0; i < refOuterPersonList.size(); i++) {
				BudgetExtractOuterperson empoutaccount = refOuterPersonList.get(i);
				BigDecimal money = payextractList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
				if (money.compareTo(BigDecimal.ZERO) > 0) {
					BudgetExtractdetail bed = new BudgetExtractdetail();
					bed.setEmpno(empoutaccount.getEmpno());
					bed.setIdnumber(empoutaccount.getIdnumber());
					bed.setEmpname(empoutaccount.getName());
					bed.setRealextract(money);
					bed.setConsotax(curEmpCalData.getConsotax());
					ExtractEmpCalDataDetail curOuterCalDataDetail = packageExtractEmpCalData(bed, extractPayCommonData);
					BudgetExtractpaydetail curOuterPaydetail = calSplitCompany(curOuterCalDataDetail, extractPayCommonData.getCurExtractBatch(), extractPayCommonData.getOuterThreshold(), extractPayCommonData.getOuterOrinalExtraTax());
					List<BudgetExtractquotaRuledetail> quotaruledetailList = extractPayCommonData.getQuotaRuleDetailMap().get(curEmpCalData.getQuotaRule().getId()).stream().filter(e -> e.getMinsalary().compareTo(curEmpPaydetail.getSalary()) <= 0 && e.getMaxsalary().compareTo(curEmpPaydetail.getSalary()) > 0).collect(Collectors.toList());
					if (quotaruledetailList.isEmpty())
						throw new RuntimeException("???????????????????????????" + curEmpCalData.getBillingUnit().getName() + "?????????????????????!????????????:" + curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + "," + curEmpPaydetail.getSalary() + ")");

					//throw new RuntimeException("???????????????????????????" + curEmpCalData.getBillingUnit().getName() + "?????????????????????!");
					if (quotaruledetailList.size() > 1)
						throw new RuntimeException("???????????????" + curEmpCalData.getBillingUnit().getName() + "???????????????????????????????????????!");
					//????????????
					BigDecimal refOuterQuotamoney = quotaruledetailList.get(0).getQuotamoney();
					//??????????????????????????????????????????
					refOuterQuotaPay(curOuterPaydetail, curOuterCalDataDetail, refOuterQuotamoney, extractPayCommonData, detailId, payextractList);
					curOuterCalDataDetail.setFinalIncorporatedCompanyPayedExtract(curOuterPaydetail.getIncorporatedcompany());
					curOuterCalDataDetail.setAvoidTaxMoney(curOuterPaydetail.getTotalaviodtax());
					//??????????????????
					handlePayResult(curOuterCalDataDetail, curOuterPaydetail.getTaxdiffrence(), curOuterPaydetail, extractPayCommonData, isReCalculate);
					extractPayCommonData.getExtractCalDataDetailList().add(curOuterCalDataDetail);
				}
			}


			BigDecimal money = payextractList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
			if (money.compareTo(BigDecimal.ZERO) > 0) {
				for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
					bed.setExcessmoney(money.add(curEmpPaydetail.getTotalaviodtax()));
					bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType());
				}

			} else {
				if(curEmpPaydetail.getTotalaviodtax().compareTo(BigDecimal.ZERO)>0){
					for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
						bed.setExcessmoney(curEmpPaydetail.getTotalaviodtax());
						bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType());
					}
				}else{
					for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
						bed.setExcessmoney(BigDecimal.ZERO);
						bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_FINISHED.getType());
					}
				}
			}

		} else {
			//?????????
			createGrantLog(curEmpCalData, extractPayCommonData, quotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract, detailId);
			curEmpPaydetail.setIncorporatedcompany(curIncorporatedCompanyPayExtract);

			if(curEmpPaydetail.getTotalaviodtax().compareTo(BigDecimal.ZERO)>0){
				for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
					bed.setExcessmoney(curEmpPaydetail.getTotalaviodtax());
					bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType());
				}
			}else{
				for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
					bed.setExcessmoney(BigDecimal.ZERO);
					bed.setExcesstype(ExtractExcessTypeEnum.NOEXCESS.getType());
				}
			}
		}
		//?????????????????????????????????false
		for (BudgetExtractdetail extractDetail : curEmpCalData.getExtractDetails()) {
			extractDetail.setHandleflag(false);
			this.extractDetailMapper.updateById(extractDetail);
		}
		this.payDetailMapper.updateById(curEmpPaydetail);
		//???????????????????????????
		curEmpCalData.setFinalIncorporatedCompanyPayedExtract(curEmpPaydetail.getIncorporatedcompany());
		curEmpCalData.setAvoidTaxMoney(BigDecimal.ZERO);
		//??????????????????
		handlePayResult(curEmpCalData, curEmpPaydetail.getTaxdiffrence(), curEmpPaydetail, extractPayCommonData, isReCalculate);
		extractPayCommonData.getExtractCalDataDetailList().add(curEmpCalData);
	}

	/**
	 * ??????????????????
	 * 1.??????????????????
	 * 2.??????????????????
	 *
	 * @param curEmpCalData
	 * @param curEmpPaydetail
	 * @param extractPayCommonData
	 * @param isReCalculate
	 */
	private void handlePayResult(ExtractEmpCalDataDetail curEmpCalData, BigDecimal taxDiffrence, BudgetExtractpaydetail curEmpPaydetail, ExtractPayCommonData extractPayCommonData, Boolean isReCalculate) {
		//????????????????????????
		BudgetExtractpayment payment = createExtractPayment(curEmpCalData, taxDiffrence, curEmpPaydetail.getId());
		extractPayCommonData.getPaymentList().add(payment);
		/**
		 * ??????????????????
		 */
		//??????????????????
		String curExtractMonth = extractPayCommonData.getCurExtractBatch().substring(0, 6);
		//???????????????????????????
		BudgetExtractArrears bea = this.extractArrearsMapper.selectOne(new QueryWrapper<BudgetExtractArrears>().eq("month", curExtractMonth).eq("idnumber", curEmpCalData.getIdnumber()).eq("bunitid", curEmpCalData.getBillingUnit().getId()));
		if (Objects.isNull(bea)) {
			bea = new BudgetExtractArrears();
			bea.setId(null);
			bea.setMonth(curExtractMonth);
			bea.setIdnumber(curEmpCalData.getIdnumber());
			bea.setEmpno(curEmpCalData.getEmpno());
			bea.setEmpname(curEmpCalData.getEmpname());
			bea.setSalary(curEmpPaydetail.getSalary());
			bea.setSalarylj(curEmpPaydetail.getSalarylj());
			bea.setBunitid(curEmpCalData.getBillingUnit().getId());
			bea.setBunitname(curEmpCalData.getBillingUnit().getName());
			bea.setRealextract(curEmpPaydetail.getIncorporatedcompany());
			bea.setFiveriskonefund(curEmpPaydetail.getFiveriskonefund());
			bea.setFiveriskonefundlj(curEmpPaydetail.getFiveriskonefundlj());
			bea.setSpecialdeductionlj(curEmpPaydetail.getSpecialdeductionlj());
			bea.setThresholdlj(curEmpPaydetail.getThresholdlj());
			bea.setIncorporatedcompanylj(curEmpPaydetail.getIncorporatedcompanylj().add(curEmpPaydetail.getIncorporatedcompany()));
			this.extractArrearsMapper.insert(bea);
		} else if (bea != null) {

			Map<Long, List<BudgetExtractpaydetail>> map = extractPayCommonData.getAgoPayDetailMap().get(curEmpCalData.getIdnumber());
			List<BudgetExtractpaydetail> curAgoPaydetails = (map == null) ? Lists.newArrayList() : map.get(curEmpCalData.getBillingUnit().getId());
			if (CollectionUtils.isEmpty(curAgoPaydetails)) {
				curAgoPaydetails = new ArrayList<>();
			}
			//????????????????????????????????????????????????????????????
			BigDecimal curMonthAgoPayExtract = curAgoPaydetails.stream().filter(e -> e.getExtractmonth().startsWith(curExtractMonth) && !curExtractMonth.equals(e.getExtractmonth()) && e.getIncorporatedcompany() != null).map(e -> e.getIncorporatedcompany()).reduce(BigDecimal.ZERO, BigDecimal::add);
			bea.setRealextract(curMonthAgoPayExtract.add(curEmpPaydetail.getIncorporatedcompany()));
			bea.setIncorporatedcompanylj(curEmpPaydetail.getIncorporatedcompanylj().add(curEmpPaydetail.getIncorporatedcompany()));
			this.extractArrearsMapper.updateById(bea);
		}/*else if(bea!=null) {

			Map<Long, List<BudgetExtractpaydetail>>  map = extractPayCommonData.getAgoPayDetailMap().get(curEmpCalData.getIdnumber());
			List<BudgetExtractpaydetail> curAgoPaydetails = (map==null)?Lists.newArrayList():map.get(curEmpCalData.getBillingUnit().getId());
			if(CollectionUtils.isEmpty(curAgoPaydetails)){
				curAgoPaydetails = new ArrayList<>();
			}
			//????????????????????????????????????????????????????????????
			BigDecimal curMonthAgoPayExtract = curAgoPaydetails.stream().filter(e->e.getExtractmonth().startsWith(curExtractMonth) && e.getIncorporatedcompany()!=null).map(e->e.getIncorporatedcompany()).reduce(BigDecimal.ZERO,BigDecimal::add);
			bea.setRealextract(curMonthAgoPayExtract.add(curEmpPaydetail.getIncorporatedcompany()));
			bea.setIncorporatedcompanylj(curEmpPaydetail.getIncorporatedcompanylj().add(curEmpPaydetail.getIncorporatedcompany()));
			this.extractArrearsMapper.updateById(bea);
		}*/

	}

	/**
	 * ????????????????????????
	 *
	 * @param curEmpCalData
	 * @param taxDiffrence
	 * @param payDetailId
	 * @return
	 */
	private BudgetExtractpayment createExtractPayment(ExtractEmpCalDataDetail curEmpCalData, BigDecimal taxDiffrence, Long payDetailId) {
		BudgetExtractpayment payment = new BudgetExtractpayment();
		payment.setBunitname1(curEmpCalData.getBillingUnit().getName());
		payment.setBunitbankaccount1(curEmpCalData.getUnitAccount().getBankaccount());
		payment.setBunitaccountbranchcode1(curEmpCalData.getUnitAccountBank().getSubBranchCode());
		payment.setBunitaccountbranchname1(curEmpCalData.getUnitAccountBank().getBankName());
		payment.setBunitid1(curEmpCalData.getBillingUnit().getId());
		payment.setPaymoney1(curEmpCalData.getFinalIncorporatedCompanyPayedExtract().setScale(2, BigDecimal.ROUND_UP));

		//add by minzhq
		payment.setBeforeCalFee(curEmpCalData.getFeePay());

		payment.setBunitname2(curEmpCalData.getAvoidBillingUnit().getName());
		payment.setBunitbankaccount2(curEmpCalData.getAvoidUnitAccount().getBankaccount());
		payment.setBunitaccountbranchcode2(curEmpCalData.getAvoidUnitAccountBank().getSubBranchCode());
		payment.setBunitaccountbranchname2(curEmpCalData.getAvoidUnitAccountBank().getBankName());
		payment.setPaymoney2(curEmpCalData.getAvoidTaxMoney().setScale(2, BigDecimal.ROUND_UP));
		payment.setBunitid2(curEmpCalData.getAvoidBillingUnit().getId());

		if (curEmpCalData.getIscompanyemp()) {
			payment.setBankaccount(curEmpCalData.getPersonAccount().getBankaccount());
			payment.setBankaccountbranchcode(curEmpCalData.getPersonAccount().getBranchcode());
			payment.setBankaccountbranchname(curEmpCalData.getPersonAccountBank().getBankName());
			payment.setBankaccountname(curEmpCalData.getEmpname());
			payment.setBankaccountopenbank(curEmpCalData.getPersonAccountBank().getSubBranchName());
		} else {
			payment.setBankaccount(curEmpCalData.getOuterPersonAccount().bankAccount);
			payment.setBankaccountbranchcode(curEmpCalData.getOuterPersonAccount().branchcode);
			payment.setBankaccountbranchname(curEmpCalData.getOuterPersonAccount().bankName);
			payment.setBankaccountname(curEmpCalData.getEmpname());
			payment.setBankaccountopenbank(curEmpCalData.getOuterPersonAccount().subBranchName);
		}
		payment.setCreatetime(new Date());
		payment.setSubsidytax(taxDiffrence);
		payment.setBudgetextractpaydetailid(payDetailId);
		payment.setExtractdetailids(curEmpCalData.getCurExtractDetailIds());
		this.paymentMapper.insert(payment);
		return payment;
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param curOuterPaydetail
	 * @param curOuterCalDataDetail
	 * @param refOuterQuotamoney
	 * @param extractPayCommonData
	 * @param detailId
	 * @param payextractList
	 */
	private void refOuterQuotaPay(BudgetExtractpaydetail curOuterPaydetail,
	                              ExtractEmpCalDataDetail curOuterCalDataDetail, BigDecimal refOuterQuotamoney,
	                              ExtractPayCommonData extractPayCommonData, Long detailId, List<BigDecimal> payextractList) {
		/**
		 * ????????????????????????????????????????????????
		 */
		BigDecimal incorporatedcompanyPayedExtract = curOuterCalDataDetail.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		//????????????????????????????????????
		BigDecimal curIncorporatedCompanyPayExtract = curOuterPaydetail.getIncorporatedcompany() == null ? BigDecimal.ZERO : curOuterPaydetail.getIncorporatedcompany();
		if (curIncorporatedCompanyPayExtract.compareTo(BigDecimal.ZERO) > 0 && curIncorporatedCompanyPayExtract.add(incorporatedcompanyPayedExtract).compareTo(refOuterQuotamoney) > 0) {
			//??????
			BigDecimal tempmoney = incorporatedcompanyPayedExtract.compareTo(refOuterQuotamoney) == 1 ? refOuterQuotamoney : incorporatedcompanyPayedExtract;
			//????????????????????????
			BigDecimal outerPayTotal = curIncorporatedCompanyPayExtract.add(tempmoney).subtract(refOuterQuotamoney);
			createGrantLog(curOuterCalDataDetail, extractPayCommonData, refOuterQuotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract.subtract(outerPayTotal), detailId);
			payextractList.add(curIncorporatedCompanyPayExtract.subtract(outerPayTotal).multiply(new BigDecimal("-1")));

			curOuterPaydetail.setIncorporatedcompany(curIncorporatedCompanyPayExtract.subtract(outerPayTotal));
			this.payDetailMapper.updateById(curOuterPaydetail);
		} else {
			createGrantLog(curOuterCalDataDetail, extractPayCommonData, refOuterQuotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract, detailId);
			payextractList.add(curIncorporatedCompanyPayExtract.multiply(new BigDecimal("-1")));
		}
	}

	/**
	 * ??????????????????
	 *
	 * @param extractPayCommonDat
	 * @param curEmpCalData
	 * @param quotamoney
	 * @param incorporatedcompanyPayedExtract  ??????
	 * @param curIncorporatedCompanyPayExtract ????????????
	 * @param couldgrantextract                ??????
	 * @param detailId
	 */
	private void createGrantLog(ExtractEmpCalDataDetail curEmpCalData,
	                            ExtractPayCommonData extractPayCommonData,
	                            BigDecimal quotamoney,
	                            BigDecimal incorporatedcompanyPayedExtract,
	                            BigDecimal curIncorporatedCompanyPayExtract,
	                            BigDecimal couldgrantextract, Long detailId) {
		BudgetExtractgrantlog log = new BudgetExtractgrantlog();
		log.setExtractmonth(extractPayCommonData.getCurExtractBatch());
		log.setIscompanyemp(curEmpCalData.getIscompanyemp());
		log.setIdnumber(curEmpCalData.getIdnumber());
		log.setEmpno(curEmpCalData.getEmpno());
		log.setEmpname(curEmpCalData.getEmpname());
		log.setBillingunitid(curEmpCalData.getBillingUnit().getId());
		log.setBillingunitname(curEmpCalData.getBillingUnit().getName());
		log.setExcessmoney(quotamoney);
		Integer payOrderno = extractPayCommonData.getPayOrderMap().get(detailId);
		log.setOrderno(payOrderno + 1);
		extractPayCommonData.getPayOrderMap().put(detailId, log.getOrderno());
		log.setAlreadygrantmoney(incorporatedcompanyPayedExtract);
		log.setShouldgrantextract(curIncorporatedCompanyPayExtract);
		log.setCouldgrantextract(couldgrantextract);
		log.setCratetime(new Date());
		this.grantLogMapper.insert(log);
	}

	/**
	 * ???????????????
	 *
	 * @param curEmpCalData
	 * @param curExtractBatch
	 * @param outerThreshold
	 * @param outerOrinalExtraTax
	 */
	private BudgetExtractpaydetail calSplitCompany(ExtractEmpCalDataDetail curEmpCalData, String curExtractBatch, BigDecimal outerThreshold, BigDecimal outerOrinalExtraTax) {

		Date currentDate = new Date();
		BudgetExtractpaydetail paydetail = new BudgetExtractpaydetail();
		paydetail.setIscompanyemp(curEmpCalData.getIscompanyemp());
		paydetail.setExtractmonth(curExtractBatch);
		BigDecimal salary = BigDecimal.ZERO;
		BigDecimal salarytax = BigDecimal.ZERO;
		BigDecimal fiveriskonefund = BigDecimal.ZERO;
		BigDecimal specialdeduction = BigDecimal.ZERO;
		BigDecimal threshold = BigDecimal.ZERO;
		List<Map> taxList = null;
		//??????????????????
		BigDecimal specialdeductionSum = BigDecimal.ZERO;
		//????????????
		BigDecimal salarySum = BigDecimal.ZERO;
		//??????????????????
		BigDecimal salarytaxSum = BigDecimal.ZERO;
		//??????????????????
		BigDecimal fiveriskonefundSum = BigDecimal.ZERO;
		//???????????????
		BigDecimal thresholdSum = BigDecimal.ZERO;
		//??????????????????
		BigDecimal criticalmoneySum = BigDecimal.ZERO;

		if (curEmpCalData.getIscompanyemp()) {
			//????????????
			Map<String, Object> salaryMsg = curEmpCalData.getSalaryMap();
			salary = new BigDecimal(salaryMsg.get("salary").toString());

			salarytax = new BigDecimal(salaryMsg.get("realpersonalincometax").toString());
			fiveriskonefund = new BigDecimal(salaryMsg.get("sociinsurcharge").toString());
			specialdeduction = new BigDecimal(salaryMsg.get("specialtax").toString());
			threshold = new BigDecimal(salaryMsg.get("startpoint").toString());
			JSONArray array = JSONArray.parseArray(salaryMsg.get("taxlist").toString());
			taxList = JSONObject.parseArray(array.toJSONString(), Map.class);
			salarySum = new BigDecimal(salaryMsg.get("salarys").toString()).add(salary);
			salarytaxSum = salarytax.add(new BigDecimal(salaryMsg.get("realpersonalincometaxs").toString()));
			fiveriskonefundSum = fiveriskonefund.add(new BigDecimal(salaryMsg.get("sociinsurcharges").toString()));
			thresholdSum = threshold.add(new BigDecimal(salaryMsg.get("startpoints").toString()));
			specialdeductionSum = specialdeduction.add(new BigDecimal(salaryMsg.get("specialtaxs").toString()));
			String curmonth = salaryMsg.get("curmonth").toString();
			String startmonth = salaryMsg.get("startmonth") == null ? curmonth : salaryMsg.get("startmonth").toString();
			int month = Integer.valueOf(curmonth) - Integer.valueOf(startmonth) + 1;
			criticalmoneySum = curEmpCalData.getPayRule().getJe().multiply(new BigDecimal("" + month));

			if (curEmpCalData.getIsSepcialPerson()) {
				//?????????????????????
				paydetail.setFreetax(new BigDecimal(salaryMsg.get("freetax") == null ? "0" : salaryMsg.get("freetax").toString()));
				BigDecimal freetaxs = new BigDecimal(salaryMsg.get("freetaxs") == null ? "0" : salaryMsg.get("freetaxs").toString());
				paydetail.setFreetaxs(freetaxs);
				thresholdSum = new BigDecimal(salaryMsg.get("startpoints").toString()).add(paydetail.getFreetax()).add(paydetail.getFreetaxs());
				threshold = thresholdSum;
			}

		} else {

			/**
			 * ??????????????????
			 * ??????>4000   ??????????????????4000
			 * ??????<=4000 ????????????????????????
			 */

			BigDecimal ordernal = outerOrinalExtraTax;
			BigDecimal freetax = curEmpCalData.getRealExtract().compareTo(ordernal) > 0 ? ordernal : curEmpCalData.getRealExtract();
			paydetail.setFreetax(freetax);

			//????????????????????????
			BigDecimal freetaxs = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getFreetax() == null ? BigDecimal.ZERO : e.getFreetax()).reduce(BigDecimal.ZERO, BigDecimal::add);
			thresholdSum = freetaxs.add(freetax).add(outerThreshold);
			threshold = thresholdSum;

			String nowmonth = curExtractBatch.substring(4, 6);
			int month = Integer.valueOf(nowmonth) - Integer.valueOf("1") + 1;
			criticalmoneySum = curEmpCalData.getPayRule().getJe().multiply(new BigDecimal("" + month));
			taxList = ExtractPayCommonData.getTaxList();

			/**
			 * ?????????????????????
			 thresholdSum = new BigDecimal("9000").multiply(new BigDecimal(""+month));
			 threshold = new BigDecimal("9000");
			 *
			 */
		}

		paydetail.setId(null);
		paydetail.setIdnumber(curEmpCalData.getIdnumber());
		paydetail.setEmpname(curEmpCalData.getEmpname());
		paydetail.setEmpno(curEmpCalData.getEmpno());
		paydetail.setCopeextract(curEmpCalData.getRealExtract());// ????????????
		paydetail.setConsotax(curEmpCalData.getConsotax());
		paydetail.setSalary(salary);
		paydetail.setSalarytax(salarytax);
		paydetail.setRealesalarytax(curEmpCalData.getConsotax().add(salarytax));
		paydetail.setFiveriskonefund(fiveriskonefund);
		paydetail.setSpecialdeduction(specialdeduction);
		paydetail.setSalarylj(salarySum);
		paydetail.setSalarytaxlj(salarytaxSum);
		paydetail.setFiveriskonefundlj(fiveriskonefundSum);
		paydetail.setSpecialdeductionlj(specialdeductionSum);
		paydetail.setThreshold(threshold);
		paydetail.setThresholdlj(thresholdSum);
		paydetail.setCriticalmoney(curEmpCalData.getPayRule().getJe());
		paydetail.setCriticalmoneylj(criticalmoneySum);

		//??????????????????????????????
		BigDecimal copeextractSum = BigDecimal.ZERO;
		//???????????????????????????
		BigDecimal consotaxSum = BigDecimal.ZERO;
		if (!CollectionUtils.isEmpty(curEmpCalData.getCurBillUnitPayDetailList())) {
			copeextractSum = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getCopeextract()).reduce(BigDecimal.ZERO, BigDecimal::add);
			consotaxSum = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getConsotax()).reduce(BigDecimal.ZERO, BigDecimal::add);
		}
		paydetail.setCopeextractlj(curEmpCalData.getRealExtract().add(copeextractSum));// ??????????????????
		paydetail.setConsotaxlj(curEmpCalData.getConsotax().add(consotaxSum));
		paydetail.setRealesalarytaxlj(paydetail.getConsotaxlj().add(salarytaxSum));

		// ????????????????????????????????????
		BigDecimal dtje = getDtje(paydetail.getRealesalarytaxlj(), taxList).add(paydetail.getThresholdlj()).add(paydetail.getSpecialdeductionlj());
		paydetail.setDtje(dtje);


		// ????????????(????????????-??????????????????(?????????)-??????????????????-????????????????????????(????????????))
		BigDecimal incorporatedcompanyPayedExtract = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal renewableextract = dtje.subtract(paydetail.getSalarylj()).subtract(paydetail.getRealesalarytaxlj()).subtract(incorporatedcompanyPayedExtract);
		paydetail.setRenewableextract(renewableextract);

		/** 5.39% ?????????????????????(????????????????????????,????????????) - ??????????????????(?????????) +
		 * ??????????????????(?????????) -????????????????????????(????????????????????????????????????????????????)???
		 */
		BigDecimal renewableextract539 = paydetail.getCriticalmoneylj().subtract(paydetail.getSalarylj()).add(paydetail.getSpecialdeductionlj()).subtract(incorporatedcompanyPayedExtract);
		paydetail.setRenewableextract539(renewableextract539);

		// ???????????? Max(??????????????????,????????????5.39%??????????????????)
		paydetail.setCompanyableextract(renewableextract.compareTo(renewableextract539) >= 0 ? renewableextract : renewableextract539);


		/**
		 * ???????????????????????? ????????????0
		 */
		BigDecimal curIncorporatedcompanyPayExtract = paydetail.getCopeextract().compareTo(paydetail.getCompanyableextract()) >= 0 ? paydetail.getCompanyableextract() : paydetail.getCopeextract();
		curIncorporatedcompanyPayExtract = curIncorporatedcompanyPayExtract.compareTo(BigDecimal.ZERO) >= 0 ? curIncorporatedcompanyPayExtract : BigDecimal.ZERO;
		/**
		 * ?????????????????????  update by minzhq 2021-12???
		 */
		curIncorporatedcompanyPayExtract = curIncorporatedcompanyPayExtract.setScale(0, BigDecimal.ROUND_DOWN);
		paydetail.setIncorporatedcompany(curIncorporatedcompanyPayExtract);

		// ??????????????????????????????????????????
		paydetail.setIncorporatedcompanylj(incorporatedcompanyPayedExtract);

		// ????????????????????????????????? - ???????????????????????????
		paydetail.setAviodtax(paydetail.getCopeextract().subtract(paydetail.getIncorporatedcompany()));

		/**
		 * ????????????????????????????????????+?????????
		 */
		Integer month = Integer.valueOf(curExtractBatch.substring(0, 6));
		Long count = curEmpCalData.getCurBillUnitPayDetailList().stream().filter(e -> e.getExtractmonth().contains(month + "")).count();
		if (count.intValue() == 0) {
			paydetail.setRealeextract(paydetail.getSalary().add(paydetail.getIncorporatedcompany()));
		} else {
			paydetail.setRealeextract(paydetail.getIncorporatedcompany());
		}
		// ????????????????????????
		paydetail.setRealeextractsum(paydetail.getSalarylj().add(paydetail.getIncorporatedcompany()).add(incorporatedcompanyPayedExtract));

		/*
		 * ???????????????????????? ??? ??????????????????????????????????????????<= ???????????????????????? +
		 * ???????????????????????????????????????????????????????????????????????? MAX(??????????????????????????????????????????- ????????????????????????
		 * - ????????????????????? -
		 * {0;252;1692;3192;5292;8592;18192}*10???/(1-{
		 * 3;10;20;25;30;35;45}%)) + ???????????????????????? + ?????????????????????
		 */
		BigDecimal sfdtje = paydetail.getRealeextractsum().compareTo(paydetail.getSpecialdeductionlj().add(paydetail.getThresholdlj())) <= 0 ? paydetail.getRealeextractsum() : getSfdtje(paydetail.getRealeextractsum(), paydetail.getSpecialdeductionlj(), paydetail.getThresholdlj(), taxList);
		paydetail.setSfdtje(sfdtje);

		/**
		 * ????????????????????????
		 */
		BigDecimal payableTaxs = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getPayabletax()).reduce(BigDecimal.ZERO, BigDecimal::add);
		//??????????????????
		BigDecimal payabletax = getPayableTax(sfdtje, paydetail.getSpecialdeductionlj(), paydetail.getThresholdlj(), payableTaxs, taxList);
		paydetail.setPayabletax(payabletax);
		paydetail.setPayabletaxlj(payableTaxs.add(payabletax));


		//????????????????????? ?????????+??????????????????
		BigDecimal taxDiffrence = curEmpCalData.getCurBillUnitPayDetailList().stream()
				.filter(e -> e.getExtractmonth().contains((month.intValue() - 1) + "")
						|| e.getExtractmonth().substring(0, 6)
						.equals(curExtractBatch.substring(0, 6)))
				.map(e -> e.getTaxdiffrence())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		//??????????????????
		paydetail.setTaxdiffrence(paydetail.getPayabletax().subtract(paydetail.getRealesalarytax()).add(taxDiffrence));

		// ???????????? ?????????????????????(????????????) / (????????????(????????????) + ????????????????????????)???
		paydetail.setReleasepercent(paydetail.getPayabletaxlj().divide(paydetail.getFiveriskonefundlj().add(paydetail.getSfdtje()), 5, BigDecimal.ROUND_HALF_DOWN));

		paydetail.setTotalextract(paydetail.getCopeextract());
		paydetail.setIncorporatedcompanyfee(BigDecimal.ZERO);
		paydetail.setTotalaviodtax(paydetail.getAviodtax());
		paydetail.setCreatetime(currentDate);
		this.payDetailMapper.insert(paydetail);
		return paydetail;
	}

	/**
	 * ??????????????????
	 *
	 * @param sfdtje
	 * @param specialdeductionlj
	 * @param thresholdlj
	 * @param sjgs
	 * @param taxList
	 * @return
	 */
	private static BigDecimal getPayableTax(BigDecimal sfdtje,
	                                        BigDecimal specialdeductionlj, BigDecimal thresholdlj,
	                                        BigDecimal sjgs, List<Map> taxList) {
		BigDecimal result = null;
		BigDecimal tmpresult = null;
		// ????????????
		for (Map taxb : taxList) {
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// ?????????
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// ??????
			tmpresult = sfdtje
					.subtract(specialdeductionlj)
					.subtract(thresholdlj)
					.multiply(a.divide(new BigDecimal("100"), 5, BigDecimal.ROUND_HALF_DOWN))
					.subtract(b.multiply(new BigDecimal("12")));
			if (null == result || tmpresult.compareTo(result) > 0) {
				result = tmpresult;
			}
		}
		return result.subtract(sjgs).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO
				: result.subtract(sjgs);
	}

	// ????????????????????????
	private BigDecimal getSfdtje(BigDecimal realeextractsum,
	                             BigDecimal specialdeductionlj, BigDecimal thresholdlj,
	                             List<Map> taxList) {
		if (taxList == null) return BigDecimal.ZERO;
		BigDecimal result = null;
		BigDecimal tmpresult = null;
		// ????????????
		for (Map taxb : taxList) {
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// ?????????
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// ??????
			tmpresult = realeextractsum.subtract(specialdeductionlj)
					.subtract(thresholdlj)
					.subtract(b.multiply(new BigDecimal("12")));
			tmpresult = tmpresult.divide(new BigDecimal("1").subtract(a.divide(
					new BigDecimal("100"), 5, BigDecimal.ROUND_HALF_DOWN)), 5,
					BigDecimal.ROUND_HALF_DOWN);
			if (null == result || tmpresult.compareTo(result) > 0) {
				result = tmpresult;
			}
		}
		return result.add(specialdeductionlj).add(thresholdlj);
	}

	// ??????????????????
	private BigDecimal getDtje(BigDecimal realesalarytaxlj, List<Map> taxList) {
		//if(taxList==null) return BigDecimal.ZERO;
		BigDecimal result = null;
		BigDecimal tmpresult = null;
		// ????????????
		for (Map taxb : taxList) {
			if (null == taxb.get("quickcal")) throw new RuntimeException("???????????????????????????????????????!");
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// ?????????
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// ??????
			tmpresult = realesalarytaxlj.add(b.multiply(new BigDecimal("12")))
					.divide(a.divide(new BigDecimal("100")), 5,
							BigDecimal.ROUND_HALF_DOWN);
			if (result == null) result = tmpresult;
			else if (tmpresult.compareTo(result) <= 0) {
				result = tmpresult;
			}
		}
		return result;
	}


	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param list_lendmoney
	 * @param isbx
	 * @return
	 */
	private List<BudgetLendmoney> getCanRepayLendMoneyList(List<BudgetLendmoney> list_lendmoney) {
		List<BudgetLendmoney> repayForLendMoneyList = new ArrayList<>();
		// ??????????????????
		if (list_lendmoney != null && !list_lendmoney.isEmpty()) {
			for (BudgetLendmoney lendmoney : list_lendmoney) {
				// ????????????
				Integer lendtype = lendmoney.getLendtype();
				if (lendtype != null && (lendtype.intValue() == LendTypeEnum.LEND_TYPE_13.getType())) {
					// ????????????
					Boolean flushingflag = lendmoney.getFlushingflag();
					Date planpaydate = lendmoney.getPlanpaydate();
					Boolean chargebillflag = lendmoney.getChargebillflag();
					if ((flushingflag != null && !flushingflag) || (flushingflag == null && chargebillflag)) {
						//????????? ??????  ???????????????+????????????
						if (planpaydate.getTime() <= new Date().getTime()) {
							// ???????????????
							if (flushingflag != null && (!flushingflag || chargebillflag)) {
								repayForLendMoneyList.add(lendmoney);
							}
						}
					}
				} else if (lendtype != null) {
					//
					Date planpaydate = lendmoney.getPlanpaydate();
					if (planpaydate.getTime() <= new Date().getTime()) {
						// ???????????????
						repayForLendMoneyList.add(lendmoney);
					}
				}
			}
		}
		return repayForLendMoneyList;
	}

	/**
	 * ??????????????????
	 *
	 * @param brm
	 * @param lendmoney
	 * @param repaymoney
	 * @param interestmoney
	 */
	private void generateRepayDetail(BudgetRepaymoney brm,
	                                 BudgetLendmoney lendmoney, BigDecimal repaymoney, BigDecimal interestmoney) {
		BudgetRepaymoneyDetail repaymoneyDetail = new BudgetRepaymoneyDetail();
		repaymoneyDetail.setId(null);
		repaymoneyDetail.setRepaymoneyid(brm.getId());
		repaymoneyDetail.setLendmoneyid(lendmoney.getId());
		repaymoneyDetail.setCurlendmoney(lendmoney.getLendmoney().subtract(lendmoney.getRepaidmoney()).subtract(lendmoney.getRepaidinterestmoney()));
		repaymoneyDetail.setRepaymoney(repaymoney);
		repaymoneyDetail.setInterestmoney(interestmoney);
		repaymoneyDetail.setNowlendmoney(repaymoneyDetail.getCurlendmoney().subtract(repaymoneyDetail.getRepaymoney()).subtract(repaymoneyDetail.getInterestmoney()));
		repaymoneyDetail.setCreatetime(new Date());
		this.repaymoneyDetailMapper.insert(repaymoneyDetail);
	}

	/**
	 * ???????????????
	 *
	 * @param extractDetails
	 * @param map
	 */
	private void extractDeduction(List<BudgetExtractdetail> extractDetails, Map<String, List<BudgetLendmoney>> map) {
		Date currentdate = new Date();
		//????????????????????????????????????
		List<BudgetLendmoney> curEmpnoLendMoneyList = map.get(extractDetails.get(0).getEmpno());
		//if(curEmpnoLendMoneyList==null || curEmpnoLendMoneyList.isEmpty()) return;
		// ????????????????????????????????????
		List<BudgetLendmoney> canRepayLendMoneyList = getCanRepayLendMoneyList(curEmpnoLendMoneyList);
		for (BudgetExtractdetail extractDetail : extractDetails) {
			if (extractDetail.getIscompanyemp()) {

				BigDecimal copeextract = extractDetail.getCopeextract();// ????????????
				BigDecimal totalwithholdmoney = BigDecimal.ZERO;
				// ????????????>0 && ????????????>0 ??????????????????
				if (!canRepayLendMoneyList.isEmpty() && copeextract.compareTo(BigDecimal.ZERO) > 0) {
					extractDetail.setUpdatetime(currentdate);
					// ??????????????????
					BudgetRepaymoney brm = new BudgetRepaymoney();
					brm.setId(null);
					brm.setEmpid(extractDetail.getEmpid());
					brm.setEmpno(extractDetail.getEmpno());
					brm.setEmpname(extractDetail.getEmpname());
					brm.setRepaydate(currentdate);
					brm.setRepaytype(4);
					// ??????id
					brm.setRepaytypeid(extractDetail.getId().toString());
					brm.setCreatetime(currentdate);
					brm.setRepaymoney(BigDecimal.ZERO);
					brm.setEffectflag(false);
					brm.setRepaymoneycode(distributedNumber.getRepayNum());
					this.repaymoneyMapper.insert(brm);
					// ???????????????????????? (????????????????????????,????????????????????????)
					List<BudgetLendmoney> sortByLendDateList = canRepayLendMoneyList.stream()
							.sorted((e1, e2) -> Long.compare(e1.getPlanpaydate().getTime(), e2.getPlanpaydate().getTime()))
							.sorted((e1, e2) -> Long.compare(e1.getLenddate().getTime(), e2.getLenddate().getTime()))
							.collect(Collectors.toList());

					// ????????????
					BigDecimal copeexSubConsotax = copeextract;
					for (int i = 0; i < sortByLendDateList.size(); i++) {
						BudgetLendmoney earlyLendMoney = sortByLendDateList.get(i);
						if (copeexSubConsotax.compareTo(BigDecimal.ZERO) > 0) {

							BigDecimal lendmoney = earlyLendMoney.getLendmoney();
							BigDecimal interestmoney = earlyLendMoney.getInterestmoney();
							BigDecimal repaidinterestmoney = earlyLendMoney.getRepaidinterestmoney();
							BigDecimal repaidmoney = earlyLendMoney.getRepaidmoney();
							boolean flag = false;
							if (earlyLendMoney.getLendtype().intValue() == 4) {
								Boolean flushingflag = earlyLendMoney.getFlushingflag();
								Boolean chargebillflag = earlyLendMoney.getChargebillflag();
								if (flushingflag == null && chargebillflag) {
									//????????????????????????
									flag = true;
								}
							}
							//????????????
							BigDecimal leftlendmoney = lendmoney.subtract(repaidmoney);
							//????????????
							BigDecimal leftinterestmoney = interestmoney.subtract(repaidinterestmoney);
							if (!flag) {
								BigDecimal copeexsubleftlendmoney = copeexSubConsotax.subtract(leftlendmoney);//10
								if (copeexsubleftlendmoney.compareTo(BigDecimal.ZERO) >= 0) {
									if (copeexsubleftlendmoney.subtract(leftinterestmoney).compareTo(BigDecimal.ZERO) >= 0) {
										//?????????????????????
										generateRepayDetail(brm, earlyLendMoney, leftlendmoney, leftinterestmoney);
										earlyLendMoney.setRepaidmoney(repaidmoney.add(leftlendmoney.add(leftinterestmoney)));
										earlyLendMoney.setRepaidinterestmoney(repaidinterestmoney.add(leftinterestmoney));
										this.lendmoneyMapper.updateById(earlyLendMoney);
										totalwithholdmoney = totalwithholdmoney.add(leftlendmoney.add(leftinterestmoney));
										copeexSubConsotax = copeexSubConsotax.subtract(leftlendmoney.add(leftinterestmoney));
									} else {
										generateRepayDetail(brm, earlyLendMoney, leftlendmoney, copeexsubleftlendmoney);
										earlyLendMoney.setRepaidmoney(leftlendmoney.add(copeexsubleftlendmoney).add(repaidmoney));
										earlyLendMoney.setRepaidinterestmoney(repaidinterestmoney.add(copeexsubleftlendmoney));
										this.lendmoneyMapper.updateById(earlyLendMoney);
										totalwithholdmoney = totalwithholdmoney.add(leftlendmoney.add(copeexsubleftlendmoney));
										copeexSubConsotax = copeexSubConsotax.subtract(leftlendmoney.add(copeexsubleftlendmoney));
									}
								} else {
									generateRepayDetail(brm, earlyLendMoney, copeexSubConsotax, BigDecimal.ZERO);
									earlyLendMoney.setRepaidmoney(repaidmoney.add(copeexSubConsotax));
									earlyLendMoney.setRepaidinterestmoney(BigDecimal.ZERO);
									this.lendmoneyMapper.updateById(earlyLendMoney);
									totalwithholdmoney = totalwithholdmoney.add(copeexSubConsotax);
									copeexSubConsotax = BigDecimal.ZERO;
								}
							} else {
								if (copeexSubConsotax.subtract(leftlendmoney).compareTo(BigDecimal.ZERO) >= 0) {
									generateRepayDetail(brm, earlyLendMoney, leftlendmoney, BigDecimal.ZERO);
									earlyLendMoney.setRepaidmoney(repaidmoney.add(leftlendmoney));
									this.lendmoneyMapper.updateById(earlyLendMoney);
									totalwithholdmoney = totalwithholdmoney.add(leftlendmoney);
									copeexSubConsotax = copeexSubConsotax.subtract(leftlendmoney);
								} else {
									generateRepayDetail(brm, earlyLendMoney, copeexSubConsotax, BigDecimal.ZERO);
									earlyLendMoney.setRepaidmoney(repaidmoney.add(copeexSubConsotax));
									this.lendmoneyMapper.updateById(earlyLendMoney);
									totalwithholdmoney = totalwithholdmoney.add(copeexSubConsotax);
									copeexSubConsotax = copeexSubConsotax.subtract(copeexSubConsotax);
								}
							}
						} else {
							break;
						}

					}
					if (copeexSubConsotax.compareTo(BigDecimal.ZERO) >= 0) {
						extractDetail.setWithholdmoney(totalwithholdmoney);
						extractDetail.setRealextract(copeexSubConsotax);
						brm.setRepaymoney(totalwithholdmoney);
					}
					createRepaymoneyArrears(brm);
					extractDetail.setRepaymoneyid(brm.getId());
					this.extractDetailMapper.updateById(extractDetail);

				} else {
					extractDetail.setWithholdmoney(BigDecimal.ZERO);
					extractDetail.setUpdatetime(currentdate);
					extractDetail.setRealextract(copeextract);
					this.extractDetailMapper.updateById(extractDetail);
				}
			} else {
				extractDetail.setWithholdmoney(BigDecimal.ZERO);
				extractDetail.setRealextract(extractDetail.getCopeextract());
				this.extractDetailMapper.updateById(extractDetail);
			}
		}

	}

	/**
	 * ????????????????????????
	 *
	 * @param budgetRepayMoney
	 */
	private void createRepaymoneyArrears(BudgetRepaymoney budgetRepayMoney) {
		synchronized (budgetRepayMoney.getEmpno()) {
			BudgetArrears arrears = this.arrearsMapper.selectOne(new QueryWrapper<BudgetArrears>().eq("empno", budgetRepayMoney.getEmpno()));
			BudgetLendandrepaymoney l_r_money = new BudgetLendandrepaymoney();
			if (arrears != null) {
				l_r_money.setId(null);
				l_r_money.setEmpid(budgetRepayMoney.getEmpid());
				l_r_money.setEmpno(budgetRepayMoney.getEmpno());
				l_r_money.setEmpname(budgetRepayMoney.getEmpname());
				l_r_money.setRepaymoneyid(budgetRepayMoney.getId());
				l_r_money.setCurmoney(arrears.getArrearsmoeny()); //????????????
				budgetRepayMoney.setEffectflag(true);
				l_r_money.setMoney(budgetRepayMoney.getRepaymoney());
				l_r_money.setMoneytype(-1); // ??????
				l_r_money.setNowmoney(arrears.getArrearsmoeny().subtract(budgetRepayMoney.getRepaymoney()));
				l_r_money.setCreatetime(new Date());
				this.lendandrepaymoneyMapper.insert(l_r_money);
				arrears.setArrearsmoeny(l_r_money.getNowmoney());
				arrears.setRepaymoney(arrears.getRepaymoney().add(l_r_money.getMoney()));
				this.arrearsMapper.updateById(arrears);

			}
			List<BudgetRepaymoney> repaymoneylist = this.repaymoneyMapper.selectList(new QueryWrapper<BudgetRepaymoney>().eq("empid", budgetRepayMoney.getEmpid()));
			if (repaymoneylist.size() == 0) {
				throw new RuntimeException("error!!!!!");
			}
			BigDecimal totalrepaymoney = new BigDecimal(0);
			for (BudgetRepaymoney repaymoney : repaymoneylist) {
				totalrepaymoney = totalrepaymoney.add(repaymoney.getRepaymoney());
			}
			budgetRepayMoney.setNowrepaymoney(totalrepaymoney);
			this.repaymoneyMapper.updateById(budgetRepayMoney);
		}
	}


	/**
	 * ??????????????????(???????????????)
	 *
	 * @param curEmpCalData
	 */
	private void handleQuitEmp(ExtractEmpCalDataDetail curEmpCalData, String extractBatch) {
		BudgetExtractpaydetail paydetail = new BudgetExtractpaydetail();
		paydetail.setEmpno(curEmpCalData.getEmpno());
		paydetail.setEmpname(curEmpCalData.getEmpname());
		paydetail.setCopeextract(curEmpCalData.getCopeextract());
		paydetail.setConsotax(curEmpCalData.getConsotax());
		paydetail.setIscompanyemp(true);
		paydetail.setIdnumber(curEmpCalData.getIdnumber());
		paydetail.setAviodtax(curEmpCalData.getCopeextract());
		paydetail.setExtractmonth(extractBatch);
		paydetail.setTotalextract(curEmpCalData.getCopeextract());
		paydetail.setCreatetime(new Date());
		this.payDetailMapper.insert(paydetail);

		BudgetExtractpayment bep = new BudgetExtractpayment();
		bep.setBeforeCalFee(curEmpCalData.getFeePay());
		String detailIds = curEmpCalData.getExtractDetails().stream().map(e -> e.getId().toString()).collect(Collectors.joining(","));
		bep.setExtractdetailids(detailIds);
		bep.setBudgetextractpaydetailid(paydetail.getId());
		bep.setBunitid2(curEmpCalData.getBillingUnit().getId());
		bep.setBunitname2(curEmpCalData.getBillingUnit().getName());
		bep.setBunitbankaccount2(curEmpCalData.getUnitAccount().getBankaccount());
		bep.setBunitaccountbranchcode2(curEmpCalData.getUnitAccount().getBranchcode());
		bep.setBunitaccountbranchname2(curEmpCalData.getUnitAccountBank().getBankName());
		bep.setPaymoney2(curEmpCalData.getCopeextract());
		bep.setCreatetime(new Date());
		if (curEmpCalData.getIscompanyemp()) {
			bep.setBankaccount(curEmpCalData.getPersonAccount().getBankaccount());
			bep.setBankaccountbranchcode(curEmpCalData.getPersonAccount().getBranchcode());
			bep.setBankaccountbranchname(curEmpCalData.getPersonAccountBank().getBankName());
			bep.setBankaccountname(curEmpCalData.getEmpname());
			bep.setBankaccountopenbank(curEmpCalData.getPersonAccountBank().getSubBranchName());
		} else {
			bep.setBankaccount(curEmpCalData.getOuterPersonAccount().bankAccount);
			bep.setBankaccountbranchcode(curEmpCalData.getOuterPersonAccount().branchcode);
			bep.setBankaccountbranchname(curEmpCalData.getOuterPersonAccount().bankName);
			bep.setBankaccountname(curEmpCalData.getEmpname());
			bep.setBankaccountopenbank(curEmpCalData.getOuterPersonAccount().subBranchName);
		}
		this.paymentMapper.insert(bep);

		BudgetExtractgrantlog log = new BudgetExtractgrantlog();
		log.setId(null);
		log.setExtractmonth(extractBatch);
		log.setIscompanyemp(true);
		log.setIdnumber(curEmpCalData.getIdnumber());
		log.setEmpno(curEmpCalData.getEmpno());
		log.setEmpname(curEmpCalData.getEmpname());
		log.setBillingunitid(curEmpCalData.getBillingUnit().getId());
		log.setBillingunitname(curEmpCalData.getBillingUnit().getName());
		log.setExcessmoney(BigDecimal.ZERO);
		log.setOrderno(999);
		/**
		 * ???????????? ????????????????????????  ?????????-1???????????????????????????
		 */
		log.setAlreadygrantmoney(new BigDecimal("-1"));
		log.setShouldgrantextract(curEmpCalData.getCopeextract());
		log.setCouldgrantextract(curEmpCalData.getCopeextract());
		log.setCratetime(new Date());
		this.grantLogMapper.insert(log);


		for (BudgetExtractdetail detail : curEmpCalData.getExtractDetails()) {
			detail.setExcessmoney(BigDecimal.ZERO);
			detail.setExcesstype(ExtractExcessTypeEnum.NOEXCESS.getType());
			detail.setHandleflag(false);
			detail.setRealextract(detail.getCopeextract().subtract(detail.getWithholdmoney() == null ? BigDecimal.ZERO : detail.getWithholdmoney()));
			this.extractDetailMapper.updateById(detail);
		}
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param extractDetails
	 * @param extractPayCommonData
	 * @return
	 */
	private ExtractEmpCalDataDetail packageExtractEmpCalData(Object obj, ExtractPayCommonData extractPayCommonData) {
		ExtractEmpCalDataDetail dataDetail = new ExtractEmpCalDataDetail();

		if (obj instanceof List) {
			List<BudgetExtractdetail> extractDetails = (List<BudgetExtractdetail>) obj;
			//????????????????????????
			String empno = extractDetails.get(0).getEmpno();
			dataDetail.setEmpno(empno);
			dataDetail.setEmpid(extractDetails.get(0).getEmpid());
			dataDetail.setIdnumber(extractDetails.get(0).getIdnumber());
			//????????????????????????
			String empname = extractDetails.get(0).getEmpname();
			dataDetail.setEmpname(empname);
			//????????????????????????????????????
			Boolean iscompanyemp = extractDetails.get(0).getIscompanyemp();
			dataDetail.setIscompanyemp(iscompanyemp);
			BigDecimal copeextract = extractDetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal consotax = extractDetails.stream().map(BudgetExtractdetail::getConsotax).reduce(BigDecimal.ZERO, BigDecimal::add);
			dataDetail.setConsotax(consotax);

			/**
			 * ?????????????????????????????? add by minzhq 2021-12-20
			 */
			List<BudgetExtractFeePayDetailBeforeCal> budgetExtractFeePayDetails = extractPayCommonData.getFeePayEmpMap().get(empno);
			if (!CollectionUtils.isEmpty(budgetExtractFeePayDetails)) {
				BigDecimal feePayDetails = budgetExtractFeePayDetails.stream().map(e -> e.getFeePay() == null ? BigDecimal.ZERO : e.getFeePay()).reduce(BigDecimal.ZERO, BigDecimal::add);
				dataDetail.setFeePay(feePayDetails);
				//copeextract = copeextract.subtract(feePayDetails);
			}
			if (copeextract.compareTo(BigDecimal.ZERO) < 0) {
				throw new RuntimeException("????????????????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")??????????????????????????????");
			}
			dataDetail.setCopeextract(copeextract);

		} else if (obj instanceof BudgetExtractdetail) {
			//?????????????????????????????????
			BudgetExtractdetail bed = (BudgetExtractdetail) obj;
			dataDetail.setEmpno(bed.getEmpno());
			dataDetail.setIdnumber(bed.getIdnumber());
			dataDetail.setEmpname(bed.getEmpname());
			dataDetail.setIscompanyemp(false);
			dataDetail.setCopeextract(bed.getRealextract());
			dataDetail.setRealExtract(bed.getRealextract());
			dataDetail.setConsotax(BigDecimal.ZERO);
		}
		if (dataDetail.getIscompanyemp()) {
			//????????????????????????????????????
			HrSalaryYearTaxUser m = extractPayCommonData.getSpecialPersonNameList().stream().filter(e -> dataDetail.getEmpno().equals(e.getEmpno()) && dataDetail.getIdnumber().equals(e.getCertno())).findFirst().orElse(null);
			dataDetail.setIsSepcialPerson(m == null ? false : true);
			Map<String, Object> salaryMap = (Map<String, Object>) extractPayCommonData.getSalaryMsg().get(dataDetail.getEmpno());
			dataDetail.setSalaryMap(salaryMap);
			if (extractPayCommonData.getUserMap().get(dataDetail.getEmpno()).getStatus().compareTo(BigDecimal.ZERO) == 0) {
				//????????????
				dataDetail.setIsQuit(true);
				dataDetail.setBillingUnit(extractPayCommonData.getQuiterBillingUnit());
				dataDetail.setUnitAccount(extractPayCommonData.getQuiterBillingUnitAccount());
				dataDetail.setUnitAccountBank(extractPayCommonData.getQuiterWbBank());
				BudgetBankAccount bankAccount = extractPayCommonData.getBankAccountMap().get(dataDetail.getEmpno() + "_" + dataDetail.getEmpname());
				if (bankAccount == null)
					throw new RuntimeException("???????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????!");
				dataDetail.setPersonAccount(bankAccount);
				WbBanks wbBank = extractPayCommonData.getBanksMap().get(bankAccount.getBranchcode());
				if (wbBank == null)
					throw new RuntimeException("???????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????!");
				dataDetail.setPersonAccountBank(wbBank);
			} else if (extractPayCommonData.getUserMap().get(dataDetail.getEmpno()).getStatus().compareTo(BigDecimal.ZERO) == 1) {
				if (salaryMap == null)
					throw new RuntimeException("?????????????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")??????????????????");
				//??????????????????outkey
				String companyidOutKey = salaryMap.get("salarycompanyid").toString();
				List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit -> companyidOutKey.equals(billingunit.getOutKey())).collect(Collectors.toList());
				if (billingUnitList.isEmpty())
					throw new RuntimeException("??????????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")????????????????????????,outkey??????" + companyidOutKey + "???");
				if (billingUnitList.size() > 1)
					throw new RuntimeException("?????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????????????????,outkey??????" + companyidOutKey + "???");
				BudgetBillingUnit billingUnit = billingUnitList.get(0);
				dataDetail.setBillingUnit(billingUnit);

				List<BudgetBillingUnitAccount> unitAccountList = extractPayCommonData.getBillingAccountMap().get(billingUnit.getId());
				/**
				 * ?????? ?????????????????????????????????
				 */
				Comparator<BudgetBillingUnitAccount> c1 = (e1, e2) -> Boolean.compare(e2.getDefaultflag(), e1.getDefaultflag());
				Comparator<BudgetBillingUnitAccount> c2 = (e1, e2) -> Integer.compare(e2.getOrderno(), e1.getOrderno());
				unitAccountList = unitAccountList.stream().sorted(c1.thenComparing(c2)).collect(Collectors.toList());
				if (unitAccountList.isEmpty())
					throw new RuntimeException("?????????????????????" + billingUnit.getName() + "?????????????????????");
				BudgetBillingUnitAccount billingUnitAccount = unitAccountList.get(0);
				dataDetail.setUnitAccount(billingUnitAccount);
				WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(billingUnitAccount.getBranchcode());
				if (billingUnitBank == null) throw new RuntimeException("?????????????????????" + billingUnit.getName() + "?????????????????????!");
				dataDetail.setUnitAccountBank(billingUnitBank);
				BudgetBankAccount bankAccount = extractPayCommonData.getBankAccountMap().get(dataDetail.getEmpno() + "_" + dataDetail.getEmpname());
				if (bankAccount == null)
					throw new RuntimeException("?????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????!");
				dataDetail.setPersonAccount(bankAccount);
				WbBanks wbBank = extractPayCommonData.getBanksMap().get(bankAccount.getBranchcode());
				if (wbBank == null)
					throw new RuntimeException("?????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????!");
				dataDetail.setPersonAccountBank(wbBank);
			}
		} else {
			/**
			 * ????????????????????????????????????
			 */
			dataDetail.setBillingUnit(extractPayCommonData.getOuterBillingUnit());
			dataDetail.setUnitAccount(extractPayCommonData.getOuterBillingUnitAccount());
			dataDetail.setUnitAccountBank(extractPayCommonData.getOuterWbBank());

			BudgetExtractOuterperson outperson = extractPayCommonData.getOutPersonMap().get(dataDetail.getIdnumber());
			if (outperson == null)
				throw new RuntimeException("???????????????" + dataDetail.getEmpname() + "(" + dataDetail.getEmpno() + ")????????????!");
			//?????????????????????????????????
			if (outperson.getBudgetbillingunitid() != null) {

				List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit -> billingunit.getId().equals(outperson.getBudgetbillingunitid())).collect(Collectors.toList());
				if (billingUnitList.isEmpty())
					throw new RuntimeException("????????????????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")????????????????????????");
				BudgetBillingUnit billingUnit = billingUnitList.get(0);
				dataDetail.setBillingUnit(billingUnit);

				List<BudgetBillingUnitAccount> unitAccountList = extractPayCommonData.getBillingAccountMap().get(billingUnit.getId());
				if (unitAccountList == null || unitAccountList.isEmpty())
					throw new RuntimeException("?????????????????????" + billingUnit.getName() + "?????????????????????");
				/**
				 * ?????? ?????????????????????????????????
				 */
				Comparator<BudgetBillingUnitAccount> c1 = (e1, e2) -> Boolean.compare(e2.getDefaultflag(), e1.getDefaultflag());
				Comparator<BudgetBillingUnitAccount> c2 = (e1, e2) -> Integer.compare(e2.getOrderno(), e1.getOrderno());
				unitAccountList = unitAccountList.stream().sorted(c1.thenComparing(c2)).collect(Collectors.toList());
				if (unitAccountList.isEmpty())
					throw new RuntimeException("?????????????????????" + billingUnit.getName() + "?????????????????????");
				BudgetBillingUnitAccount billingUnitAccount = unitAccountList.get(0);
				dataDetail.setUnitAccount(billingUnitAccount);

				WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(billingUnitAccount.getBranchcode());
				if (billingUnitBank == null) throw new RuntimeException("?????????????????????" + billingUnit.getName() + "?????????????????????!");
				dataDetail.setUnitAccountBank(billingUnitBank);

			}
			WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(outperson.getBranchcode());
			if (billingUnitBank == null)
				throw new RuntimeException("???????????????" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")?????????????????????!");
			dataDetail.setOuterPersonAccount(dataDetail.createOuterPersonAccountData(outperson.getBankaccount(), outperson.getBranchcode(), billingUnitBank.getBankName(), billingUnitBank.getSubBranchName()));
		}

		if (dataDetail.getIsQuit()) return dataDetail;

		BudgetExtractpayRule extractpayRule = extractPayCommonData.getPayRuleList().stream().filter(e -> ("," + e.getBillunitids() + ",").contains("," + dataDetail.getBillingUnit().getId().toString() + ",")).findFirst().orElse(null);
		if (extractpayRule == null)
			throw new RuntimeException("?????????????????????" + dataDetail.getBillingUnit().getName() + "???????????????????????????");
		dataDetail.setPayRule(extractpayRule);

		if (extractpayRule.getPersonunitid() == null)
			throw new RuntimeException("?????????????????????" + dataDetail.getBillingUnit().getName() + "??????????????????????????????????????????????????????");
		Long personunitid = extractpayRule.getPersonunitid(); //????????????????????????

		BudgetBillingUnitAccount avoidAccount = extractPayCommonData.getBillingAccountList().stream().filter(e -> e.getId().toString().equals(personunitid.toString())).findFirst().orElse(null);

		//List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit->billingunit.getId().equals(extractpayRule.getPersonunitid())).collect(Collectors.toList());
		if (avoidAccount == null) throw new RuntimeException("?????????????????????" + extractpayRule.getName() + ")????????????????????????????????????");
		BudgetBillingUnit avoidBillingUnit = extractPayCommonData.getAllBillingUnitList().stream().filter(e -> e.getId().toString().equals(avoidAccount.getBillingunitid().toString())).findFirst().get();
		dataDetail.setAvoidBillingUnit(avoidBillingUnit);
		dataDetail.setAvoidUnitAccount(avoidAccount);
		WbBanks avoidBillingUnitBank = extractPayCommonData.getBanksMap().get(avoidAccount.getBranchcode());
		if (avoidBillingUnitBank == null)
			throw new RuntimeException("?????????????????????" + avoidBillingUnit.getName() + "?????????????????????!");
		dataDetail.setAvoidUnitAccountBank(avoidBillingUnitBank);


		BudgetExtractquotaRule extractquotaRule = extractPayCommonData.getQuotaruleList().stream().filter(e -> ("," + e.getBillunitids() + ",").contains("," + dataDetail.getBillingUnit().getId().toString() + ",")).findFirst().orElse(null);
		if (extractquotaRule == null)
			throw new RuntimeException("?????????????????????" + dataDetail.getBillingUnit().getName() + "???????????????????????????");
		dataDetail.setQuotaRule(extractquotaRule);
		Map<Long, List<BudgetExtractpaydetail>> map = extractPayCommonData.getAgoPayDetailMap().get(dataDetail.getIdnumber());
		List<BudgetExtractpaydetail> curBillingUnitPaydetail = map == null ? Lists.newArrayList() : map.get(dataDetail.getBillingUnit().getId());
		dataDetail.setCurBillUnitPayDetailList(curBillingUnitPaydetail == null ? new ArrayList<>() : curBillingUnitPaydetail);

		//??????????????????????????????????????????
		List<BudgetExtractOuterperson> refOuterPersonList = new ArrayList<>();
		extractPayCommonData.getOutPersonMap().forEach((idnumber, outperson) -> {
			if (StringUtils.isNotBlank(outperson.getReferidnumber()) && outperson.getReferidnumber().equals(dataDetail.getIdnumber()))
				refOuterPersonList.add(outperson);
		});
		;
		//?????????????????????????????????????????????
		List<BudgetExtractOuterperson> sortEdRefOuterPersonList = refOuterPersonList.stream().sorted(Comparator.comparing(BudgetExtractOuterperson::getOrderno).reversed()).collect(Collectors.toList());
		dataDetail.setRefOuterPersonList(sortEdRefOuterPersonList);
		return dataDetail;
	}

	/**
	 * ???????????????????????????
	 *
	 * @param extractBatch
	 */
	private void clearaCalculatedData(String extractBatch, String empno) {
		//?????????????????????????????????
		QueryWrapper<BudgetExtractpaydetail> queryWrapper = new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", extractBatch);
		if (StringUtils.isNotBlank(empno)) queryWrapper.eq("empno", empno);
		List<BudgetExtractpaydetail> curBatchPaydetailList = this.payDetailMapper.selectList(queryWrapper);
		if (curBatchPaydetailList.isEmpty()) return;
		List<Long> paydetailIdList = curBatchPaydetailList.stream().map(BudgetExtractpaydetail::getId).collect(Collectors.toList());
		//?????????????????????????????????
		List<BudgetExtractpayment> curBatchPaymentList = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", paydetailIdList));
		List<BudgetExtractsum> curBatchExtractSumList = this.budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch).eq(BudgetExtractsum::getDeleteflag, 0));
		List<String> codeList = curBatchExtractSumList.stream().map(BudgetExtractsum::getCode).collect(Collectors.toList());

		List<BudgetPaymoney> paymoneyList = paymoneyMapper.selectList(new LambdaQueryWrapper<BudgetPaymoney>().in(BudgetPaymoney::getPaymoneyobjectcode, codeList).eq(BudgetPaymoney::getPaymoneytype, PaymoneyTypeEnum.EXTRACT_PAY.getType()));
		//????????????????????????????????????
		List<BudgetPaymoney> pmList = paymoneyList.stream().filter(e -> e.getPaymoneystatus().intValue() > PaymoneyStatusEnum.RECEIVE_PAY.getType()).collect(Collectors.toList());
		if (!pmList.isEmpty()) {
			String errormsg = "???????????????";
			for (int i = 0; i < pmList.size(); i++) {
				BudgetPaymoney bpm = pmList.get(i);
				String status = bpm.getPaymoneystatus().intValue() == PaymoneyStatusEnum.RECEIVE_PAY.getType() ? "????????????" : bpm.getPaymoneystatus().intValue() == PaymoneyStatusEnum.PAYING.getType() ? "????????????" : "????????????";
				errormsg = errormsg + "????????????" + bpm.getPaymoneycode() + "?????????: " + status + "???";
				if (i < pmList.size() - 1) errormsg = errormsg + "\n";
			}
			throw new RuntimeException(errormsg);
		}
		if (!CollectionUtils.isEmpty(paymoneyList)) {
			List<Long> paymoneyIdList = paymoneyList.stream().map(BudgetPaymoney::getId).collect(Collectors.toList());
			paymoneyMapper.deleteBatchIds(paymoneyIdList);
		}
		List<Long> paymentIdList = curBatchPaymentList.stream().map(BudgetExtractpayment::getId).collect(Collectors.toList());
		outerUnitService.remove(new LambdaQueryWrapper<BudgetExtractpaymentOuterUnit>().in(BudgetExtractpaymentOuterUnit::getExtractPaymentId, paymentIdList));
		paymentMapper.deleteBatchIds(paymentIdList);
		payDetailMapper.deleteBatchIds(paydetailIdList);
		QueryWrapper<BudgetExtractgrantlog> wrapper = new QueryWrapper<BudgetExtractgrantlog>().eq("extractmonth", extractBatch);
		if (StringUtils.isNotBlank(empno)) wrapper.eq("empno", empno);
		grantLogMapper.delete(wrapper);
	}

	/**
	 * ??????????????????
	 *
	 * @param salaryMonth
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSalaryMsg(String salaryMonth) throws Exception {
		String result = "";
		try {
			result = HttpClientTool.getRequest(String.format("http://127.0.0.1:9597/api/extractInfo/getSalary?salaryMonth=%s", salaryMonth));
			//result = HttpClientTool.getRequest(String.format("http://ys.jtyjy.com/api/extractInfo/getSalary?salaryMonth=%s", salaryMonth));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException("??????" + salaryMonth + "???????????????!???????????????" + e.getMessage());
		}
		HashMap hashMap = JSON.parseObject(result, HashMap.class);
		if (hashMap.get("code").toString().equals("1"))
			throw new RuntimeException("??????" + salaryMonth + "???????????????!???????????????" + hashMap.get("msg").toString());

		Map<String, Object> map = (Map<String, Object>) hashMap.get("data");
		return map;
	}

	/**
	 * ??????????????????
	 *
	 * @param extractBatch
	 * @param curBatchExtractDetailList
	 * @return
	 * @throws Exception
	 */
	private ExtractPayCommonData populateCommonData(String extractBatch, List<BudgetExtractdetail> curBatchExtractDetailList) throws Exception {

		ExtractPayCommonData commonData = new ExtractPayCommonData();

		Integer year = Integer.valueOf(extractBatch.substring(0, 4));
		//??????????????????????????????
		String curYearStartExtractBatch = year + "0100";
		//??????????????????????????????
		String curYearEndExtractBatch = year + "1299";

		commonData.setCurYearStartExtractBatch(curYearStartExtractBatch);
		commonData.setCurYearEndExtractBatch(curYearEndExtractBatch);
		commonData.setCurExtractBatch(extractBatch);

		/**
		 * ?????????????????????
		 */
		commonData.setBanksMap(bankCache.BANK_MAP);
		commonData.setAllBillingUnitList(billingUnitMapper.selectList(new QueryWrapper<BudgetBillingUnit>().eq("stopflag", 0)));
		if (!commonData.getAllBillingUnitList().isEmpty()) {
			commonData.setBillingAccountList(this.billingUnitAccountMapper.selectList(new QueryWrapper<BudgetBillingUnitAccount>().eq("stopflag", 0).in("billingunitid", commonData.getAllBillingUnitList().stream().map(BudgetBillingUnit::getId).collect(Collectors.toList()))));
			Map<Long, List<BudgetBillingUnitAccount>> billingAccountMap = commonData.getBillingAccountList()
					.stream().collect(Collectors.groupingBy(BudgetBillingUnitAccount::getBillingunitid));
			commonData.setBillingAccountMap(billingAccountMap);
		}
		/**
		 * ??????????????????????????????
		 */
		populateOuterPayBillingUnitMsg(commonData);
		/**
		 * ??????????????????????????????
		 */
		populateQuiterPayBillingUnitMsg(commonData);
		//??????????????????
		Map<String, Object> salaryMsg = getSalaryMsg(extractBatch.substring(0, 6));
		commonData.setSalaryMsg(salaryMsg);
		commonData.setPayRuleList(this.payRuleMapper.selectList(new QueryWrapper<BudgetExtractpayRule>().eq("endflag", 0).le("effectdate", new Date())));
		List<BudgetExtractquotaRule> quotaRuleList = this.quotaRuleMapper.selectList(new QueryWrapper<BudgetExtractquotaRule>().eq("endflag", 0).le("effectdate", new Date()));
		commonData.setQuotaruleList(quotaRuleList);
		if (!quotaRuleList.isEmpty()) {
			Map<Long, List<BudgetExtractquotaRuledetail>> quotaRuleDetailMap = this.quotaRuleDetailMapper.selectList(new QueryWrapper<BudgetExtractquotaRuledetail>().in("extractquotaruleid", quotaRuleList.stream().map(BudgetExtractquotaRule::getId).collect(Collectors.toList())))
					.stream().collect(Collectors.groupingBy(BudgetExtractquotaRuledetail::getExtractquotaruleid));
			commonData.setQuotaRuleDetailMap(quotaRuleDetailMap);
		}
		/**
		 * ????????????
		 */
		Map<String, BudgetBankAccount> bankAccountMap = this.bankAccountMapper.selectList(new QueryWrapper<BudgetBankAccount>().eq("stopflag", 0)).stream().collect(Collectors.toMap(e -> {
					//return e.getCode()+"_"+e.getAccountname();
					return e.getCode() + "_" + e.getPname();
				}, e -> e, (e1, e2) ->
						//??????????????????
						Integer.compare(e1.getOrderno() == null ? 0 : e1.getOrderno(), e2.getOrderno() == null ? 0 : e2.getOrderno()) == 1 ? e1 : e2
		));
		commonData.setBankAccountMap(bankAccountMap);

		Map<String, WbUser> userMap = userCache.EMPNO_USER_MAP;//this.userMapper.selectList(null).stream().collect(Collectors.toMap(WbUser::getUserName, e -> e));
		commonData.setUserMap(userMap);

		Map<String, BudgetExtractOuterperson> outPersonMap = this.outPersonMapper.selectList(new QueryWrapper<BudgetExtractOuterperson>().eq("stopflag", 0)).stream().collect(Collectors.toMap(e -> e.getIdnumber(), e -> e));
		commonData.setOutPersonMap(outPersonMap);

		/**
		 * ?????????????????????(???????????????9000)
		 */
		TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE)
				.eq("dm", OUTERTHRESHOLD));
		commonData.setOuterThreshold(StringUtils.isBlank(dm.getDmValue()) ? new BigDecimal("9000") : new BigDecimal(dm.getDmValue()));

		/**
		 * ???????????????????????????
		 */
		TabDm dm1 = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE)
				.eq("dm", OUTEREXTRATAX));
		if (dm1 == null || StringUtils.isBlank(dm1.getDmValue())) throw new RuntimeException("?????????????????????????????????????????????????????????");
		commonData.setOuterOrinalExtraTax(new BigDecimal(dm1.getDmValue()));

		/**
		 * ???????????????????????????
		 */
		TabDm tabDm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE)
				.eq("dm", ISDEDUCTION));

		if (tabDm == null || StringUtils.isBlank(tabDm.getDmValue())) commonData.setIsDeduction(false);
		if (tabDm != null) {
			Boolean flag = tabDm.getDmValue().equals("1") ? true : false;
			commonData.setIsDeduction(flag);
		}

		if (commonData.getIsDeduction()) {
			/**
			 * ??????????????????????????????
			 */
			List<String> empNoList = curBatchExtractDetailList.stream().filter(e -> e.getIscompanyemp()).map(e -> e.getEmpno()).collect(Collectors.toList());
			Map<String, List<BudgetLendmoney>> empno2LendmoneyMap = getLendmoneyMap(empNoList);
			commonData.setEmpno2LendmoneyMap(empno2LendmoneyMap);
		}


		List<String> idnumberList = curBatchExtractDetailList.stream().map(BudgetExtractdetail::getIdnumber).collect(Collectors.toList());

		//?????????????????????
		List<BudgetExtractpaydetail> agoPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().in("idnumber", idnumberList)
				.lt("extractmonth", commonData.getCurExtractBatch())
				.ge("extractmonth", commonData.getCurYearStartExtractBatch()));
		/**
		 * ??????????????????????????????????????????????????????????????????
		 */
		Map<String, Map<Long, List<BudgetExtractpaydetail>>> agoPayDetailMap = new HashMap<>();
		agoPayDetails.stream().collect(Collectors.groupingBy(BudgetExtractpaydetail::getIdnumber)).forEach((idnumber, paydetails) -> {
			List<Long> payDetailIds = paydetails.stream().map(e -> e.getId()).collect(Collectors.toList());

			Map<Long, List<BudgetExtractpayment>> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", payDetailIds).isNotNull("bunitid1")).stream().collect(Collectors.groupingBy(BudgetExtractpayment::getBunitid1));

			Map<Long, List<BudgetExtractpaydetail>> map = new HashMap<>();
			paymentMap.forEach((billUnit, payments) -> {
				String paydetailids = payments.stream().map(e -> e.getBudgetextractpaydetailid().toString()).collect(Collectors.joining(","));
				List<BudgetExtractpaydetail> curBillUnitPayDetails = paydetails.stream().filter(e -> ("," + paydetailids + ",").contains("," + e.getId().toString() + ",")).collect(Collectors.toList());
				map.put(billUnit, curBillUnitPayDetails);
			});
			agoPayDetailMap.put(idnumber, map);
		});
		commonData.setAgoPayDetailMap(agoPayDetailMap);

		/**
		 * ??????????????????????????????
		 */
		Map<String, List<BudgetExtractFeePayDetailBeforeCal>> feePayDetailMap = extractFeePayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>().eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractBatch)).stream().collect(Collectors.groupingBy(BudgetExtractFeePayDetailBeforeCal::getEmpNo));
		commonData.setFeePayEmpMap(feePayDetailMap);
		return commonData;
	}

	/**
	 * ?????????????????????????????????
	 * ?????????????????????????????????????????????
	 *
	 * @param empNoList
	 * @return
	 */
	private Map<String, List<BudgetLendmoney>> getLendmoneyMap(List<String> empNoList) {
		if (empNoList == null || empNoList.isEmpty()) return new HashMap<>();
		//???????????????????????????

		ArrayList<Integer> lendTypeList = Lists.newArrayList(LendTypeEnum.LEND_TYPE_11.getType(),
				LendTypeEnum.LEND_TYPE_12.getType(),
				LendTypeEnum.LEND_TYPE_13.getType(),
				LendTypeEnum.LEND_TYPE_14.getType(),
				LendTypeEnum.LEND_TYPE_15.getType(),
				LendTypeEnum.LEND_TYPE_16.getType());
		//???????????????????????????
		List<BudgetLendmoney> lendMoneyList = this.lendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>().eq("deleteflag", 0)
				.in("lendtype", lendTypeList)
				.in("empno", empNoList)
				.eq("effectflag", 1)
				.apply("lendmoney-repaidmoney+interestmoney-repaidinterestmoney>0"));
		/**
		 * ??????????????????????????????????????????????????????
		 */
		List<Long> lockedLendmoneyIdList = this.lendmoneyUselogMapper.selectList(new QueryWrapper<BudgetLendmoneyUselog>().eq("useflag", 1)).stream().map(BudgetLendmoneyUselog::getLendmoneyid).collect(Collectors.toList());

		Map<String, List<BudgetLendmoney>> empno2LendmoneyMap = lendMoneyList.stream().filter(e -> !lockedLendmoneyIdList.contains(e.getId())).collect(Collectors.groupingBy(BudgetLendmoney::getEmpno));
		return empno2LendmoneyMap;
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param commonData
	 */
	private void populateQuiterPayBillingUnitMsg(ExtractPayCommonData commonData) {
		List<TabDm> tabList = dmMapper.selectList(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_PAY_DM_TYPE));
		TabDm td = tabList.stream().filter(dm -> dm.getDm().equals(QUIT_PAYUNIT)).findFirst().orElse(null);
		if (Objects.isNull(td) || StringUtils.isBlank(td.getDmValue()))
			throw new RuntimeException("????????????!??????????????????????????????????????????????????????");
		List<String> quiterUnitIdList = Arrays.asList(td.getDmValue().split(","));
		List<BudgetBillingUnit> billUnitList = commonData.getAllBillingUnitList().stream().filter(e -> quiterUnitIdList.contains(e.getId().toString())).collect(Collectors.toList());
		if (billUnitList.isEmpty()) throw new RuntimeException("????????????!??????????????????????????????????????????????????????");
		Random random = new Random();
		int index = random.nextInt(billUnitList.size());
		/**
		 * ???????????????????????????
		 */
		BudgetBillingUnit quiterBillingUnit = billUnitList.get(index);
		commonData.setQuiterBillingUnit(quiterBillingUnit);
		//?????????????????????????????????
		List<BudgetBillingUnitAccount> quiterBillingUnitAccoutList = commonData.getBillingAccountMap().get(quiterBillingUnit.getId()).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getStopflag)).sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(quiterBillingUnitAccoutList) || quiterBillingUnitAccoutList.get(0).getStopflag())
			throw new RuntimeException("????????????!???????????????" + quiterBillingUnit.getName() + "???????????????");
		BudgetBillingUnitAccount quiterBillingUnitAccout = quiterBillingUnitAccoutList.get(0);
		commonData.setQuiterBillingUnitAccount(quiterBillingUnitAccout);

		String branchcode = quiterBillingUnitAccout.getBranchcode();
		WbBanks wbBank = commonData.getBanksMap().get(branchcode);
		if (wbBank == null)
			throw new RuntimeException("????????????!???????????????" + quiterBillingUnit.getName() + "???????????????" + quiterBillingUnitAccout.getBankaccount() + "????????????????????????");
		commonData.setQuiterWbBank(wbBank);
	}


	/**
	 * ??????????????????????????????
	 *
	 * @param commonData
	 */
	private void populateOuterPayBillingUnitMsg(ExtractPayCommonData commonData) {
		List<TabDm> tabList = dmMapper.selectList(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_PAY_DM_TYPE));
		TabDm td = tabList.stream().filter(dm -> dm.getDm().equals(OUTER_PAYUNIT)).findFirst().orElse(null);
		if (Objects.isNull(td) || StringUtils.isBlank(td.getDmValue()))
			throw new RuntimeException("????????????!??????????????????????????????????????????????????????");

		List<String> billUnitIdList = Arrays.asList(td.getDmValue().split(","));
		/**
		 * ??????????????????????????????
		 */

		List<BudgetBillingUnit> billUnitList = commonData.getAllBillingUnitList().stream().filter(e -> billUnitIdList.contains(e.getId().toString())).collect(Collectors.toList());
		if (billUnitList.isEmpty()) throw new RuntimeException("????????????!??????????????????????????????????????????????????????");
		//Random random = new Random();
		//int index = random.nextInt(billUnitList.size());
		/**
		 * ???????????????????????????
		 */
		//BudgetBillingUnit outBillingUnit = billUnitList.get(index);
		//????????????
		BudgetBillingUnit outBillingUnit = billUnitList.get(0);
		commonData.setOuterBillingUnit(outBillingUnit);
		//?????????????????????????????????
		List<BudgetBillingUnitAccount> outBillingUnitAccoutList = commonData.getBillingAccountMap().get(outBillingUnit.getId()).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getStopflag)).sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(outBillingUnitAccoutList) || outBillingUnitAccoutList.get(0).getStopflag())
			throw new RuntimeException("????????????!???????????????" + outBillingUnit.getName() + "???????????????");
		BudgetBillingUnitAccount outBillingUnitAccout = outBillingUnitAccoutList.get(0);
		commonData.setOuterBillingUnitAccount(outBillingUnitAccout);

		String branchcode = outBillingUnitAccout.getBranchcode();
		WbBanks wbBank = commonData.getBanksMap().get(branchcode);
		if (wbBank == null)
			throw new RuntimeException("????????????!???????????????" + outBillingUnit.getName() + "???????????????" + outBillingUnitAccout.getBankaccount() + "????????????????????????");
		commonData.setOuterWbBank(wbBank);
	}

	/**
	 * ??????????????????????????????????????????
	 * ?????????????????????????????????
	 * 1.??????????????????????????????????????????
	 * 2.?????????????????????????????????
	 * 3.??????????????????????????????????????????????????????
	 * 4.????????????????????????
	 *
	 * @param curExtractBatch        ??????????????????
	 * @param curBatchExtractSumList
	 */
	private void validateIsCanCalculate(String curExtractBatch, List<BudgetExtractsum> curBatchExtractSumList) {
		if (CollectionUtils.isEmpty(curBatchExtractSumList))
			throw new RuntimeException("???????????????" + curExtractBatch + "???????????????????????????");

		if (curBatchExtractSumList.get(0).getStatus() >= ExtractStatusEnum.CALCULATION_COMPLETE.getType()) {
			throw new RuntimeException("????????????????????????????????????");
		}
		/**
		 * 1.??????????????????????????????????????????	,???????????????
		 */
		//???????????????????????????????????????????????????
		String curBatchUnApprovedExtractCodes = curBatchExtractSumList.stream().filter(sum -> sum.getStatus() < ExtractStatusEnum.APPROVED.getType() && sum.getStatus() != ExtractStatusEnum.REJECT.getType()).map(BudgetExtractsum::getCode).collect(Collectors.joining(","));
		if (StringUtils.isNotBlank(curBatchUnApprovedExtractCodes))
			throw new RuntimeException("??????????????????????????????" + curExtractBatch + "??????????????????????????????????????????" + curBatchUnApprovedExtractCodes + "???");

		/**
		 * 2.????????????????????????????????????????????????
		 */
		Integer year = Integer.valueOf(curExtractBatch.substring(0, 4));
		//??????????????????????????????
		String curYearStartExtractBatch = year + "0100";
		//??????????????????????????????
		String curYearEndExtractBatch = year + "1299";
		//?????????????????????
		List<BudgetExtractsum> allSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("deleteflag", "0").le("extractmonth", curYearEndExtractBatch).ge("extractmonth", curYearStartExtractBatch));
		//????????????????????????????????????????????????
		List<BudgetExtractsum> agoExtractSumList = allSumList.stream().filter(sum -> Integer.valueOf(sum.getExtractmonth()) > Integer.valueOf(curYearStartExtractBatch)
				&& Integer.valueOf(sum.getExtractmonth()) < Integer.valueOf(curExtractBatch)).collect(Collectors.toList());
		//???????????????????????????????????????(?????????)????????????
		List<BudgetExtractsum> agoUnCalculateExtractSumList = agoExtractSumList.stream().filter(sum -> sum.getStatus() < ExtractStatusEnum.CALCULATION_COMPLETE.getType() && sum.getStatus() != ExtractStatusEnum.REJECT.getType()).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(agoUnCalculateExtractSumList)) {
			//????????????????????????
			String agoUnCalculateExtractBatchs = agoUnCalculateExtractSumList.stream().map(BudgetExtractsum::getExtractmonth).distinct().collect(Collectors.joining(","));
			//throw new RuntimeException("??????????????????????????????" + agoUnCalculateExtractBatchs + "????????????????????????");
		}

		/**
		 * 3.??????????????????????????????????????????????????????  ???????????????
		 */
		//???????????????????????????????????????????????????
		List<Long> agoExtractSumIdList = agoExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		List<BudgetExtractdetail> agoExtractDeailList = new ArrayList<>();
		if (!agoExtractSumIdList.isEmpty()) {
			agoExtractDeailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", agoExtractSumIdList)
					.eq("deleteflag", 0));
		}
		StringBuffer errormsg = new StringBuffer();
		//????????????????????????????????????
		agoExtractDeailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0 && e.getHandleflag() != null && !e.getHandleflag())
				.collect(Collectors.groupingBy(e -> e.getExtractsumid()))
				.forEach((sumid, extractDetailList) -> {
					String code = agoExtractSumList.stream().filter(e -> e.getId().equals(sumid)).findFirst().get().getCode();
					if (!extractDetailList.isEmpty()) {
						String empnos = extractDetailList.stream().map(e -> e.getEmpno()).collect(Collectors.joining(","));
						errormsg.append("???????????????" + code + "??????:?????????" + empnos + "???????????????????????????<br>");
					}
				});
		;
		//if (StringUtils.isNotBlank(errormsg.toString())) throw new RuntimeException(errormsg.toString());

		/**
		 * 4.????????????????????????  ???????????????
		 */
		List<BudgetExtractsum> lateExtractSumList = allSumList.stream().filter(e -> Integer.valueOf(e.getExtractmonth()).intValue() > Integer.valueOf(curExtractBatch).intValue()
				&& Integer.valueOf(e.getExtractmonth()).intValue() < Integer.valueOf(curYearEndExtractBatch).intValue()).collect(Collectors.toList());
		if (!lateExtractSumList.isEmpty()) {
			String extractmonths = lateExtractSumList.stream().filter(e -> {
				boolean isCal = false;
				BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(e.getExtractmonth());
				if(extractTaxHandleRecord!=null){
					isCal = extractTaxHandleRecord.getIsCalComplete() || extractTaxHandleRecord.getIsPersonalityComplete();
				}
				return e.getStatus().intValue() >= ExtractStatusEnum.CALCULATION_COMPLETE.getType() || isCal;
			}).map(e -> e.getExtractmonth()).distinct().collect(Collectors.joining(","));
			//if (StringUtils.isNotEmpty(extractmonths))
				//throw new RuntimeException("??????????????????????????????" + extractmonths + "??????????????????!");
		}
	}


	/**
	 * ????????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<BudgetExtractdetail> getExtractExcessDetailByExtractmonth(String extractBatch) {
		validateIsCanSetExcess(extractBatch);
		List<BudgetExtractsum> extractSumList = this.budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch).eq(BudgetExtractsum::getDeleteflag, 0).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
		List<Long> sumIds = extractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		return this.extractDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractdetail>().ne(BudgetExtractdetail::getBusinessType,ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode()).in(BudgetExtractdetail::getExtractsumid, sumIds).eq(BudgetExtractdetail::getDeleteflag, 0).eq(BudgetExtractdetail::getExcesstype, ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType()).eq(BudgetExtractdetail::getHandleflag, 0).gt(BudgetExtractdetail::getExcessmoney, 0));
	}

	public void validateIsCanSetExcess(String extractBatch) {
		validateExtractIsAllPass(extractBatch);
		BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(extractBatch);
		if (extractTaxHandleRecord == null || !extractTaxHandleRecord.getIsCalComplete()) {
			throw new RuntimeException("???????????????" + extractBatch + "????????????????????????");
		}
		if (extractTaxHandleRecord.getIsSetExcessComplete()) {
			throw new RuntimeException("???????????????" + extractBatch + "??????????????????????????????");
		}
	}

	/**
	 * ????????????????????????????????????
	 *
	 * @param extractBatch
	 * @param empnoList
	 * @return
	 */
	public Map<String, String> getIdnumber2BillingUnitNameMap(String extractBatch, List<String> empnoList) {

		List<BudgetExtractpaydetail> paydetailList = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().in("empno", empnoList).eq("extractmonth", extractBatch));

		List<Long> paydetailIds = paydetailList.stream().map(BudgetExtractpaydetail::getId).collect(Collectors.toList());
		Map<Long, BudgetExtractpayment> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", paydetailIds)).stream().collect(Collectors.toMap(BudgetExtractpayment::getBudgetextractpaydetailid, e -> e, (e1, e2) -> e1));

		return paydetailList.stream().collect(() -> new HashMap<String, String>(), (e1, e2) -> {
			BudgetExtractpayment extractpayment = paymentMap.get(e2.getId());
			e1.put(e2.getIdnumber(), extractpayment.getBunitname1());
		}, (e1, e2) -> e1.putAll(e2));
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<BudgetExtractdetail> getExtractDetailByExtractmonth(String extractBatch) {
		//????????????????????????????????????
		List<BudgetExtractsum> curBatchExtractSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0));

		List<Long> curExtractSumIdList = curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//?????????????????????????????????
		return this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().eq("deleteflag", 0).in("extractsumid", curExtractSumIdList));
	}

	/**
	 * ????????????????????????
	 *
	 * @param repaymoneyid ?????????Id
	 * @return
	 */
	public List<Map<String, Object>> getRepaymoneymsg(Long repaymoneyid) {
		return this.budgetExtractsumMapper.getRepaymoneymsg(repaymoneyid);
	}

	/**
	 * ???????????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<ExtractCCLPayExcelData> getCCLPayDetailList(String extractBatch) {
		return this.budgetExtractsumMapper.getCCLPayDetailList(extractBatch);
	}


	/**
	 * ????????????????????????
	 *
	 * @param extractMonth     ???????????????
	 * @param lastSalaryMsgMap ???????????????????????????
	 * @return
	 */
	public List<ExtractIncomeExcelData> getExtractIncomeDetails(String extractMonth,
	                                                            Map<Integer, Map<String, Object>> lastSalaryMsgMap) {

		List<ExtractIncomeExcelData> resultList = new ArrayList<>();

		String year = extractMonth.substring(0, 4);
		List<BudgetExtractArrears> curYearAllExtractArrearList = this.extractArrearsMapper.selectList(new QueryWrapper<BudgetExtractArrears>().le("month", extractMonth).ge("month", year + "01"));
		/**
		 * ??????
		 */
		calTax(curYearAllExtractArrearList);

		//?????????????????????????????? (key???????????????)
		Map<String, List<BudgetExtractArrears>> curYearAllExtractArrearMap = curYearAllExtractArrearList.stream().collect(Collectors.groupingBy(BudgetExtractArrears::getMonth));

		//???????????????????????????????????????
		//List<BudgetExtractArrears> curYearExtractArrearList = curYearAllExtractArrearList.stream().filter(e->Integer.valueOf(e.getMonth())<=Integer.valueOf(extractMonth)).collect(Collectors.toList());


		List<BudgetExtractpaydetail> curExtractPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().likeRight("extractmonth", extractMonth));
		if (CollectionUtils.isEmpty(curExtractPayDetails)) {
			return new ArrayList<>();
		}
		Map<Long, BudgetExtractpaydetail> payDetailMap = curExtractPayDetails.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getId, e -> e, (e1, e2) -> e1));
		//?????????????????????
		Map<Long, BudgetExtractpayment> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", curExtractPayDetails.stream().map(e -> e.getId()).collect(Collectors.toList()))).stream().collect(Collectors.toMap(BudgetExtractpayment::getBudgetextractpaydetailid, e -> e, (e1, e2) -> e1));

		List<BudgetExtractArrears> curBudgetExtractArrears = curYearAllExtractArrearMap.get(extractMonth);

		/**
		 * ???????????????????????????????????????????????????????????????????????????
		 */
		curBudgetExtractArrears.stream().collect(Collectors.groupingBy(e -> e.getIdnumber() + "_" + e.getBunitid().toString())).forEach((key, arrears) -> {
			String idnumber = key.split("_")[0];
			String unitId = key.split("_")[1];
			//????????????????????????????????????????????????
			BudgetExtractpaydetail curPayDetail = null;
			BudgetExtractpayment curPayment = null;
			List<BudgetExtractpaydetail> curEmpPayDetails = curExtractPayDetails.stream().filter(e -> e.getIdnumber().equals(idnumber)).collect(Collectors.toList());
			for (BudgetExtractpaydetail payDetail : curEmpPayDetails) {
				BudgetExtractpayment extractpayment = paymentMap.get(payDetail.getId());
				if (!extractpayment.getBunitid1().toString().equals(unitId)) continue;
				curPayDetail = payDetail;
				curPayment = extractpayment;
			}
			if (curPayDetail == null || curPayDetail.getIscompanyemp() == null || !curPayDetail.getIscompanyemp())
				return;
			BudgetExtractArrears extractArrears = arrears.get(0);
			ExtractIncomeExcelData ed = new ExtractIncomeExcelData();
			ed.setEmpNo(curPayDetail.getEmpno());
			ed.setEmpName(curPayDetail.getEmpname());
			ed.setSalaryUnitName(curPayment.getBunitname1());
			ed.setCurMonthSalary(curPayDetail.getSalary());
			ed.setCurMonthExtract(extractArrears.getRealextract());
			ed.setFiveRiskOneFund(extractArrears.getFiveriskonefund());
			/**
			 * ???????????????????????????
			 */
			Map<String, Object> agoIncomeMap = getLastIncome(curPayDetail, unitId, extractMonth, curYearAllExtractArrearMap, lastSalaryMsgMap);
			BudgetExtractArrears curEmpUnitLastMonthArrear = (BudgetExtractArrears) agoIncomeMap.get("curEmpUnitLastMonthArrear");
			List<BudgetExtractArrears> agoArrears = (List<BudgetExtractArrears>) agoIncomeMap.get("agoArrears");

			//????????????
			BigDecimal tax = getTax(agoArrears, extractArrears.getPayabletaxs());
			tax = tax.setScale(2, BigDecimal.ROUND_HALF_UP);
			//?????????????????????
			tax = tax.compareTo(BigDecimal.ZERO) >= 0 ? tax : BigDecimal.ZERO;
			ed.setCurTax(tax);
			/**
			 * ????????????????????? ????????????-????????????
			 */
			BigDecimal currentIncome = extractArrears.getSfdtje().add(extractArrears.getFiveriskonefundlj());
			BigDecimal lastIncome = BigDecimal.ZERO;
			if (curEmpUnitLastMonthArrear != null) {
				lastIncome = curEmpUnitLastMonthArrear.getSfdtje().add(curEmpUnitLastMonthArrear.getFiveriskonefundlj());
			}
			BigDecimal income = currentIncome.subtract(lastIncome).setScale(2, BigDecimal.ROUND_HALF_UP);
			ed.setCurIncome(income);
			resultList.add(ed);
		});
		return resultList;
	}

	/**
	 * ????????????
	 *
	 * @param agoArrears
	 * @param payabletaxs
	 * @return
	 */
	private BigDecimal getTax(List<BudgetExtractArrears> agoArrears, BigDecimal payabletaxs) {
		agoArrears = agoArrears.stream().sorted((e1, e2) -> Integer.compare(Integer.valueOf(e1.getMonth()), Integer.valueOf(e2.getMonth()))).collect(Collectors.toList());
		BigDecimal sjgsSum = BigDecimal.ZERO;
		Map<Integer, BigDecimal> taxMap = new HashMap<>();
		for (int i = 0; i < agoArrears.size(); i++) {
			BudgetExtractArrears bea = agoArrears.get(i);
			if (i == 0) {
				taxMap.put(i, bea.getPayabletaxs());
			} else {
				BigDecimal sjgs = BigDecimal.ZERO;
				for (int j = i - 1; j >= 0; j--) {
					sjgs = sjgs.add(taxMap.get(j));
				}
				//taxMap.put(i, bea.getPayabletaxs().subtract(sjgs).compareTo(BigDecimal.ZERO)<=0?BigDecimal.ZERO:bea.getPayabletaxs().subtract(sjgs));
				taxMap.put(i, bea.getPayabletaxs().subtract(sjgs));
			}
			sjgsSum = sjgsSum.add(taxMap.get(i));
		}
		return payabletaxs.subtract(sjgsSum).compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : payabletaxs.subtract(sjgsSum);
	}

	/**
	 * ??????????????????
	 *
	 * @param idnumber
	 * @param unitId
	 * @param extractMonth
	 * @param curYearAllExtractArrearMap
	 * @param lastSalaryMsgMap
	 */
	private Map<String, Object> getLastIncome(BudgetExtractpaydetail curPayDetail, String unitId, String extractMonth,
	                                          Map<String, List<BudgetExtractArrears>> curYearAllExtractArrearMap, Map<Integer, Map<String, Object>> lastSalaryMsgMap) {
		Map<String, Object> result = new HashMap<>();
		int curMonth = Integer.valueOf(extractMonth.substring(4, 6));
		int curYear = Integer.valueOf(extractMonth.substring(0, 4));

		List<BudgetExtractArrears> agoArrears = new ArrayList<>();
		BudgetExtractArrears curEmpUnitLastMonthArrear = null;
		boolean flag = false;
		for (int i = curMonth - 1; i > 0; i--) {
			String lastmonth = curYear + (i >= 10 ? i + "" : "0" + i);
			//?????????????????????????????????????????????????????????
			List<BudgetExtractArrears> list = curYearAllExtractArrearMap.get(lastmonth);
			List<BudgetExtractArrears> curEmpUnitLastMonthArrears = list == null ? Lists.newArrayList() : list.stream().filter(e -> e.getMonth().equals(lastmonth) && e.getIdnumber().equals(curPayDetail.getIdnumber()) && e.getBunitid().toString().equals(unitId)).collect(Collectors.toList());
			if (!curEmpUnitLastMonthArrears.isEmpty()) {
				agoArrears.add(curEmpUnitLastMonthArrears.get(0));
				if (!flag) {
					curEmpUnitLastMonthArrear = curEmpUnitLastMonthArrears.get(0);
					flag = true;
				}
				continue;
			}
			//???????????????????????????????????????????????????????????????????????????
			//BudgetExtractArrears arrears = createExtractArrearsIfLastmonthHaveNotExtract();
			BudgetExtractArrears arrears = new BudgetExtractArrears();
			arrears.setEmpno(curPayDetail.getEmpno());
			arrears.setEmpname(curPayDetail.getEmpname());
			arrears.setMonth(lastmonth);
			if (curPayDetail.getIscompanyemp()) {
				Map<String, Object> salaryMap = (Map<String, Object>) lastSalaryMsgMap.get(Integer.valueOf(lastmonth.substring(4, 6))).get(curPayDetail.getEmpno());
				if (salaryMap == null) continue;
				String outkey = salaryMap.get("salarycompanyid").toString();
				BudgetBillingUnit bbu = this.billingUnitMapper.selectOne(new QueryWrapper<BudgetBillingUnit>().eq("outkey", outkey));
				if (!bbu.getId().toString().equals(unitId)) continue;
				arrears.setBunitid(bbu.getId());
				arrears.setSalary(new BigDecimal(salaryMap.get("salary").toString()));
				arrears.setSalarylj(new BigDecimal(salaryMap.get("salarys").toString()).add(arrears.getSalary()));
				arrears.setSpecialdeductionlj(new BigDecimal(salaryMap.get("specialtax").toString()).add(new BigDecimal(salaryMap.get("specialtaxs").toString())));
				arrears.setFiveriskonefund(new BigDecimal(salaryMap.get("sociinsurcharge").toString()));
				arrears.setFiveriskonefundlj(new BigDecimal(salaryMap.get("sociinsurcharges").toString()).add(arrears.getFiveriskonefund()));
				arrears.setThresholdlj(new BigDecimal(salaryMap.get("startpoints").toString()).add(new BigDecimal(salaryMap.get("startpoint").toString())));

			} else {
				arrears.setBunitid(Long.valueOf(unitId));
				arrears.setSalary(BigDecimal.ZERO);
				arrears.setSalarylj(BigDecimal.ZERO);
				arrears.setSpecialdeductionlj(BigDecimal.ZERO);
				arrears.setFiveriskonefund(BigDecimal.ZERO);
				arrears.setFiveriskonefundlj(BigDecimal.ZERO);
				arrears.setThresholdlj(BigDecimal.ZERO);
			}
			//????????????????????????????????????????????????
			BigDecimal inCompanyExtract = curYearAllExtractArrearMap.values().stream().flatMap(e -> e.stream()).filter(e -> Integer.valueOf(e.getMonth()) <= Integer.valueOf(lastmonth)
					&& e.getIdnumber().equals(curPayDetail.getIdnumber())
					&& e.getBunitid().toString().equals(unitId))
					.map(BudgetExtractArrears::getRealextract)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			arrears.setIncorporatedcompanylj(inCompanyExtract);
			//?????????????????????
			BigDecimal total = arrears.getSalarylj().add(inCompanyExtract);
			BigDecimal total1 = arrears.getSpecialdeductionlj().add(arrears.getThresholdlj());
			List<Map> taxList = ExtractPayCommonData.getTaxList();
			BigDecimal sfdtje = total.compareTo(total1) <= 0 ? total : getSfdtje(total, arrears.getSpecialdeductionlj(), arrears.getThresholdlj(), taxList);
			BigDecimal payableTaxs = getPayableTax(sfdtje, arrears.getSpecialdeductionlj(), arrears.getThresholdlj(), BigDecimal.ZERO, taxList);
			arrears.setSfdtje(sfdtje);
			arrears.setRealextract(BigDecimal.ZERO);
			arrears.setPayabletaxs(payableTaxs);
			if (!flag) {
				curEmpUnitLastMonthArrear = arrears;
				flag = true;
			}
			agoArrears.add(arrears);
		}

		result.put("curEmpUnitLastMonthArrear", curEmpUnitLastMonthArrear);
		result.put("agoArrears", agoArrears);
		return result;
	}


	/**
	 * ??????
	 *
	 * @param curMonthExtractArrearList
	 */
	private void calTax(List<BudgetExtractArrears> curMonthExtractArrearList) {
		List<Map> taxList = ExtractPayCommonData.getTaxList();
		for (BudgetExtractArrears currentArrears : curMonthExtractArrearList) {
			BigDecimal total = currentArrears.getSalarylj().add(currentArrears.getIncorporatedcompanylj());
			BigDecimal total1 = currentArrears.getSpecialdeductionlj().add(currentArrears.getThresholdlj());
			BigDecimal sfdtje = total.compareTo(total1) <= 0 ? total : getSfdtje(total, currentArrears.getSpecialdeductionlj(), currentArrears.getThresholdlj(), taxList);
			BigDecimal payableTaxs = getPayableTax(sfdtje, currentArrears.getSpecialdeductionlj(), currentArrears.getThresholdlj(), BigDecimal.ZERO, taxList);
			currentArrears.setSfdtje(sfdtje);
			currentArrears.setPayabletaxs(payableTaxs);
			this.extractArrearsMapper.updateById(currentArrears);
		}
	}


	/**
	 * ??????code????????????????????????
	 *
	 * @param codes
	 * @return
	 */
	public List<BudgetExtractsum> getByCodes(Set<String> codes) {
		if (null != codes && codes.size() > 0) {
			QueryWrapper<BudgetExtractsum> wrapper = new QueryWrapper<BudgetExtractsum>();
			wrapper.in("code", codes);
			return this.list(wrapper);
		} else {
			return null;
		}

	}

	public PageResult<BudgetExtractpaydetail> getExtractCalTaxDetails(Map<String, Object> params, Integer page,
	                                                                  Integer rows) {
		Page<BudgetExtractpaydetail> pageCond = new Page<>(page, rows);
		QueryWrapper<BudgetExtractpaydetail> qw = new QueryWrapper<>();
		qw.eq("extractmonth", params.get("extractmonth"));
		if (params.containsKey("empno")) {
			qw.and(q -> {
				q.like("empno", params.get("empno")).or().like("empname", params.get("empno"));
			});
		}
		pageCond = this.payDetailMapper.selectPage(pageCond, qw);
		return PageResult.apply(pageCond.getTotal(), pageCond.getRecords());
	}


	public void deleteExtractImportDetail(Long id) {
		BudgetExtractImportdetail importdetail = this.extractImportDetailMapper.selectById(id);
		extractImportDetailMapper.deleteById(id);
		Long extractdetailId = importdetail.getExtractdetailid();
		Optional.ofNullable(extractdetailId).ifPresent(e -> {
			this.extractDetailMapper.deleteById(e);
		});
		Integer count = extractImportDetailMapper.selectCount(new QueryWrapper<BudgetExtractImportdetail>().eq("extractsumid", importdetail.getExtractsumid()));
		BudgetExtractsum extractsum = this.sumService.getById(importdetail.getExtractsumid());
		extractsum.setExtractnum(count);
		this.sumService.updateById(extractsum);
	}

	/**
	 * ?????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public String showExtractQrcode(String extractBatch) {
		BudgetExtractQrcode code = extractQrcodeMapper.selectOne(new LambdaQueryWrapper<BudgetExtractQrcode>().eq(BudgetExtractQrcode::getExtractMonth, extractBatch));
		if (Objects.nonNull(code)) return code.getQrcode();
		return null;
	}

	/**
	 * ??????????????????
	 */
	public void reset(String extractBatch) {
		List<BudgetExtractsum> budgetExtractsums = budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
		if (!CollectionUtils.isEmpty(budgetExtractsums)) {
			List<Long> sumIds = budgetExtractsums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
			Integer status = budgetExtractsums.get(0).getStatus();
			if (status >= ExtractStatusEnum.CALCULATION_COMPLETE.getType()) {
				throw new RuntimeException("????????????????????????????????????????????????");
			}
			long count = budgetExtractsums.stream().filter(e -> e.getStatus() < ExtractStatusEnum.APPROVED.type).count();
			if(count > 0){
				throw new RuntimeException("????????????????????????????????????????????????????????????");
			}
//			BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(extractBatch);
//			if(extractTaxHandleRecord==null || !extractTaxHandleRecord.getIsCalComplete()){
//				throw new RuntimeException("???????????????????????????????????????");
//			}
			clearaCalculatedData(extractBatch, null);
			extractFeePayDetailMapper.delete(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>().eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractBatch));
//			generateExtractStepLog(sumIds, OperationNodeEnum.TAX_PREPARATION_CALCULATION_SELF, "???" + OperationNodeEnum.TAX_PREPARATION_CALCULATION_SELF.getValue() + "???????????????", LogStatusEnum.REJECT.getCode());

			LambdaUpdateWrapper<BudgetExtractdetail> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.in(BudgetExtractdetail::getExtractsumid, sumIds);
			updateWrapper.eq(BudgetExtractdetail::getDeleteflag, 0);
			updateWrapper.set(BudgetExtractdetail::getExcesstype, ExtractExcessTypeEnum.NONE.type);
			updateWrapper.set(BudgetExtractdetail::getHandleflag, 0);
			updateWrapper.set(BudgetExtractdetail::getExcessmoney, 0);
			this.extractDetailMapper.update(new BudgetExtractdetail(), updateWrapper);

			LambdaUpdateWrapper<BudgetExtractTaxHandleRecord> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
			lambdaUpdateWrapper.eq(BudgetExtractTaxHandleRecord::getExtractMonth, extractBatch);
			lambdaUpdateWrapper.set(BudgetExtractTaxHandleRecord::getIsCalComplete, 0);
			lambdaUpdateWrapper.set(BudgetExtractTaxHandleRecord::getIsSetExcessComplete, 0);
			taxHandleRecordMapper.update(new BudgetExtractTaxHandleRecord(), lambdaUpdateWrapper);
		}
	}


	public static String trimZero(String sftc) {
		String[] split = sftc.split("\\.");
		if (split.length == 2) {
			Integer a = Integer.valueOf(split[1]);
			if (a == 0) {
				return split[0];
			}
		}
		return sftc;
	}

	public static void main(String[] args) throws Exception {
		//String base64Qrcode = QRCodeUtil.createBase64Qrcode("http://oauth.jtyjy.com/api/oneLogin/budgetExtractSign?extractMonth=20211204&type=2", "/home/data/tmp" + File.separator + "TC20211204" + QRCODE_FORMAT);
		//System.out.println(base64Qrcode);
		List<BudgetBillingUnit> unitList = new ArrayList<>();
		BudgetBillingUnit unit = new BudgetBillingUnit();
		unit.setId(123L);
		unitList.add(unit);

		List<BudgetBillingUnit> newList = unitList;
		newList.forEach(e->{

			System.out.println(e.getId());
			e.setId(124L);
		});

		unitList.forEach(e->{
			System.out.println(e.getId());
		});
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public Map<String, Object> getExtractPayExcelHead(String extractBatch) {
		Map<String, Object> heads = new HashMap<>();
		File pngFile = new File(this.fileShareDir + File.separator + QRCODE_PREFIX + extractBatch + QRCODE_FORMAT);
		if (pngFile.exists()) {
			heads.put("file", pngFile);
		}
		heads.put("curDate", Constants.FORMAT_10.format(new Date()));
		heads.put("curYear", extractBatch.substring(0, 4));
		heads.put("curMonth", Integer.valueOf(extractBatch.substring(4, 6)));
		heads.put("curBatch", Integer.valueOf(extractBatch.substring(6, 8)));
		heads.put("curEmpName", LoginThreadLocal.get().getEmpname());
		return heads;
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<ExtractPayApplyExcelData> getExtractExcelDetails(String extractBatch, Map<String, Object> heads) {
		List<Object> result = new ArrayList<>();
		Map<String, Object> params = new HashMap<>(1);
		params.put("extractmonth", extractBatch);
		List<ExtractPayDetailVO> payDetails = getPayDetailsByCondition(null, params);
		//?????????????????????id
		List<String> extractDetailIds = payDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		List<BudgetExtractImportdetail> importDetails = new ArrayList<>();
		if (!CollectionUtils.isEmpty(extractDetailIds)) {
			//???????????????????????????
			importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().in(BudgetExtractImportdetail::getExtractdetailid, extractDetailIds));
		}
		List<ExtractPayApplyExcelData> payApplyExcelDatas = getExtractPayExcelDetails(importDetails, heads, payDetails);
		return payApplyExcelDatas;

	}

	/**
	 * ???????????????????????????
	 */
	public List<ExtractPaySumExcelData> getExtractPaySumExcelDetails(String extractBatch) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("extractmonth", extractBatch);
		List<ExtractPayDetailVO> payDetails = getPayDetailsByCondition(null, params);
		//?????????????????????id
		List<String> extractDetailIds = payDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		List<BudgetExtractImportdetail> importDetails = new ArrayList<>();
		if (!CollectionUtils.isEmpty(extractDetailIds)) {
			//???????????????????????????
			importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().in(BudgetExtractImportdetail::getExtractdetailid, extractDetailIds));
		}
		List<ExtractPaySumExcelData> paySumExcelDatas = getExtractPaySumExcelDatas(importDetails, payDetails);
		return paySumExcelDatas;
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param importDetails ????????????
	 * @param payDetails    ????????????
	 * @return
	 */
	private List<ExtractPaySumExcelData> getExtractPaySumExcelDatas(List<BudgetExtractImportdetail> importDetails, List<ExtractPayDetailVO> payDetails) {
		List<ExtractPaySumExcelData> result = new ArrayList<>();

		List<Long> extractSumIds = importDetails.stream().map(BudgetExtractImportdetail::getExtractsumid).collect(Collectors.toList());
		Map<Long, BudgetExtractsum> budgetExtractsumMap = null;
		if (!CollectionUtils.isEmpty(extractSumIds)) {
			budgetExtractsumMap = this.listByIds(extractSumIds).stream().collect(Collectors.toMap(BudgetExtractsum::getId, Function.identity(), (e1, e2) -> e1));
		}
		if (budgetExtractsumMap == null) return result;
		Map<Long, BudgetYearPeriod> yearPeriodMap = yearMapper.selectList(null).stream().collect(Collectors.toMap(BudgetYearPeriod::getId, Function.identity()));
		//????????????
		List<BudgetExtractImportdetail> qjExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.RETURN.value.equals(e.getExtractType())).collect(Collectors.toList());
		//???????????????
		List<BudgetExtractImportdetail> zzTotalExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.DRAFT.value.equals(e.getExtractType())).collect(Collectors.toList());
		//???????????????
		List<BudgetExtractImportdetail> zzAfterExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.VERIFYING.value.equals(e.getExtractType())).collect(Collectors.toList());
		//????????????
		List<BudgetExtractImportdetail> hzExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.APPROVED.value.equals(e.getExtractType())).collect(Collectors.toList());

		packageExtractPaySumExcelData(qjExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.RETURN.value);
		packageExtractPaySumExcelData(zzTotalExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.DRAFT.value);
		packageExtractPaySumExcelData(zzAfterExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.VERIFYING.value);
		packageExtractPaySumExcelData(hzExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.APPROVED.value);
		return result;
	}

	private void packageExtractPaySumExcelData(List<BudgetExtractImportdetail> qjExtract, Map<Long, BudgetExtractsum> budgetExtractsumMap, List<ExtractPaySumExcelData> result, Map<Long, BudgetYearPeriod> yearPeriodMap, String type) {
		//?????????id??????
		qjExtract.stream().collect(Collectors.groupingBy(e -> budgetExtractsumMap.get(e.getExtractsumid()).getDeptid())).forEach((deptId, detailsByDeptIdList) -> {

			Long extractsumid = detailsByDeptIdList.get(0).getExtractsumid();
			String deptName = budgetExtractsumMap.get(extractsumid).getDeptname();
			//??????????????????
			detailsByDeptIdList.stream().collect(Collectors.groupingBy(BudgetExtractImportdetail::getYearid)).forEach((yearid, detailsByDeptIdAndYearIdList) -> {

				BudgetYearPeriod budgetYearPeriod = yearPeriodMap.get(yearid);
				ExtractPaySumExcelData excelData = new ExtractPaySumExcelData();
				excelData.setExtractType(type);
				excelData.setDeptName(deptName);
				excelData.setYearPeriod(budgetYearPeriod.getPeriod());
				//???????????? = ??????????????????should_send_extract
				BigDecimal applyExtract = detailsByDeptIdAndYearIdList.stream().map(e -> e.getShouldSendExtract() == null ? BigDecimal.ZERO : e.getShouldSendExtract()).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setApplyExtract(applyExtract);
				//???????????? = ??????+????????????
				BigDecimal extractTax = detailsByDeptIdAndYearIdList.stream().map(e -> {
					BigDecimal tax = e.getTax() == null ? BigDecimal.ZERO : e.getTax();
					BigDecimal taxReduction = e.getTaxReduction() == null ? BigDecimal.ZERO : e.getTaxReduction();
					return tax.add(taxReduction);
				}).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setExtractTax(extractTax);
				//?????????????????? = ??????????????????+????????????????????????
				BigDecimal invoiceExcessTax = detailsByDeptIdAndYearIdList.stream().map(e -> {
					BigDecimal tax = e.getInvoiceExcessTax() == null ? BigDecimal.ZERO : e.getInvoiceExcessTax();
					BigDecimal taxReduction = e.getInvoiceExcessTaxReduction() == null ? BigDecimal.ZERO : e.getInvoiceExcessTaxReduction();
					return tax.add(taxReduction);
				}).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setInvoiceExcessTax(invoiceExcessTax);

				BigDecimal extract = detailsByDeptIdAndYearIdList.stream().map(BudgetExtractImportdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setExtract(extract);

				excelData.setOther(extract.subtract(applyExtract).subtract(extractTax).subtract(invoiceExcessTax));
				result.add(excelData);
			});
		});
	}

	private List<BigDecimal> spitExtractData(List<BudgetExtractImportdetail> qjExtract, BigDecimal totalCoppextract, BigDecimal totalFeePay, BigDecimal totalExtract) {
		if (totalCoppextract.compareTo(BigDecimal.ZERO) == 0)
			return Lists.newArrayList(BigDecimal.ZERO, BigDecimal.ZERO);
		BigDecimal extract = qjExtract.stream().map(BudgetExtractImportdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal percent = extract.divide(totalCoppextract, 4, BigDecimal.ROUND_HALF_DOWN);
		return Lists.newArrayList(percent.multiply(totalFeePay).setScale(0, BigDecimal.ROUND_HALF_DOWN),
				percent.multiply(totalExtract).setScale(0, BigDecimal.ROUND_HALF_DOWN));
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param importDetails ????????????
	 * @param heads         ????????????
	 * @param payDetails    ????????????
	 * @return
	 */
	private List<ExtractPayApplyExcelData> getExtractPayExcelDetails(List<BudgetExtractImportdetail> importDetails, Map<String, Object> heads, List<ExtractPayDetailVO> payDetails) {
		List<ExtractPayApplyExcelData> payApplyExcelDatas = new ArrayList<>();
		//??????????????????????????????
		payDetails.stream().filter(e -> e.getBillingUnitId() != null).collect(Collectors.groupingBy(ExtractPayDetailVO::getBillingUnitId)).forEach((billingUnitId, curUnitPayDetails) -> {
			payApplyExcelDatas.add(packageExtractBillingPayApply(curUnitPayDetails, importDetails));
		});
		//?????????
		payApplyExcelDatas.add(packageExtractAvoidPayApply(payDetails));
		//??????
		heads.put("applyExtractSum", payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getApplyExtract).reduce(BigDecimal.ZERO, BigDecimal::add));
		heads.put("extractTaxSum", payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getExtractTax).reduce(BigDecimal.ZERO, BigDecimal::add));
		heads.put("invoiceExcessTaxSum", payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getInvoiceExcessTax).reduce(BigDecimal.ZERO, BigDecimal::add));
		heads.put("otherSum", payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getOther).reduce(BigDecimal.ZERO, BigDecimal::add));
		BigDecimal feePaySum = payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getFeePay).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal extractSum = payApplyExcelDatas.stream().map(ExtractPayApplyExcelData::getExtract).reduce(BigDecimal.ZERO, BigDecimal::add);
		heads.put("feePaySum", feePaySum);
		heads.put("extractSum", extractSum);
		heads.put("extractChinese", NumberUtil.jenumberToChinese(feePaySum.add(extractSum)));
		return payApplyExcelDatas;
	}

	/**
	 * ????????????????????????????????????(?????????)
	 *
	 * @param payDetails ????????????
	 * @return
	 */
	private ExtractPayApplyExcelData packageExtractAvoidPayApply(List<ExtractPayDetailVO> payDetails) {
		payDetails = payDetails.stream().filter(e -> StringUtils.isNotBlank(e.getAvoidBillingNnitname()) && e.getAvoidBillingPaymoney() != null).collect(Collectors.toList());
		ExtractPayApplyExcelData data = new ExtractPayApplyExcelData();
		data.setBillingUnitName(payDetails.get(0).getAvoidBillingNnitname());
		BigDecimal extract = payDetails.stream().map(e -> e.getAvoidBillingPaymoney() == null ? BigDecimal.ZERO : e.getAvoidBillingPaymoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setExtract(extract);
		return data;
	}

	/**
	 * ????????????????????????????????????(????????????)
	 *
	 * @param curUnitPayDetails ??????????????????????????????
	 * @param importDetailMap   ??????????????????
	 * @return
	 */
	private ExtractPayApplyExcelData packageExtractBillingPayApply(List<ExtractPayDetailVO> curUnitPayDetails, List<BudgetExtractImportdetail> importDetails) {
		ExtractPayApplyExcelData data = new ExtractPayApplyExcelData();
		List<String> extractDetailIds = curUnitPayDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		/**
		 * ??????????????????????????????????????????
		 */
		List<BudgetExtractImportdetail> curUnitImportDetails = importDetails.stream().filter(e -> extractDetailIds.contains(e.getExtractdetailid().toString())).collect(Collectors.toList());
		data.setBillingUnitName(curUnitPayDetails.get(0).getBillingUnitname());
		//???????????? = ??????????????????should_send_extract
		BigDecimal applyExtract = curUnitImportDetails.stream().map(e -> e.getShouldSendExtract() == null ? BigDecimal.ZERO : e.getShouldSendExtract()).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setApplyExtract(applyExtract);
		//???????????? = ??????+????????????
		BigDecimal extractTax = curUnitImportDetails.stream().map(e -> {
			BigDecimal tax = e.getTax() == null ? BigDecimal.ZERO : e.getTax();
			BigDecimal taxReduction = e.getTaxReduction() == null ? BigDecimal.ZERO : e.getTaxReduction();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setExtractTax(extractTax);
		//?????????????????? = ??????????????????+????????????????????????
		BigDecimal invoiceExcessTax = curUnitImportDetails.stream().map(e -> {
			BigDecimal tax = e.getInvoiceExcessTax() == null ? BigDecimal.ZERO : e.getInvoiceExcessTax();
			BigDecimal taxReduction = e.getInvoiceExcessTaxReduction() == null ? BigDecimal.ZERO : e.getInvoiceExcessTaxReduction();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setInvoiceExcessTax(invoiceExcessTax);
		//???????????? = ?????????????????? + ????????????
		BigDecimal feePay = curUnitPayDetails.stream().map(e -> {
			BigDecimal tax = e.getPayFee() == null ? BigDecimal.ZERO : e.getPayFee();
			BigDecimal taxReduction = e.getBeforeCalFee() == null ? BigDecimal.ZERO : e.getBeforeCalFee();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setFeePay(feePay);
		//????????????????????????
		BigDecimal extract = curUnitPayDetails.stream().map(e -> e.getBillingPaymoney() == null ? BigDecimal.ZERO : e.getBillingPaymoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setExtract(extract);

		//??????
		BigDecimal other = data.getApplyExtract().subtract(data.getExtract()).subtract(data.getExtractTax()).subtract(data.getInvoiceExcessTax()).subtract(data.getFeePay());
		data.setOther(other);
		return data;
	}

	/**
	 * ??????
	 *
	 * @param extractMonth
	 */
	public void sign(String extractMonth) {
		BudgetExtractSignLog signLog = new BudgetExtractSignLog();
		BaseUser baseUser = LoginThreadLocal.get();
		signLog.setEmpNo(baseUser.getEmpno());
		signLog.setEmpName(baseUser.getEmpname());
		signLog.setCreateTime(new Date());
		signLog.setExtractMonth(extractMonth);
		signLogMapper.insert(signLog);
	}

	/**
	 * ????????????????????????
	 */
	public BudgetExtractSignMain getExtractSignList(String extractMonth) {
		BudgetExtractSignMain result = new BudgetExtractSignMain();
		result.setExtractMonth(extractMonth);
		List<BudgetExtractSignLog> budgetExtractSignLogs = signLogMapper.selectList(new LambdaQueryWrapper<BudgetExtractSignLog>().eq(BudgetExtractSignLog::getExtractMonth, extractMonth));
		List<BudgetExtractSignDetail> details = budgetExtractSignLogs.stream().map(e -> {
			BudgetExtractSignDetail detail = new BudgetExtractSignDetail();
			detail.setEmpName(e.getEmpName());
			detail.setCreateTime(e.getCreateTime());
			return detail;
		}).sorted((e1, e2) -> Long.compare(e2.getCreateTime().getTime(), e1.getCreateTime().getTime())).collect(Collectors.toList());
		result.setSignDetails(details);
		return result;
	}


	/**
	 * ????????????????????????
	 *
	 * @param extractsum
	 * @param details
	 */
	public void exportExtractPaymentDetail(BudgetExtractsum extractsum, List<ExtractPaymentExcelData> details) {

		/**
		 * ??????????????????????????????????????????
		 * ???????????????????????????????????????
		 */
		List<BudgetExtractdetail> allExtractDetails = this.getExtractDetailByExtractmonth(extractsum.getExtractmonth());
		//???????????????????????????
		List<BudgetExtractdetail> curExtractDetails = allExtractDetails.stream().filter(e -> e.getExtractsumid().equals(extractsum.getId())).collect(Collectors.toList());

		Map<Long, List<BudgetPaymoney>> paymoneyMap = paymoneyService.list(new QueryWrapper<BudgetPaymoney>().eq("paymoneytype", PaymoneyTypeEnum.EXTRACT_PAY.getType())).stream().collect(Collectors.groupingBy(e -> e.getPaymoneyobjectid()));

		/**
		 * ????????????????????????????????????
		 */
		Long unHandleExtractCount = curExtractDetails.stream().filter(e -> e.getExcesstype() != null && e.getExcesstype().intValue() == ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType() &&
				e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0
				&& e.getHandleflag() != null && !e.getHandleflag()).count();
		if (unHandleExtractCount > 0)
			throw new RuntimeException("??????????????????????????????" + extractsum.getCode() + "??????????????????????????????????????????");

		List<BudgetExtractpaydetail> curExtractPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", extractsum.getExtractmonth()));
		Map<Long, BudgetExtractpaydetail> payDetailMap = curExtractPayDetails.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getId, e -> e, (e1, e2) -> e1));
		//?????????????????????
		List<BudgetExtractpayment> paymentList = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", curExtractPayDetails.stream().map(e -> e.getId()).collect(Collectors.toList())));

		//Map<String, WbBanks> bankMap = this.bankMapper.selectList(null).stream().collect(Collectors.toMap(WbBanks::getSubBranchCode, e -> e, (e1, e2) -> e1));


		for (BudgetExtractpayment payment : paymentList) {
			ExtractPaymentExcelData ed = new ExtractPaymentExcelData();
			BudgetExtractpaydetail extractpaydetail = payDetailMap.get(payment.getBudgetextractpaydetailid());
			ed.setEmpNo(extractpaydetail.getEmpno());
			ed.setEmpName(extractpaydetail.getEmpname());

			/**
			 * update by minzhq 2022-1-18  ????????????????????????????????????????????????????????????????????????????????????????????????
			 * 1.?????????????????????????????????????????????????????????????????????????????????????????????
			 * 2.?????????????????????????????????(?????????????????????)
			 */
			BudgetExtractdetail extractDetail = null;
			String[] detailIds = payment.getExtractdetailids().split(",");
			int extractSize = detailIds.length;
			int index = -1;
			for (int i = 0; i < extractSize; i++) {
				String detailId = detailIds[i];
				extractDetail = curExtractDetails.stream().filter(e -> e.getId().toString().equals(detailId)).findFirst().orElse(null);
				if (extractDetail == null) continue;
				index = i;
				break;
			}
			if (extractDetail == null) continue;

			String idnumber = extractDetail.getIdnumber();
			BigDecimal totalCopeextract = allExtractDetails.stream().filter(e -> idnumber.equals(e.getIdnumber())).map(e -> e.getCopeextract()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal percent = BigDecimal.ZERO;
			if (totalCopeextract.compareTo(BigDecimal.ZERO) == 0) {
			} else {
				percent = extractDetail.getCopeextract().divide(totalCopeextract, 20, BigDecimal.ROUND_HALF_UP);
			}
			ed.setRealExtract(extractDetail.getCopeextract());
			ed.setConsotax(extractDetail.getConsotax());
			ed.setSalaryUnitName(payment.getBunitname1());

			BigDecimal paymentMoney1 = payment.getPaymoney1();
			BigDecimal salaryUnitPayMoney = getPercentMoney(paymentMoney1, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails, BigDecimal.ROUND_DOWN);
			ed.setSalaryUnitPayMoney(salaryUnitPayMoney);
			ed.setAvoidUnitName(payment.getBunitname2());
			BigDecimal paymentMoney2 = payment.getPaymoney2();
			BigDecimal avoidUnitPayMoney = getPercentMoney(paymentMoney2, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails, BigDecimal.ROUND_HALF_UP);
			ed.setAvoidUnitPayMoney(avoidUnitPayMoney);

			//??????????????????
			BigDecimal excessPayfee = payment.getPayfee();
			BigDecimal fee1 = getPercentMoney(excessPayfee, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails, BigDecimal.ROUND_HALF_UP);

			//????????????
			BigDecimal beforeCalFee = payment.getBeforeCalFee();
			BigDecimal fee2 = getPercentMoney(beforeCalFee, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails, BigDecimal.ROUND_HALF_UP);
			ed.setFee(fee1.add(fee2));

			ed.setBankAccount(payment.getBankaccount());
			ed.setBankName(payment.getBankaccountbranchname());
			ed.setOpenBank(payment.getBankaccountopenbank());

			WbBanks bank = bankCache.getBankByBranchCode(payment.getBankaccountbranchcode());
			if (bank == null) {
				List<BudgetPaymoney> budgetPaymonies = paymoneyMap.get(payment.getId());
				if (CollectionUtils.isEmpty(budgetPaymonies))
					throw new RuntimeException("??????????????????????????????????????????????????????" + payment.getBankaccountbranchcode() + "???");
				bank = bankCache.getBankByBranchCode(budgetPaymonies.get(0).getBankaccountbranchcode());
				if (bank == null) {
					throw new RuntimeException("??????????????????????????????????????????????????????" + payment.getBankaccountbranchcode() + "???");
				}
			}
			String province = bank.getProvince();
			String city = bank.getCity();
			if (province.contains("??????") || province.contains("??????") || province.contains("??????") || province.contains("??????")) {
				//?????????
				ed.setProvince(province.substring(0, 2));
				ed.setCity(province.substring(0, 2));
			} else {
				ed.setProvince(province.substring(0, 2));
				ed.setCity(city.substring(0, 2));
			}

			if (extractDetail.getRepaymoneyid() != null) {
				//????????????
				List<Map<String, Object>> repayMoneyDetailList = this.getRepaymoneymsg(extractDetail.getRepaymoneyid());
				boolean flag = false;
				for (Map<String, Object> repaymoneymsg : repayMoneyDetailList) {
					ExtractPaymentExcelData newed = new ExtractPaymentExcelData();
					BeanUtils.copyProperties(ed, newed);
					newed.setProjectName(repaymoneymsg.get("projectname").toString());
					newed.setDeductionMoney(new BigDecimal(repaymoneymsg.get("repaymoney").toString()));
					if (!flag) {
						flag = true;
						newed.setIsSum(true);
					}
					details.add(newed);
				}
			} else {
				ed.setIsSum(true);
				details.add(ed);
			}
		}
	}

	private BigDecimal getPercentMoney(BigDecimal paymentMoney1, BigDecimal totalCopeextract, int index, int extractSize, BigDecimal percent, String[] detailIds, List<BudgetExtractdetail> allExtractDetails, int round) {
		BigDecimal salaryUnitPayMoney = BigDecimal.ZERO;
		if (paymentMoney1 != null) {
			if (totalCopeextract.compareTo(BigDecimal.ZERO) == 0) {
				salaryUnitPayMoney = paymentMoney1;
			} else {
				if (index < extractSize - 1) {
					//?????????
					salaryUnitPayMoney = percent.multiply(paymentMoney1).setScale(0, round);
				} else {
					BigDecimal money = BigDecimal.ZERO;
					//?????????
					for (int i = 0; i < extractSize - 1; i++) {
						String detailId = detailIds[i];
						BudgetExtractdetail lastExtractDetail = allExtractDetails.stream().filter(e -> e.getId().toString().equals(detailId)).findFirst().orElse(null);
						BigDecimal m = lastExtractDetail.getCopeextract().divide(totalCopeextract, 20, BigDecimal.ROUND_HALF_UP).multiply(paymentMoney1).setScale(0, round);
						money = money.add(m);
					}
					salaryUnitPayMoney = paymentMoney1.subtract(money);
				}
			}
		}
		return salaryUnitPayMoney;
	}

	public List<BudgetExtractsum> getCurBatchExtractSum(String extractBatch){
		return this.budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch).eq(BudgetExtractsum::getDeleteflag, 0).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
	}

	public List<BudgetExtractsum> getFutureBatchExtractSumContainSelf(String extractBatch){
		return this.budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().ge(BudgetExtractsum::getExtractmonth, extractBatch).eq(BudgetExtractsum::getDeleteflag, 0).ne(BudgetExtractsum::getStatus, ExtractStatusEnum.REJECT.getType()));
	}

	public List<BudgetExtractdetail> getExtractDetailBySumIds(List<Long> sumIds,Boolean isPersonlity,Long personalityId){
		LambdaQueryWrapper<BudgetExtractdetail> qw = new LambdaQueryWrapper<>();
		if(!CollectionUtils.isEmpty(sumIds)){
			qw.in(BudgetExtractdetail::getExtractsumid, sumIds);
		}
		if(isPersonlity!=null){
			if(isPersonlity){
				qw.eq(BudgetExtractdetail::getBusinessType,ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode());
			}else{
				qw.ne(BudgetExtractdetail::getBusinessType,ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode());
			}
		}
		if(personalityId!=null){
			IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMapper.selectById(personalityId);
			qw.eq(BudgetExtractdetail::getEmpno,individualEmployeeFiles.getEmployeeJobNum());
			qw.eq(BudgetExtractdetail::getEmpname,individualEmployeeFiles.getEmployeeName());
		}
		return this.extractDetailMapper.selectList(qw);
	}

	public List<ExtractExcessExcelData> importExtractExcessDetail(InputStream inputStream, String extractBatch) throws IOException {
		List<ExtractExcessExcelData> extractExcessExcelData = EasyExcelUtil.getExcelContent(inputStream, ExtractExcessExcelData.class);

		List<BudgetExtractsum> sums = getCurBatchExtractSum(extractBatch);
		List<Long> sumIds = sums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		List<BudgetExtractdetail> extractDetailList = getExtractDetailBySumIds(sumIds,false,null);
		//key ???????????????
		Map<String, BudgetExtractpaydetail> payDetailMap = this.payDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractpaydetail>().eq(BudgetExtractpaydetail::getExtractmonth, extractBatch)).stream().collect(Collectors.toMap(BudgetExtractpaydetail::getIdnumber, e -> e, (e1, e2) -> e1));


		extractExcessExcelData.stream().collect(Collectors.groupingBy(ExtractExcessExcelData::getIdNumber)).forEach((idnumber, list) -> {
			int size = list.stream().collect(Collectors.groupingBy(e -> e.getEmpNo().concat(e.getIsCompanyEmp()))).size();
			if (size > 1) {
				list.forEach(e -> {
					e.setErrMsg("??????????????????????????????????????????");
				});
				return;
			}
			list.forEach(e -> {
				try {
					validateImportExcessDetails(e);
				} catch (Exception ex) {
					ex.printStackTrace();
					e.setErrMsg(ex.getMessage());
				}
			});
			if (list.stream().noneMatch(e -> StringUtils.isNotBlank(e.getErrMsg()))){
				try {
					validateMoney(list, extractDetailList);
				} catch (Exception ex) {
					ex.printStackTrace();
					list.forEach(e -> e.setErrMsg(ex.getMessage()));
				}
			}
		});
		if (extractExcessExcelData.stream().noneMatch(e -> StringUtils.isNotBlank(e.getErrMsg()))) {
			extractExcessExcelData.stream().collect(Collectors.groupingBy(ExtractExcessExcelData::getIdNumber)).forEach((idnumber, list) -> {
				setExcessPay(list, extractDetailList, payDetailMap);
			});
			invokeExtractCalEndPostProcessor(getExtractTaxHandleRecord(extractBatch), extractBatch, sums, 2);
		}
		return extractExcessExcelData;
	}

	private void validateMoney(List<ExtractExcessExcelData> dataList, List<BudgetExtractdetail> extractDetailList) {
		ExtractExcessExcelData data = dataList.get(0);
		String idNumber = data.getIdNumber();
		String empNo = data.getEmpNo();
		String empName = data.getEmpName();
		BigDecimal avoidTaxMoney = dataList.stream().map(ExtractExcessExcelData::getAvoidTaxMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		String outUnit = data.getOutUnit();
		BigDecimal outUnitPayMoney = dataList.stream().map(ExtractExcessExcelData::getOutUnitPayMoney).reduce(BigDecimal.ZERO, BigDecimal::add);

		List<BudgetExtractdetail> curExtractDetails = extractDetailList.stream().filter(e -> e.getIdnumber().equals(idNumber)).collect(Collectors.toList());
		if (curExtractDetails.isEmpty()) return;
		BudgetExtractdetail extractdetail = curExtractDetails.get(0);
		if (!empNo.equals(extractdetail.getEmpno())) throw new RuntimeException("?????????????????????????????????");
		if (!empName.equals(extractdetail.getEmpname())) throw new RuntimeException("?????????????????????????????????");
		if (extractdetail.getExcessmoney().compareTo(avoidTaxMoney.add(outUnitPayMoney)) != 0)
			throw new RuntimeException("????????????????????????????????????" + NumberUtil.subZeroAndDot(extractdetail.getExcessmoney()) + "???");
	}

	private void validateImportExcessDetails(ExtractExcessExcelData excelData) {

		if (StringUtils.isBlank(excelData.getIdNumber())) throw new RuntimeException("????????????????????????");
		if (StringUtils.isBlank(excelData.getEmpNo())) throw new RuntimeException("??????????????????");
		if (StringUtils.isBlank(excelData.getEmpName())) throw new RuntimeException("??????????????????");
		if (StringUtils.isNotBlank(excelData.getOutUnit())) {
			BudgetBillingUnit budgetBillingUnit = this.billingUnitMapper.selectOne(new LambdaQueryWrapper<BudgetBillingUnit>().eq(BudgetBillingUnit::getName, excelData.getOutUnit()).eq(BudgetBillingUnit::getOwnFlag, 1));
			if (Objects.isNull(budgetBillingUnit)) {
				throw new RuntimeException("????????????????????????" + excelData.getOutUnit() + "???????????????");
			}
			if (budgetBillingUnit.getStopFlag() == 1) {
				throw new RuntimeException("????????????????????????" + excelData.getOutUnit() + "??????????????????");
			}
			List<BudgetBillingUnitAccount> billingUnitAccounts = this.billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().eq(BudgetBillingUnitAccount::getBillingunitid, budgetBillingUnit.getId()).eq(BudgetBillingUnitAccount::getStopflag, 0).orderByDesc(BudgetBillingUnitAccount::getOrderno));
			if (CollectionUtils.isEmpty(billingUnitAccounts)) {
				throw new RuntimeException("????????????????????????" + excelData.getOutUnit() + "????????????????????????");
			}
		}
		if (StringUtils.isNotBlank(excelData.getOutUnit()) && excelData.getOutUnitPayMoney() == null) {

			throw new RuntimeException("?????????????????????????????????");
		}
		if (excelData.getAvoidTaxMoney().compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("??????????????????????????????");
		}
		if (excelData.getOutUnitPayMoney().compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("??????????????????????????????");
		}

		if (excelData.getAvoidTaxMoney().compareTo(BigDecimal.ZERO) == 0 && excelData.getOutUnitPayMoney().compareTo(BigDecimal.ZERO) == 0)
			throw new RuntimeException("?????????????????????????????????????????????????????????");
	}

	/**
	 * <p>????????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/8/30 16:38
	 * @param extractBatch
	 */
	public Map<String, List<ExtractOutUnitPayExcelData>> getExtractOutUnitPayDetails(String extractBatch) {
		List<BudgetExtractpaymentOuterUnit> extractpaymentOuterUnits = outerUnitService.list(new LambdaQueryWrapper<BudgetExtractpaymentOuterUnit>().eq(BudgetExtractpaymentOuterUnit::getExtractMonth, extractBatch));
		Map<String, List<ExtractOutUnitPayExcelData>> result = new HashMap<>();
		Map<Long,BudgetExtractpayment> paymentMap = new HashMap<>();
		extractpaymentOuterUnits.stream().collect(Collectors.groupingBy(BudgetExtractpaymentOuterUnit::getBillingUnitId)).forEach((unitId,list)->{
			List<ExtractOutUnitPayExcelData> dataList = list.stream().map(e -> {
				BudgetExtractpayment extractpayment = null;
				if (paymentMap.get(e.getExtractPaymentId()) == null) {
					extractpayment = this.paymentMapper.selectById(e.getExtractPaymentId());
					paymentMap.put(e.getExtractPaymentId(), extractpayment);
				} else {
					extractpayment = paymentMap.get(e.getExtractPaymentId());
				}
				WbBanks bank = bankCache.getBankByBranchCode(extractpayment.getBankaccountbranchcode());
				return new ExtractOutUnitPayExcelData(extractpayment.getBankaccountname(),extractpayment.getBankaccount(),  extractpayment.getBankaccountbranchname(), extractpayment.getBankaccountopenbank(), bank.getProvince(), bank.getCity(), e.getPayMoney());
			}).collect(Collectors.toList());
			result.put(list.get(0).getBillingUnitName(),dataList);
		});
		return result;
	}

	/**
	 * <p>???????????????????????????</p>
	 * @author minzhq
	 * @date 2022/8/31 11:34
	 * @param extractBatch
	 */
	public List<ExtractPersonlityDetailExcelData> getExtractPersonlityDetail(String extractBatch) {

		List<BudgetExtractsum> sums = getCurBatchExtractSum(extractBatch);
		List<Long> sumIds = sums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//?????????????????????
		List<BudgetExtractdetail> extractDetailList = getExtractDetailBySumIds(sumIds,true,null);
		List<ExtractPersonlityDetailExcelData> resultList = new ArrayList<>();
		if(CollectionUtils.isEmpty(extractDetailList)) return resultList;
		//???????????????
		Map<String, List<IndividualEmployeeFiles>> individualEmployeeFilesMap = this.individualEmployeeFilesMapper.selectList(new LambdaQueryWrapper<IndividualEmployeeFiles>().eq(IndividualEmployeeFiles::getStatus,1)).stream().collect(Collectors.groupingBy(e -> e.getEmployeeJobNum().toString() + "&&" + e.getEmployeeName()));
		List<Long> individualEmployeeIdList = individualEmployeeFilesMap.values().stream().flatMap(e->e.stream()).map(e->e.getId()).collect(Collectors.toList());
		Map<String, ExtractPersonlityDetailExcelData> individualEmployeeAgoPayDetailMap = getIndividualEmployeeAgoPayDetail(individualEmployeeIdList, extractBatch);
		Map<Long, BigDecimal> receiptSum = getReceiptSum(individualEmployeeIdList, extractBatch);
		extractDetailList.stream().collect(Collectors.groupingBy(e->e.getEmpno()+"&&"+e.getEmpname())).forEach((key,list)->{
			List<IndividualEmployeeFiles> individualEmployeeFiles = individualEmployeeFilesMap.get(key);
			List<String> sizeList = new ArrayList<>(1);
			individualEmployeeFiles.forEach(individualEmployeeFile->{
				ExtractPersonlityDetailExcelData excelData = ExtractPersonlityDetailExcelData.transfer(individualEmployeeFile);
				excelData.setOrderNumber(resultList.size()+1);
				BigDecimal extract = list.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
				if(sizeList.size() == 0){
					excelData.setExtract(extract);
					sizeList.add("0");
				}
				ExtractPersonlityDetailExcelData agoExcelData = individualEmployeeAgoPayDetailMap.get(individualEmployeeFile.getId().toString() + "&&" + individualEmployeeFile.getIssuedUnit());
				if(Objects.nonNull(agoExcelData)){
					excelData.setExtractSum(agoExcelData.getExtractSum());
					excelData.setSalarySum(agoExcelData.getSalarySum());
					excelData.setWelfareSum(agoExcelData.getWelfareSum());
				}else{
					excelData.setExtractSum(BigDecimal.ZERO);
					excelData.setSalarySum(BigDecimal.ZERO);
					excelData.setWelfareSum(BigDecimal.ZERO);
				}
				excelData.setReceiptSum(receiptSum.get(individualEmployeeFile.getId()));
				excelData.setMoneySum(excelData.getExtractSum().add(excelData.getSalarySum()).add(excelData.getWelfareSum()));
				excelData.setPayStatus(ExtractPersonalityPayStatusEnum.COMMON.value);
				BudgetBillingUnit budgetBillingUnit = billingUnitMapper.selectById(individualEmployeeFile.getIssuedUnit());
				if(budgetBillingUnit!=null){
					excelData.setBillingUnitName(budgetBillingUnit.getName());
				}
				resultList.add(excelData);
			});
		});
		return resultList;
	}

	public List<IndividualEmployeeTicketReceiptInfo> getIndividualEmployeeTicketReceiptInfoList(List<Long> individualEmployeeIdList){
		return individualEmployeeTicketReceiptInfoMapper.selectList(new LambdaQueryWrapper<IndividualEmployeeTicketReceiptInfo>().in(IndividualEmployeeTicketReceiptInfo::getIndividualEmployeeInfoId, individualEmployeeIdList));
	}

	/**
	 * <p>??????????????????????????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/2 10:54
	 * @param individualEmployeeIdList ???????????????id
	 * @param extractBatch ????????????
	 */
	public Map<String,ExtractPersonlityDetailExcelData> getIndividualEmployeeAgoPayDetail(List<Long> individualEmployeeIdList,String extractBatch){
		List<BudgetExtractPersonalityPayDetail>	extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().lt(BudgetExtractPersonalityPayDetail::getExtractMonth,extractBatch).in(BudgetExtractPersonalityPayDetail::getPayStatus,Lists.newArrayList(ExtractPersonalityPayStatusEnum.COMMON.type,ExtractPersonalityPayStatusEnum.DELAY.type)).isNotNull(BudgetExtractPersonalityPayDetail::getOperateTime));

		Map<String,ExtractPersonlityDetailExcelData> result = new HashMap<>();
		individualEmployeeIdList.forEach(individualEmployeeId->{
			extractPersonalityPayDetails.stream().filter(e -> e.getPersonalityId().toString().equals(individualEmployeeId.toString())).collect(Collectors.groupingBy(BudgetExtractPersonalityPayDetail::getBillingUnitId)).forEach((curBillingUnitId,list)->{
				String key = individualEmployeeId.toString()+"&&"+curBillingUnitId.toString();
				ExtractPersonlityDetailExcelData excelData = new ExtractPersonlityDetailExcelData();
				excelData.setExtractSum(list.stream().map(BudgetExtractPersonalityPayDetail::getCurRealExtract).reduce(BigDecimal.ZERO,BigDecimal::add));
				excelData.setSalarySum(list.stream().map(BudgetExtractPersonalityPayDetail::getCurSalary).reduce(BigDecimal.ZERO,BigDecimal::add));
				excelData.setWelfareSum(list.stream().map(BudgetExtractPersonalityPayDetail::getCurWelfare).reduce(BigDecimal.ZERO,BigDecimal::add));
				result.put(key,excelData);
			});
		});
		return result;
	}

	public Map<Long,BigDecimal> getReceiptSum(List<Long> individualEmployeeIdList,String extractBatch){
		//Map<Long, BigDecimal> receiptInfoMap = getIndividualEmployeeTicketReceiptInfoList(individualEmployeeIdList).stream().collect(Collectors.groupingBy(IndividualEmployeeTicketReceiptInfo::getIndividualEmployeeInfoId, Collectors.mapping(Function.identity(), Collectors.collectingAndThen(Collectors.toList(), e -> e.stream().map(IndividualEmployeeTicketReceiptInfo::getInvoiceAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))));
		Map<Long, BigDecimal> receiptInfoMap = new HashMap<>();
		Map<Long, List<IndividualEmployeeTicketReceiptInfo>> receiptInfos = getIndividualEmployeeTicketReceiptInfoList(individualEmployeeIdList).stream().collect(Collectors.groupingBy(e -> e.getIndividualEmployeeInfoId()));
		receiptInfos.forEach((key,list)->{
			BigDecimal money = list.stream().map(e -> e.getInvoiceAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
			receiptInfoMap.put(key,money);
		});

		Map<Long, BigDecimal> resultMap = new HashMap<>();
		Map<Long, BigDecimal> initReceiptMap = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getIsInitData, 1).in(BudgetExtractPersonalityPayDetail::getPersonalityId, individualEmployeeIdList)).stream().collect(Collectors.toMap(e -> e.getPersonalityId(), e -> e.getReceiptSum(),(e1,e2)->e1));

		receiptInfoMap.forEach((id,money)->{
			if(resultMap.get(id)==null){
				resultMap.put(id,money);
			}else{
				resultMap.put(id,resultMap.get(id).add(money));
			}
		});
		initReceiptMap.forEach((id,money)->{
			if(resultMap.get(id)==null){
				resultMap.put(id,money);
			}else{
				resultMap.put(id,resultMap.get(id).add(money));
			}
		});
		return resultMap;
	}

	/**
	 * <p>???????????????????????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/8/31 15:16
	 * @param extractBatch
	 */
	public void validateIsCanOperatePersonalityPayDetail(String extractBatch) {
		validateExtractIsAllPass(extractBatch);
		BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(extractBatch);
		if (extractTaxHandleRecord!=null && extractTaxHandleRecord.getIsPersonalityComplete()) {
			throw new RuntimeException("???????????????????????????" + extractBatch + "???????????????????????????");
		}
		validateFuturePersonality(extractBatch);
//
//		Integer count1 = taxHandleRecordMapper.getOldBatchUnHandleCount(extractBatch);
//		if(count1>0){
//			throw new RuntimeException("????????????????????????????????????");
//		}
	}

	public void validateFuturePersonality(String extractBatch){
		//int count = taxHandleRecordService.count(new LambdaQueryWrapper<BudgetExtractTaxHandleRecord>().gt(BudgetExtractTaxHandleRecord::getExtractMonth, extractBatch).and(e -> {
//			e.eq(BudgetExtractTaxHandleRecord::getIsCalComplete, 1).or().eq(BudgetExtractTaxHandleRecord::getIsPersonalityComplete, 1);
//		}));
//		if(count>0){
//			throw new RuntimeException("???????????????????????????????????????");
//		}
	}

	public void validateExtractIsAllPass(String extractBatch){
		List<BudgetExtractsum> curBatchExtractSum = getCurBatchExtractSum(extractBatch);
		if(!CollectionUtils.isEmpty(curBatchExtractSum)){
			long count = curBatchExtractSum.stream().filter(e -> e.getStatus() < ExtractStatusEnum.APPROVED.type).count();
			if(count>0){
				throw new RuntimeException("????????????????????????????????????" + extractBatch + "???????????????????????????????????????");
			}
			long count1 = curBatchExtractSum.stream().filter(e -> e.getStatus() > ExtractStatusEnum.APPROVED.type).count();
			if(count1>0){
				throw new RuntimeException("????????????????????????????????????????????????");
			}
		}else{
			throw new RuntimeException("??????????????????????????????" + extractBatch + "???????????????");
		}
	}
	/**
	 * <p>?????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/1 9:55
	 * @param inputStream ?????????
	 * @param extractBatch ??????
	 */
	public List<ExtractPersonlityDetailExcelData> importPersonalityPayDetail(InputStream inputStream, String extractBatch) {
		List<ExtractPersonlityDetailExcelData> excelDataList = EasyExcelUtil.getExcelContent(inputStream, ExtractPersonlityDetailExcelData.class);
		Map<String,IndividualEmployeeFiles> employeeFilesMap = new HashMap<>();
		Map<String,BudgetBillingUnit> billingUnitMap = new HashMap<>();
		excelDataList.forEach(data->{
			try{
				validateImportPersonalityPayDetail(data,employeeFilesMap,billingUnitMap);
			}catch (Exception e){
				data.setErrMsg(e.getMessage());
				return;
			}
		});
		List<BudgetExtractdetail> extractDetailList = getCurBatchPersionalityExtract(extractBatch,null);
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>()
				.eq(BudgetExtractPersonalityPayDetail::getExtractMonth, extractBatch));
		if(excelDataList.stream().noneMatch(e->StringUtils.isNotBlank(e.getErrMsg()))){
			excelDataList.stream().collect(Collectors.groupingBy(e->{
				return e.getEmpNo().toString().concat(e.getEmpName());
			})).forEach((account,list)->{

				long count1 = extractDetailList.stream().filter(e -> e.getEmpno().equals(list.get(0).getEmpNo().toString()) && e.getEmpname().equals(list.get(0).getEmpName())).count();
				if(count1 == 0){
					list.forEach(e->{
						e.setErrMsg("???????????????????????????????????????????????????");
					});
					return;
				}

				IndividualEmployeeFiles individualEmployeeFiles = employeeFilesMap.get(list.get(0).getEmpNo() + "&&" + list.get(0).getPersonlityName());
				long count = extractPersonalityPayDetails.stream().filter(e -> e.getPersonalityId().equals(individualEmployeeFiles.getId())).count();
				if(count>0){
					list.forEach(e->{
						e.setErrMsg("??????????????????????????????????????????");
					});
					return;
				}

				list.stream().collect(Collectors.groupingBy(e->{
					return employeeFilesMap.get(e.getEmpNo() + "&&" + e.getPersonlityName()).getId();
				})).forEach((id,list1)->{
					list1.stream().collect(Collectors.groupingBy(ExtractPersonlityDetailExcelData::getBillingUnitName)).forEach((unitName,list2)->{
						if(list2.size()>1){
							list2.forEach(e->{
								e.setErrMsg("???????????????????????????????????????????????????");
							});
							return;
						}
					});

				});
				if(list.stream().anyMatch(e->StringUtils.isNotBlank(e.getErrMsg()))){
					return;
				}
				int size = list.stream().collect(Collectors.groupingBy(ExtractPersonlityDetailExcelData::getPayStatus)).size();
				if(size>1){
					list.forEach(e->{
						e.setErrMsg("????????????????????????????????????????????????");
					});
					return;
				}
				//????????????????????????????????????????????????????????????????????????
//				List<BudgetExtractPersonalityPayDetail> dbList = extractPersonalityPayDetails.stream().filter(e->e.getPersonalityId().equals(individualEmployeeFiles.getId())).filter(dbDetail -> {
//					return list.stream().noneMatch(e -> {
//						IndividualEmployeeFiles individualEmployeeFiles1 = employeeFilesMap.get(e.getEmpNo() + "&&" + e.getPersonlityName());
//						BudgetBillingUnit budgetBillingUnit = billingUnitMap.get(e.getBillingUnitName());
//						return individualEmployeeFiles1.getId().equals(dbDetail.getPersonalityId()) && dbDetail.getBillingUnitId().equals(budgetBillingUnit.getId());
//					});
//				}).collect(Collectors.toList());

				//????????????
				BigDecimal extract = extractDetailList.stream().filter(detail -> {
					return detail.getEmpno().equals(individualEmployeeFiles.getEmployeeJobNum().toString()) && detail.getEmpname().equals(individualEmployeeFiles.getEmployeeName()) && ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode().equals(detail.getBusinessType());
				}).map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
//				BigDecimal dbMoney = dbList.stream().filter(e -> e.getPersonalityId().equals(individualEmployeeFiles.getId())).map(e -> {
//					return e.getCurRealExtract();
//				}).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal dbMoney = BigDecimal.ZERO;
				BigDecimal importMoney = list.stream().map(e -> {
					BigDecimal t1 = StringUtils.isBlank(e.getCurExtract())?BigDecimal.ZERO:new BigDecimal(e.getCurExtract());
					//BigDecimal t2 = StringUtils.isBlank(e.getCurSalary())?BigDecimal.ZERO:new BigDecimal(e.getCurSalary());
					//BigDecimal t3 = StringUtils.isBlank(e.getCurWelfare())?BigDecimal.ZERO:new BigDecimal(e.getCurWelfare());
					return t1;
				}).reduce(BigDecimal.ZERO, BigDecimal::add);

				if(extract.subtract(dbMoney).subtract(importMoney).compareTo(BigDecimal.ZERO)<0){
					list.forEach(e->{
						e.setErrMsg("?????????????????????????????????????????????????????????????????????????????????");
					});
				}
			});
		}
		if(excelDataList.stream().noneMatch(e->StringUtils.isNotBlank(e.getErrMsg()))){
			doImportPersonalityPayDetail(excelDataList,employeeFilesMap,extractBatch,billingUnitMap,extractPersonalityPayDetails);
			reCalculateInvoice(employeeFilesMap.values().stream().map(IndividualEmployeeFiles::getId).collect(Collectors.toList()),extractBatch);
		}

		return excelDataList;
	}

	private void doImportPersonalityPayDetail(List<ExtractPersonlityDetailExcelData> excelDataList, Map<String,IndividualEmployeeFiles> employeeFilesMap,String extractBatch,Map<String,BudgetBillingUnit> billingUnitMap,List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails) {

		List<Long> IndividualEmployeeFilesIdList = employeeFilesMap.values().stream().map(IndividualEmployeeFiles::getId).distinct().collect(Collectors.toList());
		Map<String, ExtractPersonlityDetailExcelData> individualEmployeeAgoPayDetailMap = getIndividualEmployeeAgoPayDetail(IndividualEmployeeFilesIdList, extractBatch);

		List<BudgetExtractdetail> extractDetailList = getCurBatchPersionalityExtract(extractBatch,null);
		Map<Long, List<IndividualEmployeeTicketReceiptInfo>> receiptInfoMap = getIndividualEmployeeTicketReceiptInfoList(IndividualEmployeeFilesIdList).stream().collect(Collectors.groupingBy(IndividualEmployeeTicketReceiptInfo::getIndividualEmployeeInfoId));

		List<BudgetExtractPersonalityPayDetail> personalityPayDetails = excelDataList.stream().map(e -> {
			IndividualEmployeeFiles individualEmployeeFiles = employeeFilesMap.get(e.getEmpNo() + "&&" + e.getPersonlityName());
			BudgetBillingUnit budgetBillingUnit = billingUnitMap.get(e.getBillingUnitName());
			BudgetExtractPersonalityPayDetail payDetail = extractPersonalityPayDetails.stream().filter(dbDetail -> dbDetail.getPersonalityId().equals(individualEmployeeFiles.getId()) &&
					dbDetail.getBillingUnitId().equals(budgetBillingUnit.getId())).findFirst().orElse(null);
			if(Objects.isNull(payDetail)){
				payDetail = new BudgetExtractPersonalityPayDetail();
				payDetail.setIsInitData(false);
				payDetail.setIsSend(false);
				payDetail.setPersonalityId(individualEmployeeFiles.getId());
				payDetail.setCreateTime(new Date());
				payDetail.setBillingUnitId(budgetBillingUnit.getId());
				payDetail.setExtractMonth(extractBatch);
			}else{
				payDetail.setUpdateTime(new Date());
			}
			ExtractPersonlityDetailExcelData agoExcelData = individualEmployeeAgoPayDetailMap.get(individualEmployeeFiles.getId().toString() + "&&" + budgetBillingUnit.getId().toString());
			payDetail.setCurExtract(extractDetailList.stream().filter(detail -> {
						return detail.getEmpno().equals(individualEmployeeFiles.getEmployeeJobNum().toString()) && detail.getEmpname().equals(individualEmployeeFiles.getEmployeeName()) && ExtractUserTypeEnum.SELF_EMPLOYED_EMPLOYEES.getCode().equals(detail.getBusinessType());
					}).map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add));
			BigDecimal t1 = StringUtils.isBlank(e.getCurExtract())?BigDecimal.ZERO:new BigDecimal(e.getCurExtract());
			BigDecimal t2 = StringUtils.isBlank(e.getCurSalary())?BigDecimal.ZERO:new BigDecimal(e.getCurSalary());
			BigDecimal t3 = StringUtils.isBlank(e.getCurWelfare())?BigDecimal.ZERO:new BigDecimal(e.getCurWelfare());
			payDetail.setCurRealExtract(t1);
			payDetail.setCurSalary(t2);
			payDetail.setCurWelfare(t3);
			setPayDetail(agoExcelData,payDetail,individualEmployeeFiles.getId(),budgetBillingUnit.getId());
			payDetail.setPayStatus(ExtractPersonalityPayStatusEnum.getEnumeByValue(e.getPayStatus()).type);
			return payDetail;
		}).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(personalityPayDetails)){
			personalityPayDetailService.saveOrUpdateBatch(personalityPayDetails);
		}
	}

	public List<BudgetExtractdetail> getCurBatchPersionalityExtract(String extractBatch,Long personalityId){
		List<BudgetExtractsum> sums = getCurBatchExtractSum(extractBatch);
		List<Long> sumIds = sums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//?????????????????????
		return getExtractDetailBySumIds(sumIds,true,personalityId);
	}

	public void setPayDetail(ExtractPersonlityDetailExcelData agoExcelData,BudgetExtractPersonalityPayDetail payDetail,Long personalityId,Long billingUnitId){
		Map<Long, BigDecimal> receiptSum = getReceiptSum(Lists.newArrayList(personalityId), payDetail.getExtractMonth());
		if (Objects.nonNull(agoExcelData)) {
			payDetail.setExtractSum(agoExcelData.getExtractSum());
			payDetail.setSalarySum(agoExcelData.getSalarySum());
			payDetail.setWelfareSum(agoExcelData.getWelfareSum());
		} else {
			payDetail.setExtractSum(BigDecimal.ZERO);
			payDetail.setSalarySum(BigDecimal.ZERO);
			payDetail.setWelfareSum(BigDecimal.ZERO);
		}
		payDetail.setReceiptSum(receiptSum.get(personalityId)==null?BigDecimal.ZERO:receiptSum.get(personalityId));
	}

	/**
	 * <p>??????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/6 14:38
	 * @param individualEmployeeIdList
	 * @param extractBatch
	 */
	public void reCalculateInvoice(List<Long> individualEmployeeIdList,String extractBatch){
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getExtractMonth, extractBatch).in(BudgetExtractPersonalityPayDetail::getPersonalityId,individualEmployeeIdList));
		Map<Long, BudgetBillingUnit> billingUnitMap = billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));
		Map<Long, IndividualEmployeeFiles> individualEmployeeFilesMap = individualEmployeeFilesMapper.selectBatchIds(individualEmployeeIdList).stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));
		Map<Long, List<IndividualEmployeeTicketReceiptInfo>> receiptInfoMap = getIndividualEmployeeTicketReceiptInfoList(individualEmployeeIdList).stream().collect(Collectors.groupingBy(e -> e.getIndividualEmployeeInfoId()));

		Map<Long, BigDecimal> receiptSum = getReceiptSum(individualEmployeeIdList, extractBatch);
		Map<Long, BigDecimal> initReceiptMap = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getIsInitData, 1).in(BudgetExtractPersonalityPayDetail::getPersonalityId, individualEmployeeIdList)).stream().collect(Collectors.toMap(e -> e.getPersonalityId(), e -> e.getReceiptSum(),(e1,e2)->e1));
		extractPersonalityPayDetails.stream().collect(Collectors.groupingBy(BudgetExtractPersonalityPayDetail::getPersonalityId)).forEach((personalityId,list)->{
			IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMap.get(personalityId);
			BigDecimal initReceipt = BigDecimal.ZERO;
			if(initReceiptMap.get(personalityId)!=null){
				initReceipt = initReceiptMap.get(personalityId);
			}
			BigDecimal initReceiptTemp = initReceipt;
			list.stream().collect(Collectors.groupingBy(e->e.getBillingUnitId())).forEach((billingUnitId,detailList)->{
				BudgetBillingUnit budgetBillingUnit = billingUnitMap.get(billingUnitId);
				if((budgetBillingUnit.getCorporation() == 0 || (budgetBillingUnit.getCorporation() == 1 && individualEmployeeFiles.getAccountType() == 1))){
					detailList.forEach(payDetail->{
						payDetail.setRemainingInvoices(BigDecimal.ZERO);
						payDetail.setRemainingPayLimitMoney(BigDecimal.ZERO);
					});
				}else{
					BigDecimal total = detailList.stream().map(e->{
						return e.getExtractSum().add(e.getSalarySum()).add(e.getWelfareSum()).add(e.getCurSalary()).add(e.getCurRealExtract()).add(e.getCurWelfare());
					}).reduce(BigDecimal.ZERO,BigDecimal::add);
					BigDecimal subtract = (receiptSum.get(personalityId)==null?BigDecimal.ZERO:receiptSum.get(personalityId)).subtract(total);
					BigDecimal annualQuota = individualEmployeeFiles.getAnnualQuota() == null ? BigDecimal.ZERO : individualEmployeeFiles.getAnnualQuota();
					List<IndividualEmployeeTicketReceiptInfo> individualEmployeeTicketReceiptInfos = receiptInfoMap.get(personalityId);
					if (!CollectionUtils.isEmpty(individualEmployeeTicketReceiptInfos)) {
						List<IndividualEmployeeTicketReceiptInfo> sortedReceiptInfoList = individualEmployeeTicketReceiptInfos.stream().sorted(Comparator.comparing(IndividualEmployeeTicketReceiptInfo::getYear)).sorted(Comparator.comparing(IndividualEmployeeTicketReceiptInfo::getMonth)).collect(Collectors.toList());
						IndividualEmployeeTicketReceiptInfo individualEmployeeTicketReceiptInfo = sortedReceiptInfoList.get(0);
						String yearMonth = calNextYearMonth(individualEmployeeTicketReceiptInfo.getYear(), individualEmployeeTicketReceiptInfo.getMonth());
						BigDecimal receiptInfoMoney = sortedReceiptInfoList.stream().filter(e -> {
							Integer year = e.getYear();
							Integer month = e.getMonth();
							String yearmonth = year + (month<10?("0"+month):month+"");
							return Integer.parseInt(yearmonth)<Integer.parseInt(yearMonth);
						}).map(e -> {
							return e.getInvoiceAmount() == null ? BigDecimal.ZERO : e.getInvoiceAmount();
						}).reduce(BigDecimal.ZERO, BigDecimal::add);
						annualQuota = annualQuota.subtract(receiptInfoMoney).subtract(initReceiptTemp);
					}
					BigDecimal annualQuotaTemp = annualQuota;
					detailList.forEach(payDetail->{
						payDetail.setRemainingInvoices(subtract);
						payDetail.setRemainingPayLimitMoney(annualQuotaTemp);
					});
				}

			});
			for (int i = 0; i < list.size(); i++) {
				BudgetExtractPersonalityPayDetail payDetail = list.get(i);
				if(i!=0){
					payDetail.setReceiptSum(BigDecimal.ZERO);
				}
			}
		});
		if(!CollectionUtils.isEmpty(extractPersonalityPayDetails))personalityPayDetailService.updateBatchById(extractPersonalityPayDetails);
	}

	public String calNextYearMonth(Integer oldYear, Integer oldMonth) {
		int finalYear = oldYear + 1;
		int finalMonth = oldMonth;
		if (oldMonth == 1) {
			finalYear = oldYear;
			finalMonth = 12;
		} else {
			finalMonth = oldMonth - 1;
		}
		return finalYear+ (finalMonth<10?("0"+finalMonth):finalMonth+"");
	}

	private void moneyValidate(String text,String type){
		if(StringUtils.isNotBlank(text)){
			try{
				String[] split = text.split("\\.");
				if(split.length != 1){
					if(split[1].length()>2) throw new RuntimeException("??????????????????");
				}
				if(new BigDecimal(text).compareTo(BigDecimal.ZERO)<0){
					throw new RuntimeException("??????????????????0");
				}
			}catch (Exception e){
				throw new RuntimeException(type+"????????????");
			}
		}
	}

	private void validateImportPersonalityPayDetail(ExtractPersonlityDetailExcelData data,Map<String,IndividualEmployeeFiles> employeeFilesMap,Map<String,BudgetBillingUnit> billingUnitMap){
		String errMsg = BaseController.validate(data);
		if(StringUtils.isNotBlank(errMsg)){
			throw new RuntimeException(errMsg);
		}
		moneyValidate(data.getCurExtract(),"????????????????????????");
		moneyValidate(data.getCurSalary(),"????????????????????????");
		moneyValidate(data.getCurWelfare(),"????????????????????????");

		if( (StringUtils.isBlank(data.getCurExtract()) || new BigDecimal(data.getCurExtract()).compareTo(BigDecimal.ZERO) == 0) &&
				(StringUtils.isBlank(data.getCurSalary()) || new BigDecimal(data.getCurSalary()).compareTo(BigDecimal.ZERO) == 0) &&
				(StringUtils.isBlank(data.getCurWelfare()) || new BigDecimal(data.getCurWelfare()).compareTo(BigDecimal.ZERO) == 0)){
			throw new RuntimeException("?????????????????????????????????????????????????????????????????????????????????????????????0???");
		}
		IndividualEmployeeFiles individualEmployeeFiles = getIndividualEmployeeFiles(data.getEmpNo().toString(), data.getPersonlityName());
		if(Objects.isNull(individualEmployeeFiles)){
			throw new RuntimeException("???????????????????????????"+data.getPersonlityName()+"("+data.getEmpNo()+")"+"???");
		}
		if(individualEmployeeFiles.getStatus()==2){
			throw new RuntimeException("?????????????????????????????????");
		}
		employeeFilesMap.put(data.getEmpNo().toString()+"&&"+data.getPersonlityName(),individualEmployeeFiles);
		BudgetBillingUnit budgetBillingUnit = this.billingUnitMapper.selectOne(new LambdaQueryWrapper<BudgetBillingUnit>().eq(BudgetBillingUnit::getName, data.getBillingUnitName()).eq(BudgetBillingUnit::getStopFlag, 0));
		if(Objects.isNull(budgetBillingUnit)){
			throw new RuntimeException("???????????????"+data.getBillingUnitName()+"???????????????");
		}
		billingUnitMap.put(data.getBillingUnitName(),budgetBillingUnit);
		if(ExtractPersonalityPayStatusEnum.getEnumeByValue(data.getPayStatus()) == null){
			throw new RuntimeException("??????????????????????????????????????????????????????");
		}
	}

	public IndividualEmployeeFiles getIndividualEmployeeFiles(String empNo,String accountName){
		return individualEmployeeFilesMapper.selectOne(new LambdaQueryWrapper<IndividualEmployeeFiles>().eq(IndividualEmployeeFiles::getEmployeeJobNum,empNo).eq(IndividualEmployeeFiles::getAccountName,accountName).eq(IndividualEmployeeFiles::getStatus,1));
	}


	/**
	 * <p>????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/1 15:50
	 * @param params
	 * @param page
	 * @param rows
	 * @param extractBatch
	 */
	public PageResult<ExtractPersonalityPayDetailVO> getExtractPersonalityPayDetailVO(ExtractPersonalityPayDetailQueryVO params, Integer page, Integer rows, String extractBatch) {
		if(StringUtils.isBlank(extractBatch) && params.getSumId()!=null){
			BudgetExtractsum extractsum = this.getById(params.getSumId());
			if(extractsum.getStatus() < ExtractStatusEnum.APPROVED.getType()){
				return PageResult.apply(0,null);
			}
			extractBatch = extractsum.getExtractmonth();
		}
		List<ExtractPersonalityPayDetailVO> list = null;
		if(page!=null && rows !=null){
			if(params.getSumId()==null){
				Page<ExtractPersonalityPayDetailVO> pageCond = new Page<ExtractPersonalityPayDetailVO>(page, rows);
				list = this.personalityPayDetailMapper.getExtractPersonalityPayDetailVO(pageCond, params,extractBatch);
				list.forEach(e->{
					e.setPayStatusName(ExtractPersonalityPayStatusEnum.getValue(e.getPayStatus()));
				});
				return PageResult.apply(pageCond.getTotal(), list);
			}else{
				list = this.personalityPayDetailMapper.getExtractPersonalityPayDetailVO(null, params,extractBatch);
				list.forEach(e->{
					e.setPayStatusName(ExtractPersonalityPayStatusEnum.getValue(e.getPayStatus()));
				});
				if(!list.isEmpty()){
					splitPersonalityOrder(params.getSumId(),extractBatch,list);
				}
				List<ExtractPersonalityPayDetailVO> collect = list.stream().filter(e -> e.getIsSelf()).collect(Collectors.toList());
				List<ExtractPersonalityPayDetailVO> resultList = collect.stream().skip((page - 1) * rows).limit(rows).peek(e->{
					e.setCurPaySum(e.getCurExtract().add(e.getCurSalary()).add(e.getCurWelfare()));
				}).collect(Collectors.toList());
				return PageResult.apply(collect.size(), resultList);
			}
		}else{
			list = this.personalityPayDetailMapper.getExtractPersonalityPayDetailVO(null, params,extractBatch);
			list.forEach(e->{
				e.setPayStatusName(ExtractPersonalityPayStatusEnum.getValue(e.getPayStatus()));
			});
			if(params.getSumId()!=null && !list.isEmpty()){
				splitPersonalityOrder(params.getSumId(),extractBatch,list);
				list = list.stream().filter(e -> e.getIsSelf()).collect(Collectors.toList());
			}
			return PageResult.apply(list.size(), list);
		}
	}


	private void splitPersonalityOrder(Long sumId,String extractBatch,List<ExtractPersonalityPayDetailVO> list) {
		if(sumId!=null) {
			List<BudgetExtractsum> curBatchExtractSum = getCurBatchExtractSum(extractBatch);
			List<Long> sumIds = curBatchExtractSum.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
			List<BudgetExtractdetail> extractDetailBySumIds = getExtractDetailBySumIds(sumIds, true, null);
			//Map<Long, List<ExtractPersonalityPayDetailVO>> personalityMap = list.stream().collect(Collectors.groupingBy(e -> e.getPersonalityId()));
			//ExtractPersonalityPayDetailVO extractPersonalityPayDetailVO = list.get(0);
			Map<Long, List<List<BigDecimal>>> map = new HashMap<>();
			Map<Long, List<BigDecimal>> map1 = new HashMap<>();
			Map<Integer, Map<Long,List<BigDecimal>>> result = new HashMap<>();
			int k = -1;
			for (int i = 0; i < curBatchExtractSum.size(); i++) {
				BudgetExtractsum extractsum = curBatchExtractSum.get(i);
				if(extractsum.getId().equals(sumId)){
					k = i;
				}
				Map<String, BigDecimal> individualEmployeeMap = extractDetailBySumIds.stream().filter(e -> e.getExtractsumid().equals(extractsum.getId())).collect(Collectors.toMap(e->{
					return e.getEmpno()+"&&"+e.getEmpname();
				}, e -> e.getCopeextract()));
				Map<Long,List<BigDecimal>> r = new HashMap<>();
				for (int j = 0; j < list.size(); j++) {
					ExtractPersonalityPayDetailVO e = list.get(j);
					//???????????????????????????
					IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMapper.selectById(e.getPersonalityId());
					BigDecimal curOrderExtractSum = individualEmployeeMap.get(individualEmployeeFiles.getEmployeeJobNum().toString()+"&&"+individualEmployeeFiles.getEmployeeName());
					if(curOrderExtractSum == null){
						if(extractsum.getId().equals(sumId)){
							e.setIsSelf(false);
						}
						continue;
					}
					BigDecimal extract = e.getExtract();
					BigDecimal percent = curOrderExtractSum.divide(extract, 20, BigDecimal.ROUND_HALF_UP);
					if(map1.get(e.getId())==null)map1.put(e.getId(),Lists.newArrayList(e.getCurExtract(),e.getCurSalary(),e.getCurWelfare()));
					BigDecimal bigDecimal1 = map1.get(e.getId()).get(0).multiply(percent).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal bigDecimal2 = map1.get(e.getId()).get(1).multiply(percent).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal bigDecimal3 = map1.get(e.getId()).get(2).multiply(percent).setScale(2, BigDecimal.ROUND_HALF_UP);
					if (i == curBatchExtractSum.size() - 1) {
						BigDecimal m1 = BigDecimal.ZERO;
						BigDecimal m2 = BigDecimal.ZERO;
						BigDecimal m3 = BigDecimal.ZERO;
						if(map.get(e.getId())!=null){
							m1 = map.get(e.getId()).stream().map(m -> m.get(0)).reduce(BigDecimal.ZERO, BigDecimal::add);
							m2 = map.get(e.getId()).stream().map(m -> m.get(1)).reduce(BigDecimal.ZERO, BigDecimal::add);
							m3 = map.get(e.getId()).stream().map(m -> m.get(2)).reduce(BigDecimal.ZERO, BigDecimal::add);
						}
						r.put(e.getId(),Lists.newArrayList(map1.get(e.getId()).get(0).subtract(m1),map1.get(e.getId()).get(1).subtract(m2),map1.get(e.getId()).get(2).subtract(m3)));
						result.put(i,r);
					} else {
						if (map.get(e.getId()) == null) {
							List<BigDecimal> bigDecimals = Lists.newArrayList(bigDecimal1, bigDecimal2, bigDecimal3);
							List<List<BigDecimal>> l = new ArrayList<>();
							l.add(bigDecimals);
							map.put(e.getId(), l);
						} else {
							map.get(e.getId()).add(Lists.newArrayList(bigDecimal1, bigDecimal2, bigDecimal3));
						}
						r.put(e.getId(),Lists.newArrayList(bigDecimal1,bigDecimal2,bigDecimal3));
						result.put(i,r);
					}
				}
			}
			int k1 = k;
			list.forEach(l->{
				if(result.get(k1)!=null && result.get(k1).get(l.getId())!=null){
					l.setCurExtract(result.get(k1).get(l.getId()).get(0));
					l.setCurSalary(result.get(k1).get(l.getId()).get(1));
					l.setCurWelfare(result.get(k1).get(l.getId()).get(2));
				}else{
					l.setCurExtract(BigDecimal.ZERO);
					l.setCurSalary(BigDecimal.ZERO);
					l.setCurWelfare(BigDecimal.ZERO);
				}
			});
		}
	}
	/**
	 * <p>???????????????</p>
	 * @author minzhq
	 * @date 2022/9/3 14:23
	 * @param extractBatch
	 */
	public void taxGroupSuccess(String extractBatch) {
		BudgetExtractTaxHandleRecord record = getExtractTaxHandleRecord(extractBatch);
		if (record != null && record.getIsCalComplete() && record.getIsSetExcessComplete() && record.getIsPersonalityComplete()) {
			List<BudgetExtractsum> curBatchExtractSumList = getCurBatchExtractSum(extractBatch);

			Map<Long, BudgetBillingUnit> unitMap = this.billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(BudgetBillingUnit::getId, Function.identity()));
			//??????????????????
			List<BudgetExtractPerPayDetail> perPayDetails = generateSplitOrderDetail(extractBatch,unitMap);
			//??????????????????
			List<BudgetExtractAccountTask> accountTasks = createAccountTask(extractBatch, perPayDetails, curBatchExtractSumList, unitMap);

			if(CollectionUtils.isEmpty(accountTasks)){
				//????????????????????????????????????
				curBatchExtractSumList.forEach(sum -> {
					sum.setStatus(ExtractStatusEnum.ACCOUNT.getType());
				});
				this.updateBatchById(curBatchExtractSumList);
				generateExtractStepLog(curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList()), OperationNodeEnum.ACCOUNTING,"???"+OperationNodeEnum.getValue(OperationNodeEnum.ACCOUNTING.getType()) + "?????????",1);
				finishAccount(false,null,extractBatch,unitMap);
			}else{
				curBatchExtractSumList.forEach(sum -> {
					sum.setStatus(ExtractStatusEnum.CALCULATION_COMPLETE.getType());
				});
				this.updateBatchById(curBatchExtractSumList);
			}

		}
	}

	/**
	 * <p>??????????????????</p>
	 * @author minzhq
	 * @date 2022/9/7 14:25
	 * @param extractBatch ????????????
	 */
	private List<BudgetExtractPerPayDetail> generateSplitOrderDetail(String extractBatch,Map<Long, BudgetBillingUnit> unitMap) {

		List<BudgetExtractsum> curBatchExtractSum = getCurBatchExtractSum(extractBatch);

		Map<Long, List<BudgetBillingUnitAccount>> unitAccountMap = this.billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().in(BudgetBillingUnitAccount::getBillingunitid, unitMap.keySet())).stream().collect(Collectors.groupingBy(BudgetBillingUnitAccount::getBillingunitid));
		Map<String, Object> params = new HashMap<>();
		params.put("extractmonth", extractBatch);
		ExtractPersonalityPayDetailQueryVO vo = new ExtractPersonalityPayDetailQueryVO();
		vo.setPayStatus(ExtractPersonalityPayStatusEnum.COMMON.type);
		List<ExtractPersonalityPayDetailVO> personalityPayDetails = this.personalityPayDetailMapper.getExtractPersonalityPayDetailVO(null, vo, extractBatch);
		personalityPayDetails.forEach(e->{
			e.setPayStatusName(ExtractPersonalityPayStatusEnum.getValue(e.getPayStatus()));
		});
		List<ExtractPayDetailVO> empPayDetails = getPayDetailsByCondition(null, params);

		List<BudgetExtractPerPayDetail> perPayDetails = new ArrayList<>();

		Map<Long, IndividualEmployeeFiles> individualEmployeeFilesMap = null;
		if(!CollectionUtils.isEmpty(personalityPayDetails)){
			individualEmployeeFilesMap = individualEmployeeFilesMapper.selectBatchIds(personalityPayDetails.stream().map(ExtractPersonalityPayDetailVO::getPersonalityId).distinct().collect(Collectors.toList())).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		}
		Map<Long, IndividualEmployeeFiles> individualEmployeeFilesMapTemp = individualEmployeeFilesMap;
		curBatchExtractSum.forEach(sum-> {
			Long sumId = sum.getId();

			List<ExtractPayDetailVO> details = empPayDetails.stream().map(e-> {
				try {
					return (ExtractPayDetailVO)e.clone();
				} catch (CloneNotSupportedException cloneNotSupportedException) {
					cloneNotSupportedException.printStackTrace();
				}
				return e;
			}).collect(Collectors.toList());

			List<ExtractPayDetailVO> empOrderPayDetails = details.stream().peek(e -> {
				List<BudgetExtractdetail> budgetExtractdetails = extractDetailMapper.selectBatchIds(Arrays.asList(e.getExtractdetailids().split(",")));
				e.setRealextract(budgetExtractdetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add));
				e.setRepaymoney(budgetExtractdetails.stream().filter(e1 -> e1.getWithholdmoney() != null).map(BudgetExtractdetail::getWithholdmoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				e.setConsotax(budgetExtractdetails.stream().map(BudgetExtractdetail::getConsotax).reduce(BigDecimal.ZERO, BigDecimal::add));
				splitOrder(e, sumId);
			}).filter(e -> e.getIsSelf()).collect(Collectors.toList());

			List<ExtractPersonalityPayDetailVO> orderPersonalityPayDetails = null;
			if(!CollectionUtils.isEmpty(personalityPayDetails)){
				List<ExtractPersonalityPayDetailVO> details1 = personalityPayDetails.stream().map(e-> {
					try {
						return (ExtractPersonalityPayDetailVO)e.clone();
					} catch (CloneNotSupportedException cloneNotSupportedException) {
						cloneNotSupportedException.printStackTrace();
					}
					return e;
				}).collect(Collectors.toList());
				splitPersonalityOrder(sumId,extractBatch,details1);
				orderPersonalityPayDetails = details1.stream().filter(e -> e.getIsSelf()).collect(Collectors.toList());
			}
			//??????????????????????????????
			doGenerateSplitOrderDetail(perPayDetails,empOrderPayDetails,orderPersonalityPayDetails,sum,unitMap,unitAccountMap,individualEmployeeFilesMapTemp);
		});
		if(!CollectionUtils.isEmpty(perPayDetails)) perPayDetailService.saveBatch(perPayDetails);
		return perPayDetails;
	}

	public void generateDelayApplyOrder(String extractBatch, String ids,List<BudgetExtractsum> curBatchExtractSum){
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectBatchIds(Arrays.asList(ids.split(",")));
		Map<Long, BudgetBillingUnit> unitMap = this.billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(BudgetBillingUnit::getId, Function.identity()));
		Map<Long, List<BudgetBillingUnitAccount>> unitAccountMap = this.billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().in(BudgetBillingUnitAccount::getBillingunitid, unitMap.keySet())).stream().collect(Collectors.groupingBy(BudgetBillingUnitAccount::getBillingunitid));
		List<Long> personalityIds = extractPersonalityPayDetails.stream().map(BudgetExtractPersonalityPayDetail::getPersonalityId).collect(Collectors.toList());
		Map<Long, IndividualEmployeeFiles> individualEmployeeFilesMap = individualEmployeeFilesMapper.selectBatchIds(personalityIds).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		ExtractPersonalityPayDetailQueryVO vo = new ExtractPersonalityPayDetailQueryVO();
		vo.setPayStatus(ExtractPersonalityPayStatusEnum.DELAY.type);
		vo.setPersonalityIds(personalityIds);
		List<ExtractPersonalityPayDetailVO> personalityPayDetails = this.personalityPayDetailMapper.getExtractPersonalityPayDetailVO(null, vo, extractBatch);
		List<BudgetExtractPerPayDetail> perPayDetails = new ArrayList<>();
		curBatchExtractSum.forEach(sum-> {
			Long sumId = sum.getId();
			List<ExtractPersonalityPayDetailVO> details = personalityPayDetails.stream().map(e-> {
				try {
					return (ExtractPersonalityPayDetailVO)e.clone();
				} catch (CloneNotSupportedException cloneNotSupportedException) {
					cloneNotSupportedException.printStackTrace();
				}
				return e;
			}).collect(Collectors.toList());
			splitPersonalityOrder(sumId,extractBatch,details);
			List<ExtractPersonalityPayDetailVO> orderPersonalityPayDetails = details.stream().filter(e -> e.getIsSelf()).collect(Collectors.toList());
			//??????????????????????????????
			doGenerateSplitOrderDetail(perPayDetails,null,orderPersonalityPayDetails,sum,unitMap,unitAccountMap,individualEmployeeFilesMap);
		});
		//????????????????????????
		createDelayAccountTask(extractBatch,perPayDetails,curBatchExtractSum,unitMap);
	}


	private void createDelayAccountTask(String extractBatch,List<BudgetExtractPerPayDetail> perPayDetails,List<BudgetExtractsum> curBatchExtractSum,Map<Long, BudgetBillingUnit> unitMap){

		List<BudgetExtractAccountTask> accountTasks = new ArrayList<>();
		List<BudgetExtractAccountTask> dbTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractMonth, extractBatch).eq(BudgetExtractAccountTask::getTaskType, ExtractTaskTypeEnum.DELAY.type).orderByDesc(BudgetExtractAccountTask::getCreateTime));
		Integer batch = dbTasks.isEmpty()?1:dbTasks.get(0).getBatch()+1;
		Date date = new Date();
		perPayDetails.stream().collect(Collectors.groupingBy(e->e.getExtractCode())).forEach((code,list)->{
			String extractDelayNum = distributedNumber.getExtractDelayNum();
			list.stream().collect(Collectors.groupingBy(e->e.getBillingUnitId())).forEach((billingUnitId,sameBillingUnitList)->{
				String personalityIds = sameBillingUnitList.stream().map(e -> e.getPersonalityId().toString()).collect(Collectors.joining(","));
				BudgetBillingUnit budgetBillingUnit = unitMap.get(billingUnitId);
				BudgetExtractAccountTask task = new BudgetExtractAccountTask();
				task.setExtractMonth(extractBatch);
				task.setCreateTime(date);
				task.setRelationExtractCode(code);
				task.setBillingUnitId(budgetBillingUnit.getId());
				if("1".equals(budgetBillingUnit.getBillingUnitType()) && budgetBillingUnit.getOwnFlag() == 0){
					task.setAccountantStatus(0);
					task.setIsShouldAccount(true);
					String accountants = budgetBillingUnit.getAccountants();
					if (StringUtils.isBlank(accountants)) {
						throw new RuntimeException("???????????????" + budgetBillingUnit.getName() + "??????????????????");
					}
					String empNos = Arrays.stream(accountants.split(",")).map(a -> UserCache.getUserByUserId(a).getUserName()).collect(Collectors.joining(","));
					task.setPlanAccountantEmpNos(empNos);
				}else{
					//????????????
					task.setAccountantStatus(1);
					task.setIsShouldAccount(false);
				}
				task.setExtractCode(extractDelayNum);
				task.setTaskType(ExtractTaskTypeEnum.DELAY.type);
				sameBillingUnitList.forEach(e->{
					e.setExtractCode(task.getExtractCode());
					e.setPayStatus(task.getTaskType());
				});
				task.setPersonalityIds(personalityIds);
				task.setBatch(batch);
				accountTasks.add(task);
			});
		});
		if(!CollectionUtils.isEmpty(perPayDetails)) perPayDetailService.saveBatch(perPayDetails);
		if(!accountTasks.isEmpty()) {
			accountTaskService.saveBatch(accountTasks);

			accountTasks.stream().map(e->{
				BudgetExtractDelayApplication delayApplication = new BudgetExtractDelayApplication();
				delayApplication.setDelayCode(e.getExtractCode());
				delayApplication.setCreateTime(new Date());
				delayApplication.setRelationExtractCode(e.getRelationExtractCode());
				delayApplication.setStatus(ExtractDelayStatusEnum.CALCULATION_COMPLETE.type);
				delayApplication.setBatch(e.getBatch());
				delayApplication.setExtractMonth(e.getExtractMonth());
				return delayApplication;
			}).filter(distinct(BudgetExtractDelayApplication::getDelayCode)).forEach(e->{
				delayApplicationMapper.insert(e);
			});

			long count = accountTasks.stream().filter(e -> e.getAccountantStatus() == 0).count();
			if(count == 0){
				finishAccount(true,accountTasks.stream().map(e->e.getExtractCode()).collect(Collectors.toList()),extractBatch,unitMap);
			}
		}
	}

	public <T> Predicate<T> distinct(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * <p>??????????????????</p>
	 * @author minzhq
	 * @date 2022/9/13 14:46
	 * @param isDelay ??????????????????????????????
	 * @param extractCode
	 * @param extractBatch
	 */
	 public void finishAccount(boolean isDelay, List<String> delayExtractCodeList,String extractBatch,Map<Long, BudgetBillingUnit> unitMap) {


		List<BudgetExtractPerPayDetail> perPayDetails = null;
		if(isDelay){
			perPayDetails = perPayDetailService.list(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().in(BudgetExtractPerPayDetail::getExtractCode, delayExtractCodeList).eq(BudgetExtractPerPayDetail::getPayStatus, ExtractPersonalityPayStatusEnum.DELAY.type));

			delayExtractCodeList.forEach(e->{
				BudgetExtractDelayApplication delayApplication = delayApplicationMapper.selectOne(new LambdaQueryWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getDelayCode, e));
				delayApplication.setStatus(ExtractDelayStatusEnum.ACCOUNT.type);
				delayApplicationMapper.updateById(delayApplication);
			});

		}else{
			perPayDetails = perPayDetailService.list(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().eq(BudgetExtractPerPayDetail::getExtractMonth, extractBatch).eq(BudgetExtractPerPayDetail::getPayStatus, ExtractPersonalityPayStatusEnum.COMMON.type));
		}
		List<BudgetPaymoney> payMoneyList = perPayDetails.stream().filter(e->{
			BudgetBillingUnit budgetBillingUnit = unitMap.get(e.getBillingUnitId());
			if(budgetBillingUnit.getOwnFlag() == 1) return false;
			return true;
		}).map(e -> {
			BudgetPaymoney payMoney = new BudgetPaymoney();
			payMoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
			payMoney.setPaymoneyobjectcode(e.getExtractCode());
			payMoney.setPaymoneyobjectid(e.getId());
			payMoney.setPaymoney(e.getPayMoney());
			payMoney.setPaytype(1);
			payMoney.setPaymoneytype(PaymoneyTypeEnum.EXTRACT_PAY.type);
			payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.type);
			payMoney.setVerifystatus(0);
			payMoney.setCreatetime(new Date());
			payMoney.setBunitname(e.getBillingUnitName());
			payMoney.setBunitbankaccount(e.getBillingUnitAccount());
			payMoney.setBunitaccountbranchcode(e.getBillingUnitBranchCode());
			payMoney.setBunitaccountbranchname(e.getBillingUnitBankName());
			payMoney.setBankaccount(e.getReceiverBankAccount());
			payMoney.setBankaccountname(e.getReceiverAccountName());
			payMoney.setBankaccountbranchcode(e.getReceiverBankAccountBranchCode());
			payMoney.setBankaccountbranchname(e.getReceiveBankAccountBankName());
			payMoney.setOpenbank(e.getReceiverOpenBank());
			payMoney.setRemark("?????????" + e.getExtractCode() + "?????????");
			return payMoney;
		}).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(payMoneyList)){
			paymoneyService.saveBatch(payMoneyList);
		}
	}

	private void doGenerateSplitOrderDetail(List<BudgetExtractPerPayDetail> perPayDetails,List<ExtractPayDetailVO> empOrderPayDetails,List<ExtractPersonalityPayDetailVO> orderPersonalityPayDetails,BudgetExtractsum extractsum,Map<Long, BudgetBillingUnit> unitMap,Map<Long, List<BudgetBillingUnitAccount>> unitAccountMap,Map<Long, IndividualEmployeeFiles> individualEmployeeFilesMap){

		if(!CollectionUtils.isEmpty(empOrderPayDetails)){
			empOrderPayDetails.stream().collect(Collectors.groupingBy(e->{
				return e.getIsCompanyEmp().toString()+"&&"+e.getIdnumber();
			})).forEach((key,list)->{
				ExtractPayDetailVO e = list.get(0);

				if(e.getBillingUnitId()!=null && e.getBillingPaymoney()!=null && e.getBillingPaymoney().compareTo(BigDecimal.ZERO)>0){
					BigDecimal money = BigDecimal.ZERO;
					if(e.getBillingPaymoney()!=null && e.getBillingPaymoney().compareTo(BigDecimal.ZERO)>0){
						money = money.add(e.getBillingPaymoney());
					}
					if(e.getBeforeCalFee()!=null && e.getBeforeCalFee().compareTo(BigDecimal.ZERO)>0){
						money = money.add(e.getBeforeCalFee());
					}
					if(money.compareTo(BigDecimal.ZERO)>0)perPayDetails.add(generateBudgetExtractPerPayDetail(e,e.getBillingUnitId(),money,unitMap,unitAccountMap,extractsum));
				}
				if(e.getAvoidBillingUnitId()!=null && e.getAvoidBillingPaymoney()!=null && e.getAvoidBillingPaymoney().compareTo(BigDecimal.ZERO)>0){
					perPayDetails.add(generateBudgetExtractPerPayDetail(e,e.getAvoidBillingUnitId(),e.getAvoidBillingPaymoney(),unitMap,unitAccountMap,extractsum));
				}
				list.forEach(out->{
					if(out.getOutUnitId()!=null && out.getOutUnitPayMoney()!=null && out.getOutUnitPayMoney().compareTo(BigDecimal.ZERO)>0){
						perPayDetails.add(generateBudgetExtractPerPayDetail(out,out.getOutUnitId(),out.getOutUnitPayMoney(),unitMap,unitAccountMap,extractsum));
					}
				});

			});
		}
		if(!CollectionUtils.isEmpty(orderPersonalityPayDetails)){
			perPayDetails.addAll(orderPersonalityPayDetails.stream().map(personality->{
				IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMap.get(personality.getPersonalityId());
				Long unitId = personality.getBillingUnitId();
				BudgetExtractPerPayDetail perPayDetail = new BudgetExtractPerPayDetail();
				perPayDetail.setExtractCode(extractsum.getCode());
				perPayDetail.setExtractMonth(extractsum.getExtractmonth());
				perPayDetail.setBillingUnitId(unitId);
				List<BudgetBillingUnitAccount> budgetBillingUnitAccounts = unitAccountMap.get(unitId);
				if(CollectionUtils.isEmpty(budgetBillingUnitAccounts)){
					throw new RuntimeException("???????????????"+unitMap.get(unitId).getName()+"????????????????????????");
				}
				budgetBillingUnitAccounts = budgetBillingUnitAccounts.stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getStopflag)).sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
				BudgetBillingUnitAccount budgetBillingUnitAccount = budgetBillingUnitAccounts.get(0);
				if (budgetBillingUnitAccount.getStopflag()){
					throw new RuntimeException("???????????????" + unitMap.get(unitId).getName() + "????????????????????????");
				}
				perPayDetail.setBillingUnitAccount(budgetBillingUnitAccount.getBankaccount());
				perPayDetail.setBillingUnitBranchCode(budgetBillingUnitAccount.getBranchcode());
				WbBanks bank = bankCache.getBankByBranchCode(budgetBillingUnitAccount.getBranchcode());
				if(Objects.isNull(bank)){
					throw new RuntimeException("???????????????" + unitMap.get(unitId).getName() + "???????????????"+budgetBillingUnitAccount.getBankaccount()+"???????????????????????????");
				}
				perPayDetail.setBillingUnitBankName(bank.getBankName());
				perPayDetail.setBillingUnitOpenBank(bank.getSubBranchName());
				perPayDetail.setBillingUnitName(unitMap.get(unitId).getName());
				perPayDetail.setPayMoney(personality.getCurExtract().add(personality.getCurWelfare()).add(personality.getCurSalary()));
				perPayDetail.setIsCompanyEmp(false);
				perPayDetail.setPersonalityId(personality.getPersonalityId());
				perPayDetail.setSourceId(personality.getId());
				perPayDetail.setReceiverCode(individualEmployeeFiles.getEmployeeJobNum().toString());
				perPayDetail.setReceiverName(individualEmployeeFiles.getEmployeeName());
				perPayDetail.setReceiverAccountName(individualEmployeeFiles.getAccountName());
				perPayDetail.setReceiverBankAccount(individualEmployeeFiles.getAccount());
				WbBanks bank1 = bankCache.getBankByBranchCode(individualEmployeeFiles.getDepositBank());
				if(Objects.isNull(bank1)){
					throw new RuntimeException("??????????????????" + individualEmployeeFiles.getEmployeeName() + "???????????????"+individualEmployeeFiles.getAccount()+"???????????????????????????");
				}
				perPayDetail.setReceiverBankAccountBranchCode(bank1.getSubBranchCode());
				perPayDetail.setReceiveBankAccountBankName(bank1.getBankName());
				perPayDetail.setReceiverOpenBank(bank1.getSubBranchName());
				perPayDetail.setCreateTime(new Date());
				perPayDetail.setRelationExtractCode(extractsum.getCode());
				perPayDetail.setPayStatus(ExtractPersonalityPayStatusEnum.COMMON.type);
				return perPayDetail;
			}).collect(Collectors.toList()));
		}
	}
	/**
	 * <p>?????????????????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/7 16:10
	 * @param e ????????????
	 * @param unitId ????????????
	 * @param money ???
	 * @param unitMap
	 * @param unitAccountMap
	 * @param extractsum
	 */
	private BudgetExtractPerPayDetail generateBudgetExtractPerPayDetail(ExtractPayDetailVO e,Long unitId,BigDecimal money,Map<Long, BudgetBillingUnit> unitMap,Map<Long, List<BudgetBillingUnitAccount>> unitAccountMap,BudgetExtractsum extractsum){
		BudgetExtractPerPayDetail perPayDetail = new BudgetExtractPerPayDetail();
		perPayDetail.setExtractCode(extractsum.getCode());
		perPayDetail.setExtractMonth(extractsum.getExtractmonth());
		perPayDetail.setBillingUnitId(unitId);
		List<BudgetBillingUnitAccount> budgetBillingUnitAccounts = unitAccountMap.get(unitId);
		if(CollectionUtils.isEmpty(budgetBillingUnitAccounts)){
			throw new RuntimeException("???????????????"+unitMap.get(unitId).getName()+"????????????????????????");
		}
		budgetBillingUnitAccounts = budgetBillingUnitAccounts.stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getStopflag)).sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
		BudgetBillingUnitAccount budgetBillingUnitAccount = budgetBillingUnitAccounts.get(0);
		if (budgetBillingUnitAccount.getStopflag()){
			throw new RuntimeException("???????????????" + unitMap.get(unitId).getName() + "????????????????????????");
		}
		perPayDetail.setBillingUnitAccount(budgetBillingUnitAccount.getBankaccount());
		perPayDetail.setBillingUnitBranchCode(budgetBillingUnitAccount.getBranchcode());
		WbBanks bank = bankCache.getBankByBranchCode(budgetBillingUnitAccount.getBranchcode());
		if(Objects.isNull(bank)){
			throw new RuntimeException("???????????????" + unitMap.get(unitId).getName() + "???????????????"+budgetBillingUnitAccount.getBankaccount()+"???????????????????????????");
		}
		perPayDetail.setBillingUnitBankName(bank.getBankName());
		perPayDetail.setBillingUnitOpenBank(bank.getSubBranchName());
		perPayDetail.setBillingUnitName(unitMap.get(unitId).getName());
		perPayDetail.setPayMoney(money);
		perPayDetail.setIsCompanyEmp(e.getIsCompanyEmp());
		perPayDetail.setPersonalityId(null);
		BudgetExtractpayment extractpayment = paymentMapper.selectById(e.getId1());
		perPayDetail.setReceiverCode(e.getEmpno());
		perPayDetail.setReceiverName(extractpayment.getBankaccountname());
		perPayDetail.setReceiverAccountName(extractpayment.getBankaccountname());
		perPayDetail.setReceiverBankAccount(extractpayment.getBankaccount());
		perPayDetail.setReceiverBankAccountBranchCode(extractpayment.getBankaccountbranchcode());
		perPayDetail.setReceiveBankAccountBankName(extractpayment.getBankaccountbranchname());
		perPayDetail.setReceiverOpenBank(extractpayment.getBankaccountopenbank());
		perPayDetail.setCreateTime(new Date());
		perPayDetail.setRelationExtractCode(perPayDetail.getExtractCode());
		perPayDetail.setSourceId(e.getId1());
		return perPayDetail;
	}

	private List<BudgetExtractAccountTask> createAccountTask(String extractBatch,List<BudgetExtractPerPayDetail> perPayDetails,List<BudgetExtractsum> curBatchExtractSum,Map<Long, BudgetBillingUnit> unitMap){

		List<BudgetExtractAccountTask> accountTasks = new ArrayList<>();
		//?????????????????????
		boolean test = isTest();
		//?????????????????????
		String testNotice = getTestNotice();
		Date date = new Date();
		perPayDetails.stream().collect(Collectors.groupingBy(e->e.getExtractCode())).forEach((code,list)->{
			accountTasks.addAll(list.stream().map(e->e.getBillingUnitId()).distinct().filter(e->{
				BudgetBillingUnit unit = unitMap.get(e);
				//???????????????????????????????????????????????????
				return "1".equals(unit.getBillingUnitType()) && unit.getOwnFlag() == 0;
			}).map(e->{
				BudgetExtractAccountTask task = new BudgetExtractAccountTask();
				task.setExtractMonth(extractBatch);
				task.setCreateTime(date);
				task.setExtractCode(code);
				task.setBillingUnitId(e);
				task.setAccountantStatus(0);
				task.setTaskType(ExtractTaskTypeEnum.COMMON.type);
				task.setIsShouldAccount(true);
				task.setRelationExtractCode(code);
				BudgetBillingUnit budgetBillingUnit = unitMap.get(e);
				String accountants = budgetBillingUnit.getAccountants();
				if (StringUtils.isBlank(accountants)) {
					throw new RuntimeException("???????????????" + budgetBillingUnit.getName() + "??????????????????");
				}
				String empNos = Arrays.stream(accountants.split(",")).map(a -> UserCache.getUserByUserId(a).getUserName()).collect(Collectors.joining(","));
				task.setPlanAccountantEmpNos(empNos);
				return task;
			}).collect(Collectors.toList()));
		});

		if(!accountTasks.isEmpty()) {
			String accountants = accountTasks.stream().flatMap(e -> {
				BudgetBillingUnit budgetBillingUnit = unitMap.get(e.getBillingUnitId());
				if(StringUtils.isNotBlank(budgetBillingUnit.getAccountants())){
					return Arrays.stream(budgetBillingUnit.getAccountants().split(","));
				}
				return null;
			}).filter(StringUtils::isNotBlank).map(e->UserCache.getUserByUserId(e).getUserName()).distinct().collect(Collectors.joining("|"));
			if(test){
				accountants = testNotice;
			}
			try{
				List<BudgetExtractsum> list = this.list(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch));
				BudgetYearPeriod budgetYearPeriod = yearMapper.selectById(list.get(0).getYearid());
				if(StringUtils.isNotBlank(accountants))sender.sendQywxMsg(new QywxTextMsg(accountants, null, null, 0, budgetYearPeriod.getPeriod()+Integer.parseInt(extractBatch.substring(4,6))+"???"+Integer.parseInt(extractBatch.substring(6,8))+"???????????????????????????????????????????????????", null));
			}catch (Exception e){}
			accountTaskService.saveBatch(accountTasks);
		}
		return accountTasks;
	}

	/**
	 * <p>???????????????????????????????????????</p>
	 * @author minzhq
	 * @date 2022/9/3 16:17
	 * @param extractPayApplyId
	 */
	public ExtractPayApplyPayDetailVO getExtractPayApplyPayDetail(Long extractSumId) {
		BudgetExtractsum extractsum = this.getById(extractSumId);
		ExtractPayApplyPayDetailVO result = new ExtractPayApplyPayDetailVO();
		BudgetExtractTaxHandleRecord extractTaxHandleRecord = getExtractTaxHandleRecord(extractsum.getExtractmonth());
		if(extractTaxHandleRecord == null || extractsum.getStatus()<ExtractStatusEnum.APPROVED.getType()){
			return result;
		}

		boolean isSetExcess = false;
		boolean isShowPersonality = false;
		if(extractTaxHandleRecord!=null && extractTaxHandleRecord.getIsCalComplete() && extractTaxHandleRecord.getIsSetExcessComplete()){
			isSetExcess = true;
		}
		if(extractTaxHandleRecord!=null && extractTaxHandleRecord.getIsPersonalityComplete()){
//			Integer integer = personalityPayDetailMapper.selectCount(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>()
//					.eq(BudgetExtractPersonalityPayDetail::getExtractMonth, extractsum.getExtractmonth())
//					.eq(BudgetExtractPersonalityPayDetail::getPayStatus, ExtractPersonalityPayStatusEnum.DELAY.type)
//					.isNull(BudgetExtractPersonalityPayDetail::getOperateTime));
//			if(integer == 0)
			isShowPersonality = true;
		}
		Map<Long, BudgetBillingUnit> unitMap = this.billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(BudgetBillingUnit::getId, Function.identity()));
		//??????????????????
		List<BigDecimal> innerPayMoney = new ArrayList<>();
		//??????????????????
		List<BigDecimal> outPayMoney = new ArrayList<>();
		//??????????????????
		Map<Long,Map<String,List<BigDecimal>>> unitPayDetailMap = new HashMap<>();
		List<BigDecimal> unPayMoney = new ArrayList<>();

		if(isSetExcess){
			//????????????????????????
			Map<String, Object> params = new HashMap<>();
			params.put("extractmonth", extractsum.getExtractmonth());
			List<ExtractPayDetailVO> resultList = getPayDetailsByCondition(null, params);
			resultList = resultList.stream().peek(e -> {
				splitOrder(e, extractSumId);
			}).filter(e->e.getIsSelf()).collect(Collectors.toList());

			List<BigDecimal> money = new ArrayList<>();
			resultList.stream().collect(Collectors.groupingBy(e->{
				return e.getIsCompanyEmp().toString()+"&&"+e.getEmpno();
			})).forEach((key,list)->{
				ExtractPayDetailVO extractPayDetailVO = list.get(0);
				setUnitPayDetail(unitMap,unitPayDetailMap,extractPayDetailVO.getBillingUnitId(),extractPayDetailVO.getBillingPaymoney(),innerPayMoney,outPayMoney,"1");
				setUnitPayDetail(unitMap,unitPayDetailMap,extractPayDetailVO.getAvoidBillingUnitId(),extractPayDetailVO.getAvoidBillingPaymoney(),innerPayMoney,outPayMoney,"1");
				setUnitPayDetail(unitMap,unitPayDetailMap,extractPayDetailVO.getBillingUnitId(),extractPayDetailVO.getBeforeCalFee(),innerPayMoney,outPayMoney,"2");
				if(extractPayDetailVO.getBillingPaymoney()!=null){
					money.add(extractPayDetailVO.getBillingPaymoney());
				}
				if(extractPayDetailVO.getAvoidBillingPaymoney()!=null){
					money.add(extractPayDetailVO.getAvoidBillingPaymoney());
				}
				if(extractPayDetailVO.getBeforeCalFee()!=null){
					money.add(extractPayDetailVO.getBeforeCalFee());
				}
				list.forEach(l->{
					setUnitPayDetail(unitMap,unitPayDetailMap,l.getOutUnitId(),l.getOutUnitPayMoney(),innerPayMoney,outPayMoney,"1");
				});
			});
		}


		if(isShowPersonality){
			ExtractPersonalityPayDetailQueryVO vo = new ExtractPersonalityPayDetailQueryVO();
			vo.setSumId(extractSumId);
			PageResult<ExtractPersonalityPayDetailVO> extractPersonalityPayDetailVO = this.getExtractPersonalityPayDetailVO(vo, null, null, extractsum.getExtractmonth());
			List<ExtractPersonalityPayDetailVO> personalityPayDetailVOList = extractPersonalityPayDetailVO.getList();
			personalityPayDetailVOList.stream().filter(e->e.getPayStatus() == ExtractPersonalityPayStatusEnum.COMMON.type).forEach(detail->{
				IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMapper.selectById(detail.getPersonalityId());
				if(individualEmployeeFiles.getAccountType()==1){
					setUnitPayDetail(unitMap,unitPayDetailMap,detail.getBillingUnitId(),detail.getCurExtract().add(detail.getCurSalary()).add(detail.getCurWelfare()),innerPayMoney,outPayMoney,"4");
				}else if(individualEmployeeFiles.getAccountType()==2){
					setUnitPayDetail(unitMap,unitPayDetailMap,detail.getBillingUnitId(),detail.getCurExtract().add(detail.getCurSalary()).add(detail.getCurWelfare()),innerPayMoney,outPayMoney,"5");
				}
			});
			personalityPayDetailVOList.stream().filter(e->e.getPayStatus() != ExtractPersonalityPayStatusEnum.COMMON.type).forEach(detail->{
				unPayMoney.add(detail.getCurExtract().add(detail.getCurSalary()).add(detail.getCurWelfare()));
			});
		}

		result.setInnerPayMoney(innerPayMoney.stream().reduce(BigDecimal.ZERO,BigDecimal::add));
		result.setOutUnitPayMoney(outPayMoney.stream().reduce(BigDecimal.ZERO,BigDecimal::add));
		result.setUnPayMoney(unPayMoney.stream().reduce(BigDecimal.ZERO,BigDecimal::add));

		List<ExtractPayApplyPayDetailVO.ExtractUnitPayDetail> detailList = new ArrayList<>();
		boolean isSetExcess1 = isSetExcess;
		boolean isShowPersonality1 = isShowPersonality;
		unitPayDetailMap.forEach((unitId,map)->{
			ExtractPayApplyPayDetailVO.ExtractUnitPayDetail d = new ExtractPayApplyPayDetailVO.ExtractUnitPayDetail();
			d.setBillingUnitName(unitMap.get(unitId).getName());
			if(isSetExcess1){
				if(map.get("1")!=null){
					d.setPayMoney(map.get("1").stream().reduce(BigDecimal.ZERO,BigDecimal::add));
				}
				if(map.get("2")!=null){
					d.setFee(map.get("2").stream().reduce(BigDecimal.ZERO,BigDecimal::add));
				}
			}
			if(isShowPersonality1){
				if(map.get("5")!=null){
					d.setPersonalityPayMoney1(map.get("5").stream().reduce(BigDecimal.ZERO,BigDecimal::add));
				}
				if(map.get("4")!=null){
					d.setPersonalityPayMoney2(map.get("4").stream().reduce(BigDecimal.ZERO,BigDecimal::add));
				}

			}
			d.setTotal(d.getPayMoney().add(d.getFee()).add(d.getPersonalityPayMoney1()).add(d.getPersonalityPayMoney2()));
			if(d.getTotal().compareTo(BigDecimal.ZERO) != 0){
				detailList.add(d);
			}
		});
		result.setPayDetails(detailList);
		return result;
	}

	private void setUnitPayDetail(Map<Long, BudgetBillingUnit> unitMap,Map<Long,Map<String,List<BigDecimal>>> unitPayDetailMap,Long unitId,BigDecimal money,List<BigDecimal> innerPayMoney,List<BigDecimal> outPayMoney,String type){
		Optional.ofNullable(unitId).ifPresent(id->{
			BudgetBillingUnit budgetBillingUnit = unitMap.get(id);
			BigDecimal money1 = (money==null?BigDecimal.ZERO:money);
			if(budgetBillingUnit.getOwnFlag() == 1){
				//??????
				outPayMoney.add(money1);
			}else if(budgetBillingUnit.getOwnFlag() == 0){
				innerPayMoney.add(money1);
			}
			if(unitPayDetailMap.get(id)==null){
				Map<String,List<BigDecimal>> map = new HashMap<>();
				map.put(type,Lists.newArrayList(money1));
				unitPayDetailMap.put(id,map);
			}else{
				if(unitPayDetailMap.get(id).get(type)==null){
					unitPayDetailMap.get(id).put(type,Lists.newArrayList(money1));
				}else{
					unitPayDetailMap.get(id).get(type).add(money1);
				}

			}
		});

	}
}



