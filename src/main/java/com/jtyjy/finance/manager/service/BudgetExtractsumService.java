package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
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
import com.jtyjy.finance.manager.controller.extract.BudgetExtractController;
import com.jtyjy.finance.manager.controller.extract.pay.ExtractEmpCalDataDetail;
import com.jtyjy.finance.manager.controller.extract.pay.ExtractPayCommonData;
import com.jtyjy.finance.manager.easyexcel.*;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
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
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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

	@Autowired
	private BudgetExtractsumMapper budgetExtractsumMapper;
	@Autowired
	private IndividualEmployeeFilesService individualService;
	@Autowired
	private BudgetExtractCommissionApplicationService applicationService;

	@Autowired
	private BudgetExtractsumService sumService;

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

	@Value("${extract.qrcode.url}")
	private String extract_qrcode_url;

	private static final String QRCODE_FORMAT = ".png";

	@Value("${file.shareDir}")
	private String fileShareDir;

	@Autowired
	private BankCache bankCache;


	@Autowired
	private UserCache userCache;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_extractsum"));
	}

	public PageResult<ExtractDeductionDetailVO> getExtractDeductionReport(String empNo, Integer page, Integer rows) {

		//借款类型列表
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
	 * 获取提成届别导航栏
	 *
	 * @return
	 */
	public List<ExtractPeriodNavigateTreeVO> getExtractPeriodNavigateTree() {
		//获取届别列表
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
		//获取已导入的提成批次
		QueryWrapper<BudgetExtractsum> wrapper = new QueryWrapper<>();
		wrapper.groupBy("extractmonth").select("extractmonth");
		List<Map<String, Object>> extractMonths = this.budgetExtractsumMapper.selectMaps(wrapper);
		//给届别组装月数据
		yearList.stream().forEach(year -> {
			List<ExtractPeriodNavigateTreeVO> details = monthList.stream().map(e -> {
				ExtractPeriodNavigateTreeVO detailvo = new ExtractPeriodNavigateTreeVO();
				detailvo.setQuery(year.getQuery().concat("-").concat(e.getCode().toString()));
				detailvo.setParentId(year.getQuery());
				detailvo.setText(e.getPeriod());
				detailvo.setLevel(2);
				detailvo.setCurYearFlag(year.isCurYearFlag());
				//给月组装提成批次数据
				createMonthExtractChild(extractMonths, detailvo, Integer.parseInt(year.getYearCode()), Integer.parseInt(e.getCode()));
				return detailvo;
			}).collect(Collectors.toList());
			year.setChildren(details);
		});

		return yearList;
	}

	/**
	 * 组装提成批次数据
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
	 * 根据年和月获取提成批次
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
		 * query   年-月-提成批次
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
		//表头
		String year = data.get(1); //届别
		String extractMonth = data.get(3); //提成期间
		String unitname = data.get(5); //预算单位
		if (StringUtils.isBlank(year)) throw new RuntimeException("届别不能为空");
		if (StringUtils.isBlank(extractMonth)) throw new RuntimeException("提成期间不能为空");
		if (StringUtils.isBlank(unitname)) throw new RuntimeException("预算单位不能为空");
		BudgetYearPeriod yearPeriod = getPeriodByName(year);
		if (Objects.isNull(yearPeriod)) throw new RuntimeException("届别【" + year + "】不存在");
		BudgetUnit budgetUnit = getBudgetUnitByYearAndName(yearPeriod.getId(), unitname);
		if (Objects.isNull(budgetUnit)) throw new RuntimeException("届别【" + year + "】不存在预算单位【" + unitname + "】");

		if (StringUtils.isEmpty(extractMonth)) {
			throw new RuntimeException("提成期间不能为空!");
		} else if (!extractMonth.matches("^2\\d{3}(0[1-9]|1[0-2])\\d{2}$")) {
			throw new RuntimeException("提成期间请填写正确的格式!");
		} else {
			String extractBatch = extractMonth.substring(0, 6);
			List<String> periodMonthList = getPeriodMonthList(Integer.valueOf(yearPeriod.getCode()));
			if (!periodMonthList.contains(extractBatch))
				throw new RuntimeException("请根据届别【" + yearPeriod.getPeriod() + "】填写正确的月份。当前届别年为【" + yearPeriod.getCode() + "】");
		}

		/**
		 * 判断当前导入的提成期间后面有没有已计算或者已计算过的提成批次
		 * 如果存在则不允许导入
		 */
		String maxextractmonth = extractMonth.substring(0, 4) + "1299";
		Integer count = this.budgetExtractsumMapper.selectCount(
				new QueryWrapper<BudgetExtractsum>().eq("deleteflag", 0)
						.gt("extractmonth", Integer.valueOf(extractMonth))
						.le("extractmonth", Integer.valueOf(maxextractmonth))
						.gt("status", ExtractStatusEnum.APPROVED.getType()));
		if (count > 0) throw new RuntimeException("导入失败！存在当前提成批次之后且已经计算的提成批次");
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
	public Object validate(Integer row, Map<Integer, String> data, String importType,Object head, Object... params) {
		if (BudgetExtractController.TCIMPORT.equals(importType)) {
			if (row == 1) {
				//校验导入的表头
				validateImportTableHead(data);
			} else if (row >= 3) {
				//校验明细数据
				validateImportTableDetails(data);
			}
		} else if (BudgetExtractController.TCEXCESS.equals(importType)) {
			/**
			 * 导入超额明细
			 */
			if (row > 0) {
				String extractBatch = params[0].toString();
				validateImportExcessDetails(data);
			}
		} else if (BudgetExtractController.FEEPAY.equals(importType)) {
			/**
			 * 导入费用发放
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
		if (StringUtils.isBlank(empNo)) throw new RuntimeException("工号不能为空");
		if (StringUtils.isBlank(empName)) throw new RuntimeException("姓名不能为空");
		if (StringUtils.isNotBlank(feeStr)) {
			BigDecimal fee = BigDecimal.ZERO;
			try {
				fee = new BigDecimal(feeStr);
			} catch (Exception e) {
				throw new RuntimeException("费用金额格式错误");
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
				throw new RuntimeException(empName + "(" + empNo + ")导入后提成将为负数。请确定金额后再导入");
		}
	}

	/**
	 * 验证导入超额明细
	 *
	 * @param data
	 */
	private void validateImportExcessDetails(Map<Integer, String> data) {
		String idNumber = data.get(0);
		String isCompanyEmp = data.get(1);
		String empNo = data.get(2);
		String empName = data.get(3);
		String billingUnitName = data.get(4);
		String excessMoney = data.get(5);
		String fee = data.get(6);
		String avoidTaxMoney = data.get(7);
		if (StringUtils.isBlank(idNumber)) throw new RuntimeException("身份证号不能为空");
		if (StringUtils.isBlank(empNo)) throw new RuntimeException("工号不能为空");
		if (StringUtils.isBlank(empName)) throw new RuntimeException("姓名不能为空");

		if (StringUtils.isBlank(fee) && StringUtils.isBlank(avoidTaxMoney))
			throw new RuntimeException("法人公司费用和避税发放不能同时为空");

		if (StringUtils.isNotBlank(fee)) {
			try {
				new BigDecimal(fee);
			} catch (Exception e) {
				throw new RuntimeException("法人公司费用格式错误");
			}
		}
		if (StringUtils.isNotBlank(avoidTaxMoney)) {
			try {
				new BigDecimal(avoidTaxMoney);
			} catch (Exception e) {
				throw new RuntimeException("避税发放格式错误");
			}
		}
	}

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
		String isCompanyEmp = data.get(0); //是否公司员工
		String empNo = data.get(1); //工号
		String empName = data.get(2); //姓名
		String sftc = data.get(3); //实发提成
		String zhs = data.get(4); //综合税
		String tcPeriod = data.get(5); //提成届别
		String isDebt = data.get(6); //是否坏账

		String extractType = data.get(7);//提成类型
		String shouldSendExtract = data.get(8);//应发提成
		String tax = data.get(9);//个税
		String taxReduction = data.get(10);//个税减免
		String invoiceExcessTax = data.get(11);//发票超额税金
		String invoiceExcessTaxReduction = data.get(12);//发票超额税金减免


		if (StringUtils.isBlank(isCompanyEmp)) {
			throw new RuntimeException("是否公司员工不能为空!");
		} else if (!"是".equals(isCompanyEmp) && !"否".equals(isCompanyEmp)) {
			throw new RuntimeException("是否公司员工请填写是或否!");
		}
		if (StringUtils.isBlank(empNo)) {
			throw new RuntimeException("工号不能为空!");
		}
		if (StringUtils.isBlank(empName)) {
			throw new RuntimeException("姓名不能为空!");
		}

		if ("是".equals(isCompanyEmp)) {
			WbUser user = getUserByEmpno(empNo);
			if (user == null) {
				throw new RuntimeException("工号【" + empNo + "】不存在!");
			} else {
				if (!empName.equals(user.getDisplayName())) {
					throw new RuntimeException("工号与姓名不匹配!正确姓名为【" + user.getDisplayName() + "】");
				}
			}
		} else if ("否".equals(isCompanyEmp)) {
			BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
			if (outerPerson == null) throw new RuntimeException("外部人员【" + empNo + "," + empName + "】不存在!");
			if (outerPerson.getStopflag()) throw new RuntimeException("外部人员【" + empNo + "," + empName + "】已被停用!");
		}

		if (StringUtils.isBlank(sftc)) {
			throw new RuntimeException("实发提成不能为空!");
		} else {
			BigDecimal tc = BigDecimal.ZERO;
			try {
				tc = new BigDecimal(sftc);
			} catch (Exception e) {
				throw new RuntimeException("实发提成格式不正确!");
			}
			if (tc.compareTo(BigDecimal.ZERO) != 0 && !NumberUtil.isInteger(trimZero(sftc))) {
				throw new RuntimeException("按财务的要求，实发提成目前只支持导入整数!");
			}
			if (tc.compareTo(BigDecimal.ZERO) < 0) {
				throw new RuntimeException("实发提成不能小于0!");
			}
		}
		if (StringUtils.isBlank(zhs)) {
			throw new RuntimeException("综合税不能为空!");
		} else {
			try {
				new BigDecimal(zhs);
			} catch (Exception e) {
				throw new RuntimeException("综合税格式不正确!");
			}
		}
		if (StringUtils.isBlank(tcPeriod)) throw new RuntimeException("提成明细届别不能为空!");
		BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
		if (null == yearPeriod) throw new RuntimeException("提成明细届别【" + tcPeriod + "】不存在!");

		if (StringUtils.isBlank(isDebt)) throw new RuntimeException("是否坏账不能为空!");
		if (!("是".equals(isDebt) || "否".equals(isDebt))) throw new RuntimeException("坏账请填写是或者否!");

		//2021-12月新增
		if (StringUtils.isBlank(extractType)) throw new RuntimeException("提成类型不能为空!");
		if (ExtractTypeEnum.getEnumeByvalue(extractType) == null)
			throw new RuntimeException("提成类型填写错误【期间提成，扎账总提成，扎账后提成，坏账明细】");
		if (StringUtils.isBlank(shouldSendExtract)) {
			throw new RuntimeException("应发提成不能为空!");
		} else {
			try {
				new BigDecimal(shouldSendExtract);
			} catch (Exception e) {
				throw new RuntimeException("应发提成格式不正确!");
			}
		}
		if (StringUtils.isNotBlank(tax)) {
			try {
				new BigDecimal(tax);
			} catch (Exception e) {
				throw new RuntimeException("个税格式不正确!");
			}
		}
		if (StringUtils.isNotBlank(taxReduction)) {
			try {
				new BigDecimal(taxReduction);
			} catch (Exception e) {
				throw new RuntimeException("个税减免格式不正确!");
			}
		}
		if (StringUtils.isNotBlank(invoiceExcessTax)) {
			try {
				new BigDecimal(invoiceExcessTax);
			} catch (Exception e) {
				throw new RuntimeException("发票超额税金格式不正确!");
			}
		}
		if (StringUtils.isNotBlank(invoiceExcessTaxReduction)) {
			try {
				new BigDecimal(invoiceExcessTaxReduction);
			} catch (Exception e) {
				throw new RuntimeException("发票超额税金减免格式不正确!");
			}
		}
	}


	@Override
	public void saveData(Map<Integer, Map<Integer, String>> successMap, String importType
			, Map<Integer, Map<Integer, String>> errorMap, List<String> headErrorMsg,Object head,List<Object> details, Object... params) {
		if (BudgetExtractController.TCIMPORT.equals(importType)) {
			List<Map<Integer, Map<Integer, String>>> errorDatas = new ArrayList<>();
			BudgetExtractsum extractsum = null;

			if (!errorMap.isEmpty()) return;

			//插入表头数据--保存提成主表数据		
			Map<Integer, String> headMap = successMap.get(1);
			try {
				extractsum =  saveExtractSum(headMap);
			} catch (Exception e) {
				e.printStackTrace();
				headErrorMsg.add(e.getMessage());
				//表头信息有错误直接return
				return;
			}
			if (Objects.nonNull(extractsum)) {
				/**
				 * 提成明细数据
				 */
				List<Integer> errorKeyList = new ArrayList<>();
				String badDebt = "提成";
				for (int i = 3; i <= successMap.size(); i++) {
					Map<Integer, String> detailMap = successMap.get(i);
					if (detailMap == null) continue;
					try {
						//@ApiModelProperty(value = "是否坏账 0否1是")
						//"是".equals(isDebt) ? true : false
						badDebt = successMap.get(3).get(6).equals("是")?"提成":"坏账";
						//插入提成明细
						applicationService.saveExtractImportDetails(detailMap, extractsum);
//						saveExtractImportDetails(detailMap, extractsum);
					} catch (Exception e) {
						e.printStackTrace();
						detailMap.put(detailMap.size(), e.getMessage());
						errorMap.put(i, detailMap);
						errorKeyList.add(i);
					}
				}
				//生成提成明细申请单
				//支付+“届别”+“月份”+“批次”+“提成/坏账”
				applicationService.saveEntity(extractsum,badDebt,params);


				errorKeyList.stream().forEach(e -> successMap.remove(e));
				//获取人数
				Integer personNum = this.extractImportDetailMapper.selectCount(new LambdaQueryWrapper<BudgetExtractImportdetail>().eq(BudgetExtractImportdetail::getExtractsumid, extractsum.getId()));
				extractsum.setExtractnum(personNum);
				this.budgetExtractsumMapper.updateById(extractsum);
			}
		} else if (BudgetExtractController.TCEXCESS.equals(importType)) {
			String curExtractBatch = params[0].toString();
			List<BudgetExtractsum> sums = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", curExtractBatch).eq("deleteflag", 0));
			if (sums.isEmpty()) return;
			List<Long> sumIds = sums.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
			List<BudgetExtractdetail> extractDetailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", sumIds).eq("deleteflag", 0));
			//key 为身份证号
			Map<String, BudgetExtractpaydetail> payDetailMap = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", curExtractBatch)).stream().collect(Collectors.toMap(BudgetExtractpaydetail::getIdnumber, e -> e, (e1, e2) -> e1));
			//执行超额导入
			for (int i = 1; i <= successMap.size(); i++) {
				Map<Integer, String> data = successMap.get(i);
				try {
					setExcessPay(data, extractDetailList, payDetailMap);
				} catch (Exception e) {
					e.printStackTrace();
					data.put(data.size(), e.getMessage());
					errorMap.put(i, data);
				}
			}
			generatePaymoneyOrderIfExcessHandleOver(curExtractBatch, sums, 2, null);
		} else if (BudgetExtractController.FEEPAY.equals(importType)) {

			if (!errorMap.isEmpty()) return;
			/**
			 * 导入费用发放
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
	 * 生成付款单在设置超额处理完后
	 *
	 * @param sums
	 * @param type              1.计算  2 超额
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
			//超额处理完成
			QueryWrapper<BudgetExtractpaydetail> wrapper = new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", curExtractBatch);
			if (StringUtils.isNotBlank(empno)) wrapper.eq("empno", empno);
			List<BudgetExtractpaydetail> payDetailList = this.payDetailMapper.selectList(wrapper);
			Map<String, BudgetExtractpaydetail> payDetailMap = payDetailList.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getIdnumber, e -> e, (e1, e2) -> e1));
			Map<Long, BudgetExtractpayment> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", payDetailList.stream().map(e -> e.getId()).collect(Collectors.toList()))).stream().collect(Collectors.toMap(BudgetExtractpayment::getBudgetextractpaydetailid, e -> e, (e1, e2) -> e1));
			List<BudgetPaymoney> paymoneyList = new ArrayList<>();
			extractDetailList.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getIdnumber)).forEach((idnumber, curExtractDetails) -> {
				//当前人的计税明细 
				BudgetExtractpaydetail extractpaydetail = payDetailMap.get(idnumber);
				if (extractpaydetail == null) return;
				if (StringUtils.isNotBlank(empno)) {
					if (!empno.equals(extractpaydetail.getEmpno())) return;
				}
				BudgetExtractpayment extractpayment = paymentMap.get(extractpaydetail.getId());
				String code = this.budgetExtractsumMapper.selectById(curExtractDetails.get(0).getExtractsumid()).getCode();
				//创建付款单
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
					paymoney.setRemark("提成法人公司实发费用");
					paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
					paymoneyList.add(paymoney);
				}
				if (extractpayment.getPaymoney2() != null && extractpayment.getPaymoney2().compareTo(BigDecimal.ZERO) > 0) {
					//存在避税发放
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
					sender.sendQywxMsgSyn(new QywxTextMsg(extractImportors, null, null, 0, "提成批次【" + curExtractBatch + "】已处理完,可导出发放表!", null));
					if (type == 1) {
						TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
						if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
							sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "提成批次【" + curExtractBatch + "】已计算完成，无超额记录!", null));
					} else if (type == 2) {
						TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
						if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
							sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "提成批次【" + curExtractBatch + "】超额记录已处理完!!", null));
					}

				} catch (Exception e) {
				}
			}

		} else {
			if (StringUtils.isBlank(empno)) {
				try {
					TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", "EXTRACTEXCESS"));
					if (dm != null && StringUtils.isNotBlank(dm.getDmValue()))
						sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "提成批次【" + curExtractBatch + "】有超额,请尽快在财务管理系统中处理!", null));
				} catch (Exception e) {
				}
			}

		}

	}

	@Autowired
	private BudgetPaymoneyService paymoneyService;

	/**
	 * 设置超额发放
	 *
	 * @param data
	 * @param extractDetailList
	 * @param payDetailMap
	 * @param paymentList
	 */
	private void setExcessPay(Map<Integer, String> data, List<BudgetExtractdetail> extractDetailList, Map<String, BudgetExtractpaydetail> payDetailMap) {
		String idNumber = data.get(0);
		String isCompanyEmp = data.get(1);
		String empNo = data.get(2);
		String empName = data.get(3);
		String billingUnitName = data.get(4);
		String excessMoney = data.get(5);
		String feeStr = data.get(6);
		String avoidTaxMoney = data.get(7);

		List<BudgetExtractdetail> curExtractDetails = extractDetailList.stream().filter(e -> e.getIdnumber().equals(idNumber)).collect(Collectors.toList());
		if (curExtractDetails.isEmpty()) return;
		BudgetExtractdetail extractdetail = curExtractDetails.get(0);
		if (!empNo.equals(extractdetail.getEmpno())) throw new RuntimeException("工号与身份证号匹配错误");
		if (!empName.equals(extractdetail.getEmpname())) throw new RuntimeException("名称与身份证号匹配错误");
		if (extractdetail.getHandleflag() || extractdetail.getExcessmoney() == null || extractdetail.getExcessmoney().compareTo(BigDecimal.ZERO) <= 0)
			return;
		//陈彩莲发放
		BigDecimal avoidtaxpay = StringUtils.isBlank(avoidTaxMoney) ? BigDecimal.ZERO : new BigDecimal(avoidTaxMoney);
		//工资单位发放
		BigDecimal fee = StringUtils.isBlank(feeStr) ? BigDecimal.ZERO : new BigDecimal(feeStr);
		if (extractdetail.getExcessmoney().compareTo(avoidtaxpay.add(fee)) != 0)
			throw new RuntimeException("金额总和需等于超额金额【" + NumberUtil.subZeroAndDot(extractdetail.getExcessmoney()) + "】");
		BudgetExtractpaydetail extractpaydetail = payDetailMap.get(idNumber);
		if (Objects.isNull(extractpaydetail)) return;

		extractpaydetail.setTotalaviodtax(extractpaydetail.getAviodtax().add(avoidtaxpay));
		extractpaydetail.setIncorporatedcompanyfee(fee);
		this.payDetailMapper.updateById(extractpaydetail);

		BudgetExtractpayment extractpayment = this.paymentMapper.selectOne(new QueryWrapper<BudgetExtractpayment>().eq("budgetextractpaydetailid", extractpaydetail.getId()));
		extractpayment.setPayfee(fee);
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
		if (fee.compareTo(BigDecimal.ZERO) > 0) {
			BudgetExtractgrantlog log = new BudgetExtractgrantlog();
			log.setIdnumber(idNumber);
			log.setExtractmonth(extractpaydetail.getExtractmonth());
			log.setIscompanyemp(extractdetail.getIscompanyemp());
			log.setEmpno(extractdetail.getEmpno());
			log.setExcessmoney(BigDecimal.ZERO);
			log.setAlreadygrantmoney(extractpaydetail.getIncorporatedcompanylj());
			log.setEmpname(extractdetail.getEmpname());
			log.setBillingunitid(extractpayment.getBunitid1());
			log.setBillingunitname(extractpayment.getBunitname1());
			log.setShouldgrantextract(fee);
			log.setCouldgrantextract(fee);
			log.setOrderno(999);
			log.setCratetime(new Date());
			log.setExcessgrantflag(true);
			this.extractgrantlogMapper.insert(log);
		}

		for (BudgetExtractdetail bbed : curExtractDetails) {
			bbed.setHandleflag(true);
		}
		this.extractDetailService.updateBatchById(curExtractDetails);
	}

	private void saveExtractImportDetails(Map<Integer, String> data, BudgetExtractsum extractsum) {
		String isCompanyEmp = data.get(0); //是否公司员工
		//新增员工个体户。


		String empNo = data.get(1); //工号
		String empName = data.get(2); //姓名
		String sftc = data.get(3); //实发提成
		String zhs = data.get(4); //综合税
		String tcPeriod = data.get(5); //提成届别
		String isDebt = data.get(6); //是否坏账

		String extractType = data.get(7);//提成类型
		String shouldSendExtract = data.get(8);//应发提成
		String tax = data.get(9);//个税
		String taxReduction = data.get(10);//个税减免
		String invoiceExcessTax = data.get(11);//发票超额税金
		String invoiceExcessTaxReduction = data.get(12);//发票超额税金减免








		BudgetYearPeriod yearPeriod = getPeriodByName(tcPeriod);
		//判断是否存在
		BudgetExtractImportdetail extractImportdetail = null;
		if (Objects.nonNull(extractImportdetail)) {
			extractImportdetail.setConsotax(new BigDecimal(zhs)); // 综合税
			extractImportdetail.setCopeextract(new BigDecimal(sftc));// 应付提成
			extractImportdetail.setUpdatetime(new Date());
			extractImportDetailMapper.updateById(extractImportdetail);
		} else {
			extractImportdetail = new BudgetExtractImportdetail();
			extractImportdetail.setId(null);
			extractImportdetail.setExtractsumid(extractsum.getId());
			//赋值 员工类型
			setUserTypeValue(isCompanyEmp, empNo, empName, extractImportdetail);

//			if ("是".equals(isCompanyEmp)) {
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
			extractImportdetail.setConsotax(new BigDecimal(zhs)); // 综合税
			extractImportdetail.setCopeextract(new BigDecimal(sftc));// 应付提成
			extractImportdetail.setCreatetime(new Date());
			extractImportdetail.setUpdatetime(extractImportdetail.getCreatetime());
			extractImportdetail.setIscompanyemp("是".equals(isCompanyEmp) ? true : false);
			extractImportdetail.setYearid(yearPeriod.getId());
			extractImportdetail.setIsbaddebt("是".equals(isDebt) ? true : false);
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
				break;
			case EXTERNAL_STAFF:
				BudgetExtractOuterperson outerPerson = getExtractOuterpersonByEmpnoAndEmpname(empNo, empName);
				extractImportdetail.setEmpid(outerPerson.getId().toString());
				extractImportdetail.setIdnumber(outerPerson.getIdnumber());
				break;
			case SELF_EMPLOYED_EMPLOYEES:
				//todo 个体户
				IndividualEmployeeFiles employeeFiles = individualService.lambdaQuery().eq(IndividualEmployeeFiles::getEmployeeJobNum, empNo).eq(IndividualEmployeeFiles::getAccountName, empName).last("limit 1").one();
				WbUser user2 = getUserByEmpno(empNo);
				extractImportdetail.setEmpid(user2.getUserId());
				extractImportdetail.setIdnumber(user2.getIdNumber());
				extractImportdetail.setIndividualEmployeeId(employeeFiles.getId());
			break;
			default:
				break;
		}
	}

	private BudgetExtractsum saveExtractSum(Map<Integer, String> data) {

		String year = data.get(1); //届别
		String extractMonth = data.get(3); //提成期间
		String unitname = data.get(5); //预算单位
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
	 * 提交
	 * 1.根据身份证号合并导入明细
	 *
	 * @param sumId
	 */
	public void submit(String ids) {
		Optional.ofNullable(ids).orElseThrow(() -> new RuntimeException("参数错误！"));
		List<BudgetExtractsum> budgetExtractsums = this.budgetExtractsumMapper.selectBatchIds(Arrays.asList(ids.split(",")));

		/**
		 * 重复提交
		 * 清理导入明细中所关联的提成明细数据（外键约束）
		 */
		extractImportDetailMapper.clearExtractDetail(ids);
		//清空提成明细
		extractDetailMapper.delete(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", Arrays.asList(ids.split(","))));

		List<BudgetExtractdetail> bedList = new ArrayList<>();
		List<BudgetExtractImportdetail> allimportDetails = new ArrayList<>();
		budgetExtractsums.forEach(extractsum -> {
			//审核通过及已计算的不允许提交
			if (extractsum.getStatus() > ExtractStatusEnum.VERIFYING.getType())
				throw new RuntimeException("操作失败！提成单号【" + extractsum.getCode() + "】不允许提交!");
			//合并
			combine(extractsum.getId(), allimportDetails);
			extractsum.setStatus(ExtractStatusEnum.VERIFYING.getType());
		});
		if (!allimportDetails.isEmpty()) this.extractImportDetailService.updateBatchById(allimportDetails);
		this.updateBatchById(budgetExtractsums);
		budgetExtractsums.forEach(extractsum -> {
			try {
				TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACTVERIFY).eq("dm", extractsum.getDeptname()));
				if (dm != null && StringUtils.isNotBlank(dm.getDmValue())) {
					sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "提成单号【" + extractsum.getCode() + "】已提交，请尽快到财务管理系统中审核。", null));
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	private void combine(Long sumId, List<BudgetExtractImportdetail> allimportDetails) {

		//获取所有的提成导入明细
		List<BudgetExtractImportdetail> importDetails = extractImportDetailMapper.selectList(new QueryWrapper<BudgetExtractImportdetail>().eq("extractsumid", sumId));
		if (!CollectionUtils.isEmpty(importDetails)) {
			importDetails.stream().collect(Collectors.groupingBy(BudgetExtractImportdetail::getIdnumber)).forEach((idnumber, importdetailByEmpnoList) -> {
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
			throw new RuntimeException("提交失败！该批次没有提成，请将其删除");
		}
	}

	/**
	 * 删除
	 *
	 * @param sumId
	 */
	public void deleteExtractSum(Long sumId) {
		BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(sumId);
		if (extractsum.getStatus() >= ExtractStatusEnum.VERIFYING.getType())
			throw new RuntimeException("操作失败!该单据不允许删除");
		extractImportDetailMapper.delete(new QueryWrapper<BudgetExtractImportdetail>().eq("extractsumid", sumId));
		extractDetailMapper.delete(new QueryWrapper<BudgetExtractdetail>().eq("extractsumid", sumId));
		budgetExtractsumMapper.deleteById(sumId);
	}

	/**
	 * 获取提成导入明细列表
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
	 * 获取提成明细列表
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
	 * 获取提成扣款明细
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
	 * 获取提成发放日志
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
	 * 获取提成发放明细
	 *
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<ExtractPayDetailVO> getExtractPayDetails(Map<String, Object> params, Integer page, Integer rows) {
		Page<ExtractPayDetailVO> pageCond = new Page<ExtractPayDetailVO>(page, rows);
		List<ExtractPayDetailVO> resultList = getPayDetailsByCondition(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), resultList);
	}

	private List<ExtractPayDetailVO> getPayDetailsByCondition(Page<ExtractPayDetailVO> pageCond, Map<String, Object> params) {
		return this.budgetExtractsumMapper.getExtractPayDetails(pageCond, params);
	}

	/**
	 * 审核通过
	 *
	 * @param sumId
	 */
	public void agree(String ids) {

		Optional.ofNullable(ids).orElseThrow(() -> new RuntimeException("参数错误！"));
		List<BudgetExtractsum> budgetExtractsums = this.budgetExtractsumMapper.selectBatchIds(Arrays.asList(ids.split(",")));

		budgetExtractsums.forEach(extractsum -> {
			if (extractsum.getStatus() != ExtractStatusEnum.VERIFYING.getType())
				throw new RuntimeException("操作失败!只允许审核已提交的单子");
			extractsum.setStatus(ExtractStatusEnum.APPROVED.getType());
			extractsum.setVerifytime(new Date());
			WbUser curUser = UserThreadLocal.get();
			extractsum.setVerifyorname(curUser.getDisplayName());
			extractsum.setVerifyorid(curUser.getUserId());
		});
		this.updateBatchById(budgetExtractsums);
		/**
		 * 如果全部审核通过通知相关人员
		 */
		List<BudgetExtractsum> totalExtractSums = budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", budgetExtractsums.get(0).getExtractmonth()));
		long approvedcount = totalExtractSums.stream().filter(e -> ExtractStatusEnum.APPROVED.getType() == e.getStatus().intValue()).count();
		if (totalExtractSums.size() == approvedcount) {
			try {
				TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE).eq("dm", EXTRACT_ALL_VERIFY));
				sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, "提成批次【" + budgetExtractsums.get(0).getExtractmonth() + "】已全部审核通过，请及时在财务管理系统中处理。", null));
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 退回
	 *
	 * @param sumId
	 * @param remark
	 */
	public void reject(Long sumId, String remark) {
		BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(sumId);
		if (extractsum.getStatus() != ExtractStatusEnum.VERIFYING.getType())
			throw new RuntimeException("操作失败!只允许审核已提交的单子");
		extractsum.setStatus(ExtractStatusEnum.RETURN.getType());
		extractsum.setVerifytime(new Date());
		WbUser curUser = UserThreadLocal.get();
		extractsum.setVerifyorname(curUser.getDisplayName());
		extractsum.setVerifyorid(curUser.getUserName());
		extractsum.setRemark(remark);
		this.budgetExtractsumMapper.updateById(extractsum);

		try {
			sender.sendQywxMsgSyn(new QywxTextMsg(extractsum.getCreator(), null, null, 0, "提成单号【" + extractsum.getCode() + "】已被退回。退回意见：" + remark, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 计算发放
	 *
	 * @param extractBatch          提成批次
	 * @param specialPersonNameList 特殊人员名单
	 * @throws Exception
	 */
	public synchronized void calculate(String extractBatch, List<HrSalaryYearTaxUser> specialPersonNameList, String empno) throws Exception {
		//获取当前批次下所有的提成
		List<BudgetExtractsum> curBatchExtractSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0));
		/**
		 * 验证是否能够开始计算
		 */
		if (StringUtils.isBlank(empno)) validateIsCanCalculate(extractBatch, curBatchExtractSumList);

		//判断是否是重新计算
		Boolean isReCalculate = curBatchExtractSumList.get(0).getStatus() == ExtractStatusEnum.CALCULATION_COMPLETE.getType();

		if (isReCalculate) {
			//重新计算 清理已计算的数据
			clearaCalculatedData(extractBatch, empno);
		}
		List<Long> curExtractSumIdList = curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//当前批次的所有提成明细
		List<BudgetExtractdetail> curBatchExtractDetailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().eq("deleteflag", 0).in("extractsumid", curExtractSumIdList));
		if (curBatchExtractDetailList.isEmpty()) return;
		/**
		 * 填充基础数据（外部人员发放单位，工资信息，限额规则，发放规则）
		 */
		ExtractPayCommonData extractPayCommonData = populateCommonData(extractBatch, curBatchExtractDetailList);
		extractPayCommonData.setCurBatchExtractSumList(curBatchExtractSumList);
		extractPayCommonData.setSpecialPersonNameList(specialPersonNameList);
		//开始计算
		doCalculate(extractPayCommonData, curBatchExtractDetailList, isReCalculate, empno);

		curBatchExtractSumList.stream().forEach(sum -> {
			sum.setStatus(ExtractStatusEnum.CALCULATION_COMPLETE.getType());
		});
		this.sumService.updateBatchById(curBatchExtractSumList);

	}

	/**
	 * 开始计算
	 *
	 * @param extractPayCommonData
	 * @param curBatchExtractDetailList
	 */
	private void doCalculate(ExtractPayCommonData extractPayCommonData,
	                         List<BudgetExtractdetail> curBatchExtractDetailList, Boolean isReCalculate, String empno) throws Exception {
		List<String> errorMsg = new ArrayList<>();
		//以身份证号分组
		curBatchExtractDetailList.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getIdnumber)).forEach((idnumber, extractDetails) -> {
			try {
				int size = extractDetails.stream().collect(Collectors.groupingBy(BudgetExtractdetail::getEmpno)).size();
				if (size > 1) throw new RuntimeException("计算失败！数据异常，身份证号【" + idnumber + "】下存在两个编号！");
				if (StringUtils.isNotBlank(empno)) {
					if (!empno.equals(extractDetails.get(0).getEmpno())) return;
				}
				//组装计算数据
				ExtractEmpCalDataDetail curEmpCalData = packageExtractEmpCalData(extractDetails, extractPayCommonData);
				curEmpCalData.setExtractDetails(extractDetails);
				if (curEmpCalData.getIscompanyemp() && curEmpCalData.getIsQuit()) {
					/**
					 * 处理离职人员
					 */
					handleQuitEmp(curEmpCalData, extractPayCommonData.getCurExtractBatch());
					return;
				}

				/**
				 * 不需要参与计算（应发提成<=0 并且 综合税为0的时候）
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
					//提成冲借款
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
				//最终应发金额
				BigDecimal realextract = curEmpCalData.getExtractDetails().stream().map(BudgetExtractdetail::getRealextract).reduce(BigDecimal.ZERO, BigDecimal::add);
				curEmpCalData.setRealExtract(realextract.subtract(curEmpCalData.getFeePay()));

				if (curEmpCalData.getRealExtract().compareTo(BigDecimal.ZERO) < 0)
					throw new RuntimeException(curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + ")的提成数据为负数!");
				if (curEmpCalData.getRealExtract().compareTo(BigDecimal.ZERO) == 0) return;
				/**
				 * 先计算分公司
				 */
				BudgetExtractpaydetail curEmpPaydetail = calSplitCompany(curEmpCalData, extractPayCommonData.getCurExtractBatch(), extractPayCommonData.getOuterThreshold(), extractPayCommonData.getOuterOrinalExtraTax());

				List<BudgetExtractquotaRuledetail> quotaruledetailList = extractPayCommonData.getQuotaRuleDetailMap().get(curEmpCalData.getQuotaRule().getId()).stream().filter(e -> e.getMinsalary().compareTo(curEmpPaydetail.getSalary()) <= 0 && e.getMaxsalary().compareTo(curEmpPaydetail.getSalary()) > 0).collect(Collectors.toList());
				if (quotaruledetailList.isEmpty())
					throw new RuntimeException("请先设置工资单位【" + curEmpCalData.getBillingUnit().getName() + "】限额规则明细!错误标记:" + curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + "," + curEmpPaydetail.getSalary() + ")");
				if (quotaruledetailList.size() > 1)
					throw new RuntimeException("工资单位【" + curEmpCalData.getBillingUnit().getName() + "】限额规则明细存在金额重复!");
				//获取限额金额
				BigDecimal quotamoney = quotaruledetailList.get(0).getQuotamoney();
				/**
				 * 限额发放
				 */
				Long detailId = extractDetails.get(0).getId();
				extractPayCommonData.getPayOrderMap().put(detailId, 0);
				quotaPay(curEmpPaydetail, curEmpCalData, quotamoney, extractPayCommonData, detailId, isReCalculate);

			} catch (Exception e) {
				e.printStackTrace();
				errorMsg.add(e.getMessage());
			}
		});
		if (!errorMsg.isEmpty()) {
			throw new RuntimeException(errorMsg.stream().collect(Collectors.joining("<br>")));
		}
		//生成提成批次二维码
		generateExtractBatchCode(extractPayCommonData.getCurExtractBatch());

		/**
		 * 生成付款单【如果没有超额，或者超额都已经处理】
		 */
		generatePaymoneyOrderIfExcessHandleOver(extractPayCommonData.getCurExtractBatch(), extractPayCommonData.getCurBatchExtractSumList(), 1, empno);
	}

	/**
	 * 生成提成批次二维码
	 *
	 * @param curExtractBatch 提成批次
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
	 * 生成付款单【如果没有超额，或者超额都已经处理】
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
//			 * 条件满足，生成付款单
//			 */
//			List<BudgetPaymoney> paymoneyList = new ArrayList<>();
//			for(BudgetExtractpayment payment : paymentList) {
//				String detailId = payment.getExtractdetailids().split(",")[0];
//				BudgetExtractdetail extractdetail = curBatchExtractDetailList.stream().filter(e->e.getId().toString().equals(detailId)).findFirst().orElse(null);
//				ExtractEmpCalDataDetail empCalDataDetail = calDataList.stream().filter(e->e.getIdnumber().equals(extractdetail.getIdnumber())).findFirst().orElse(null);
//				BudgetExtractsum extractsum = this.budgetExtractsumMapper.selectById(extractdetail.getExtractsumid());
//				extractmonth = extractsum.getExtractmonth();
//				//创建付款单
//				createPaymoney(empCalDataDetail,payment,extractsum.getCode(),paymoneyList);
//			}
//			if(!paymoneyList.isEmpty()) this.paymoneyMapper.batchSavePaymoney(paymoneyList);
//			/**
//			 * 通知商务部相关人员
//			 */
//			try {
//				sender.sendQywxMsgSyn(new QywxTextMsg(extractImportors, null, null, 0, "提成批次【"+extractmonth+"】已处理完,可导出发放表!", null));
//			}catch(Exception e) {}
//		}else {
//			try {
//				sender.sendQywxMsgSyn(new QywxTextMsg("17474", null, null, 0, "提成批次【"+extractmonth+"】有超额,请尽快在预算系统中处理!", null));
//			}catch(Exception e) {}
//		}
//	}

	/**
	 * 创建付款单
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
			paymoney.setRemark("提成法人公司实发费用");
			paymoney.setPaymoneycode(distributedNumber.getPaymoneyNum());
			paymoneyList.add(paymoney);
		}
		if (payment.getPaymoney2() != null && payment.getPaymoney2().compareTo(BigDecimal.ZERO) > 0) {
			//存在避税发放
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
	 * 判断是否超过限额
	 * 超过限额走关联外部人员发放
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
		 * 获取当前人当年的法人公司已发金额
		 */
		BigDecimal incorporatedcompanyPayedExtract = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		//当前计算分公司的法人实发
		BigDecimal curIncorporatedCompanyPayExtract = curEmpPaydetail.getIncorporatedcompany() == null ? BigDecimal.ZERO : curEmpPaydetail.getIncorporatedcompany();
		if (curIncorporatedCompanyPayExtract.compareTo(BigDecimal.ZERO) > 0 && curIncorporatedCompanyPayExtract.add(incorporatedcompanyPayedExtract).compareTo(quotamoney) > 0) {
			/**
			 * 如果超额
			 */
			//tempmoney 主要是为了解决 法人公司已发>限额金额的情况（修改限额金额）
			BigDecimal tempmoney = incorporatedcompanyPayedExtract.compareTo(quotamoney) == 1 ? quotamoney : incorporatedcompanyPayedExtract;
			//外部人员发放总额
			BigDecimal outerPayTotal = curIncorporatedCompanyPayExtract.add(tempmoney).subtract(quotamoney);
			createGrantLog(curEmpCalData, extractPayCommonData, quotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract.subtract(outerPayTotal), detailId);
			curEmpPaydetail.setIncorporatedcompany(curIncorporatedCompanyPayExtract.subtract(outerPayTotal));

			//外部人员的发放总额
			List<BigDecimal> payextractList = new ArrayList<>();
			payextractList.add(outerPayTotal);
			//获取关联的外部人员
			//List<BudgetExtractOuterperson> refOuterPersonList = curEmpCalData.getRefOuterPersonList();
			/**
			 * 先不使用这块。
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
						throw new RuntimeException("请先设置工资单位【" + curEmpCalData.getBillingUnit().getName() + "】限额规则明细!错误标记:" + curEmpCalData.getEmpname() + "(" + curEmpCalData.getEmpno() + "," + curEmpPaydetail.getSalary() + ")");

					//throw new RuntimeException("请先设置工资单位【" + curEmpCalData.getBillingUnit().getName() + "】限额规则明细!");
					if (quotaruledetailList.size() > 1)
						throw new RuntimeException("工资单位【" + curEmpCalData.getBillingUnit().getName() + "】限额规则明细存在金额重复!");
					//限额金额
					BigDecimal refOuterQuotamoney = quotaruledetailList.get(0).getQuotamoney();
					//当前人员所关联的外部人员发放
					refOuterQuotaPay(curOuterPaydetail, curOuterCalDataDetail, refOuterQuotamoney, extractPayCommonData, detailId, payextractList);
					curOuterCalDataDetail.setFinalIncorporatedCompanyPayedExtract(curOuterPaydetail.getIncorporatedcompany());
					curOuterCalDataDetail.setAvoidTaxMoney(curOuterPaydetail.getTotalaviodtax());
					//处理计算结果
					handlePayResult(curOuterCalDataDetail, curOuterPaydetail.getTaxdiffrence(), curOuterPaydetail, extractPayCommonData, isReCalculate);
					extractPayCommonData.getExtractCalDataDetailList().add(curOuterCalDataDetail);
				}
			}


			BigDecimal money = payextractList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
			if (money.compareTo(BigDecimal.ZERO) > 0) {
				for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
					bed.setExcessmoney(money);
					bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType());
				}

			} else {
				for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
					bed.setExcessmoney(BigDecimal.ZERO);
					bed.setExcesstype(ExtractExcessTypeEnum.EXCESS_FINISHED.getType());
				}
			}

		} else {
			//未超额
			createGrantLog(curEmpCalData, extractPayCommonData, quotamoney, incorporatedcompanyPayedExtract, curIncorporatedCompanyPayExtract, curIncorporatedCompanyPayExtract, detailId);
			curEmpPaydetail.setIncorporatedcompany(curIncorporatedCompanyPayExtract);
			for (BudgetExtractdetail bed : curEmpCalData.getExtractDetails()) {
				bed.setExcessmoney(BigDecimal.ZERO);
				bed.setExcesstype(ExtractExcessTypeEnum.NOEXCESS.getType());
			}
		}
		//设置默认超额处理状态为false
		for (BudgetExtractdetail extractDetail : curEmpCalData.getExtractDetails()) {
			extractDetail.setHandleflag(false);
			this.extractDetailMapper.updateById(extractDetail);
		}
		this.payDetailMapper.updateById(curEmpPaydetail);
		//最终的法人公司发放
		curEmpCalData.setFinalIncorporatedCompanyPayedExtract(curEmpPaydetail.getIncorporatedcompany());
		curEmpCalData.setAvoidTaxMoney(curEmpPaydetail.getTotalaviodtax());
		//处理计算结果
		handlePayResult(curEmpCalData, curEmpPaydetail.getTaxdiffrence(), curEmpPaydetail, extractPayCommonData, isReCalculate);
		extractPayCommonData.getExtractCalDataDetailList().add(curEmpCalData);
	}

	/**
	 * 处理发放结果
	 * 1.生成发放表。
	 * 2.生成提成台账
	 *
	 * @param curEmpCalData
	 * @param curEmpPaydetail
	 * @param extractPayCommonData
	 * @param isReCalculate
	 */
	private void handlePayResult(ExtractEmpCalDataDetail curEmpCalData, BigDecimal taxDiffrence, BudgetExtractpaydetail curEmpPaydetail, ExtractPayCommonData extractPayCommonData, Boolean isReCalculate) {
		//创建提成发放记录
		BudgetExtractpayment payment = createExtractPayment(curEmpCalData, taxDiffrence, curEmpPaydetail.getId());
		extractPayCommonData.getPaymentList().add(payment);
		/**
		 * 创建提成台账
		 */
		//当前提成月份
		String curExtractMonth = extractPayCommonData.getCurExtractBatch().substring(0, 6);
		//获取本月的提成台账
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
			//获取当月已经发的法人公司实发（除了本次）
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
			//获取当月已经发的法人公司实发（除了本次）
			BigDecimal curMonthAgoPayExtract = curAgoPaydetails.stream().filter(e->e.getExtractmonth().startsWith(curExtractMonth) && e.getIncorporatedcompany()!=null).map(e->e.getIncorporatedcompany()).reduce(BigDecimal.ZERO,BigDecimal::add);
			bea.setRealextract(curMonthAgoPayExtract.add(curEmpPaydetail.getIncorporatedcompany()));
			bea.setIncorporatedcompanylj(curEmpPaydetail.getIncorporatedcompanylj().add(curEmpPaydetail.getIncorporatedcompany()));
			this.extractArrearsMapper.updateById(bea);
		}*/

	}

	/**
	 * 创建提成发放记录
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
	 * 关联外部人员限额发放
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
		 * 获取当前人当年的法人公司已发金额
		 */
		BigDecimal incorporatedcompanyPayedExtract = curOuterCalDataDetail.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);

		//当前计算分公司的法人实发
		BigDecimal curIncorporatedCompanyPayExtract = curOuterPaydetail.getIncorporatedcompany() == null ? BigDecimal.ZERO : curOuterPaydetail.getIncorporatedcompany();
		if (curIncorporatedCompanyPayExtract.compareTo(BigDecimal.ZERO) > 0 && curIncorporatedCompanyPayExtract.add(incorporatedcompanyPayedExtract).compareTo(refOuterQuotamoney) > 0) {
			//超额
			BigDecimal tempmoney = incorporatedcompanyPayedExtract.compareTo(refOuterQuotamoney) == 1 ? refOuterQuotamoney : incorporatedcompanyPayedExtract;
			//外部人员发放总额
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
	 * 创建发放日志
	 *
	 * @param extractPayCommonDat
	 * @param curEmpCalData
	 * @param quotamoney
	 * @param incorporatedcompanyPayedExtract  已发
	 * @param curIncorporatedCompanyPayExtract 当前应发
	 * @param couldgrantextract                可发
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
	 * 计算分公司
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
		//累计专项扣除
		BigDecimal specialdeductionSum = BigDecimal.ZERO;
		//累计工资
		BigDecimal salarySum = BigDecimal.ZERO;
		//累计工资个税
		BigDecimal salarytaxSum = BigDecimal.ZERO;
		//累计五险一金
		BigDecimal fiveriskonefundSum = BigDecimal.ZERO;
		//累计起征点
		BigDecimal thresholdSum = BigDecimal.ZERO;
		//累计临界金额
		BigDecimal criticalmoneySum = BigDecimal.ZERO;

		if (curEmpCalData.getIscompanyemp()) {
			//内部员工
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
				//如果是特殊名单
				paydetail.setFreetax(new BigDecimal(salaryMsg.get("freetax") == null ? "0" : salaryMsg.get("freetax").toString()));
				BigDecimal freetaxs = new BigDecimal(salaryMsg.get("freetaxs") == null ? "0" : salaryMsg.get("freetaxs").toString());
				paydetail.setFreetaxs(freetaxs);
				thresholdSum = new BigDecimal(salaryMsg.get("startpoints").toString()).add(paydetail.getFreetax()).add(paydetail.getFreetaxs());
				threshold = thresholdSum;
			}

		} else {

			/**
			 * 针对外部人员 
			 * 提成>4000   本月额外税为4000
			 * 提成<=4000 本月额外税为提成
			 */

			BigDecimal ordernal = outerOrinalExtraTax;
			BigDecimal freetax = curEmpCalData.getRealExtract().compareTo(ordernal) > 0 ? ordernal : curEmpCalData.getRealExtract();
			paydetail.setFreetax(freetax);

			//以前的累计额外税
			BigDecimal freetaxs = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getFreetax() == null ? BigDecimal.ZERO : e.getFreetax()).reduce(BigDecimal.ZERO, BigDecimal::add);
			thresholdSum = freetaxs.add(freetax).add(outerThreshold);
			threshold = thresholdSum;

			String nowmonth = curExtractBatch.substring(4, 6);
			int month = Integer.valueOf(nowmonth) - Integer.valueOf("1") + 1;
			criticalmoneySum = curEmpCalData.getPayRule().getJe().multiply(new BigDecimal("" + month));
			taxList = ExtractPayCommonData.getTaxList();

			/**
			 * 老政策（作废）
			 thresholdSum = new BigDecimal("9000").multiply(new BigDecimal(""+month));
			 threshold = new BigDecimal("9000");
			 *
			 */
		}

		paydetail.setId(null);
		paydetail.setIdnumber(curEmpCalData.getIdnumber());
		paydetail.setEmpname(curEmpCalData.getEmpname());
		paydetail.setEmpno(curEmpCalData.getEmpno());
		paydetail.setCopeextract(curEmpCalData.getRealExtract());// 待发提成
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

		//累计实发金额（之前）
		BigDecimal copeextractSum = BigDecimal.ZERO;
		//累计综合税（之前）
		BigDecimal consotaxSum = BigDecimal.ZERO;
		if (!CollectionUtils.isEmpty(curEmpCalData.getCurBillUnitPayDetailList())) {
			copeextractSum = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getCopeextract()).reduce(BigDecimal.ZERO, BigDecimal::add);
			consotaxSum = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getConsotax()).reduce(BigDecimal.ZERO, BigDecimal::add);
		}
		paydetail.setCopeextractlj(curEmpCalData.getRealExtract().add(copeextractSum));// 累计待发提成
		paydetail.setConsotaxlj(curEmpCalData.getConsotax().add(consotaxSum));
		paydetail.setRealesalarytaxlj(paydetail.getConsotaxlj().add(salarytaxSum));

		// 根据实扣个税计算倒推金额
		BigDecimal dtje = getDtje(paydetail.getRealesalarytaxlj(), taxList).add(paydetail.getThresholdlj()).add(paydetail.getSpecialdeductionlj());
		paydetail.setDtje(dtje);


		// 可发提成(倒推金额-累计实发工资(含本次)-累计实扣个税-累计法人公司实发(不含本次))
		BigDecimal incorporatedcompanyPayedExtract = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> {
			return e.getIncorporatedcompany() == null ? BigDecimal.ZERO : e.getIncorporatedcompany();
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal renewableextract = dtje.subtract(paydetail.getSalarylj()).subtract(paydetail.getRealesalarytaxlj()).subtract(incorporatedcompanyPayedExtract);
		paydetail.setRenewableextract(renewableextract);

		/** 5.39% 【累计临界金额(同月多次只加一次,包含本月) - 累计实发工资(含本月) +
		 * 累计专项扣除(含本月) -法人公司累计实发(不包括本次，同月多次需加上前几次)】
		 */
		BigDecimal renewableextract539 = paydetail.getCriticalmoneylj().subtract(paydetail.getSalarylj()).add(paydetail.getSpecialdeductionlj()).subtract(incorporatedcompanyPayedExtract);
		paydetail.setRenewableextract539(renewableextract539);

		// 公司可发 Max(本次可发提成,本次累计5.39%释放可发提成)
		paydetail.setCompanyableextract(renewableextract.compareTo(renewableextract539) >= 0 ? renewableextract : renewableextract539);


		/**
		 * 获取法人公司实发 最小值为0
		 */
		BigDecimal curIncorporatedcompanyPayExtract = paydetail.getCopeextract().compareTo(paydetail.getCompanyableextract()) >= 0 ? paydetail.getCompanyableextract() : paydetail.getCopeextract();
		curIncorporatedcompanyPayExtract = curIncorporatedcompanyPayExtract.compareTo(BigDecimal.ZERO) >= 0 ? curIncorporatedcompanyPayExtract : BigDecimal.ZERO;
		/**
		 * 直接去掉小数点  update by minzhq 2021-12月
		 */
		curIncorporatedcompanyPayExtract = curIncorporatedcompanyPayExtract.setScale(0, BigDecimal.ROUND_DOWN);
		paydetail.setIncorporatedcompany(curIncorporatedcompanyPayExtract);

		// 法人公司累计实发（不含本次）
		paydetail.setIncorporatedcompanylj(incorporatedcompanyPayedExtract);

		// 避税发放【本次待发提成 - 本次法人公司实发】
		paydetail.setAviodtax(paydetail.getCopeextract().subtract(paydetail.getIncorporatedcompany()));


		/**
		 * 实发提成合计（本年的工资+提成）
		 */
		Integer month = Integer.valueOf(curExtractBatch.substring(0, 6));
		Long count = curEmpCalData.getCurBillUnitPayDetailList().stream().filter(e -> e.getExtractmonth().contains(month + "")).count();
		if (count.intValue() == 0) {
			paydetail.setRealeextract(paydetail.getSalary().add(paydetail.getIncorporatedcompany()));
		} else {
			paydetail.setRealeextract(paydetail.getIncorporatedcompany());
		}
		// 累计实发提成合计				
		paydetail.setRealeextractsum(paydetail.getSalarylj().add(paydetail.getIncorporatedcompany()).add(incorporatedcompanyPayedExtract));

		/*
		 * 实发提成倒推金额 若 “各月累计实发合计（含本月）<= 各月累计专项扣除 +
		 * 各月累计起征点”，则：各月累计实发合计（含本月） MAX(【各月累计实发合计（含本月）- 各月累计专项扣除
		 * - 各月累计起征点 -
		 * {0;252;1692;3192;5292;8592;18192}*10】/(1-{
		 * 3;10;20;25;30;35;45}%)) + 各月累计专项扣除 + 各月累计起征点
		 */
		BigDecimal sfdtje = paydetail.getRealeextractsum().compareTo(paydetail.getSpecialdeductionlj().add(paydetail.getThresholdlj())) <= 0 ? paydetail.getRealeextractsum() : getSfdtje(paydetail.getRealeextractsum(), paydetail.getSpecialdeductionlj(), paydetail.getThresholdlj(), taxList);
		paydetail.setSfdtje(sfdtje);

		/**
		 * 获取累计上交个税
		 */
		BigDecimal payableTaxs = curEmpCalData.getCurBillUnitPayDetailList().stream().map(e -> e.getPayabletax()).reduce(BigDecimal.ZERO, BigDecimal::add);
		//本次上交个税
		BigDecimal payabletax = getPayableTax(sfdtje, paydetail.getSpecialdeductionlj(), paydetail.getThresholdlj(), payableTaxs, taxList);
		paydetail.setPayabletax(payabletax);
		paydetail.setPayabletaxlj(payableTaxs.add(payabletax));


		//上次个税差异（ 上个月+本月前几次）
		BigDecimal taxDiffrence = curEmpCalData.getCurBillUnitPayDetailList().stream()
				.filter(e -> e.getExtractmonth().contains((month.intValue() - 1) + "")
						|| e.getExtractmonth().substring(0, 6)
						.equals(curExtractBatch.substring(0, 6)))
				.map(e -> e.getTaxdiffrence())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		//本次个税差异
		paydetail.setTaxdiffrence(paydetail.getPayabletax().subtract(paydetail.getRealesalarytax()).add(taxDiffrence));

		// 释放比例 【累计上次个税(包含本次) / (累计五险(包含本次) + 本次实发提成倒推)】
		paydetail.setReleasepercent(paydetail.getPayabletaxlj().divide(paydetail.getFiveriskonefundlj().add(paydetail.getSfdtje()), 5, BigDecimal.ROUND_HALF_DOWN));

		paydetail.setTotalextract(paydetail.getCopeextract());
		paydetail.setIncorporatedcompanyfee(BigDecimal.ZERO);
		paydetail.setTotalaviodtax(paydetail.getAviodtax());
		paydetail.setCreatetime(currentDate);
		this.payDetailMapper.insert(paydetail);
		return paydetail;
	}

	/**
	 * 获取上交个税
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
		// 取最大值
		for (Map taxb : taxList) {
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// 扣除数
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// 税率
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

	// 获取实发倒推金额
	private BigDecimal getSfdtje(BigDecimal realeextractsum,
	                             BigDecimal specialdeductionlj, BigDecimal thresholdlj,
	                             List<Map> taxList) {
		if (taxList == null) return BigDecimal.ZERO;
		BigDecimal result = null;
		BigDecimal tmpresult = null;
		// 取最大值
		for (Map taxb : taxList) {
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// 扣除数
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// 税率
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

	// 获取倒推金额
	private BigDecimal getDtje(BigDecimal realesalarytaxlj, List<Map> taxList) {
		//if(taxList==null) return BigDecimal.ZERO;
		BigDecimal result = null;
		BigDecimal tmpresult = null;
		// 取最小值
		for (Map taxb : taxList) {
			if (null == taxb.get("quickcal")) throw new RuntimeException("请联系管理员维护税率表关系!");
			BigDecimal b = new BigDecimal(taxb.get("quickcal").toString());// 扣除数
			BigDecimal a = new BigDecimal(taxb.get("taxrate").toString());// 税率
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
	 * 获取能够还的借款单（先到先还）
	 *
	 * @param list_lendmoney
	 * @param isbx
	 * @return
	 */
	private List<BudgetLendmoney> getCanRepayLendMoneyList(List<BudgetLendmoney> list_lendmoney) {
		List<BudgetLendmoney> repayForLendMoneyList = new ArrayList<>();
		// 可还借款总额
		if (list_lendmoney != null && !list_lendmoney.isEmpty()) {
			for (BudgetLendmoney lendmoney : list_lendmoney) {
				// 借款类型
				Integer lendtype = lendmoney.getLendtype();
				if (lendtype != null && (lendtype.intValue() == LendTypeEnum.LEND_TYPE_13.getType())) {
					// 项目借款
					Boolean flushingflag = lendmoney.getFlushingflag();
					Date planpaydate = lendmoney.getPlanpaydate();
					Boolean chargebillflag = lendmoney.getChargebillflag();
					if ((flushingflag != null && !flushingflag) || (flushingflag == null && chargebillflag)) {
						//不达标 或者  未设置达标+允许还款
						if (planpaydate.getTime() <= new Date().getTime()) {
							// 还款日期到
							if (flushingflag != null && (!flushingflag || chargebillflag)) {
								repayForLendMoneyList.add(lendmoney);
							}
						}
					}
				} else if (lendtype != null) {
					// 
					Date planpaydate = lendmoney.getPlanpaydate();
					if (planpaydate.getTime() <= new Date().getTime()) {
						// 还款日期到
						repayForLendMoneyList.add(lendmoney);
					}
				}
			}
		}
		return repayForLendMoneyList;
	}

	/**
	 * 生成还款明细
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
	 * 提成冲借款
	 *
	 * @param extractDetails
	 * @param map
	 */
	private void extractDeduction(List<BudgetExtractdetail> extractDetails, Map<String, List<BudgetLendmoney>> map) {
		Date currentdate = new Date();
		//当前提成人员的所有借款单
		List<BudgetLendmoney> curEmpnoLendMoneyList = map.get(extractDetails.get(0).getEmpno());
		//if(curEmpnoLendMoneyList==null || curEmpnoLendMoneyList.isEmpty()) return;
		// 可还的借款单（先到先还）
		List<BudgetLendmoney> canRepayLendMoneyList = getCanRepayLendMoneyList(curEmpnoLendMoneyList);
		for (BudgetExtractdetail extractDetail : extractDetails) {
			if (extractDetail.getIscompanyemp()) {

				BigDecimal copeextract = extractDetail.getCopeextract();// 实付提成
				BigDecimal totalwithholdmoney = BigDecimal.ZERO;
				// 欠钱总额>0 && 应付提成>0 具备还款资格
				if (!canRepayLendMoneyList.isEmpty() && copeextract.compareTo(BigDecimal.ZERO) > 0) {
					extractDetail.setUpdatetime(currentdate);
					// 保存还款主表
					BudgetRepaymoney brm = new BudgetRepaymoney();
					brm.setId(null);
					brm.setEmpid(extractDetail.getEmpid());
					brm.setEmpno(extractDetail.getEmpno());
					brm.setEmpname(extractDetail.getEmpname());
					brm.setRepaydate(currentdate);
					brm.setRepaytype(4);
					// 提成id
					brm.setRepaytypeid(extractDetail.getId().toString());
					brm.setCreatetime(currentdate);
					brm.setRepaymoney(BigDecimal.ZERO);
					brm.setEffectflag(false);
					brm.setRepaymoneycode(distributedNumber.getRepayNum());
					this.repaymoneyMapper.insert(brm);
					// 按照借钱日期排序 (还款日期先到先还,借款日期先借先还)
					List<BudgetLendmoney> sortByLendDateList = canRepayLendMoneyList.stream()
							.sorted((e1, e2) -> Long.compare(e1.getPlanpaydate().getTime(), e2.getPlanpaydate().getTime()))
							.sorted((e1, e2) -> Long.compare(e1.getLenddate().getTime(), e2.getLenddate().getTime()))
							.collect(Collectors.toList());

					// 应付提成
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
									//允许还款不冲利息
									flag = true;
								}
							}
							//还欠金额
							BigDecimal leftlendmoney = lendmoney.subtract(repaidmoney);
							//还欠利息
							BigDecimal leftinterestmoney = interestmoney.subtract(repaidinterestmoney);
							if (!flag) {
								BigDecimal copeexsubleftlendmoney = copeexSubConsotax.subtract(leftlendmoney);//10
								if (copeexsubleftlendmoney.compareTo(BigDecimal.ZERO) >= 0) {
									if (copeexsubleftlendmoney.subtract(leftinterestmoney).compareTo(BigDecimal.ZERO) >= 0) {
										//还利息（全还）
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
	 * 生成还款往来记录
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
				l_r_money.setCurmoney(arrears.getArrearsmoeny()); //当前欠款
				budgetRepayMoney.setEffectflag(true);
				l_r_money.setMoney(budgetRepayMoney.getRepaymoney());
				l_r_money.setMoneytype(-1); // 还款
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
	 * 处理离职人员(全部走避税)
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
		 * 离职人员 不需要获取已发。  暂设置-1区分这笔为离职人员
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
			this.extractDetailMapper.updateById(detail);
		}
	}

	/**
	 * 当前提成人员的提成计算前数据
	 *
	 * @param extractDetails
	 * @param extractPayCommonData
	 * @return
	 */
	private ExtractEmpCalDataDetail packageExtractEmpCalData(Object obj, ExtractPayCommonData extractPayCommonData) {
		ExtractEmpCalDataDetail dataDetail = new ExtractEmpCalDataDetail();

		if (obj instanceof List) {
			List<BudgetExtractdetail> extractDetails = (List<BudgetExtractdetail>) obj;
			//当前计算人的编号
			String empno = extractDetails.get(0).getEmpno();
			dataDetail.setEmpno(empno);
			dataDetail.setEmpid(extractDetails.get(0).getEmpid());
			dataDetail.setIdnumber(extractDetails.get(0).getIdnumber());
			//当前计算人的名称
			String empname = extractDetails.get(0).getEmpname();
			dataDetail.setEmpname(empname);
			//当前计算人是否是公司员工
			Boolean iscompanyemp = extractDetails.get(0).getIscompanyemp();
			dataDetail.setIscompanyemp(iscompanyemp);
			BigDecimal copeextract = extractDetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal consotax = extractDetails.stream().map(BudgetExtractdetail::getConsotax).reduce(BigDecimal.ZERO, BigDecimal::add);
			dataDetail.setConsotax(consotax);

			/**
			 * 除去费用发放的金额。 add by minzhq 2021-12-20
			 */
			List<BudgetExtractFeePayDetailBeforeCal> budgetExtractFeePayDetails = extractPayCommonData.getFeePayEmpMap().get(empno);
			if (!CollectionUtils.isEmpty(budgetExtractFeePayDetails)) {
				BigDecimal feePayDetails = budgetExtractFeePayDetails.stream().map(e -> e.getFeePay() == null ? BigDecimal.ZERO : e.getFeePay()).reduce(BigDecimal.ZERO, BigDecimal::add);
				dataDetail.setFeePay(feePayDetails);
				//copeextract = copeextract.subtract(feePayDetails);
			}
			if (copeextract.compareTo(BigDecimal.ZERO) < 0) {
				throw new RuntimeException("计算失败！员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】的提成数据为负数。");
			}
			dataDetail.setCopeextract(copeextract);

		} else if (obj instanceof BudgetExtractdetail) {
			//提成关联人员发放时使用
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
			//判断是否是特殊人单中的人			
			HrSalaryYearTaxUser m = extractPayCommonData.getSpecialPersonNameList().stream().filter(e -> dataDetail.getEmpno().equals(e.getEmpno()) && dataDetail.getIdnumber().equals(e.getCertno())).findFirst().orElse(null);
			dataDetail.setIsSepcialPerson(m == null ? false : true);
			Map<String, Object> salaryMap = (Map<String, Object>) extractPayCommonData.getSalaryMsg().get(dataDetail.getEmpno());
			dataDetail.setSalaryMap(salaryMap);
			if (extractPayCommonData.getUserMap().get(dataDetail.getEmpno()).getStatus().compareTo(BigDecimal.ZERO) == 0) {
				//离职员工
				dataDetail.setIsQuit(true);
				dataDetail.setBillingUnit(extractPayCommonData.getQuiterBillingUnit());
				dataDetail.setUnitAccount(extractPayCommonData.getQuiterBillingUnitAccount());
				dataDetail.setUnitAccountBank(extractPayCommonData.getQuiterWbBank());
				BudgetBankAccount bankAccount = extractPayCommonData.getBankAccountMap().get(dataDetail.getEmpno() + "_" + dataDetail.getEmpname());
				if (bankAccount == null)
					throw new RuntimeException("离职人员【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】没有银行账户!");
				dataDetail.setPersonAccount(bankAccount);
				WbBanks wbBank = extractPayCommonData.getBanksMap().get(bankAccount.getBranchcode());
				if (wbBank == null)
					throw new RuntimeException("离职人员【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】银行信息错误!");
				dataDetail.setPersonAccountBank(wbBank);
			} else if (extractPayCommonData.getUserMap().get(dataDetail.getEmpno()).getStatus().compareTo(BigDecimal.ZERO) == 1) {
				if (salaryMap == null)
					throw new RuntimeException("获取不到员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】的工资信息");
				//工资发放单位outkey
				String companyidOutKey = salaryMap.get("salarycompanyid").toString();
				List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit -> companyidOutKey.equals(billingunit.getOutKey())).collect(Collectors.toList());
				if (billingUnitList.isEmpty())
					throw new RuntimeException("找不到员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】的工资发放单位,outkey为【" + companyidOutKey + "】");
				if (billingUnitList.size() > 1)
					throw new RuntimeException("员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】的工资发放单位有多个,outkey为【" + companyidOutKey + "】");
				BudgetBillingUnit billingUnit = billingUnitList.get(0);
				dataDetail.setBillingUnit(billingUnit);

				List<BudgetBillingUnitAccount> unitAccountList = extractPayCommonData.getBillingAccountMap().get(billingUnit.getId());
				/**
				 * 排序 先默认再排序号大者优先
				 */
				Comparator<BudgetBillingUnitAccount> c1 = (e1, e2) -> Boolean.compare(e2.getDefaultflag(), e1.getDefaultflag());
				Comparator<BudgetBillingUnitAccount> c2 = (e1, e2) -> Integer.compare(e2.getOrderno(), e1.getOrderno());
				unitAccountList = unitAccountList.stream().sorted(c1.thenComparing(c2)).collect(Collectors.toList());
				if (unitAccountList.isEmpty())
					throw new RuntimeException("工资发放单位【" + billingUnit.getName() + "】没有单位账户");
				BudgetBillingUnitAccount billingUnitAccount = unitAccountList.get(0);
				dataDetail.setUnitAccount(billingUnitAccount);
				WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(billingUnitAccount.getBranchcode());
				if (billingUnitBank == null) throw new RuntimeException("工资发放单位【" + billingUnit.getName() + "】银行信息错误!");
				dataDetail.setUnitAccountBank(billingUnitBank);
				BudgetBankAccount bankAccount = extractPayCommonData.getBankAccountMap().get(dataDetail.getEmpno() + "_" + dataDetail.getEmpname());
				if (bankAccount == null)
					throw new RuntimeException("员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】没有银行账户!");
				dataDetail.setPersonAccount(bankAccount);
				WbBanks wbBank = extractPayCommonData.getBanksMap().get(bankAccount.getBranchcode());
				if (wbBank == null)
					throw new RuntimeException("员工【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】银行信息错误!");
				dataDetail.setPersonAccountBank(wbBank);
			}
		} else {
			/**
			 * 外部人员默认发放单位信息
			 */
			dataDetail.setBillingUnit(extractPayCommonData.getOuterBillingUnit());
			dataDetail.setUnitAccount(extractPayCommonData.getOuterBillingUnitAccount());
			dataDetail.setUnitAccountBank(extractPayCommonData.getOuterWbBank());

			BudgetExtractOuterperson outperson = extractPayCommonData.getOutPersonMap().get(dataDetail.getIdnumber());
			if (outperson == null)
				throw new RuntimeException("外部人员【" + dataDetail.getEmpname() + "(" + dataDetail.getEmpno() + ")】不存在!");
			//如果有自定义的发放单位
			if (outperson.getBudgetbillingunitid() != null) {

				List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit -> billingunit.getId().equals(outperson.getBudgetbillingunitid())).collect(Collectors.toList());
				if (billingUnitList.isEmpty())
					throw new RuntimeException("找不到外部人员【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】的工资发放单位");
				BudgetBillingUnit billingUnit = billingUnitList.get(0);
				dataDetail.setBillingUnit(billingUnit);

				List<BudgetBillingUnitAccount> unitAccountList = extractPayCommonData.getBillingAccountMap().get(billingUnit.getId());
				if (unitAccountList == null || unitAccountList.isEmpty())
					throw new RuntimeException("工资发放单位【" + billingUnit.getName() + "】没有单位账户");
				/**
				 * 排序 先默认再排序号大者优先
				 */
				Comparator<BudgetBillingUnitAccount> c1 = (e1, e2) -> Boolean.compare(e2.getDefaultflag(), e1.getDefaultflag());
				Comparator<BudgetBillingUnitAccount> c2 = (e1, e2) -> Integer.compare(e2.getOrderno(), e1.getOrderno());
				unitAccountList = unitAccountList.stream().sorted(c1.thenComparing(c2)).collect(Collectors.toList());
				if (unitAccountList.isEmpty())
					throw new RuntimeException("工资发放单位【" + billingUnit.getName() + "】没有单位账户");
				BudgetBillingUnitAccount billingUnitAccount = unitAccountList.get(0);
				dataDetail.setUnitAccount(billingUnitAccount);

				WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(billingUnitAccount.getBranchcode());
				if (billingUnitBank == null) throw new RuntimeException("工资发放单位【" + billingUnit.getName() + "】银行信息错误!");
				dataDetail.setUnitAccountBank(billingUnitBank);

			}
			WbBanks billingUnitBank = extractPayCommonData.getBanksMap().get(outperson.getBranchcode());
			if (billingUnitBank == null)
				throw new RuntimeException("外部人员【" + dataDetail.getEmpno() + "(" + dataDetail.getEmpname() + ")】银行信息错误!");
			dataDetail.setOuterPersonAccount(dataDetail.createOuterPersonAccountData(outperson.getBankaccount(), outperson.getBranchcode(), billingUnitBank.getBankName(), billingUnitBank.getSubBranchName()));
		}

		if (dataDetail.getIsQuit()) return dataDetail;

		BudgetExtractpayRule extractpayRule = extractPayCommonData.getPayRuleList().stream().filter(e -> ("," + e.getBillunitids() + ",").contains("," + dataDetail.getBillingUnit().getId().toString() + ",")).findFirst().orElse(null);
		if (extractpayRule == null)
			throw new RuntimeException("工资发放单位【" + dataDetail.getBillingUnit().getName() + "】没有设置发放规则");
		dataDetail.setPayRule(extractpayRule);

		if (extractpayRule.getPersonunitid() == null)
			throw new RuntimeException("工资发放单位【" + dataDetail.getBillingUnit().getName() + "】发放规则没有设置避税发放单位账户！");
		Long personunitid = extractpayRule.getPersonunitid(); //避税发放单位账户

		BudgetBillingUnitAccount avoidAccount = extractPayCommonData.getBillingAccountList().stream().filter(e -> e.getId().toString().equals(personunitid.toString())).findFirst().orElse(null);

		//List<BudgetBillingUnit> billingUnitList = extractPayCommonData.getAllBillingUnitList().stream().filter(billingunit->billingunit.getId().equals(extractpayRule.getPersonunitid())).collect(Collectors.toList());				
		if (avoidAccount == null) throw new RuntimeException("提成发放规则【" + extractpayRule.getName() + ")】找不到避税发放单位账户");
		BudgetBillingUnit avoidBillingUnit = extractPayCommonData.getAllBillingUnitList().stream().filter(e -> e.getId().toString().equals(avoidAccount.getBillingunitid().toString())).findFirst().get();
		dataDetail.setAvoidBillingUnit(avoidBillingUnit);
		dataDetail.setAvoidUnitAccount(avoidAccount);
		WbBanks avoidBillingUnitBank = extractPayCommonData.getBanksMap().get(avoidAccount.getBranchcode());
		if (avoidBillingUnitBank == null)
			throw new RuntimeException("工资发放单位【" + avoidBillingUnit.getName() + "】银行信息错误!");
		dataDetail.setAvoidUnitAccountBank(avoidBillingUnitBank);


		BudgetExtractquotaRule extractquotaRule = extractPayCommonData.getQuotaruleList().stream().filter(e -> ("," + e.getBillunitids() + ",").contains("," + dataDetail.getBillingUnit().getId().toString() + ",")).findFirst().orElse(null);
		if (extractquotaRule == null)
			throw new RuntimeException("工资发放单位【" + dataDetail.getBillingUnit().getName() + "】没有设置限额规则");
		dataDetail.setQuotaRule(extractquotaRule);
		Map<Long, List<BudgetExtractpaydetail>> map = extractPayCommonData.getAgoPayDetailMap().get(dataDetail.getIdnumber());
		List<BudgetExtractpaydetail> curBillingUnitPaydetail = map == null ? Lists.newArrayList() : map.get(dataDetail.getBillingUnit().getId());
		dataDetail.setCurBillUnitPayDetailList(curBillingUnitPaydetail == null ? new ArrayList<>() : curBillingUnitPaydetail);

		//当前发放人员所关联的外部人员
		List<BudgetExtractOuterperson> refOuterPersonList = new ArrayList<>();
		extractPayCommonData.getOutPersonMap().forEach((idnumber, outperson) -> {
			if (StringUtils.isNotBlank(outperson.getReferidnumber()) && outperson.getReferidnumber().equals(dataDetail.getIdnumber()))
				refOuterPersonList.add(outperson);
		});
		;
		//按排序号排序。优先发排序号高的
		List<BudgetExtractOuterperson> sortEdRefOuterPersonList = refOuterPersonList.stream().sorted(Comparator.comparing(BudgetExtractOuterperson::getOrderno).reversed()).collect(Collectors.toList());
		dataDetail.setRefOuterPersonList(sortEdRefOuterPersonList);
		return dataDetail;
	}

	/**
	 * 重新计算时清理数据
	 *
	 * @param extractBatch
	 */
	private void clearaCalculatedData(String extractBatch, String empno) {
		//当前提成批次的计税明细
		QueryWrapper<BudgetExtractpaydetail> queryWrapper = new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", extractBatch);
		if (StringUtils.isNotBlank(empno)) queryWrapper.eq("empno", empno);
		List<BudgetExtractpaydetail> curBatchPaydetailList = this.payDetailMapper.selectList(queryWrapper);
		if (curBatchPaydetailList.isEmpty()) return;
		List<Long> paydetailIdList = curBatchPaydetailList.stream().map(BudgetExtractpaydetail::getId).collect(Collectors.toList());
		//当前提成批次的发放明细
		List<BudgetExtractpayment> curBatchPaymentList = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", paydetailIdList));

		List<BudgetPaymoney> paymoneyList = paymoneyMapper.selectList(new QueryWrapper<BudgetPaymoney>().in("paymoneyobjectid", curBatchPaymentList.stream().map(BudgetExtractpayment::getId).collect(Collectors.toList())).eq("paymoneytype", PaymoneyTypeEnum.EXTRACT_PAY.getType()));
		//状态不为等待付款的付款单
		List<BudgetPaymoney> pmList = paymoneyList.stream().filter(e -> e.getPaymoneystatus().intValue() > PaymoneyStatusEnum.RECEIVE_PAY.getType()).collect(Collectors.toList());
		if (!pmList.isEmpty()) {
			String errormsg = "计算失败！";
			for (int i = 0; i < pmList.size(); i++) {
				BudgetPaymoney bpm = pmList.get(i);
				String status = bpm.getPaymoneystatus().intValue() == PaymoneyStatusEnum.RECEIVE_PAY.getType() ? "接收付款" : bpm.getPaymoneystatus().intValue() == PaymoneyStatusEnum.PAYING.getType() ? "正在付款" : "已经付款";
				errormsg = errormsg + "付款单【" + bpm.getPaymoneycode() + "】状态: " + status + "】";
				if (i < pmList.size() - 1) errormsg = errormsg + "\n";
			}
			throw new RuntimeException(errormsg);
		}

		if (!CollectionUtils.isEmpty(paymoneyList)) {
			List<Long> paymoneyIdList = paymoneyList.stream().map(BudgetPaymoney::getId).collect(Collectors.toList());
			codeMapper.delete(new QueryWrapper<BudgetPaymoneycode>().in("paymoneyid", paymoneyIdList));
			paymoneyMapper.deleteBatchIds(paymoneyIdList);
		}
		paymentMapper.deleteBatchIds(curBatchPaymentList.stream().map(BudgetExtractpayment::getId).collect(Collectors.toList()));
		payDetailMapper.deleteBatchIds(paydetailIdList);
		QueryWrapper<BudgetExtractgrantlog> wrapper = new QueryWrapper<BudgetExtractgrantlog>().eq("extractmonth", extractBatch);
		if (StringUtils.isNotBlank(empno)) wrapper.eq("empno", empno);
		grantLogMapper.delete(wrapper);

	}

	/**
	 * 获取工资信息
	 *
	 * @param salaryMonth
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getSalaryMsg(String salaryMonth) throws Exception {
		String result = "";
		try {
			result = HttpClientTool.getRequest(String.format("http://127.0.0.1:9597/api/extractInfo/getSalary?salaryMonth=%s", salaryMonth));
			//result = HttpClientTool.getRequest(String.format("http://ys.jtyjy.com/api/extractInfo/getSalary?salaryMonth=%s", salaryMonth));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException("获取" + salaryMonth + "的工资失败!错误详情：" + e.getMessage());
		}
		HashMap hashMap = JSON.parseObject(result, HashMap.class);
		if (hashMap.get("code").toString().equals("1"))
			throw new RuntimeException("获取" + salaryMonth + "的工资失败!错误详情：" + hashMap.get("msg").toString());

		Map<String, Object> map = (Map<String, Object>) hashMap.get("data");
		return map;
	}

	/**
	 * 填充通用数据
	 *
	 * @param extractBatch
	 * @param curBatchExtractDetailList
	 * @return
	 * @throws Exception
	 */
	private ExtractPayCommonData populateCommonData(String extractBatch, List<BudgetExtractdetail> curBatchExtractDetailList) throws Exception {

		ExtractPayCommonData commonData = new ExtractPayCommonData();

		Integer year = Integer.valueOf(extractBatch.substring(0, 4));
		//当前年的开始提成批次
		String curYearStartExtractBatch = year + "0100";
		//当前年的结束提成批次
		String curYearEndExtractBatch = year + "1299";

		commonData.setCurYearStartExtractBatch(curYearStartExtractBatch);
		commonData.setCurYearEndExtractBatch(curYearEndExtractBatch);
		commonData.setCurExtractBatch(extractBatch);

		/**
		 * 银行及开票单位
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
		 * 填充外部人员发放单位
		 */
		populateOuterPayBillingUnitMsg(commonData);
		/**
		 * 填充离职人员发放单位
		 */
		populateQuiterPayBillingUnitMsg(commonData);
		//获取工资信息
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
		 * 银行账号
		 */
		Map<String, BudgetBankAccount> bankAccountMap = this.bankAccountMapper.selectList(new QueryWrapper<BudgetBankAccount>().eq("stopflag", 0)).stream().collect(Collectors.toMap(e -> {
					//return e.getCode()+"_"+e.getAccountname();
					return e.getCode() + "_" + e.getPname();
				}, e -> e, (e1, e2) ->
						//取排序号高的
						Integer.compare(e1.getOrderno() == null ? 0 : e1.getOrderno(), e2.getOrderno() == null ? 0 : e2.getOrderno()) == 1 ? e1 : e2
		));
		commonData.setBankAccountMap(bankAccountMap);

		Map<String, WbUser> userMap = userCache.EMPNO_USER_MAP;//this.userMapper.selectList(null).stream().collect(Collectors.toMap(WbUser::getUserName, e -> e));
		commonData.setUserMap(userMap);

		Map<String, BudgetExtractOuterperson> outPersonMap = this.outPersonMapper.selectList(new QueryWrapper<BudgetExtractOuterperson>().eq("stopflag", 0)).stream().collect(Collectors.toMap(e -> e.getIdnumber(), e -> e));
		commonData.setOutPersonMap(outPersonMap);

		/**
		 * 外部人员起征点(设置默认为9000)
		 */
		TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE)
				.eq("dm", OUTERTHRESHOLD));
		commonData.setOuterThreshold(StringUtils.isBlank(dm.getDmValue()) ? new BigDecimal("9000") : new BigDecimal(dm.getDmValue()));

		/**
		 * 外部人员标准额外税
		 */
		TabDm dm1 = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_CAL_DM_TYPE)
				.eq("dm", OUTEREXTRATAX));
		if (dm1 == null || StringUtils.isBlank(dm1.getDmValue())) throw new RuntimeException("计算失败！请先添加外部人员标准额外税！");
		commonData.setOuterOrinalExtraTax(new BigDecimal(dm1.getDmValue()));

		/**
		 * 判断是否需要冲借款
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
			 * 获取公司员工借款数据
			 */
			List<String> empNoList = curBatchExtractDetailList.stream().filter(e -> e.getIscompanyemp()).map(e -> e.getEmpno()).collect(Collectors.toList());
			Map<String, List<BudgetLendmoney>> empno2LendmoneyMap = getLendmoneyMap(empNoList);
			commonData.setEmpno2LendmoneyMap(empno2LendmoneyMap);
		}


		List<String> idnumberList = curBatchExtractDetailList.stream().map(BudgetExtractdetail::getIdnumber).collect(Collectors.toList());

		//之前的计税明细
		List<BudgetExtractpaydetail> agoPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().in("idnumber", idnumberList)
				.lt("extractmonth", commonData.getCurExtractBatch())
				.ge("extractmonth", commonData.getCurYearStartExtractBatch()));
		/**
		 * 根据身份证获取每个人每个工资发位的计税明细。
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
		 * 提成费用发放人员信息
		 */
		Map<String, List<BudgetExtractFeePayDetailBeforeCal>> feePayDetailMap = extractFeePayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>().eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractBatch)).stream().collect(Collectors.groupingBy(BudgetExtractFeePayDetailBeforeCal::getEmpNo));
		commonData.setFeePayEmpMap(feePayDetailMap);
		return commonData;
	}

	/**
	 * 获取可以还的所有借款单
	 * （除日常，临时，合同，非合同）
	 *
	 * @param empNoList
	 * @return
	 */
	private Map<String, List<BudgetLendmoney>> getLendmoneyMap(List<String> empNoList) {
		if (empNoList == null || empNoList.isEmpty()) return new HashMap<>();
		//提成可冲的借款类型

		ArrayList<Integer> lendTypeList = Lists.newArrayList(LendTypeEnum.LEND_TYPE_11.getType(),
				LendTypeEnum.LEND_TYPE_12.getType(),
				LendTypeEnum.LEND_TYPE_13.getType(),
				LendTypeEnum.LEND_TYPE_14.getType(),
				LendTypeEnum.LEND_TYPE_15.getType(),
				LendTypeEnum.LEND_TYPE_16.getType());
		//当前提成可冲的借款
		List<BudgetLendmoney> lendMoneyList = this.lendmoneyMapper.selectList(new QueryWrapper<BudgetLendmoney>().eq("deleteflag", 0)
				.in("lendtype", lendTypeList)
				.in("empno", empNoList)
				.eq("effectflag", 1)
				.apply("lendmoney-repaidmoney+interestmoney-repaidinterestmoney>0"));
		/**
		 * 获取被报销单锁定的借款（需要过滤掉）
		 */
		List<Long> lockedLendmoneyIdList = this.lendmoneyUselogMapper.selectList(new QueryWrapper<BudgetLendmoneyUselog>().eq("useflag", 1)).stream().map(BudgetLendmoneyUselog::getLendmoneyid).collect(Collectors.toList());

		Map<String, List<BudgetLendmoney>> empno2LendmoneyMap = lendMoneyList.stream().filter(e -> !lockedLendmoneyIdList.contains(e.getId())).collect(Collectors.groupingBy(BudgetLendmoney::getEmpno));
		return empno2LendmoneyMap;
	}

	/**
	 * 填充离职人员发放单位
	 *
	 * @param commonData
	 */
	private void populateQuiterPayBillingUnitMsg(ExtractPayCommonData commonData) {
		List<TabDm> tabList = dmMapper.selectList(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_PAY_DM_TYPE));
		TabDm td = tabList.stream().filter(dm -> dm.getDm().equals(QUIT_PAYUNIT)).findFirst().orElse(null);
		if (Objects.isNull(td) || StringUtils.isBlank(td.getDmValue()))
			throw new RuntimeException("计算失败!请联系预算管理员添加离职人员发放单位");
		List<String> quiterUnitIdList = Arrays.asList(td.getDmValue().split(","));
		List<BudgetBillingUnit> billUnitList = commonData.getAllBillingUnitList().stream().filter(e -> quiterUnitIdList.contains(e.getId().toString())).collect(Collectors.toList());
		if (billUnitList.isEmpty()) throw new RuntimeException("计算失败!请联系预算管理员添加离职人员发放单位");
		Random random = new Random();
		int index = random.nextInt(billUnitList.size());
		/**
		 * 随机取一个发放单位
		 */
		BudgetBillingUnit quiterBillingUnit = billUnitList.get(index);
		commonData.setQuiterBillingUnit(quiterBillingUnit);
		//获取当前发放单位的账户
		List<BudgetBillingUnitAccount> quiterBillingUnitAccoutList = commonData.getBillingAccountMap().get(quiterBillingUnit.getId()).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(quiterBillingUnitAccoutList))
			throw new RuntimeException("计算失败!开票单位【" + quiterBillingUnit.getName() + "】下无账户");
		BudgetBillingUnitAccount quiterBillingUnitAccout = quiterBillingUnitAccoutList.get(0);
		commonData.setQuiterBillingUnitAccount(quiterBillingUnitAccout);

		String branchcode = quiterBillingUnitAccout.getBranchcode();
		WbBanks wbBank = commonData.getBanksMap().get(branchcode);
		if (wbBank == null)
			throw new RuntimeException("计算失败!开票单位【" + quiterBillingUnit.getName() + "】下账户【" + quiterBillingUnitAccout.getBankaccount() + "】开户行信息错误");
		commonData.setQuiterWbBank(wbBank);
	}


	/**
	 * 填充外部人员发放单位
	 *
	 * @param commonData
	 */
	private void populateOuterPayBillingUnitMsg(ExtractPayCommonData commonData) {
		List<TabDm> tabList = dmMapper.selectList(new QueryWrapper<TabDm>().eq("dm_type", EXTRACT_PAY_DM_TYPE));
		TabDm td = tabList.stream().filter(dm -> dm.getDm().equals(OUTER_PAYUNIT)).findFirst().orElse(null);
		if (Objects.isNull(td) || StringUtils.isBlank(td.getDmValue()))
			throw new RuntimeException("计算失败!请联系预算管理员添加外部人员发放单位");

		List<String> billUnitIdList = Arrays.asList(td.getDmValue().split(","));
		/**
		 * 获取外部人员发放单位
		 */

		List<BudgetBillingUnit> billUnitList = commonData.getAllBillingUnitList().stream().filter(e -> billUnitIdList.contains(e.getId().toString())).collect(Collectors.toList());
		if (billUnitList.isEmpty()) throw new RuntimeException("计算失败!请联系预算管理员添加外部人员发放单位");
		//Random random = new Random();
		//int index = random.nextInt(billUnitList.size());
		/**
		 * 随机取一个发放单位
		 */
		//BudgetBillingUnit outBillingUnit = billUnitList.get(index);
		//取第一个
		BudgetBillingUnit outBillingUnit = billUnitList.get(0);
		commonData.setOuterBillingUnit(outBillingUnit);
		//获取当前发放单位的账户
		List<BudgetBillingUnitAccount> outBillingUnitAccoutList = commonData.getBillingAccountMap().get(outBillingUnit.getId()).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed().thenComparing(Comparator.comparing(BudgetBillingUnitAccount::getOrderno).reversed())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(outBillingUnitAccoutList))
			throw new RuntimeException("计算失败!开票单位【" + outBillingUnit.getName() + "】下无账户");
		BudgetBillingUnitAccount outBillingUnitAccout = outBillingUnitAccoutList.get(0);
		commonData.setOuterBillingUnitAccount(outBillingUnitAccout);

		String branchcode = outBillingUnitAccout.getBranchcode();
		WbBanks wbBank = commonData.getBanksMap().get(branchcode);
		if (wbBank == null)
			throw new RuntimeException("计算失败!开票单位【" + outBillingUnit.getName() + "】下账户【" + outBillingUnitAccout.getBankaccount() + "】开户行信息错误");
		commonData.setOuterWbBank(wbBank);
	}

	/**
	 * 校验当前提成批次是否可以计算
	 * 不允许计算的条件如下：
	 * 1.当前提成批次下存在未审核通过
	 * 2.之前的提成存在未计算。
	 * 3.之前的提成已计算，但是未设置超额处理
	 * 4.之后的提成已计算
	 *
	 * @param curExtractBatch        当前提成批次
	 * @param curBatchExtractSumList
	 */
	private void validateIsCanCalculate(String curExtractBatch, List<BudgetExtractsum> curBatchExtractSumList) {
		if (CollectionUtils.isEmpty(curBatchExtractSumList))
			throw new RuntimeException("提成批次【" + curExtractBatch + "】下无提成可计算！");
		/**
		 * 1.当前提成批次下存在未审核通过	,不允许计算
		 */
		//获取当前批次下未审核通过的提成单号
		String curBatchUnApprovedExtractCodes = curBatchExtractSumList.stream().filter(sum -> sum.getStatus() < ExtractStatusEnum.APPROVED.getType()).map(BudgetExtractsum::getCode).collect(Collectors.joining(","));
		if (StringUtils.isNotBlank(curBatchUnApprovedExtractCodes))
			throw new RuntimeException("操作失败！提成批次【" + curExtractBatch + "】存在未审核通过的提成单号【" + curBatchUnApprovedExtractCodes + "】");

		/**
		 * 2.之前的提成存在未计算。不允许计算
		 */
		Integer year = Integer.valueOf(curExtractBatch.substring(0, 4));
		//当前年的开始提成批次
		String curYearStartExtractBatch = year + "0100";
		//当前年的结束提成批次
		String curYearEndExtractBatch = year + "1299";
		//所有的提成
		List<BudgetExtractsum> allSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("deleteflag", "0"));
		//获取当前年当前批次前的所有提成。
		List<BudgetExtractsum> agoExtractSumList = allSumList.stream().filter(sum -> Integer.valueOf(sum.getExtractmonth()) > Integer.valueOf(curYearStartExtractBatch)
				&& Integer.valueOf(sum.getExtractmonth()) < Integer.valueOf(curExtractBatch)).collect(Collectors.toList());
		//获取当前年当前批次前的所有(未计算)的提成。
		List<BudgetExtractsum> agoUnCalculateExtractSumList = agoExtractSumList.stream().filter(sum -> sum.getStatus() < ExtractStatusEnum.CALCULATION_COMPLETE.getType()).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(agoUnCalculateExtractSumList)) {
			//未计算的所有批次
			String agoUnCalculateExtractBatchs = agoUnCalculateExtractSumList.stream().map(BudgetExtractsum::getExtractmonth).distinct().collect(Collectors.joining(","));
			throw new RuntimeException("操作失败！提成批次【" + agoUnCalculateExtractBatchs + "】还未计算发放！");
		}

		/**
		 * 3.之前的提成已计算，但是未设置超额处理  不允许计算
		 */
		//获取当前年当前批次前的所有提成明细
		List<Long> agoExtractSumIdList = agoExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		List<BudgetExtractdetail> agoExtractDeailList = new ArrayList<>();
		if (!agoExtractSumIdList.isEmpty()) {
			agoExtractDeailList = this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", agoExtractSumIdList)
					.eq("deleteflag", 0));
		}
		StringBuffer errormsg = new StringBuffer();
		//遍历超额未处理的提成明细
		agoExtractDeailList.stream().filter(e -> e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0 && e.getHandleflag() != null && !e.getHandleflag())
				.collect(Collectors.groupingBy(e -> e.getExtractsumid()))
				.forEach((sumid, extractDetailList) -> {
					String code = agoExtractSumList.stream().filter(e -> e.getId().equals(sumid)).findFirst().get().getCode();
					if (!extractDetailList.isEmpty()) {
						String empnos = extractDetailList.stream().map(e -> e.getEmpno()).collect(Collectors.joining(","));
						errormsg.append("提成单号【" + code + "】中:工号【" + empnos + "】还未设置超额发放<br>");
					}
				});
		;
		if (StringUtils.isNotBlank(errormsg.toString())) throw new RuntimeException(errormsg.toString());

		/**
		 * 4.之后的提成已计算  不允许计算
		 */
		List<BudgetExtractsum> lateExtractSumList = allSumList.stream().filter(e -> Integer.valueOf(e.getExtractmonth()).intValue() > Integer.valueOf(curExtractBatch).intValue()
				&& Integer.valueOf(e.getExtractmonth()).intValue() < Integer.valueOf(curYearEndExtractBatch).intValue()).collect(Collectors.toList());
		if (!lateExtractSumList.isEmpty()) {
			String extractmonths = lateExtractSumList.stream().filter(e -> e.getStatus().intValue() >= 3).map(e -> e.getExtractmonth()).distinct().collect(Collectors.joining(","));
			if (StringUtils.isNotEmpty(extractmonths))
				throw new RuntimeException("操作失败！提成批次【" + extractmonths + "】已计算发放!");
		}
	}


	/**
	 * 获取提成超额明细
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<BudgetExtractdetail> getExtractExcessDetailByExtractmonth(String extractBatch) {
		List<BudgetExtractsum> extractSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0).eq("status", ExtractStatusEnum.CALCULATION_COMPLETE.getType()));
		if (extractSumList.isEmpty()) throw new RuntimeException("提成批次【" + extractBatch + "】无超额记录可处理!");
		List<Long> sumIds = extractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());

		return this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().in("extractsumid", sumIds).eq("deleteflag", 0).eq("excesstype", ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType()).eq("handleflag", 0).gt("excessmoney", 0));
	}

	/**
	 * 获取工号与工资单位的映射
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
	 * 通过提成批次获取所有的提成明细
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<BudgetExtractdetail> getExtractDetailByExtractmonth(String extractBatch) {
		//获取当前批次下所有的提成
		List<BudgetExtractsum> curBatchExtractSumList = this.budgetExtractsumMapper.selectList(new QueryWrapper<BudgetExtractsum>().eq("extractmonth", extractBatch).eq("deleteflag", 0));

		List<Long> curExtractSumIdList = curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList());
		//当前批次的所有提成明细
		return this.extractDetailMapper.selectList(new QueryWrapper<BudgetExtractdetail>().eq("deleteflag", 0).in("extractsumid", curExtractSumIdList));
	}

	/**
	 * 获取提成扣款明细
	 *
	 * @param repaymoneyid 还款单Id
	 * @return
	 */
	public List<Map<String, Object>> getRepaymoneymsg(Long repaymoneyid) {
		return this.budgetExtractsumMapper.getRepaymoneymsg(repaymoneyid);
	}

	/**
	 * 获取陈彩莲发放明细
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<ExtractCCLPayExcelData> getCCLPayDetailList(String extractBatch) {
		return this.budgetExtractsumMapper.getCCLPayDetailList(extractBatch);
	}


	/**
	 * 获取提成报税明细
	 *
	 * @param extractMonth     当前提成月
	 * @param lastSalaryMsgMap 当月之前的工资明细
	 * @return
	 */
	public List<ExtractIncomeExcelData> getExtractIncomeDetails(String extractMonth,
	                                                            Map<Integer, Map<String, Object>> lastSalaryMsgMap) {

		List<ExtractIncomeExcelData> resultList = new ArrayList<>();

		String year = extractMonth.substring(0, 4);
		List<BudgetExtractArrears> curYearAllExtractArrearList = this.extractArrearsMapper.selectList(new QueryWrapper<BudgetExtractArrears>().le("month", extractMonth).ge("month", year + "01"));
		/**
		 * 计税
		 */
		calTax(curYearAllExtractArrearList);

		//当前年所有的提成台账 (key为提成月份)
		Map<String, List<BudgetExtractArrears>> curYearAllExtractArrearMap = curYearAllExtractArrearList.stream().collect(Collectors.groupingBy(BudgetExtractArrears::getMonth));

		//获取当月以及之前的提成台账
		//List<BudgetExtractArrears> curYearExtractArrearList = curYearAllExtractArrearList.stream().filter(e->Integer.valueOf(e.getMonth())<=Integer.valueOf(extractMonth)).collect(Collectors.toList());


		List<BudgetExtractpaydetail> curExtractPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().likeRight("extractmonth", extractMonth));
		if (CollectionUtils.isEmpty(curExtractPayDetails)) {
			return new ArrayList<>();
		}
		Map<Long, BudgetExtractpaydetail> payDetailMap = curExtractPayDetails.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getId, e -> e, (e1, e2) -> e1));
		//所有的发放记录
		Map<Long, BudgetExtractpayment> paymentMap = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", curExtractPayDetails.stream().map(e -> e.getId()).collect(Collectors.toList()))).stream().collect(Collectors.toMap(BudgetExtractpayment::getBudgetextractpaydetailid, e -> e, (e1, e2) -> e1));

		List<BudgetExtractArrears> curBudgetExtractArrears = curYearAllExtractArrearMap.get(extractMonth);

		/**
		 * 存在一个业务员在相同提成月份下在不同单位发放的情况
		 */
		curBudgetExtractArrears.stream().collect(Collectors.groupingBy(e -> e.getIdnumber() + "_" + e.getBunitid().toString())).forEach((key, arrears) -> {
			String idnumber = key.split("_")[0];
			String unitId = key.split("_")[1];
			//获取当前人当前发放单位的计税明细
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
			 * 获取以前的收入明细
			 */
			Map<String, Object> agoIncomeMap = getLastIncome(curPayDetail, unitId, extractMonth, curYearAllExtractArrearMap, lastSalaryMsgMap);
			BudgetExtractArrears curEmpUnitLastMonthArrear = (BudgetExtractArrears) agoIncomeMap.get("curEmpUnitLastMonthArrear");
			List<BudgetExtractArrears> agoArrears = (List<BudgetExtractArrears>) agoIncomeMap.get("agoArrears");

			//获取个税
			BigDecimal tax = getTax(agoArrears, extractArrears.getPayabletaxs());
			tax = tax.setScale(2, BigDecimal.ROUND_HALF_UP);
			//个税不体现负数
			tax = tax.compareTo(BigDecimal.ZERO) >= 0 ? tax : BigDecimal.ZERO;
			ed.setCurTax(tax);
			/**
			 * 获取本期收入。 本期收入-上期收入
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
	 * 获取个税
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
	 * 获取上期收入
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
			//获取当前人上个月当前单位发放的提成台账
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
			//如果当前月没有提成台账。需要手动创建一条虚拟数据。
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
			//获取目前法人公司实发提成（上月）
			BigDecimal inCompanyExtract = curYearAllExtractArrearMap.values().stream().flatMap(e -> e.stream()).filter(e -> Integer.valueOf(e.getMonth()) <= Integer.valueOf(lastmonth)
					&& e.getIdnumber().equals(curPayDetail.getIdnumber())
					&& e.getBunitid().toString().equals(unitId))
					.map(BudgetExtractArrears::getRealextract)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			arrears.setIncorporatedcompanylj(inCompanyExtract);
			//计算虚拟数据税
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
	 * 计税
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
	 * 根据code集合查询提成总额
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
	 * 显示提成二维码
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
	 * 重置计算结果
	 */
	public void reset(String extractBatch) {
		List<BudgetExtractsum> budgetExtractsums = budgetExtractsumMapper.selectList(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch));
		if (!CollectionUtils.isEmpty(budgetExtractsums)) {
			Integer status = budgetExtractsums.get(0).getStatus();
			if (status != ExtractStatusEnum.CALCULATION_COMPLETE.getType())
				throw new RuntimeException("操作失败！提成批次计算完成之后才允许撤回");
			clearaCalculatedData(extractBatch, null);
			extractFeePayDetailMapper.delete(new LambdaQueryWrapper<BudgetExtractFeePayDetailBeforeCal>().eq(BudgetExtractFeePayDetailBeforeCal::getExtractMonth, extractBatch));
			budgetExtractsums.stream().forEach(e -> {
				e.setStatus(ExtractStatusEnum.DRAFT.getType());
			});
			this.updateBatchById(budgetExtractsums);
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
		Boolean integer = NumberUtil.isInteger(trimZero("-1"));
		System.out.println(integer);
		System.out.println(trimZero("0"));
	}

	/**
	 * 获取提成支付申请单表头数据
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
	 * 获取提成支付申请表的明细数据
	 *
	 * @param extractBatch
	 * @return
	 */
	public List<ExtractPayApplyExcelData> getExtractExcelDetails(String extractBatch, Map<String, Object> heads) {
		List<Object> result = new ArrayList<>();
		Map<String, Object> params = new HashMap<>(1);
		params.put("extractmonth", extractBatch);
		List<ExtractPayDetailVO> payDetails = getPayDetailsByCondition(null, params);
		//所有的提成明细id
		List<String> extractDetailIds = payDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		List<BudgetExtractImportdetail> importDetails = new ArrayList<>();
		if (!CollectionUtils.isEmpty(extractDetailIds)) {
			//获取提成导入的明细
			importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().in(BudgetExtractImportdetail::getExtractdetailid, extractDetailIds));
		}
		List<ExtractPayApplyExcelData> payApplyExcelDatas = getExtractPayExcelDetails(importDetails, heads, payDetails);
		return payApplyExcelDatas;

	}

	/**
	 * 获取提成支付汇总表
	 */
	public List<ExtractPaySumExcelData> getExtractPaySumExcelDetails(String extractBatch) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("extractmonth", extractBatch);
		List<ExtractPayDetailVO> payDetails = getPayDetailsByCondition(null, params);
		//所有的提成明细id
		List<String> extractDetailIds = payDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		List<BudgetExtractImportdetail> importDetails = new ArrayList<>();
		if (!CollectionUtils.isEmpty(extractDetailIds)) {
			//获取提成导入的明细
			importDetails = extractImportDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractImportdetail>().in(BudgetExtractImportdetail::getExtractdetailid, extractDetailIds));
		}
		List<ExtractPaySumExcelData> paySumExcelDatas = getExtractPaySumExcelDatas(importDetails, payDetails);
		return paySumExcelDatas;
	}

	/**
	 * 获取提成支付汇总表数据
	 *
	 * @param importDetails 导入明细
	 * @param payDetails    发放明细
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
		//期间提成
		List<BudgetExtractImportdetail> qjExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.RETURN.value.equals(e.getExtractType())).collect(Collectors.toList());
		//扎账总提成
		List<BudgetExtractImportdetail> zzTotalExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.DRAFT.value.equals(e.getExtractType())).collect(Collectors.toList());
		//扎账后提成
		List<BudgetExtractImportdetail> zzAfterExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.VERIFYING.value.equals(e.getExtractType())).collect(Collectors.toList());
		//坏账明细
		List<BudgetExtractImportdetail> hzExtract = importDetails.stream().filter(e -> e.getExtractType() != null && ExtractTypeEnum.APPROVED.value.equals(e.getExtractType())).collect(Collectors.toList());

		packageExtractPaySumExcelData(qjExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.RETURN.value);
		packageExtractPaySumExcelData(zzTotalExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.DRAFT.value);
		packageExtractPaySumExcelData(zzAfterExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.VERIFYING.value);
		packageExtractPaySumExcelData(hzExtract, budgetExtractsumMap, result, yearPeriodMap, ExtractTypeEnum.APPROVED.value);
		return result;
	}

	private void packageExtractPaySumExcelData(List<BudgetExtractImportdetail> qjExtract, Map<Long, BudgetExtractsum> budgetExtractsumMap, List<ExtractPaySumExcelData> result, Map<Long, BudgetYearPeriod> yearPeriodMap, String type) {
		//以部门id分组
		qjExtract.stream().collect(Collectors.groupingBy(e -> budgetExtractsumMap.get(e.getExtractsumid()).getDeptid())).forEach((deptId, detailsByDeptIdList) -> {

			Long extractsumid = detailsByDeptIdList.get(0).getExtractsumid();
			String deptName = budgetExtractsumMap.get(extractsumid).getDeptname();
			//再以届别分组
			detailsByDeptIdList.stream().collect(Collectors.groupingBy(BudgetExtractImportdetail::getYearid)).forEach((yearid, detailsByDeptIdAndYearIdList) -> {

				BudgetYearPeriod budgetYearPeriod = yearPeriodMap.get(yearid);
				ExtractPaySumExcelData excelData = new ExtractPaySumExcelData();
				excelData.setExtractType(type);
				excelData.setDeptName(deptName);
				excelData.setYearPeriod(budgetYearPeriod.getPeriod());
				//申请提成 = 导入明细中的should_send_extract
				BigDecimal applyExtract = detailsByDeptIdAndYearIdList.stream().map(e -> e.getShouldSendExtract() == null ? BigDecimal.ZERO : e.getShouldSendExtract()).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setApplyExtract(applyExtract);
				//提成个税 = 个税+个税减免
				BigDecimal extractTax = detailsByDeptIdAndYearIdList.stream().map(e -> {
					BigDecimal tax = e.getTax() == null ? BigDecimal.ZERO : e.getTax();
					BigDecimal taxReduction = e.getTaxReduction() == null ? BigDecimal.ZERO : e.getTaxReduction();
					return tax.add(taxReduction);
				}).reduce(BigDecimal.ZERO, BigDecimal::add);
				excelData.setExtractTax(extractTax);
				//发票超额税金 = 发票超额税金+发票超额税金减免
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
	 * 获取提成支付申请数据
	 *
	 * @param importDetails 导入明细
	 * @param heads         表头数据
	 * @param payDetails    发放明细
	 * @return
	 */
	private List<ExtractPayApplyExcelData> getExtractPayExcelDetails(List<BudgetExtractImportdetail> importDetails, Map<String, Object> heads, List<ExtractPayDetailVO> payDetails) {
		List<ExtractPayApplyExcelData> payApplyExcelDatas = new ArrayList<>();
		//法人公司发放单位分组
		payDetails.stream().filter(e -> e.getBillingUnitId() != null).collect(Collectors.groupingBy(ExtractPayDetailVO::getBillingUnitId)).forEach((billingUnitId, curUnitPayDetails) -> {
			payApplyExcelDatas.add(packageExtractBillingPayApply(curUnitPayDetails, importDetails));
		});
		//陈彩莲
		payApplyExcelDatas.add(packageExtractAvoidPayApply(payDetails));
		//合计
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
	 * 组装提成支付申请明细数据(陈彩莲)
	 *
	 * @param payDetails 发放明细
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
	 * 组装提成支付申请明细数据(法人公司)
	 *
	 * @param curUnitPayDetails 当前法人公司发放明细
	 * @param importDetailMap   提成导入明细
	 * @return
	 */
	private ExtractPayApplyExcelData packageExtractBillingPayApply(List<ExtractPayDetailVO> curUnitPayDetails, List<BudgetExtractImportdetail> importDetails) {
		ExtractPayApplyExcelData data = new ExtractPayApplyExcelData();
		List<String> extractDetailIds = curUnitPayDetails.stream().flatMap(e -> Arrays.stream(e.getExtractdetailids().split(","))).collect(Collectors.toList());
		/**
		 * 获取当前发放单位下的导入明细
		 */
		List<BudgetExtractImportdetail> curUnitImportDetails = importDetails.stream().filter(e -> extractDetailIds.contains(e.getExtractdetailid().toString())).collect(Collectors.toList());
		data.setBillingUnitName(curUnitPayDetails.get(0).getBillingUnitname());
		//申请提成 = 导入明细中的should_send_extract
		BigDecimal applyExtract = curUnitImportDetails.stream().map(e -> e.getShouldSendExtract() == null ? BigDecimal.ZERO : e.getShouldSendExtract()).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setApplyExtract(applyExtract);
		//提成个税 = 个税+个税减免
		BigDecimal extractTax = curUnitImportDetails.stream().map(e -> {
			BigDecimal tax = e.getTax() == null ? BigDecimal.ZERO : e.getTax();
			BigDecimal taxReduction = e.getTaxReduction() == null ? BigDecimal.ZERO : e.getTaxReduction();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setExtractTax(extractTax);
		//发票超额税金 = 发票超额税金+发票超额税金减免
		BigDecimal invoiceExcessTax = curUnitImportDetails.stream().map(e -> {
			BigDecimal tax = e.getInvoiceExcessTax() == null ? BigDecimal.ZERO : e.getInvoiceExcessTax();
			BigDecimal taxReduction = e.getInvoiceExcessTaxReduction() == null ? BigDecimal.ZERO : e.getInvoiceExcessTaxReduction();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setInvoiceExcessTax(invoiceExcessTax);
		//费用发放 = 超额费用发放 + 费用发放
		BigDecimal feePay = curUnitPayDetails.stream().map(e -> {
			BigDecimal tax = e.getPayFee() == null ? BigDecimal.ZERO : e.getPayFee();
			BigDecimal taxReduction = e.getBeforeCalFee() == null ? BigDecimal.ZERO : e.getBeforeCalFee();
			return tax.add(taxReduction);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setFeePay(feePay);
		//法人公司发的提成
		BigDecimal extract = curUnitPayDetails.stream().map(e -> e.getBillingPaymoney() == null ? BigDecimal.ZERO : e.getBillingPaymoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
		data.setExtract(extract);

		//其他
		BigDecimal other = data.getApplyExtract().subtract(data.getExtract()).subtract(data.getExtractTax()).subtract(data.getInvoiceExcessTax()).subtract(data.getFeePay());
		data.setOther(other);
		return data;
	}

	/**
	 * 签收
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
	 * 获取提成签收日志
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
	 * 获取提成发放明细
	 *
	 * @param extractsum
	 * @param details
	 */
	public void exportExtractPaymentDetail(BudgetExtractsum extractsum, List<ExtractPaymentExcelData> details) {

		/**
		 * 获取当前批次所有的提成明细。
		 * 存在人员跨部门发提成的情况
		 */
		List<BudgetExtractdetail> allExtractDetails = this.getExtractDetailByExtractmonth(extractsum.getExtractmonth());
		//当前部门的提成明细
		List<BudgetExtractdetail> curExtractDetails = allExtractDetails.stream().filter(e -> e.getExtractsumid().equals(extractsum.getId())).collect(Collectors.toList());

		Map<Long, List<BudgetPaymoney>> paymoneyMap = paymoneyService.list(new QueryWrapper<BudgetPaymoney>().eq("paymoneytype", PaymoneyTypeEnum.EXTRACT_PAY.getType())).stream().collect(Collectors.groupingBy(e -> e.getPaymoneyobjectid()));

		/**
		 * 判断是否存在未设置超额的
		 */
		Long unHandleExtractCount = curExtractDetails.stream().filter(e -> e.getExcesstype() != null && e.getExcesstype().intValue() == ExtractExcessTypeEnum.EXCESS_NOFINISHED.getType() &&
				e.getExcessmoney() != null && e.getExcessmoney().compareTo(BigDecimal.ZERO) > 0
				&& e.getHandleflag() != null && !e.getHandleflag()).count();
		if (unHandleExtractCount > 0)
			throw new RuntimeException("导出失败！提成单号【" + extractsum.getCode() + "】中存在记录未进行超额发放。");

		List<BudgetExtractpaydetail> curExtractPayDetails = this.payDetailMapper.selectList(new QueryWrapper<BudgetExtractpaydetail>().eq("extractmonth", extractsum.getExtractmonth()));
		Map<Long, BudgetExtractpaydetail> payDetailMap = curExtractPayDetails.stream().collect(Collectors.toMap(BudgetExtractpaydetail::getId, e -> e, (e1, e2) -> e1));
		//所有的发放记录
		List<BudgetExtractpayment> paymentList = this.paymentMapper.selectList(new QueryWrapper<BudgetExtractpayment>().in("budgetextractpaydetailid", curExtractPayDetails.stream().map(e -> e.getId()).collect(Collectors.toList())));

		//Map<String, WbBanks> bankMap = this.bankMapper.selectList(null).stream().collect(Collectors.toMap(WbBanks::getSubBranchCode, e -> e, (e1, e2) -> e1));


		for (BudgetExtractpayment payment : paymentList) {
			ExtractPaymentExcelData ed = new ExtractPaymentExcelData();
			BudgetExtractpaydetail extractpaydetail = payDetailMap.get(payment.getBudgetextractpaydetailid());
			ed.setEmpNo(extractpaydetail.getEmpno());
			ed.setEmpName(extractpaydetail.getEmpname());

			/**
			 * update by minzhq 2022-1-18  修改跨部门业务员提成平摊的逻辑。（由保留２位有效数字改为取整数）
			 * 1.所有数据取整数（法人公司，费用发放，超额费用发放，陈彩莲发放）
			 * 2.法人公司的数据向下取整(最后一个做减法)
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
			if(totalCopeextract.compareTo(BigDecimal.ZERO)==0){
			}else{
				percent = extractDetail.getCopeextract().divide(totalCopeextract, 20, BigDecimal.ROUND_HALF_UP);
			}
			ed.setRealExtract(extractDetail.getCopeextract());
			ed.setConsotax(extractDetail.getConsotax());
			ed.setSalaryUnitName(payment.getBunitname1());

			BigDecimal paymentMoney1 = payment.getPaymoney1();
			BigDecimal salaryUnitPayMoney = getPercentMoney(paymentMoney1, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails,BigDecimal.ROUND_DOWN);
			ed.setSalaryUnitPayMoney(salaryUnitPayMoney);
			ed.setAvoidUnitName(payment.getBunitname2());
			BigDecimal paymentMoney2 = payment.getPaymoney2();
			BigDecimal avoidUnitPayMoney = getPercentMoney(paymentMoney2, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails,BigDecimal.ROUND_HALF_UP);
			ed.setAvoidUnitPayMoney(avoidUnitPayMoney);

			//超额费用发放
			BigDecimal excessPayfee = payment.getPayfee();
			BigDecimal fee1 = getPercentMoney(excessPayfee, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails,BigDecimal.ROUND_HALF_UP);

			//费用发放
			BigDecimal beforeCalFee = payment.getBeforeCalFee();
			BigDecimal fee2 = getPercentMoney(beforeCalFee, totalCopeextract, index, extractSize, percent, detailIds, allExtractDetails,BigDecimal.ROUND_HALF_UP);
			ed.setFee(fee1.add(fee2));

			ed.setBankAccount(payment.getBankaccount());
			ed.setBankName(payment.getBankaccountbranchname());
			ed.setOpenBank(payment.getBankaccountopenbank());

			WbBanks bank = bankCache.getBankByBranchCode(payment.getBankaccountbranchcode());
			if (bank == null) {
				List<BudgetPaymoney> budgetPaymonies = paymoneyMap.get(payment.getId());
				if (CollectionUtils.isEmpty(budgetPaymonies))
					throw new RuntimeException("导出失败。银联号信息错误。错误标记【" + payment.getBankaccountbranchcode() + "】");
				bank = bankCache.getBankByBranchCode(budgetPaymonies.get(0).getBankaccountbranchcode());
				if (bank == null) {
					throw new RuntimeException("导出失败。银联号信息错误。错误标记【" + payment.getBankaccountbranchcode() + "】");
				}
			}
			String province = bank.getProvince();
			String city = bank.getCity();
			if (province.contains("北京") || province.contains("上海") || province.contains("重庆") || province.contains("天津")) {
				//直辖市
				ed.setProvince(province.substring(0, 2));
				ed.setCity(province.substring(0, 2));
			} else {
				ed.setProvince(province.substring(0, 2));
				ed.setCity(city.substring(0, 2));
			}

			if (extractDetail.getRepaymoneyid() != null) {
				//还款明细
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

	private BigDecimal getPercentMoney(BigDecimal paymentMoney1, BigDecimal totalCopeextract, int index, int extractSize, BigDecimal percent, String[] detailIds, List<BudgetExtractdetail> allExtractDetails,int round) {
		BigDecimal salaryUnitPayMoney = BigDecimal.ZERO;
		if (paymentMoney1 != null) {
			if (totalCopeextract.compareTo(BigDecimal.ZERO) == 0) {
				salaryUnitPayMoney = paymentMoney1;
			} else {
				if (index < extractSize - 1) {
					//走比率
					salaryUnitPayMoney = percent.multiply(paymentMoney1).setScale(0, round);
				} else {
					BigDecimal money = BigDecimal.ZERO;
					//做减法
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
}



