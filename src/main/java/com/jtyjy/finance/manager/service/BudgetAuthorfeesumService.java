package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.cache.PersonCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.controller.authorfee.AuthorFeeController;
import com.jtyjy.finance.manager.controller.authorfee.calpay.AuthorFeeCalCommonData;
import com.jtyjy.finance.manager.controller.authorfee.calpay.AuthorFeeReimCommonData;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExcelDetail;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExcelHead;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExportExcelDetail;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeImportCommonData;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.easyexcel.AuthorFeeCalTaxDetailExcelData;
import com.jtyjy.finance.manager.enmus.AuthorFeeStatusEnum;
import com.jtyjy.finance.manager.enmus.ReimbursementFromEnmu;
import com.jtyjy.finance.manager.enmus.ReimbursementTypeEnmu;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.QRCodeUtil;
import com.jtyjy.finance.manager.vo.AuthorFeeDetailVO;
import com.jtyjy.finance.manager.vo.AuthorFeeMainVO;
import com.jtyjy.finance.manager.vo.AuthorFeePeriodNavigateTreeVO;
import com.jtyjy.finance.manager.vo.AuthorFeeReportVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class BudgetAuthorfeesumService extends DefaultBaseService<BudgetAuthorfeesumMapper, BudgetAuthorfeesum> implements ImportBaseInterface {

	public static final String TAX_TYPE_YES = "是";
	public static final String TAX_TYPE_NO = "否";
	public static final String AUTHOR_TYPE_INNER = "公司内部";
	public static final String AUTHOR_TYPE_OUTER = "公司外部";
	public static final String CONTRIBUTION_FEE = "稿费";
	public static final String EXTERNAL_AUDIT_FEE = "外审外包费";
	public static final String CONTRIBUTION_FEE_NEXT = "待摊-稿费";
	public static final String EXTERNAL_AUDIT_FEE_NEXT = "待摊-外审外包费";

	private static final String AUTHORPAYRULE = "AUTHORPAYRULE";
	private static final String CCLACCOUNT = "CCLACCOUNT";
	private static final String EDUCTIONBANKACCOUNT = "EDUCTIONBANKACCOUNT";
	private static final String NOTAXACCOUNT = "NOTAXACCOUNT";
	private static final String ReimburseGroupByUnit = "ReimburseGroupByUnit";
	private static final String SERVICE_FEE = "SERVICE_FEE";
	//二维码格式
	private static final String QRCODE_FORMAT = ".png";

	@Value("${file.temp.path}")
	private String file_temp_path;

	//扫描报销二维码页面
	@Value("${bx.qrcode.url}")
	private String bx_qrcode_url;

	private final static String SPLIT_SYMBOL = "&%";
	private final static String SPLIT_SYMBOL1 = "&%%";

	private final TabChangeLogMapper loggerMapper;

	@Autowired
	private BudgetYearPeriodMapper yearMapper;

	@Autowired
	private WbBanksMapper bankMapper;

	@Autowired
	private MessageSender sender;

	@Autowired
	private WbUserMapper userMapper;

	@Autowired
	private TabDmMapper dmMapper;


	@Autowired
	private BudgetAuthorfeesumMapper budgetAuthorfeesumMapper;

	@Autowired
	private BudgetAuthorfeedetailMapper budgetAuthorfeedetailMapper;

	@Autowired
	private BudgetAuthorfeedetailService detailService;

	@Autowired
	private TabProcedureService procedureService;

	@Autowired
	private BudgetAuthorfeeReportMapper feeReportMapper;

	@Autowired
	private BudgetAuthorfeedtlMergeMapper mergeMapper;

	@Autowired
	private BudgetAuthorfeedtlMergeService mergeService;

	@Autowired
	private BudgetSubjectMapper subjectMapper;

	@Autowired
	private BudgetReimbursementorderService reimbursementService;

	@Autowired
	private BudgetReimbursementorderMapper reimbursementMapper;

	@Autowired
	private BudgetAuthorfeepayRuleMapper payRuleMapper;

	@Autowired
	private BudgetAuthorfeepayRuledetailMapper payRuleDetailMapper;

	@Autowired
	private BudgetAuthorfeetaxRuleMapper taxRuleMapper;

	@Autowired
	private BudgetAuthorfeetaxRuledetailMapper taxRuleDetailMapper;

	@Autowired
	private BudgetBillingUnitMapper billingUnitMapper;

	@Autowired
	private BudgetBaseUnitMapper baseUnitMapper;

	@Autowired
	private BudgetBillingUnitAccountMapper billingUnitAccountMapper;

	@Autowired
	private BudgetMonthAgentMapper monthAgentMapper;

	@Autowired
	private BudgetAuthorMapper authorMapper;

	@Autowired
	private BudgetUnitMapper unitMapper;

	@Autowired
	private DistributedNumber distributedNumber;

	@Autowired
	private BudgetReimbursementorderTransMapper transMapper;

	@Autowired
	private BudgetMonthSubjectMapper monthSubjectMapper;

	@Autowired
	private BudgetYearAgentMapper yearAgentMapper;

	@Autowired
	private BudgetReimbursementorderAllocatedMapper allocatedMapper;

	@Autowired
	private BudgetReimbursementorderDetailMapper reimDetailMapper;

	@Autowired
	private BudgetReimbursementorderService orderService;

	@Autowired
	private BudgetReimbursementorderService service;

	@Autowired
	private BudgetMonthPeriodMapper monthMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_authorfeesum"));
	}


	/**
	 * 验证身份证号的正确性
	 */
	public boolean validateIDCardNo(String IDCardNo) {
		if (null == IDCardNo || " " == IDCardNo) {
			return false;
		}

		//(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)
		//18位
		String regExp = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)";
		//15位
		String regExp2 = "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
		return IDCardNo.matches(regExp);
	}

	public BudgetAuthorfeesum saveAuthorFeeSum(ContributionFeeExcelHead head) {
		BudgetAuthorfeesum budgetAuthorFeeSum = budgetAuthorfeesumMapper.selectOne(new QueryWrapper<BudgetAuthorfeesum>().eq("code", head.getContributionFeeNo()));
		boolean isNew = false;
		if (budgetAuthorFeeSum == null) {
			budgetAuthorFeeSum = new BudgetAuthorfeesum();
			budgetAuthorFeeSum.setCode(head.getContributionFeeNo());
			budgetAuthorFeeSum.setStatus(AuthorFeeStatusEnum.STATUS_UNAUDITED.getType());
			budgetAuthorFeeSum.setDeleteflag(false);
			budgetAuthorFeeSum.setTimes(1);
			budgetAuthorFeeSum.setCreatetime(new Date());
			isNew = true;
		} else {
			if (budgetAuthorFeeSum.getStatus() > AuthorFeeStatusEnum.STATUS_UNAUDITED.getType())
				throw new RuntimeException("稿酬编号【" + head.getContributionFeeNo() + "】已经不能导入！");
		}
		budgetAuthorFeeSum.setReimid(head.getBxUser().getUserId());
		budgetAuthorFeeSum.setReimno(head.getBxUser().getUserName());
		budgetAuthorFeeSum.setReimname(head.getBxUser().getDisplayName());
		budgetAuthorFeeSum.setFeedeptid(head.getUnit().getId());
		budgetAuthorFeeSum.setFeedeptname(head.getUnit().getName());
		budgetAuthorFeeSum.setYearid(head.getYearPeriod().getId());
		budgetAuthorFeeSum.setYearperiod(head.getYearPeriod().getPeriod());
		budgetAuthorFeeSum.setMonthid(head.getMonthPeriod().getId());
		budgetAuthorFeeSum.setFeemonth(head.getContributionFeeMonth());
		budgetAuthorFeeSum.setReimbursemonth(head.getContributionFeeMonth());
		budgetAuthorFeeSum.setCreatorid(UserThreadLocal.get().getUserId());
		budgetAuthorFeeSum.setCreatorname(UserThreadLocal.get().getDisplayName());
		//if(isNew) budgetAuthorfeesumMapper.insert(budgetAuthorFeeSum);
		//if(!isNew) budgetAuthorfeesumMapper.updateById(budgetAuthorFeeSum);
		return budgetAuthorFeeSum;
	}

	/**
	 * 执行导入稿费明细
	 *
	 * @param headMap
	 */
	public void saveFeeDetails(Map<String, Object> headMap) {
		BudgetAuthorfeesum budgetAuthorFeeSum = (BudgetAuthorfeesum) headMap.get("feeSum");
		if (budgetAuthorFeeSum.getId() == null) this.budgetAuthorfeesumMapper.insert(budgetAuthorFeeSum);
		if (budgetAuthorFeeSum.getId() != null) this.budgetAuthorfeesumMapper.updateById(budgetAuthorFeeSum);

		/**
		 * 清除数据
		 */
		budgetAuthorfeedetailMapper.delete(new QueryWrapper<BudgetAuthorfeedetail>().eq("authorfeesumid", budgetAuthorFeeSum.getId()));
		if (headMap.get("feeDetails") != null) {
			Map<String, WbBanks> bankMap = bankMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getSubBranchCode(), e -> e, (e1, e2) -> e1));
			List<ContributionFeeExcelDetail> details = (List<ContributionFeeExcelDetail>) headMap.get("feeDetails");
			List<BudgetAuthorfeedetail> feedetails = new ArrayList<>();
			for (ContributionFeeExcelDetail excelDetail : details) {
				BudgetAuthorfeedetail feeDetail = new BudgetAuthorfeedetail();
				feeDetail.setYearperiod(budgetAuthorFeeSum.getYearperiod());
				feeDetail.setMonthid(budgetAuthorFeeSum.getMonthid());
				feeDetail.setFeemonth(budgetAuthorFeeSum.getFeemonth());
				feeDetail.setAuthorfeesumid(budgetAuthorFeeSum.getId());
				feeDetail.setAuthortype(AUTHOR_TYPE_INNER.equals(excelDetail.getAuthorType()) ? true : false);
				feeDetail.setAuthorid(excelDetail.getAuthor().getId());
				feeDetail.setAuthorname(excelDetail.getAuthor().getAuthor());
				feeDetail.setAuthoridnumber(excelDetail.getAuthor().getIdnumber());
				feeDetail.setTaxpayeridnumber(excelDetail.getAuthor().getTaxpayernumber());
				feeDetail.setAuthorcompany(excelDetail.getAuthor().getCompany());
				WbBanks bank = bankMap.get(excelDetail.getAuthor().getBranchcode());
				if (bank == null)
					throw new RuntimeException("电子银联号【" + excelDetail.getAuthor().getBranchcode() + "】不存在！");
				feeDetail.setAuthorprovince(bank.getProvince());
				feeDetail.setAuthorcity(bank.getCity());
				feeDetail.setBankaccount(excelDetail.getAuthor().getBankaccount());
				feeDetail.setBankaccountbranchcode(excelDetail.getAuthor().getBranchcode());
				feeDetail.setBankaccountbranchname(bank.getSubBranchName());
				feeDetail.setTaxpayeridnumber(excelDetail.getAuthor().getTaxpayernumber());
				feeDetail.setFeestandard(new BigDecimal(excelDetail.getContributionFeeStandard()));
				feeDetail.setCopefee(new BigDecimal(excelDetail.getContributionFee()));
				feeDetail.setTaxtype(TAX_TYPE_YES.equals(excelDetail.getIsDecutionTax()) ? true : false);
				feeDetail.setAgentid(excelDetail.getMonthAgent().getId());
				feeDetail.setAgentname(excelDetail.getMonthAgent().getName());
				feeDetail.setSubjectid(Long.valueOf(excelDetail.getSubjectId()));
				feeDetail.setReimbursesubject(excelDetail.getSubjectName());
				feeDetail.setProducttype(excelDetail.getProductForm());
				feeDetail.setProductbgtcls(excelDetail.getMonthAgentName());
				feeDetail.setEmpid(excelDetail.getTeacher().getUserId());
				feeDetail.setEmpno(excelDetail.getTeacher().getUserName());
				feeDetail.setEmpname(excelDetail.getTeacher().getDisplayName());
				feeDetail.setCreatetime(new Date());
				feeDetail.setSubject(excelDetail.getSubject());
				feeDetail.setContext(excelDetail.getRemark());
				feeDetail.setPaperquality(excelDetail.getManuscriptQuality());
				feeDetail.setPageorcopy(excelDetail.getPageNumber());
				feeDetail.setFeebdgdeptid(excelDetail.getHbUnit().getId());
				feeDetail.setFeebdgdept(excelDetail.getHbUnit().getName());
				feeDetail.setBusinessgroup(excelDetail.getAscriptionUnitName());
				feeDetail.setNeedzz(TAX_TYPE_YES.equals(excelDetail.getIsNeedTran()) ? true : false);
				feedetails.add(feeDetail);
			}
			if (!feedetails.isEmpty()) this.detailService.saveBatch(feedetails);
			budgetAuthorFeeSum.setAuthorfeenum(details.size());
			/**
			 * 统计稿费总额、外审外包总额、待摊-稿费、待摊-外审外包
			 */
			BigDecimal gfTotal = details.stream().filter(e -> e.getSubjectName().equals(CONTRIBUTION_FEE)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setContributionfee(gfTotal);
			BigDecimal wswbTotal = details.stream().filter(e -> e.getSubjectName().equals(EXTERNAL_AUDIT_FEE)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setExternalauditfee(wswbTotal);
			BigDecimal dtgfTotal = details.stream().filter(e -> e.getSubjectName().equals(CONTRIBUTION_FEE_NEXT)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setContributionfeenext(dtgfTotal);
			BigDecimal dtwswbTotal = details.stream().filter(e -> e.getSubjectName().equals(EXTERNAL_AUDIT_FEE_NEXT)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setExternalauditfeenext(dtwswbTotal);

			BigDecimal jsTotal = details.stream().filter(e -> e.getIsDecutionTax().equals(TAX_TYPE_YES)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal bjsTotal = details.stream().filter(e -> e.getIsDecutionTax().equals(TAX_TYPE_NO)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setNeedtaxtotal(jsTotal);
			budgetAuthorFeeSum.setNoneedtaxtotal(bjsTotal);
			budgetAuthorfeesumMapper.updateById(budgetAuthorFeeSum);
		}


	}

	public List<AuthorFeeMainVO> getAuthorSumList(Map<String, Object> params) {
		if (params.containsKey("query")) {
			String query = params.get("query").toString();
			String[] queryArr = query.split("-");
			if (queryArr.length == 1) {
				BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
				params.put("yearid", yearPeriod.getId());
			} else if (queryArr.length == 2) {
				BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
				String feeMonth = BudgetExtractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));
				params.put("feeMonth", feeMonth);
			}
		}
		List<AuthorFeeMainVO> resultList = budgetAuthorfeesumMapper.getAuthorSumList(params);
		resultList = resultList.stream().peek(e -> {
			e.setTotalAuthorFee(e.getNeedTaxTotal() == null ? BigDecimal.ZERO : e.getNeedTaxTotal().add(e.getNoneedTaxTotal() == null ? BigDecimal.ZERO : e.getNoneedTaxTotal()));
		}).collect(Collectors.toList());
		return resultList;
	}

	public PageResult<AuthorFeeDetailVO> getAuthorDetailList(Map<String, Object> params, Integer page, Integer rows) {
		Page<AuthorFeeDetailVO> pageCond = new Page<AuthorFeeDetailVO>(page, rows);
		List<AuthorFeeDetailVO> list = this.budgetAuthorfeesumMapper.getAuthorDetailList(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), list);
	}

	/**
	 * 批量提交
	 *
	 * @param ids
	 */
	public void batchSubmitAuthorFee(String ids) {
		for (String id : ids.split(",")) {
			BudgetAuthorfeesum authorfeesum = this.budgetAuthorfeesumMapper.selectById(Long.valueOf(id));
			if (authorfeesum.getStatus() >= AuthorFeeStatusEnum.STATUS_AUDITING.getType())
				throw new RuntimeException("提交失败！稿费单号【" + authorfeesum.getCode() + "】不允许提交");
			authorfeesum.setStatus(AuthorFeeStatusEnum.STATUS_AUDITING.getType());
			authorfeesum.setUpdatetime(new Date());
			this.budgetAuthorfeesumMapper.updateById(authorfeesum);
		}
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 */
	public void batchDeleteAuthorFee(String ids) {
		for (String id : ids.split(",")) {
			BudgetAuthorfeesum authorfeesum = this.budgetAuthorfeesumMapper.selectById(Long.valueOf(id));
			if (authorfeesum.getStatus() >= AuthorFeeStatusEnum.STATUS_AUDITING.getType())
				throw new RuntimeException("删除失败！稿费单号【" + authorfeesum.getCode() + "】不允许删除");

			//删除稿费明细
			budgetAuthorfeedetailMapper.delete(new QueryWrapper<BudgetAuthorfeedetail>().eq("authorfeesumid", id));

			this.budgetAuthorfeesumMapper.deleteById(Long.valueOf(id));
		}
	}

	/**
	 * 批量审核
	 *
	 * @param ids
	 * @param status -1退回 2通过
	 * @param remark
	 */
	public void batchVerifyAuthorFee(String ids, Integer status, String remark) {
		List<String> empIds = new ArrayList<>();
		for (String id : ids.split(",")) {
			BudgetAuthorfeesum authorfeesum = this.budgetAuthorfeesumMapper.selectById(Long.valueOf(id));
			if (authorfeesum.getStatus() != AuthorFeeStatusEnum.STATUS_AUDITING.getType() && status == -1)
				throw new RuntimeException("退回失败！稿费单号【" + authorfeesum.getCode() + "】不允许退回");
			if (authorfeesum.getStatus() != AuthorFeeStatusEnum.STATUS_AUDITING.getType() && status == 2)
				throw new RuntimeException("通过失败！稿费单号【" + authorfeesum.getCode() + "】不允许通过");
			authorfeesum.setStatus(status);
			authorfeesum.setRemark(remark);
			authorfeesum.setUpdatetime(new Date());
			this.budgetAuthorfeesumMapper.updateById(authorfeesum);
			empIds.add(authorfeesum.getCreatorid());
		}

		if (status == AuthorFeeStatusEnum.STATUS_WITHDRAWL.getType()) {
			String empNos = userMapper.selectList(new QueryWrapper<WbUser>().in("USER_ID", empIds)).stream().map(e -> e.getUserName()).collect(Collectors.joining("|"));
			/**
			 * 退回消息通知导入人
			 */
			try {
				sender.sendQywxMsgSyn(new QywxTextMsg(empNos, null, null, 0, "稿费退回通知\n您于" + Constants.FORMAT_10.format(new Date()) + "有稿费被退回\n审批意见【" + remark + "】\n请尽快处理！", null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 稿费计税
	 * 一、校验
	 * 1.。
	 * 二、计税（可支持重复计税）
	 * 1.根据是否扣税+报销科目+作者身份证号（纳税人识别号）合并。
	 * 2.不扣税的不计税。 扣税的按照计税规则算税。
	 *
	 * @param feeMonth
	 * @param salaryCompanyMap
	 */
	public void calculateTax(String feeMonth, Map<String, Object> salaryCompanyMap) {
		//获取当前月份的所有稿费（考虑一个月多批次稿费的情况）
		List<BudgetAuthorfeesum> feeSumList = this.budgetAuthorfeesumMapper.selectList(new QueryWrapper<BudgetAuthorfeesum>().likeRight("feemonth", feeMonth));

		long count = feeSumList.stream().filter(e -> e.getStatus() < AuthorFeeStatusEnum.STATUS_AUDITED.getType()).count();
		if (count > 0) throw new RuntimeException("计税失败！该批次存在未审核通过的稿费");
		//已经报销的数据
		long alreadyBxCount = feeSumList.stream().filter(e -> e.getStatus() == AuthorFeeStatusEnum.STATUS_REIMBURSED.getType()).count();
		if (alreadyBxCount > 0) {
			/**
			 * 计税时验证是否有报销单已经走完了审核
			 */
			List<BudgetReimbursementorder> bxOrderList = this.reimbursementMapper.selectList(new QueryWrapper<BudgetReimbursementorder>().likeRight("interimbatch", feeMonth));
			String bxOrder = bxOrderList.stream().filter(e -> e.getReuqeststatus() == StatusConstants.BX_PASS).map(e -> e.getReimcode()).collect(Collectors.joining(","));
			if (StringUtils.isNotBlank(bxOrder))
				throw new RuntimeException("重新计税失败！稿费月份【" + feeMonth + "】中存在报销单【" + bxOrder + "】已被计入执行！");
			//TODO 删除报销单数据
			bxOrderList.forEach(e -> orderService.delete(e.getId()));
		}
		List<Long> sumIds = feeSumList.stream().map(e -> e.getId()).collect(Collectors.toList());
		//获取所有的稿费明细
		List<BudgetAuthorfeedetail> feeDetails = this.budgetAuthorfeedetailMapper.selectList(new QueryWrapper<BudgetAuthorfeedetail>().in("authorfeesumid", sumIds));
		//身份证和纳税人识别号都存在的数据
		List<BudgetAuthorfeedetail> list = feeDetails.stream().filter(e -> StringUtils.isNotBlank(e.getAuthoridnumber()) && StringUtils.isNotBlank(e.getTaxpayeridnumber())).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(list)) {
			String errorEmpName = list.stream().map(e -> e.getEmpname().concat("(").concat(e.getEmpno()).concat(")")).collect(Collectors.joining(","));
			throw new RuntimeException("存在身份证号和纳税人识别号都存在的记录：" + errorEmpName);
		}

		//清空数据
		clearFeeTaxData(feeDetails);

		AuthorFeeCalCommonData commonData = new AuthorFeeCalCommonData();
		commonData.setSalaryCompanyMap(salaryCompanyMap);
		/**
		 * 组装计税发放数据
		 */
		populateCalPayCommonData(commonData);

		prepareCalTax(feeDetails, commonData, feeMonth);

		feeSumList.stream().forEach(e -> {
			e.setStatus(AuthorFeeStatusEnum.STATUS_TAX_CALCULATED.getType());
			this.budgetAuthorfeesumMapper.updateById(e);
		});
	}

	private void populateCalPayCommonData(AuthorFeeCalCommonData commonData) {
		//发放明细
		List<BudgetAuthorfeepayRule> payRuleList = this.payRuleMapper.selectList(new QueryWrapper<BudgetAuthorfeepayRule>().eq("endflag", 0).eq("taxflag", 1).lt("effectdate", Constants.FORMAT_10.format(new Date())));
		if (payRuleList.isEmpty()) throw new RuntimeException("发放规则设置错误！请联系预算管理员");
		BudgetAuthorfeepayRule budgetAuthorfeepayRule = payRuleList.stream().sorted(Comparator.comparing(BudgetAuthorfeepayRule::getEffectdate).reversed()).findFirst().get();

		List<BudgetAuthorfeepayRuledetail> payRuleDetail = this.payRuleDetailMapper.selectList(new QueryWrapper<BudgetAuthorfeepayRuledetail>().eq("payruleid", budgetAuthorfeepayRule.getId()));
		commonData.setPayRuleDetailList(payRuleDetail);

		//计税规则
		List<BudgetAuthorfeetaxRule> taxRuleList = this.taxRuleMapper.selectList(new QueryWrapper<BudgetAuthorfeetaxRule>().eq("endflag", 0).eq("effectflag", 1).lt("effectdate", Constants.FORMAT_10.format(new Date())));
		if (taxRuleList.isEmpty()) throw new RuntimeException("计税规则设置错误！请联系预算管理员");
		BudgetAuthorfeetaxRule authorfeetaxRule = taxRuleList.stream().sorted(Comparator.comparing(BudgetAuthorfeetaxRule::getEffectdate).reversed()).findFirst().get();
		List<BudgetAuthorfeetaxRuledetail> taxRuleDetails = this.taxRuleDetailMapper.selectList(new QueryWrapper<BudgetAuthorfeetaxRuledetail>().eq("taxruleid", authorfeetaxRule.getId()));
		commonData.setTaxRuleDetailList(taxRuleDetails);

		Map<String, WbBanks> bankMap = bankMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getSubBranchCode(), e -> e));
		commonData.setBankMap(bankMap);

		//默认发放账户
		TabDm cclDm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", CCLACCOUNT));
		if (cclDm == null || StringUtils.isBlank(cclDm.getDmValue())) throw new RuntimeException("请先联系管理员维护陈彩莲账号");
		BudgetBillingUnitAccount unitAccount = this.billingUnitAccountMapper.selectOne(new QueryWrapper<BudgetBillingUnitAccount>().eq("bankaccount", cclDm.getDmValue()).eq("stopflag", 0));
		if (unitAccount == null) throw new RuntimeException("银行账户【" + cclDm.getDmValue() + "】不存在！");
		BudgetBillingUnit billingUnit = this.billingUnitMapper.selectById(unitAccount.getBillingunitid());
		WbBanks bank = bankMap.get(unitAccount.getBranchcode());
		if (bank == null) throw new RuntimeException("银行账户【" + cclDm.getDmValue() + "】开户行不存在！");
		commonData.setDefaultPayUnit(commonData.createPayUnit(unitAccount.getBillingunitid(), unitAccount.getId(), bank.getSubBranchName(), unitAccount.getBankaccount(), billingUnit.getName()));

		//不计税发放账户（外部人员）
		TabDm wbDm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", NOTAXACCOUNT));
		if (wbDm == null || StringUtils.isBlank(wbDm.getDmValue())) throw new RuntimeException("请先联系管理员维护不计税发放账号");
		BudgetBillingUnitAccount wbunitAccount = this.billingUnitAccountMapper.selectOne(new QueryWrapper<BudgetBillingUnitAccount>().eq("bankaccount", wbDm.getDmValue()).eq("stopflag", 0));
		if (wbunitAccount == null) throw new RuntimeException("银行账户【" + wbDm.getDmValue() + "】不存在！");
		BudgetBillingUnit wbbillingUnit = this.billingUnitMapper.selectById(wbunitAccount.getBillingunitid());
		WbBanks wbbank = bankMap.get(wbunitAccount.getBranchcode());
		if (wbbank == null) throw new RuntimeException("银行账户【" + wbDm.getDmValue() + "】开户行不存在！");
		commonData.setNoTaxPayUnit(commonData.createPayUnit(wbunitAccount.getBillingunitid(), wbunitAccount.getId(), wbbank.getSubBranchName(), wbunitAccount.getBankaccount(), wbbillingUnit.getName()));

		//代收的信息
		TabDm dsDm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", EDUCTIONBANKACCOUNT));
		if (dsDm == null || StringUtils.isBlank(dsDm.getDmValue())) throw new RuntimeException("请先联系管理员维护稿费代收账号");
		BudgetAuthor author = this.authorMapper.selectOne(new QueryWrapper<BudgetAuthor>().eq("bankaccount", dsDm.getDmValue()));
		if (author == null) throw new RuntimeException("稿费代收账号不存在！");
		WbBanks dsbank = bankMap.get(author.getBranchcode());
		if (dsbank == null) throw new RuntimeException("银行账户【" + wbDm.getDmValue() + "】开户行不存在！");
		commonData.setReplaceReceiveData(commonData.createReplaceReceiveData(author.getCode(), dsbank.getSubBranchName(), author.getBankaccount(), author.getAuthor()));

		/*
		  先写死
		 */
		BudgetBillingUnit jty = this.billingUnitMapper.selectById(64L);
		BudgetBillingUnit hg = this.billingUnitMapper.selectById(67L);
		BudgetBillingUnit rc = this.billingUnitMapper.selectById(71L);
		BudgetBillingUnitAccount hgUnitAccount = billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().eq(BudgetBillingUnitAccount::getStopflag, 0).eq(BudgetBillingUnitAccount::getBillingunitid, hg.getId())).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed()).sorted(Comparator.comparing(BudgetBillingUnitAccount::getOrderno)).findFirst().orElseThrow(() -> new RuntimeException("江西慧谷文化传播有限公司没有单位账户。"));
		BudgetBillingUnitAccount jtyUnitAccount = billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().eq(BudgetBillingUnitAccount::getStopflag, 0).eq(BudgetBillingUnitAccount::getBillingunitid, jty.getId())).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed()).sorted(Comparator.comparing(BudgetBillingUnitAccount::getOrderno)).findFirst().orElseThrow(() -> new RuntimeException("江西金太阳教育研究有限公司没有单位账户。"));
		BudgetBillingUnitAccount rcUnitAccount = billingUnitAccountMapper.selectList(new LambdaQueryWrapper<BudgetBillingUnitAccount>().eq(BudgetBillingUnitAccount::getStopflag, 0).eq(BudgetBillingUnitAccount::getBillingunitid, rc.getId())).stream().sorted(Comparator.comparing(BudgetBillingUnitAccount::getDefaultflag).reversed()).sorted(Comparator.comparing(BudgetBillingUnitAccount::getOrderno)).findFirst().orElseThrow(() -> new RuntimeException("江西日出文化传播有限公司没有单位账户。"));
		WbBanks hgwbbank = bankMap.get(hgUnitAccount.getBranchcode());
		if (hgwbbank == null) throw new RuntimeException("银行账户【" + hgUnitAccount.getBankaccount() + "】开户行不存在！");
		commonData.setHg(commonData.createPayUnit(hg.getId(),hgUnitAccount.getId(),hgwbbank.getSubBranchName(),hgUnitAccount.getBankaccount(),hg.getName()));

		WbBanks jtywbbank = bankMap.get(jtyUnitAccount.getBranchcode());
		if (jtywbbank == null) throw new RuntimeException("银行账户【" + jtyUnitAccount.getBankaccount() + "】开户行不存在！");
		commonData.setJty(commonData.createPayUnit(jty.getId(),jtyUnitAccount.getId(),jtywbbank.getSubBranchName(),jtyUnitAccount.getBankaccount(),jty.getName()));

		WbBanks rcwbbank = bankMap.get(rcUnitAccount.getBranchcode());
		if (rcwbbank == null) throw new RuntimeException("银行账户【" + rcUnitAccount.getBankaccount() + "】开户行不存在！");
		commonData.setRc(commonData.createPayUnit(rc.getId(),rcUnitAccount.getId(),rcwbbank.getSubBranchName(),rcUnitAccount.getBankaccount(),rc.getName()));
	}

	/**
	 * 准备计税
	 *
	 * @param feeDetails
	 * @param commonData
	 */
	private void prepareCalTax(List<BudgetAuthorfeedetail> feeDetails, AuthorFeeCalCommonData commonData, String feemonth) {
		//以是否扣税+报销科目+身份证号分组
		Map<String, List<BudgetAuthorfeedetail>> feeDetailMap = feeDetails.stream().filter(e -> StringUtils.isNotBlank(e.getAuthoridnumber())).collect(Collectors.groupingBy(e -> {
			return e.getTaxtype() + SPLIT_SYMBOL + e.getReimbursesubject() + SPLIT_SYMBOL + e.getAuthoridnumber();
		}));
		//以是否扣税+报销科目+纳税人识别号分组
		feeDetailMap.putAll(feeDetails.stream().filter(e -> StringUtils.isNotBlank(e.getTaxpayeridnumber())).collect(Collectors.groupingBy(e -> {
			return e.getTaxtype() + SPLIT_SYMBOL1 + e.getReimbursesubject() + SPLIT_SYMBOL1 + e.getTaxpayeridnumber();
		})));
		List<BudgetAuthorfeedtlMerge> mergeList = new ArrayList<>();
		feeDetailMap.forEach((key, feeDetailList) -> {
			String subjectname = "";
			String idnumber = "";
			String taxIdnumber = "";
			boolean isIdnumberMerage = false;
			String[] keyArr1 = key.split(SPLIT_SYMBOL1);
			String[] keyArr2 = key.split(SPLIT_SYMBOL);
			if (keyArr1.length == 3) {
				taxIdnumber = keyArr1[2];
				subjectname = keyArr1[1];
			} else if (keyArr2.length == 3) {
				isIdnumberMerage = true;
				idnumber = keyArr2[2];
				subjectname = keyArr2[1];
			}

			/*
			 * 当“作者”为“读者出版传媒股份有限公司”且“产品预算II类”为“读者（原创版）”，发放单位为“江西慧谷文化传播有限公司”；
				当“作者”为“读者出版传媒股份有限公司”且“产品预算II类”为“读者（校园版）”，发放单位为“江西日出文化传播有限公司”；
				当“作者”所对应的身份证号匹配到【稿费作者】中的纳税人识别号时，表示该作者为对公单位，若纳税人识别号不等于“9162000069563405XC”，发放单位为“江西金太阳教育研究有限公司”
			 */
			if (!isIdnumberMerage) {
				if("9162000069563405XC".equals(taxIdnumber)){
					//读者出版传媒股份有限公司

					List<BudgetAuthorfeedetail> detail1 = feeDetailList.stream().filter(e -> "读者（原创版）".equals(e.getAgentname())).collect(Collectors.toList());
					List<BudgetAuthorfeedetail> detail2 = feeDetailList.stream().filter(e -> "读者（校园版）".equals(e.getAgentname())).collect(Collectors.toList());
					List<BudgetAuthorfeedetail> detail3 = feeDetailList.stream().filter(e -> !"读者（校园版）".equals(e.getAgentname()) && !"读者（原创版）".equals(e.getAgentname())).collect(Collectors.toList());

					if(!CollectionUtils.isEmpty(detail1)){
						BudgetAuthorfeedtlMerge feeMerge = createFeeMerageDetail(detail1, idnumber, taxIdnumber, isIdnumberMerage, subjectname);
						feeMerge.setCommon(false);
						feeMerge.setDzcb(true);
						feeMerge.setYcb(true);
						feeMerge.setXyb(false);
						//计算并生成发放数据
						doCalculate(feeMerge, commonData);
						handleTaxAccuracy(detail1,feeMerge);
						mergeList.add(feeMerge);
					}
					if(!CollectionUtils.isEmpty(detail2)){
						BudgetAuthorfeedtlMerge feeMerge = createFeeMerageDetail(detail2, idnumber, taxIdnumber, isIdnumberMerage, subjectname);
						feeMerge.setCommon(false);
						feeMerge.setDzcb(true);
						feeMerge.setYcb(false);
						feeMerge.setXyb(true);
						//计算并生成发放数据
						doCalculate(feeMerge, commonData);
						handleTaxAccuracy(detail2,feeMerge);
						mergeList.add(feeMerge);
					}
					if(!CollectionUtils.isEmpty(detail3)){
						BudgetAuthorfeedtlMerge feeMerge = createFeeMerageDetail(detail3, idnumber, taxIdnumber, isIdnumberMerage, subjectname);
						feeMerge.setCommon(false);
						feeMerge.setDzcb(true);
						feeMerge.setYcb(false);
						feeMerge.setXyb(false);
						//计算并生成发放数据
						doCalculate(feeMerge, commonData);
						handleTaxAccuracy(detail3,feeMerge);
						mergeList.add(feeMerge);
					}
				}else{
					//非读者出版传媒股份有限公司的对公单位
					//创建稿费合并的明细
					BudgetAuthorfeedtlMerge feeMerge = createFeeMerageDetail(feeDetailList, idnumber, taxIdnumber, isIdnumberMerage, subjectname);
					feeMerge.setCommon(false);
					feeMerge.setDzcb(false);
					//计算并生成发放数据
					doCalculate(feeMerge, commonData);
					handleTaxAccuracy(feeDetailList,feeMerge);
					mergeList.add(feeMerge);
				}
			}else {
				//创建稿费合并的明细
				BudgetAuthorfeedtlMerge feeMerge = createFeeMerageDetail(feeDetailList, idnumber, taxIdnumber, isIdnumberMerage, subjectname);
				//计算并生成发放数据
				doCalculate(feeMerge, commonData);
				handleTaxAccuracy(feeDetailList,feeMerge);
				mergeList.add(feeMerge);
			}

		});
		if (!mergeList.isEmpty()) {
			/**
			 * 生成报表
			 */
			BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", mergeList.get(0).getYearperiod()));
			feeReportMapper.delete(new QueryWrapper<BudgetAuthorfeeReport>().eq("feemonth", feemonth).eq("yearid", yearPeriod.getId()));
			BudgetAuthorfeeReport report = new BudgetAuthorfeeReport();
			report.setYearid(yearPeriod.getId());
			report.setYearperiod(mergeList.get(0).getYearperiod());
			report.setMonthid(mergeList.get(0).getMonthid());
			report.setFeemonth(mergeList.get(0).getFeemonth());
			report.setReportcode(mergeList.get(0).getFeemonth() + '0' + mergeList.get(0).getTimes());
			BigDecimal copeFeeSum = mergeList.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal taxSum = mergeList.stream().map(e -> e.getTax()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal realFeeSum = mergeList.stream().map(e -> e.getRealfee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			report.setCopefeesum(copeFeeSum);
			report.setTaxsum(taxSum);
			report.setRealfeesum(realFeeSum);
			report.setCreatetime(new Date());
			feeReportMapper.insert(report);

			mergeList.forEach(e -> {
				e.setReportid(report.getId());
			});
			this.mergeService.updateBatchById(mergeList);
		}
	}

	private void handleTaxAccuracy(List<BudgetAuthorfeedetail> feeDetailList,BudgetAuthorfeedtlMerge feeMerge){
		List<BigDecimal> taxSum = new ArrayList<>();
		for (int i = 0; i < feeDetailList.size(); i++) {
			BudgetAuthorfeedetail budgetAuthorFeeDetail = feeDetailList.get(i);
			budgetAuthorFeeDetail.setAuthormergeid(feeMerge.getId());
			/**
			 * 处理四舍五入损失的数据问题
			 */
			if (i != feeDetailList.size() - 1) {
				BigDecimal tax = budgetAuthorFeeDetail.getCopefee().divide(feeMerge.getCopefee(), 20, BigDecimal.ROUND_HALF_UP).multiply(feeMerge.getTax()).setScale(2, BigDecimal.ROUND_HALF_UP);
				budgetAuthorFeeDetail.setTax(tax);
				taxSum.add(tax);
			} else if (i == feeDetailList.size() - 1) {
				BigDecimal taxSums = taxSum.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
				budgetAuthorFeeDetail.setTax(feeMerge.getTax().subtract(taxSums));
			}
			this.budgetAuthorfeedetailMapper.updateById(budgetAuthorFeeDetail);
		}
	}

	private void doCalculate(BudgetAuthorfeedtlMerge feeMerge, AuthorFeeCalCommonData commonData) {
		if (feeMerge.getTaxtype()) {
			//计算发放(扣税)
			doCalPayIfHaveTax(feeMerge, commonData);
		} else {
			//计算发放(不扣税)
			doCalPayIfNoTax(feeMerge, commonData);
		}

	}

	private void doCalPayIfNoTax(BudgetAuthorfeedtlMerge feeMerge, AuthorFeeCalCommonData commonData) {
		BudgetAuthor curAuthor = this.authorMapper.selectById(feeMerge.getAuthorid());
		WbBanks curBank = commonData.getBankMap().get(curAuthor.getBranchcode());
		if (curBank == null) throw new RuntimeException("稿费作者【" + curAuthor.getAuthor() + "】开户行不存在");
		feeMerge.setGathercode(curAuthor.getCode());
		feeMerge.setGatherbank(curBank.getSubBranchName());
		feeMerge.setGatherbanktype(curBank.getBankName());
		feeMerge.setGatherbankaccount(curAuthor.getBankaccount());
		feeMerge.setGatherunit(curAuthor.getAuthor());
		feeMerge.setEdupubflag(false);
		if (StringUtils.isNotBlank(feeMerge.getAuthoridnumber()) && StringUtils.isBlank(feeMerge.getTaxpayeridnumber())) {
			//个人作者：走陈彩莲发放
			feeMerge.setPayunitid(commonData.getDefaultPayUnit().getPayUnitid());
			feeMerge.setPayunitaccountid(commonData.getDefaultPayUnit().getPayUnitAccountId());
			feeMerge.setPaybank(commonData.getDefaultPayUnit().getPayBank());
			feeMerge.setPaybankaccount(commonData.getDefaultPayUnit().getPayBankAccount());
			feeMerge.setPayunit(commonData.getDefaultPayUnit().getPayUnitName());
			feeMerge.setPrlautflag(true);
		} else if (StringUtils.isNotBlank(feeMerge.getTaxpayeridnumber()) && StringUtils.isBlank(feeMerge.getAuthoridnumber())) {
			//纳税人识别号的发放
			feeMerge.setPayunitid(commonData.getNoTaxPayUnit().getPayUnitid());
			feeMerge.setPayunitaccountid(commonData.getNoTaxPayUnit().getPayUnitAccountId());
			feeMerge.setPaybank(commonData.getNoTaxPayUnit().getPayBank());
			feeMerge.setPaybankaccount(commonData.getNoTaxPayUnit().getPayBankAccount());
			feeMerge.setPayunit(commonData.getNoTaxPayUnit().getPayUnitName());
			feeMerge.setPrlautflag(false);
		}
		feeMerge.setTax(BigDecimal.ZERO);
		feeMerge.setRealfee(feeMerge.getCopefee());

		handleDzcb(feeMerge,commonData);
	}

	private void doCalPayIfHaveTax(BudgetAuthorfeedtlMerge feeMerge, AuthorFeeCalCommonData commonData) {
		//应发稿费
		BigDecimal copefee = feeMerge.getCopefee();
		//匹配发放规则中的规则明细
		List<BudgetAuthorfeepayRuledetail> payRuleDetailList = commonData.getPayRuleDetailList();
		List<BudgetAuthorfeepayRuledetail> details = payRuleDetailList.stream().filter(e -> e.getMin().compareTo(copefee) <= 0 && e.getMax().compareTo(copefee) > 0).collect(Collectors.toList());
		if (details.isEmpty()) throw new RuntimeException("稿费发放规则设置错误！请联系管理员重新设置。错误标记：" + copefee);

		//计税明细
		List<BudgetAuthorfeetaxRuledetail> taxRuleDetailList = commonData.getTaxRuleDetailList();
		List<BudgetAuthorfeetaxRuledetail> matchingTaxRuleDetailList = taxRuleDetailList.stream().filter(e -> e.getMin().compareTo(copefee) <= 0 && e.getMax().compareTo(copefee) > 0).collect(Collectors.toList());
		if (matchingTaxRuleDetailList.isEmpty() || matchingTaxRuleDetailList.size() > 1) {
			throw new RuntimeException("稿费计税规则设置错误！请联系管理员重新设置。错误标记：" + copefee);
		}
		BudgetAuthorfeetaxRuledetail taxRuleDetail = matchingTaxRuleDetailList.get(0);

		BudgetAuthor curAuthor = this.authorMapper.selectById(feeMerge.getAuthorid());
		WbBanks curBank = commonData.getBankMap().get(curAuthor.getBranchcode());
		if (curBank == null) throw new RuntimeException("稿费作者【" + curAuthor.getAuthor() + "】开户行不存在");
		List<BudgetAuthorfeepayRuledetail> matchBankDetails = details.stream().filter(e -> ("," + e.getBanks() + ",").contains("," + curBank.getBankName() + ",")).collect(Collectors.toList());
		BudgetAuthorfeepayRuledetail ruledetail = null;
		if (matchBankDetails.size() > 1) {
			throw new RuntimeException("发放规则设置错误。错误标记：稿费作者【" + curAuthor.getAuthor() + "(" + copefee + "," + curBank.getBankName() + ")】");
		} else if (matchBankDetails.size() == 1) {
			ruledetail = matchBankDetails.get(0);
		} else {
			//没有匹配上银行
			List<BudgetAuthorfeepayRuledetail> nullBankRuleDetailList = details.stream().filter(e -> StringUtils.isBlank(e.getBanks())).collect(Collectors.toList());
			if (nullBankRuleDetailList.size() != 1)
				throw new RuntimeException("发放规则设置错误。错误标记：稿费作者【" + curAuthor.getAuthor() + "(" + copefee + "," + curBank.getBankName() + ")】");
			ruledetail = details.get(0);
		}
		if (ruledetail.getIsreplacesend()) {
			//如果是代收
			feeMerge.setGathercode(commonData.getReplaceReceiveData().getCode());
			feeMerge.setGatherbank(commonData.getReplaceReceiveData().getOpenBank());
			feeMerge.setGatherbanktype(curBank.getBankName());
			feeMerge.setGatherauthoraccount(curAuthor.getBankaccount());
			feeMerge.setGatherbankaccount(commonData.getReplaceReceiveData().getBankAccount());
			feeMerge.setGatherunit(commonData.getReplaceReceiveData().getUnit());
			feeMerge.setEdupubflag(true);
		} else {
			feeMerge.setGathercode(curAuthor.getCode());
			feeMerge.setGatherbank(curBank.getSubBranchName());
			feeMerge.setGatherbanktype(curBank.getBankName());
			feeMerge.setGatherbankaccount(curAuthor.getBankaccount());
			feeMerge.setGatherunit(curAuthor.getAuthor());
			feeMerge.setEdupubflag(false);
		}
		feeMerge.setPrlautflag(false);

		//付款方
		feeMerge.setPayunitid(ruledetail.getBillunitid());
		feeMerge.setPayunitaccountid(ruledetail.getBillunitaccountid());
		feeMerge.setPaybank(ruledetail.getBillunitopenbank());
		feeMerge.setPaybankaccount(ruledetail.getBillunitaccount());
		feeMerge.setPayunit(ruledetail.getBillunitname());

		doCalTax(feeMerge, commonData, taxRuleDetail);

		if (feeMerge.getAuthortype()) {
			Object obj = commonData.getSalaryCompanyMap().get(feeMerge.getAuthorname() + "_" + feeMerge.getAuthoridnumber());
			if (obj != null && StringUtils.isNotBlank(obj.toString())) {
				String outkey = obj.toString().split("\\|")[0];
				BudgetBillingUnit curBillUnit = this.billingUnitMapper.selectById(ruledetail.getBillunitid());
				if (curBillUnit.getOutKey().equals(outkey)) {
					feeMerge.setSalaryunit(curBillUnit.getName());
				}
			}
		}

		//若稿费作者是公司员工 且 工资的发放单位 与 稿费的发放单位 相同（江西教育社代发的不包含在此）------>走陈彩莲账号,个税公司留存
		if (!feeMerge.getEdupubflag() && feeMerge.getAuthortype()) {
			Object obj = commonData.getSalaryCompanyMap().get(feeMerge.getAuthorname() + "_" + feeMerge.getAuthoridnumber());
			if (obj != null && StringUtils.isNotBlank(obj.toString())) {
				String outkey = obj.toString().split("\\|")[0];
				BudgetBillingUnit curBillUnit = this.billingUnitMapper.selectById(ruledetail.getBillunitid());
				if (curBillUnit.getOutKey().equals(outkey)) {
					feeMerge.setPayunitid(commonData.getDefaultPayUnit().getPayUnitid());
					feeMerge.setPayunitaccountid(commonData.getDefaultPayUnit().getPayUnitAccountId());
					feeMerge.setPaybank(commonData.getDefaultPayUnit().getPayBank());
					feeMerge.setPaybankaccount(commonData.getDefaultPayUnit().getPayBankAccount());
					feeMerge.setPayunit(commonData.getDefaultPayUnit().getPayUnitName());
					feeMerge.setCompanystoretax(feeMerge.getTax());//公司留存个税
					feeMerge.setPrlautflag(true);
					feeMerge.setEdupubflag(false);
					feeMerge.setSalaryunit(curBillUnit.getName());
				}
			}
		}

		handleDzcb(feeMerge,commonData);
	}

	/**
	 * <p>读者出版</p>
	 * @author minzhq
	 * @date 2022/7/21 14:47
	 * @param feeMerge
	 * @param commonData
	 */
	private void handleDzcb(BudgetAuthorfeedtlMerge feeMerge, AuthorFeeCalCommonData commonData) {
		if(!feeMerge.isCommon()){
			if(feeMerge.isDzcb()){
				if(feeMerge.isXyb()){
					feeMerge.setPayunitid(commonData.getRc().getPayUnitid());
					feeMerge.setPayunitaccountid(commonData.getRc().getPayUnitAccountId());
					feeMerge.setPaybank(commonData.getRc().getPayBank());
					feeMerge.setPaybankaccount(commonData.getRc().getPayBankAccount());
					feeMerge.setPayunit(commonData.getRc().getPayUnitName());
				}else if(feeMerge.isYcb()){
					feeMerge.setPayunitid(commonData.getHg().getPayUnitid());
					feeMerge.setPayunitaccountid(commonData.getHg().getPayUnitAccountId());
					feeMerge.setPaybank(commonData.getHg().getPayBank());
					feeMerge.setPaybankaccount(commonData.getHg().getPayBankAccount());
					feeMerge.setPayunit(commonData.getHg().getPayUnitName());
				}
			}else{
				feeMerge.setPayunitid(commonData.getJty().getPayUnitid());
				feeMerge.setPayunitaccountid(commonData.getJty().getPayUnitAccountId());
				feeMerge.setPaybank(commonData.getJty().getPayBank());
				feeMerge.setPaybankaccount(commonData.getJty().getPayBankAccount());
				feeMerge.setPayunit(commonData.getJty().getPayUnitName());
			}
		}

	}

	/**
	 * 计税
	 *
	 * @param feeMerge
	 * @param commonData
	 * @param taxRuleDetail
	 */
	private void doCalTax(BudgetAuthorfeedtlMerge feeMerge, AuthorFeeCalCommonData commonData, BudgetAuthorfeetaxRuledetail taxRuleDetail) {
		ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
		String formula = taxRuleDetail.getFormula();
		BigDecimal tax = BigDecimal.ZERO;
		if (StringUtils.isNotBlank(formula)) {
			if (formula.contains("[this]")) {
				formula = formula.replace("[this]", feeMerge.getCopefee().toString());
			}
			try {
				tax = new BigDecimal(jse.eval(formula).toString());
				tax = tax.setScale(2, BigDecimal.ROUND_HALF_UP); //四舍五入保留两位小数
			} catch (ScriptException e) {
				e.printStackTrace();
				throw new RuntimeException("稿费规则公式设置有误，请重新设置！");
			}
		}
		feeMerge.setTax(tax.compareTo(BigDecimal.ZERO) > 0 ? tax : BigDecimal.ZERO);
		BigDecimal realFee = feeMerge.getCopefee().subtract(feeMerge.getTax()).setScale(2, BigDecimal.ROUND_HALF_UP);
		feeMerge.setRealfee(realFee);
	}

	private BudgetAuthorfeedtlMerge createFeeMerageDetail(List<BudgetAuthorfeedetail> feeDetailList, String idnumber, String taxIdnumber, boolean isIdnumberMerage, String subjectname) {
		BudgetAuthorfeedtlMerge feeMerge = new BudgetAuthorfeedtlMerge();
		feeMerge.setYearperiod(feeDetailList.get(0).getYearperiod());
		feeMerge.setMonthid(feeDetailList.get(0).getMonthid());
		feeMerge.setFeemonth(feeDetailList.get(0).getFeemonth());
		feeMerge.setStatus(AuthorFeeStatusEnum.STATUS_TAX_CALCULATED.getType());
		feeMerge.setAuthortype(feeDetailList.get(0).getAuthortype());
		feeMerge.setAuthorid(feeDetailList.get(0).getAuthorid());
		feeMerge.setAuthorname(feeDetailList.get(0).getAuthorname());
		feeMerge.setAuthoridnumber(isIdnumberMerage ? idnumber : "");
		feeMerge.setTaxpayeridnumber(isIdnumberMerage ? "" : taxIdnumber);
		feeMerge.setTaxtype(feeDetailList.get(0).getTaxtype());
		feeMerge.setTimes(1);
		feeMerge.setFeebdgdept(feeDetailList.stream().map(BudgetAuthorfeedetail::getFeebdgdept).distinct().collect(Collectors.joining(",")));
		feeMerge.setBusinessgroup(feeDetailList.stream().map(BudgetAuthorfeedetail::getBusinessgroup).distinct().collect(Collectors.joining(",")));
		feeMerge.setReimbursesubject(subjectname);
		feeMerge.setCreatetime(new Date());
		BigDecimal copeFeesum = feeDetailList.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
		feeMerge.setCopefee(copeFeesum);
		this.mergeMapper.insert(feeMerge);
		return feeMerge;
	}


	private void clearFeeTaxData(List<BudgetAuthorfeedetail> feeDetails) {
		//稿费合并的列表
		List<Long> feeMergeIdList = feeDetails.stream().map(e -> e.getAuthormergeid()).distinct().collect(Collectors.toList());
		List<BudgetAuthorfeedtlMerge> feeMergeList = this.mergeMapper.selectList(new QueryWrapper<BudgetAuthorfeedtlMerge>().in("id", feeMergeIdList));
		List<Long> reportIds = feeMergeList.stream().map(e -> e.getReportid()).collect(Collectors.toList());
		if (!reportIds.isEmpty()) this.feeReportMapper.deleteBatchIds(reportIds);
		if (!feeMergeIdList.isEmpty()) {
			List<Long> detailIdList = feeDetails.stream().map(e -> e.getId()).distinct().collect(Collectors.toList());
			budgetAuthorfeesumMapper.setAuthormergeidIsNull(detailIdList);
			this.mergeMapper.deleteBatchIds(feeMergeIdList);
		}
	}

	/**
	 * 生成报销单
	 *
	 * @param queryArr
	 * @throws Exception
	 */
	public void generateAuthorFeeReim(String[] queryArr, String ids) throws Exception {

		BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
		String feeMonth = BudgetExtractsumService.getExtractMonth(Integer.valueOf(yearPeriod.getCode()), Integer.valueOf(queryArr[1]));

		/**
		 * 获取当前稿费月份的所有稿费
		 */
		List<BudgetAuthorfeesum> curMonthFeeSumList = this.budgetAuthorfeesumMapper.selectList(new QueryWrapper<BudgetAuthorfeesum>().eq("feemonth", feeMonth));

		List<String> checkedFeeSumIdList = Arrays.asList(ids.split(","));

		List<BudgetAuthorfeesum> curCheckFeeSumList = curMonthFeeSumList.stream().filter(e -> checkedFeeSumIdList.contains(e.getId().toString())).collect(Collectors.toList());
		String sumIds = curCheckFeeSumList.stream().map(e -> e.getId()).sorted((e1, e2) -> Long.compare(e1, e2)).map(e -> e.toString()).collect(Collectors.joining("-"));
		Map<Integer, List<BudgetAuthorfeesum>> statusMap = curCheckFeeSumList.stream().collect(Collectors.groupingBy(e -> e.getStatus()));
		Integer status = statusMap.keySet().stream().findFirst().get();
		if (status == AuthorFeeStatusEnum.STATUS_REIMBURSED.getType()) throw new RuntimeException("报销失败！该稿费批次已经报销！");
		AuthorFeeReimCommonData commonData = new AuthorFeeReimCommonData();
		commonData.setSumIds(sumIds);
		commonData.setFeeMonth(feeMonth);
		//校验
		validateIsCanReim(curCheckFeeSumList, statusMap, curMonthFeeSumList, commonData);

		/**
		 * 填充报销的一些通用数据 
		 */
		populateReimCommonData(commonData, yearPeriod, checkedFeeSumIdList);
		//报销前的一些验证
		validateBeforeReim(commonData);


		//if(status == AuthorFeeStatusEnum.STATUS_TAX_CALCULATED.getType()) {
		/**
		 * 计税状态下报销
		 */
		//创建报销主表数据
		createReimbursementMainData(commonData);
		//创建报销转账
		createReimbursementTrans(commonData);
		//创建划拨明细
		createReimbursementAllocated(commonData);
		//创建报销明细
		createReimbursementDetails(commonData);
		//检测预算是否足够。
		checkBudgetIsEnough(commonData);

		BigDecimal reimMoney = commonData.getBxDetailList().stream().map(e -> e.getReimmoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal transMoney = commonData.getTransList().stream().map(e -> e.getTransmoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal allocatedMoney = commonData.getAllocatedList().stream().map(e -> e.getAllocatedmoney()).reduce(BigDecimal.ZERO, BigDecimal::add);

		BudgetReimbursementorder curBxOrder = commonData.getCurCreatedBxOrder();
		this.service.checkIsMonthEnd(curBxOrder, commonData.getAllocatedList());
		curBxOrder.setReuqeststatus(StatusConstants.BX_SUBMIT);
		curBxOrder.setReimmoney(reimMoney);
		curBxOrder.setTransmoney(transMoney);
		curBxOrder.setNonreimmoney(reimMoney);
		curBxOrder.setAllocatedmoney(allocatedMoney);
		curBxOrder.setOthermoney(commonData.getOthermoney());

		/**
		 * 增加流程环节
		 */
		ReimbursementRequest request = new ReimbursementRequest();
		request.setOrder(curBxOrder);
		request.setOrderDetail(commonData.getBxDetailList());
		request.setOrderAllocated(commonData.getAllocatedList());
		String workFlowStep = this.orderService.ensureWorkFlowStep(request);
		curBxOrder.setWorkFlowStep(workFlowStep);
		TabProcedure tabProcedure = this.procedureService.getCurrentProcedure(request.getOrder().getYearid(), "1");
		Optional.ofNullable(tabProcedure).orElseThrow(() -> new RuntimeException("生成报销失败！此届别下没有流程模板,请联系财务系统管理员添加！"));
		Integer conditionVersion = tabProcedure.getId().intValue();
		curBxOrder.setWorkFlowVersion(conditionVersion);
		this.reimbursementMapper.updateById(curBxOrder);

		//}else if(status == AuthorFeeStatusEnum.STATUS_REIMBURSED.getType()) {
		//报销状态下。（重复报销）
		//}
		curCheckFeeSumList.forEach(e -> {
			e.setStatus(AuthorFeeStatusEnum.STATUS_REIMBURSED.getType());
			this.budgetAuthorfeesumMapper.updateById(e);
		});
	}

	/**
	 * 校验预算是否足够报销
	 *
	 * @param commonData
	 */
	private void checkBudgetIsEnough(AuthorFeeReimCommonData commonData) {
		Map<String, BigDecimal> moneyMap = new HashMap<>();
		commonData.getAllocatedList().stream().collect(Collectors.groupingBy(e -> e.getMonthagentid())).forEach((monthagentid, allocatedList) -> {
			BigDecimal money = allocatedList.stream().map(e -> e.getAllocatedmoney()).reduce(BigDecimal.ZERO, BigDecimal::add);
			Long unitId = allocatedList.get(0).getUnitid();
			Long subjectId = allocatedList.get(0).getSubjectid();

			if (moneyMap.get(unitId + "_" + subjectId) == null) {
				moneyMap.put(unitId + "_" + subjectId, money);
			} else {
				moneyMap.put(unitId + "_" + subjectId, money.add(moneyMap.get(unitId + "_" + subjectId)));
			}

			BudgetUnitSubject unitSubject = unitSubjectMapper.selectOne(new QueryWrapper<BudgetUnitSubject>().eq("unitid", unitId).eq("subjectid", subjectId));
			BudgetMonthAgent monthAgent = this.monthAgentMapper.selectById(monthagentid);
			BudgetYearAgent yearAgent = this.yearAgentMapper.selectById(monthAgent.getYearagentid());
			if (unitSubject.getYearcontrolflag()) {
				/**
				 * 年度动因控制
				 */
				//审核中的数据
				List<Map<String, Object>> verifyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_SUBMIT.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString())
						&& e.get("monthagentid").toString().equals(monthagentid.toString()))
						.collect(Collectors.toList());
				//已报销的数据
				List<Map<String, Object>> alreadyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_PASS.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString())
						&& e.get("monthagentid").toString().equals(monthagentid.toString()))
						.collect(Collectors.toList());
				BigDecimal lockedMoney = BigDecimal.ZERO;
				BigDecimal executedMoney = BigDecimal.ZERO;

				if (!verifyBxDataList.isEmpty()) {
					lockedMoney = verifyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}
				if (!alreadyBxDataList.isEmpty()) {
					executedMoney = alreadyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}
				//年度可用余额
				BigDecimal ye = yearAgent.getTotal().add(yearAgent.getAddmoney())
						.add(yearAgent.getLendinmoney())
						.subtract(yearAgent.getLendoutmoney())
						.subtract(executedMoney)
						.subtract(lockedMoney);
				if (ye.compareTo(money) < 0) {
					//余额小于当前报销金额
					String errorMsg = "预算单位【" + allocatedList.get(0).getUnitname() + "】动因【" + monthAgent.getName() + "】年度可用预算【" + ye.stripTrailingZeros().toPlainString() + "】不足以报销金额【" + money.stripTrailingZeros().toPlainString() + "】";
					if (lockedMoney.compareTo(BigDecimal.ZERO) > 0)
						errorMsg = errorMsg + ",其中锁定金额为【" + lockedMoney.stripTrailingZeros().toPlainString() + "】";
					throw new RuntimeException(errorMsg);
				}
			}

			if (unitSubject.getMonthcontrolflag()) {
				//月度科目控制
				BudgetMonthSubject monthSubject = this.monthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unitId).eq("subjectid", subjectId).eq("monthid", monthAgent.getMonthid()));
				//审核中的数据
				List<Map<String, Object>> verifyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_SUBMIT.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString())
						&& e.get("monthid").toString().equals(monthAgent.getMonthid().toString()))
						.collect(Collectors.toList());
				//已报销的数据
				List<Map<String, Object>> alreadyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_PASS.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString())
						&& e.get("monthid").toString().equals(monthAgent.getMonthid().toString()))
						.collect(Collectors.toList());
				BigDecimal lockedMoney = BigDecimal.ZERO;
				BigDecimal executedMoney = BigDecimal.ZERO;

				if (!verifyBxDataList.isEmpty()) {
					lockedMoney = verifyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}
				if (!alreadyBxDataList.isEmpty()) {
					executedMoney = alreadyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}

				//科目月度可用余额
				BigDecimal ye = monthSubject.getTotal().add(monthSubject.getAddmoney())
						.add(monthSubject.getLendinmoney())
						.subtract(monthSubject.getLendoutmoney())
						.subtract(executedMoney)
						.subtract(lockedMoney);
				if (ye.compareTo(moneyMap.get(unitId + "_" + subjectId)) < 0) {
					//余额小于当前报销金额
					String errorMsg = "预算单位【" + allocatedList.get(0).getUnitname() + "】动因【" + monthAgent.getName() + "】科目月度可用预算【" + ye.stripTrailingZeros().toPlainString() + "】不足以报销金额【" + moneyMap.get(unitId + "_" + subjectId).stripTrailingZeros().toPlainString() + "】";
					if (lockedMoney.compareTo(BigDecimal.ZERO) > 0)
						errorMsg = errorMsg + ",其中锁定金额为【" + lockedMoney.stripTrailingZeros().toPlainString() + "】";
					throw new RuntimeException(errorMsg);
				}
			}

			if (unitSubject.getYearsubjectcontrolflag()) {
				/**
				 * 年度科目控制
				 */
				BudgetYearSubject yearSubject = this.yearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitId).eq("subjectid", subjectId));
				//审核中的数据
				List<Map<String, Object>> verifyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_SUBMIT.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString()))
						.collect(Collectors.toList());
				//已报销的数据
				List<Map<String, Object>> alreadyBxDataList = commonData.getExecuteDataList().stream().filter(e -> StatusConstants.BX_PASS.toString().equals(e.get("reuqeststatus").toString())
						&& e.get("unitid").toString().equals(unitId.toString())
						&& e.get("subjectid").toString().equals(subjectId.toString()))
						.collect(Collectors.toList());
				BigDecimal lockedMoney = BigDecimal.ZERO;
				BigDecimal executedMoney = BigDecimal.ZERO;

				if (!verifyBxDataList.isEmpty()) {
					lockedMoney = verifyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}
				if (!alreadyBxDataList.isEmpty()) {
					executedMoney = alreadyBxDataList.stream().map(e -> new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
				}

				//年度科目可用余额
				BigDecimal ye = yearSubject.getTotal().add(yearSubject.getAddmoney())
						.add(yearSubject.getLendinmoney())
						.subtract(yearSubject.getLendoutmoney())
						.subtract(executedMoney)
						.subtract(lockedMoney);
				if (ye.compareTo(moneyMap.get(unitId + "_" + subjectId)) < 0) {
					//余额小于当前报销金额
					String errorMsg = "预算单位【" + allocatedList.get(0).getUnitname() + "】动因【" + monthAgent.getName() + "】科目年度可用预算【" + ye.stripTrailingZeros().toPlainString() + "】不足以报销金额【" + moneyMap.get(unitId + "_" + subjectId).stripTrailingZeros().toPlainString() + "】";
					if (lockedMoney.compareTo(BigDecimal.ZERO) > 0)
						errorMsg = errorMsg + ",其中锁定金额为【" + lockedMoney.stripTrailingZeros().toPlainString() + "】";
					throw new RuntimeException(errorMsg);
				}
			}
		});
	}

	@Autowired
	private BudgetYearSubjectMapper yearSubjectMapper;

	@Autowired
	private BudgetUnitSubjectMapper unitSubjectMapper;

	/**
	 * 创建报销明细
	 *
	 * @param commonData
	 */
	private void createReimbursementDetails(AuthorFeeReimCommonData commonData) {
		commonData.getCurCheckMergeFeeDetailList().stream()
				.collect(Collectors.groupingBy(e -> e.getPayunitid() + SPLIT_SYMBOL + e.getReimbursesubject()))
				.forEach((key, details) -> {

					String[] keyArr = key.split(SPLIT_SYMBOL);
					String payunitId = keyArr[0];
					String subjectName = keyArr[1];
					BudgetReimbursementorderDetail reimDetail = new BudgetReimbursementorderDetail();
					reimDetail.setReimbursementid(commonData.getCurCreatedBxOrder().getId());


					List<Long> mergeIdList = details.stream().map(detail -> detail.getId()).collect(Collectors.toList());
					List<BudgetAuthorfeedetail> feeDetails = commonData.getCurCheckFeeDetailList().stream().filter(detail -> mergeIdList.contains(detail.getAuthormergeid())).collect(Collectors.toList());

					Long subjectid = commonData.getCurCheckFeeDetailList().stream().filter(e -> e.getAuthormergeid().toString()
							.equals(details.get(0).getId().toString())).findFirst().get().getSubjectid();

					List<BudgetMonthAgent> monthAgentList = this.monthAgentMapper.selectList(new QueryWrapper<BudgetMonthAgent>().eq("unitid", commonData.getCurCreatedBxOrder().getUnitid()
					).eq("monthid", commonData.getCurCreatedBxOrder().getMonthid())
							.eq("subjectid", subjectid));
					if (monthAgentList.isEmpty())
						throw new RuntimeException(commonData.getCurCreatedBxOrder().getUnitName() + "下科目【" + subjectName + "】没有" + commonData.getCurCreatedBxOrder().getMonthid() + "月月度动因");
					reimDetail.setMonthagentid(monthAgentList.get(0).getId());
					if (CONTRIBUTION_FEE.equals(subjectName)) {
						reimDetail.setMonthagentname(CONTRIBUTION_FEE);
						reimDetail.setRemark("写稿");
					} else if (EXTERNAL_AUDIT_FEE.equals(subjectName)) {
						reimDetail.setMonthagentname(EXTERNAL_AUDIT_FEE);
						reimDetail.setRemark("审稿及制作费");
					} else if (CONTRIBUTION_FEE_NEXT.equals(subjectName)) {
						reimDetail.setMonthagentname(CONTRIBUTION_FEE_NEXT);
						reimDetail.setRemark("写稿");
					} else if (EXTERNAL_AUDIT_FEE_NEXT.equals(subjectName)) {
						reimDetail.setMonthagentname(EXTERNAL_AUDIT_FEE_NEXT);
						reimDetail.setRemark("审稿及制作费");
					} else {
						reimDetail.setMonthagentname(monthAgentList.get(0).getName());
						reimDetail.setRemark("稿费的备注");
					}
					BudgetSubject subject = subjectMapper.selectOne(new QueryWrapper<BudgetSubject>().eq("name", subjectName).eq("yearid", commonData.getCurCreatedBxOrder().getYearid()));
					reimDetail.setSubjectid(subject.getId());
					reimDetail.setSubjectname(subjectName);
					reimDetail.setBunitid(Long.valueOf(payunitId));
					reimDetail.setBunitname(details.get(0).getPayunit());
					reimDetail.setReimflag(false);
					BigDecimal dtlMoney = feeDetails.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
					reimDetail.setReimmoney(dtlMoney);
					BudgetMonthAgent monthAgent = monthAgentList.get(0);
					reimDetail.setMonthagentmoney(monthAgent.getTotal());
					reimDetail.setMonthagentunmoney(monthAgent.getTotal().add(monthAgent.getLendinmoney()).add(monthAgent.getAddmoney())
							.subtract(monthAgent.getExecutemoney()).subtract(monthAgent.getLendoutmoney()));
					BudgetYearAgent yearAgent = this.yearAgentMapper.selectById(monthAgent.getYearagentid());
					reimDetail.setYearagentmoney(yearAgent.getTotal());
					reimDetail.setYearagentunmoney(yearAgent.getTotal().add(yearAgent.getLendinmoney()).add(yearAgent.getAddmoney())
							.subtract(yearAgent.getExecutemoney()).subtract(yearAgent.getLendoutmoney()));
					reimDetailMapper.insert(reimDetail);
					commonData.getBxDetailList().add(reimDetail);
				});
	}

	public static void main(String[] args) {
		String abc = "123&%456767&%$67677";
		int length = abc.split(SPLIT_SYMBOL).length;
		int length1 = abc.split(SPLIT_SYMBOL1).length;
		System.out.println(length);
		System.out.println(length1);
	}

	/**
	 * 创建划拨明细
	 *
	 * @param commonData
	 */
	private void createReimbursementAllocated(AuthorFeeReimCommonData commonData) {

		commonData.getCurCheckFeeDetailList().stream().collect(Collectors.groupingBy(
				detail -> detail.getFeebdgdeptid() + SPLIT_SYMBOL + detail.getSubjectid() + SPLIT_SYMBOL + detail.getAgentname()))
				.forEach((key, details) -> {
					BudgetReimbursementorderAllocated reimAllocated = new BudgetReimbursementorderAllocated();//要使用提报部门的id
					String[] keyArr = key.split(SPLIT_SYMBOL);
					//划拨部门
					Long feebdgdeptid = Long.valueOf(keyArr[0]);
					//划拨的科目
					String subjectId = keyArr[1];
					//动因名称
					String agentName = keyArr[2];
					Long subjectid = details.get(0).getSubjectid();

					BudgetMonthAgent monthAgent = monthAgentMapper.selectOne(new QueryWrapper<BudgetMonthAgent>().eq("yearid", commonData.getBxUnit().getYearid()).eq("unitid", feebdgdeptid).eq("name", agentName).eq("monthid", commonData.getCurCreatedBxOrder().getMonthid()).eq("subjectid", subjectId));
					if (Objects.isNull(monthAgent)) throw new RuntimeException("月度产品动因【" + agentName + "】不存在");

					reimAllocated.setReimbursementid(commonData.getCurCreatedBxOrder().getId());
					reimAllocated.setMonthagentid(monthAgent.getId());
					reimAllocated.setMonthagentname(monthAgent.getName());
					reimAllocated.setRemark("稿费划拨备注");
					reimAllocated.setSubjectid(subjectid);
					reimAllocated.setSubjectname(details.get(0).getReimbursesubject());
					reimAllocated.setUnitid(details.get(0).getFeebdgdeptid());
					reimAllocated.setUnitname(details.get(0).getFeebdgdept());
					reimAllocated.setReimflag(true);

					BigDecimal alMoney = details.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
					reimAllocated.setAllocatedmoney(alMoney);
					//科目月度信息-----------动因年度信息
					BudgetMonthSubject monthSubject = this.monthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>().eq("yearid", monthAgent.getYearid()).eq("unitid", feebdgdeptid).eq("monthid", monthAgent.getMonthid()).eq("subjectid", subjectid));
					if (null == monthSubject) {
						throw new RuntimeException("科目月度预算还未造！");
					}
					BudgetYearAgent yearAgent = this.yearAgentMapper.selectOne(new QueryWrapper<BudgetYearAgent>().eq("unitid", feebdgdeptid).eq("subjectid", subjectid).eq("name", agentName));
					if (null == yearAgent) {
						throw new RuntimeException("没有年度动因【" + agentName + "】！");
					}
					reimAllocated.setMonthagentmoney(monthSubject.getTotal());
					reimAllocated.setMonthagentunmoney(monthSubject.getTotal().add(monthSubject.getAddmoney()).add(monthSubject.getLendinmoney()).subtract(monthSubject.getLendoutmoney()).subtract(monthSubject.getExecutemoney()));
					reimAllocated.setYearagentmoney(yearAgent.getTotal());
					reimAllocated.setYearagentunmoney(yearAgent.getTotal().add(yearAgent.getAddmoney()).add(yearAgent.getLendinmoney()).subtract(yearAgent.getLendoutmoney()).subtract(yearAgent.getExecutemoney()));
					//allocatedMapper.insert(reimAllocated);
					commonData.getAllocatedList().add(reimAllocated);
				});
		if (!commonData.getAllocatedList().isEmpty()) allocatedService.saveBatch(commonData.getAllocatedList());
	}

	@Autowired
	private BudgetReimbursementorderAllocatedService allocatedService;

	/**
	 * 创建报销转账
	 *
	 * @param commonData
	 */
	private void createReimbursementTrans(AuthorFeeReimCommonData commonData) {
		List<BudgetReimbursementorderTrans> newTrans = new ArrayList<>();
		commonData.getCurCheckMergeFeeDetailList().forEach(e -> {
			/**
			 * 税和稿费转账按百分比计算。
			 * 存在计税时合并算，报销分开报的情况
			 */

			List<BudgetAuthorfeedetail> feeDetails = commonData.getFeeMonthDetails().stream().filter(detail -> detail.getAuthormergeid().equals(e.getId())).collect(Collectors.toList());
			//总的稿费
			BigDecimal copefee = feeDetails.stream().map(f -> f.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);

			List<BudgetAuthorfeedetail> curCheckFeeDetails = commonData.getCurCheckFeeDetailList().stream().filter(detail -> detail.getAuthormergeid().equals(e.getId())).collect(Collectors.toList());
			//不需要转账
			//List<BudgetAuthorfeedetail> notZZDetails = feeDetails.stream().filter(detail->!detail.getNeedzz()).collect(Collectors.toList());
			//BigDecimal notZZMoney = notZZDetails.stream().map(detail->detail.getCopefee()).reduce(BigDecimal.ZERO,BigDecimal::add);
			//转账
			//List<BudgetAuthorfeedetail> zzDetails = feeDetails.stream().filter(detail->detail.getNeedzz()).collect(Collectors.toList());
			//BigDecimal zZMoney = zzDetails.stream().map(detail->detail.getCopefee()).reduce(BigDecimal.ZERO,BigDecimal::add);

			//当前选择的税前稿费
			BigDecimal curCheckTotalCopefee = curCheckFeeDetails.stream().map(f -> f.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			//此次报销的税
			BigDecimal tax = BigDecimal.ZERO;
			if (e.getCopefee().compareTo(BigDecimal.ZERO) > 0) {
				tax = curCheckTotalCopefee.divide(e.getCopefee(), 20, BigDecimal.ROUND_HALF_UP).multiply(e.getTax()).setScale(2, BigDecimal.ROUND_HALF_UP);
			} else {
				throw new RuntimeException("报销失败！" + e.getAuthorname() + "的稿费数据错误");
			}
			BigDecimal curCheckedZZMoney = curCheckFeeDetails.stream().filter(detail -> detail.getNeedzz()).map(detail -> detail.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal curCheckedNotZZMoney = curCheckFeeDetails.stream().filter(detail -> !detail.getNeedzz()).map(detail -> detail.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add);
			if (curCheckedZZMoney.compareTo(BigDecimal.ZERO) == 0) {
				//没有需要转账的数据
				commonData.setOthermoney(commonData.getOthermoney().add(curCheckTotalCopefee));
				return;
			}
			BigDecimal othermoney = BigDecimal.ZERO;
			BudgetReimbursementorderTrans reimTrans = new BudgetReimbursementorderTrans();
			reimTrans.setReimbursementid(commonData.getCurCreatedBxOrder().getId());
			reimTrans.setPayeecode(e.getGathercode());
			reimTrans.setPayeename(e.getGatherunit());
			reimTrans.setPayeebankname(e.getGatherbank());
			reimTrans.setPayeebankaccount(e.getGatherbankaccount());

			if (e.getEdupubflag()) {
				//收款方为教育社时（为第三方公司代发的时候）， 转账金额应该是税前稿酬，而不是税后稿酬
				//减去服务费 TODO
				reimTrans.setTransmoney(curCheckTotalCopefee.subtract(curCheckedNotZZMoney));
				othermoney = othermoney.add(curCheckedNotZZMoney);
				reimTrans.setTax(BigDecimal.ZERO);
			} else {
				reimTrans.setTransmoney(curCheckTotalCopefee.subtract(tax).subtract(curCheckedNotZZMoney));
				othermoney = othermoney.add(curCheckedNotZZMoney).add(tax);
				reimTrans.setTax(tax);
			}
			commonData.setOthermoney(othermoney.add(commonData.getOthermoney()));
			reimTrans.setDraweeunitaccountid(e.getPayunitaccountid());//付款单位账户id
			reimTrans.setOlddraweeunitaccountid(e.getPayunitaccountid());
			reimTrans.setDraweeunitname(e.getPayunit());
			reimTrans.setDraweebankaccount(e.getPaybankaccount());
			reimTrans.setDraweebankname(e.getPaybank());
			//transMapper.insert(reimTrans);
			commonData.getTransList().add(reimTrans);
		});
		if (!commonData.getTransList().isEmpty()) transService.saveBatch(commonData.getTransList());
	}

	@Autowired
	private BudgetReimbursementorderTransService transService;


	/**
	 * 创建报销主数据
	 *
	 * @param commonData
	 * @throws Exception
	 */
	private void createReimbursementMainData(AuthorFeeReimCommonData commonData) throws Exception {
		BudgetReimbursementorder reimOrder = new BudgetReimbursementorder();
		String yearperiod = commonData.getCurCheckMergeFeeDetailList().get(0).getYearperiod();
		String feemonth = commonData.getCurCheckMergeFeeDetailList().get(0).getFeemonth();
		Long monthid = Long.valueOf(feemonth.substring(4, 6));
		//reimOrder.setInterimbatch(feemonth+commonData.getBxUnit().getId()+""); //-----------期间批次
		reimOrder.setInterimbatch(commonData.getFeeMonth() + "|" + commonData.getSumIds());
		reimOrder.setOrderscrtype(ReimbursementFromEnmu.PAYMENT.getCode());
		reimOrder.setYearid(commonData.getBxUnit().getYearid());
		reimOrder.setUnitid(commonData.getBxUnit().getId());//------------------------报销单位id
		reimOrder.setUnitName(commonData.getBxUnit().getName());
		reimOrder.setMonthid(monthid);// 报销月份为导入月份        -------------------------------------------写死为6月份
		//报销人改为操作人，点击“报销”按钮的人
		WbUser curUser = UserThreadLocal.get();
		reimOrder.setReimperonsid(curUser.getUserId());
		reimOrder.setReimperonsname(curUser.getDisplayName());
		reimOrder.setReimdate(new Date());
		reimOrder.setReimmoney(BigDecimal.ZERO);//报销金额
		reimOrder.setNonreimmoney(BigDecimal.ZERO);//不计入执行报销金额
		reimOrder.setTransmoney(BigDecimal.ZERO);//转账金额
		reimOrder.setAllocatedmoney(BigDecimal.ZERO);//划拨金额
		reimOrder.setAttachcount(0);
		reimOrder.setSubmittime(new Date());
		reimOrder.setCreatetime(new Date());
		reimOrder.setApplicantid(curUser.getUserId());//申请人id
		reimOrder.setApplicantame(curUser.getDisplayName());//申请人名字
		reimOrder.setApplicanttime(new Date());
		reimOrder.setBxtype(ReimbursementTypeEnmu.COMMON.getCode());
		reimOrder.setVersion("1");
		reimOrder.setReceivestatus(0);//
		reimOrder.setReuqeststatus(StatusConstants.BX_SAVE);
		reimOrder.setFinancialmanagereceivestatus(false);
		reimOrder.setGeneralmanagereceivestatus(false);
		reimOrder.setFinancialmanagestatus(false);
		reimOrder.setGeneralmanagestatus(false);
		String bxdNum = distributedNumber.getBxdNum();
		reimOrder.setReimcode(bxdNum);
		reimbursementMapper.insert(reimOrder);
		String qrcodebase64str = QRCodeUtil.createBase64Qrcode(this.bx_qrcode_url + reimOrder.getId() + "-" + reimOrder.getVersion(), this.file_temp_path + File.separator + reimOrder.getId() + QRCODE_FORMAT);
		reimOrder.setQrcodebase64str(qrcodebase64str);
		reimbursementMapper.updateById(reimOrder);
		commonData.setCurCreatedBxOrder(reimOrder);
	}

	/**
	 * 准备报销。 报销前的一些验证
	 *
	 * @param commonData
	 * @throws Exception
	 */
	private void validateBeforeReim(AuthorFeeReimCommonData commonData) throws Exception {
		//通过报销科目进行分组
		Map<String, List<BudgetAuthorfeedtlMerge>> subjectMap = commonData.getCurCheckMergeFeeDetailList().stream().collect(Collectors.groupingBy(e -> e.getReimbursesubject()));
		Set<String> subjectNameSet = subjectMap.keySet();
		/**
		 * 验证当前报销单位下是否存在这些报销产品科目。
		 */
		List<Map<String, Object>> budgetSubjectInUnitList = this.budgetAuthorfeesumMapper.getBudgetSubjectInUnit(commonData.getBxUnit().getId(), subjectNameSet);
		List<String> existSubjectNameList = budgetSubjectInUnitList.stream().map(e -> e.get("name").toString()).distinct().collect(Collectors.toList());
		String notExistSubjectNames = subjectNameSet.stream().filter(e -> !existSubjectNameList.contains(e)).collect(Collectors.joining(","));
		if (StringUtils.isNotBlank(notExistSubjectNames)) {
			throw new RuntimeException("操作失败！报销单位【" + commonData.getBxUnit().getName() + "】没有科目【" + notExistSubjectNames + "】");
		}


	}

	/**
	 * 填充报销的一些通用数据
	 *
	 * @param commonData
	 * @param yearPeriod
	 * @param checkedFeeSumIdList
	 */
	private void populateReimCommonData(AuthorFeeReimCommonData commonData, BudgetYearPeriod yearPeriod, List<String> checkedFeeSumIdList) {
		//报销的单位
		BudgetUnit bxUnit = this.unitMapper.selectOne(new QueryWrapper<BudgetUnit>().eq("yearid", yearPeriod.getId()).eq("baseunitid", commonData.getBxBaseUnit().getId()));
		if (bxUnit == null)
			throw new RuntimeException("操作失败！基础单位【" + commonData.getBxBaseUnit().getName() + "】在届别【" + yearPeriod.getPeriod() + "】下没有关联预算单位");
		commonData.setBxUnit(bxUnit);

		List<BudgetAuthorfeedetail> feeMonthDetails = this.budgetAuthorfeedetailMapper.selectList(new QueryWrapper<BudgetAuthorfeedetail>().eq("feemonth", commonData.getFeeMonth()));
		commonData.setFeeMonthDetails(feeMonthDetails);
		//当前勾选的所有稿费明细
		//List<BudgetAuthorfeedetail> curCheckFeeDetailList = this.budgetAuthorfeedetailMapper.selectList(new QueryWrapper<BudgetAuthorfeedetail>().in("authorfeesumid", checkedFeeSumIdList));
		List<BudgetAuthorfeedetail> curCheckFeeDetailList = feeMonthDetails.stream().filter(e -> checkedFeeSumIdList.contains(e.getAuthorfeesumid().toString())).collect(Collectors.toList());


		commonData.setCurCheckFeeDetailList(curCheckFeeDetailList);
		List<BudgetAuthorfeedtlMerge> curCheckMergeFeeDetailList = this.mergeMapper.selectBatchIds(curCheckFeeDetailList.stream().map(e -> e.getAuthormergeid()).collect(Collectors.toList()));
		commonData.setCurCheckMergeFeeDetailList(curCheckMergeFeeDetailList);

		//服务费
		long count = curCheckMergeFeeDetailList.stream().filter(e -> e.getEdupubflag() != null && e.getEdupubflag()).count();
		if (count > 0) {
			TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", SERVICE_FEE));
			if (dm == null || StringUtils.isBlank(dm.getDmValue())) throw new RuntimeException("操作失败！请先联系管理员添加服务费配置！");
			commonData.setServiceFee(new BigDecimal(dm.getDmValue()));
		}

		List<Map<String, Object>> executeDataList = this.budgetAuthorfeesumMapper.getExecuteDatas(yearPeriod.getId());
		commonData.setExecuteDataList(executeDataList);
	}

	/**
	 * 校验是否可以进行报销
	 * 1.勾选的状态必须相同
	 * 2.状态必须是已计税或者已报销
	 * 3.勾选的月份必须相同
	 * 4.验证组
	 *
	 * @param feeSumList
	 * @param statusMap
	 * @param curMonthFeeSumList
	 * @param commonData
	 */
	private void validateIsCanReim(List<BudgetAuthorfeesum> curCheckFeeSumList, Map<Integer, List<BudgetAuthorfeesum>> statusMap, List<BudgetAuthorfeesum> curMonthFeeSumList, AuthorFeeReimCommonData commonData) {
		Map<String, List<BudgetAuthorfeesum>> feeMonthMap = curCheckFeeSumList.stream().collect(Collectors.groupingBy(e -> e.getFeemonth()));
		if (feeMonthMap.size() > 1) throw new RuntimeException("操作失败！请勾选相同月份的单据再进行报销！");
		//获取所选记录中不能报销的记录
		List<BudgetAuthorfeesum> unGenerateReimSumList = curCheckFeeSumList.stream().filter(e -> e.getStatus() < AuthorFeeStatusEnum.STATUS_TAX_CALCULATED.getType()).collect(Collectors.toList());
		if (!unGenerateReimSumList.isEmpty()) throw new RuntimeException("操作失败！所选记录中存在不能报销的数据！");
		if (statusMap.size() > 1) throw new RuntimeException("操作失败！只能选择同一种状态下的稿费进行报销！");
		/**
		 * 验证组
		 * 1。不能跨组
		 * 2。同一个组的必须全部选中
		 */
		//验证报销组
		String bxBaseUnitId = validateBxGroup(curMonthFeeSumList, curCheckFeeSumList);
		commonData.setBxBaseUnit(this.baseUnitMapper.selectById(bxBaseUnitId));
	}

	/**
	 * 验证组
	 * 1。不能跨组
	 * 2。同一个组的必须全部选中
	 *
	 * @param curMonthFeeSumList
	 * @param curCheckFeeSumList
	 * @return
	 */
	public String validateBxGroup(List<BudgetAuthorfeesum> curMonthFeeSumList, List<BudgetAuthorfeesum> curCheckFeeSumList) {
		TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", ReimburseGroupByUnit));
		if (dm == null || StringUtils.isBlank(dm.getDmValue())) throw new RuntimeException("操作失败！请联系管理员维护稿费报销组配置！");
		String[] groupArr = dm.getDmValue().split("\\|");
		Map<Long, BudgetUnit> unitMap = this.unitMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
		List<String> curMonthbaseUnitIdList = curMonthFeeSumList.stream().map(e -> unitMap.get(e.getFeedeptid()).getBaseunitid().toString()).collect(Collectors.toList());
		List<String> curCheckBaseUnitIdList = curCheckFeeSumList.stream().map(e -> unitMap.get(e.getFeedeptid()).getBaseunitid().toString()).collect(Collectors.toList());
		Map<Integer, List<String>> curMonthMap = new HashMap<>(groupArr.length);
		Map<Integer, List<String>> checkMap = new HashMap<>(groupArr.length);


		List<String> outerGroudBaseUnitIdList = new ArrayList<>();
		boolean flag = false;
		for (int i = 0; i < groupArr.length; i++) {
			List<String> curGroupList = Arrays.asList(groupArr[i].split(",")).stream().collect(Collectors.toList());
			List<String> l1 = curMonthbaseUnitIdList.stream().filter(e -> curGroupList.contains(e)).sorted((e1, e2) -> Integer.compare(Integer.valueOf(e1), Integer.valueOf(e2))).collect(Collectors.toList());
			l1.add(i + "");
			curMonthMap.put(i, l1);
			List<String> l2 = curCheckBaseUnitIdList.stream().filter(e -> curGroupList.contains(e)).sorted((e1, e2) -> Integer.compare(Integer.valueOf(e1), Integer.valueOf(e2))).collect(Collectors.toList());
			l2.add(i + "");
			/**
			 * 找出组内不存在的部门
			 */
			if (l2.size() > 1) {
				outerGroudBaseUnitIdList.addAll(curCheckBaseUnitIdList.stream().filter(e -> !curGroupList.contains(e)).collect(Collectors.toList()));
			}
			checkMap.put(i, l2);
		}
		List<List<String>> list = checkMap.values().stream().filter(e -> e.size() > 1).collect(Collectors.toList());
		if (list.size() > 1) throw new RuntimeException("操作失败！请选择同一部门组的稿费进行报销！");
		if (list.size() == 0)
			throw new RuntimeException("操作失败！存在没有配置组的部门,请联系财务系统管理员。错误标记：" + curCheckBaseUnitIdList.stream().distinct().collect(Collectors.joining(",")));
		Integer k = Integer.valueOf(list.get(0).get(list.get(0).size() - 1));
		String a = curMonthMap.get(k).stream().collect(Collectors.joining(","));
		String b = checkMap.get(k).stream().collect(Collectors.joining(","));
		if (!a.equals(b)) throw new RuntimeException("操作失败！请将当前组的稿费全部选中后再进行报销！");
		if (!outerGroudBaseUnitIdList.isEmpty())
			throw new RuntimeException("操作失败！存在没有配置组的部门,请联系财务系统管理员。错误标记：" + outerGroudBaseUnitIdList.stream().collect(Collectors.joining(",")));
		/**
		 * 报销的预算单位默认取匹配组的第一个
		 */
		return groupArr[k].split(",")[0];
	}

	/**
	 * 对稿费主表进行分组。基于基础单位组的配置
	 *
	 * @param feeSumList
	 * @return
	 */
	public List<List<BudgetAuthorfeesum>> splitGroupFeeSum(List<BudgetAuthorfeesum> feeSumList) {
		TabDm dm = dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", AUTHORPAYRULE).eq("dm", ReimburseGroupByUnit));
		if (dm == null || StringUtils.isBlank(dm.getDmValue())) throw new RuntimeException("请联系管理员维护稿费报销组配置！");
		String[] groupArr = dm.getDmValue().split("\\|");
		List<Long> curMonthFeeUnitIdList = feeSumList.stream().map(e -> e.getFeedeptid()).collect(Collectors.toList());
		List<BudgetUnit> curMonthUnitList = this.unitMapper.selectBatchIds(curMonthFeeUnitIdList);
		List<String> curMonthbaseUnitIdList = curMonthUnitList.stream().map(e -> e.getBaseunitid().toString()).distinct().collect(Collectors.toList());
		List<List<BudgetAuthorfeesum>> groupList = new ArrayList<>();
		for (int i = 0; i < groupArr.length; i++) {
			if (StringUtils.isBlank(groupArr[i])) continue;
			List<String> curGroupBaseUnitIdList = Arrays.asList(groupArr[i].split(","));
			String resultGroupBaseUnitIds = curMonthbaseUnitIdList.stream().filter(e -> curGroupBaseUnitIdList.contains(e)).distinct().sorted(Comparator.comparing(e -> Integer.valueOf(e))).collect(Collectors.joining(","));
			List<String> unitIdList = curMonthUnitList.stream().filter(e -> ("," + resultGroupBaseUnitIds + ",").contains("," + e.getBaseunitid() + ",")).map(e -> e.getId().toString()).collect(Collectors.toList());
			List<BudgetAuthorfeesum> groupFeeSumList = feeSumList.stream().filter(e -> unitIdList.contains(e.getFeedeptid().toString())).collect(Collectors.toList());
			if (groupFeeSumList.isEmpty()) {
				groupList.add(Lists.newArrayList());
				continue;
			}
			groupList.add(groupFeeSumList);
		}
		return groupList;
	}

	public PageResult<AuthorFeeReportVO> getAuthorfeeReportList(Long yearid, Integer page, Integer rows) {
		Page<AuthorFeeReportVO> pageCond = new Page<AuthorFeeReportVO>(page, rows);
		List<AuthorFeeReportVO> list = this.budgetAuthorfeesumMapper.getAuthorfeeReportList(pageCond, yearid);
		return PageResult.apply(pageCond.getTotal(), list);
	}

	public PageResult<AuthorFeeCalTaxDetailExcelData> getAuthorFeeCalTaxDetailList(Map<String, Object> params,
	                                                                               Integer page, Integer rows) {
		Page<AuthorFeeCalTaxDetailExcelData> pageCond = new Page<AuthorFeeCalTaxDetailExcelData>(page, rows);
		List<AuthorFeeCalTaxDetailExcelData> list = this.budgetAuthorfeesumMapper.getAuthorFeeCalTaxDetailList(pageCond, params);
		List<Long> mergeIdList = list.stream().map(AuthorFeeCalTaxDetailExcelData::getId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(mergeIdList)) {
			Map<Long, List<BudgetAuthorfeedetail>> feeDetailMap = budgetAuthorfeedetailMapper.selectList(new LambdaQueryWrapper<BudgetAuthorfeedetail>().in(BudgetAuthorfeedetail::getAuthormergeid, mergeIdList)).stream().collect(Collectors.groupingBy(BudgetAuthorfeedetail::getAuthormergeid));
			list.forEach(data -> {
				List<BudgetAuthorfeedetail> budgetAuthorfeedetails = feeDetailMap.get(data.getId());
				List<String> showMsg = new ArrayList<>();
				if (CollectionUtils.isEmpty(budgetAuthorfeedetails)) return;
				Map<String, List<BudgetAuthorfeedetail>> curFeeDetailMap = budgetAuthorfeedetails.stream().collect(Collectors.groupingBy(BudgetAuthorfeedetail::getFeebdgdept));
				curFeeDetailMap.forEach((deptName, feeDetails) -> {
					BigDecimal realCopeFee = feeDetails.stream().map(e -> {
						BigDecimal copefee = e.getCopefee() == null ? BigDecimal.ZERO : e.getCopefee();
						BigDecimal tax = e.getTax() == null ? BigDecimal.ZERO : e.getTax();
						return copefee.subtract(tax);
					}).reduce(BigDecimal.ZERO, BigDecimal::add);
					showMsg.add(deptName + "(" + realCopeFee.stripTrailingZeros().toPlainString() + ")");
				});
				data.setFeebdgdept(showMsg.stream().collect(Collectors.joining(",")));
			});
		}
		return PageResult.apply(pageCond.getTotal(), list);
	}

	public List<AuthorFeeCalTaxDetailExcelData> getAuthorFeeCalTaxDetailListNoPage(Map<String, Object> params) {
		List<AuthorFeeCalTaxDetailExcelData> list = this.budgetAuthorfeesumMapper.getAuthorFeeCalTaxDetailList(null, params);
		return list;
	}

	public List<Map<String, Object>> getUnitSubjects() {
		return budgetAuthorfeesumMapper.getBudgetSubjectInUnit(null, null);
	}

	/**
	 * 获取稿费左侧导航栏
	 *
	 * @return
	 */
	public List<AuthorFeePeriodNavigateTreeVO> getAuthorFeePeriodNavigateTree() {
		//获取届别列表
		List<AuthorFeePeriodNavigateTreeVO> yearList = yearMapper.selectList(new QueryWrapper<BudgetYearPeriod>().orderByDesc("code"))
				.stream().map(e -> {
					AuthorFeePeriodNavigateTreeVO vo = new AuthorFeePeriodNavigateTreeVO();
					vo.setQuery(e.getPeriod().toString());
					vo.setYearCode(e.getCode());
					vo.setParentId("0");
					vo.setText(e.getPeriod());
					vo.setLevel(1);
					vo.setCurYearFlag(e.getCurrentflag());
					return vo;
				}).collect(Collectors.toList());
		List<BudgetMonthPeriod> monthList = monthMapper.selectList(new QueryWrapper<BudgetMonthPeriod>().orderByAsc("orderno"));
		//获取已导入的稿费批次
		//QueryWrapper<BudgetAuthorfeesum> wrapper = new QueryWrapper<>();
		//wrapper.groupBy("feemonth").select("feemonth");
		//List<Map<String,Object>> feemonths = this.budgetAuthorfeesumMapper.selectMaps(wrapper);
		//给届别组装月数据
		yearList.stream().forEach(year -> {
			List<AuthorFeePeriodNavigateTreeVO> details = monthList.stream().map(e -> {
				AuthorFeePeriodNavigateTreeVO detailvo = new AuthorFeePeriodNavigateTreeVO();
				detailvo.setQuery(year.getQuery().concat("-").concat(e.getCode().toString()));
				detailvo.setParentId(year.getQuery());
				detailvo.setText(e.getPeriod());
				detailvo.setLevel(2);
				detailvo.setCurYearFlag(year.isCurYearFlag());
				//给月组装稿费批次数据
				//createMonthAuthorChild(feemonths,detailvo,Integer.parseInt(year.getYearCode()),Integer.parseInt(e.getCode()));
				return detailvo;
			}).collect(Collectors.toList());
			year.setChildren(details);
		});

		return yearList;
	}

	private void createMonthAuthorChild(List<Map<String, Object>> extractMonths, AuthorFeePeriodNavigateTreeVO monthMap, Integer yearCode, Integer monthCode) {
		String curExtractMonth = BudgetExtractsumService.getExtractMonth(yearCode, monthCode);
		List<AuthorFeePeriodNavigateTreeVO> curExtractMonthList = extractMonths.stream().filter(e -> e.get("feemonth").toString().startsWith(curExtractMonth)).map(e -> {
			AuthorFeePeriodNavigateTreeVO detailvo = new AuthorFeePeriodNavigateTreeVO();
			detailvo.setQuery(monthMap.getQuery() + "-" + e.get("feemonth").toString());
			detailvo.setParentId(monthMap.getQuery());
			detailvo.setText(e.get("feemonth").toString());
			detailvo.setLevel(3);
			return detailvo;
		}).collect(Collectors.toList());
		monthMap.setChildren(curExtractMonthList);
	}

	@Override
	public Object validate(Integer row, Map<Integer, String> data, String importType, Object head, Object... params) {
		if (AuthorFeeController.AUTHOR_IMPORT.equals(importType)) {
			if (row == 1) {
				//校验导入的表头
				return validateImportTableHead(data);
			} else if (row >= 3) {
				//校验明细数据
				ContributionFeeExcelHead excelHead = (ContributionFeeExcelHead) head;
				ContributionFeeImportCommonData commonData = (ContributionFeeImportCommonData) params[0];
				return validateImportTableDetails(data, excelHead, commonData);
			}
		}
		return head;
	}

	private ContributionFeeExcelDetail validateImportTableDetails(Map<Integer, String> data, ContributionFeeExcelHead excelHead, ContributionFeeImportCommonData commonData) {
		String subjectName = data.get(0); //报销科目
		String isDecutionTax = data.get(1); //是否扣税
		String productForm = data.get(2); //产品形态
		String monthAgentName = data.get(3); //产品预算II类
		String subject = data.get(4); //学科
		String remark = data.get(5); //邀稿内容及去向
		String authorType = data.get(6); //作者类型
		String authorName = data.get(7);//作者
		String authorIdnumber = data.get(8);//身份证号
		String manuscriptQuality = data.get(9);//稿件质量
		String pageNumber = data.get(10);//页/份/题数
		String contributionFeeStandard = data.get(11);//稿酬标准
		String contributionFee = data.get(12);//应发稿酬
		String teacherEmpno = data.get(13);//约稿教师工号
		String teacherEmpname = data.get(14);//约稿教师姓名
		String contributionFeeUnitName = data.get(15);//稿费所属部门
		String ascriptionUnitName = data.get(16);//归属事业群（预算单位）
		String isNeedTran = data.get(17);//是否转账(是/否)

		ContributionFeeExcelDetail detail = new ContributionFeeExcelDetail(subjectName, isDecutionTax, productForm, monthAgentName, subject, remark, authorType, authorName, authorIdnumber, manuscriptQuality, pageNumber, contributionFeeStandard, contributionFee, teacherEmpno, teacherEmpname, contributionFeeUnitName, ascriptionUnitName, isNeedTran);
		String errorInfo = BaseController.validate(detail);
		if (StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
		/**
		 * 校验填写的数据是否正确
		 */
		validateDataDetailIsTrue(detail, excelHead, commonData);

		return detail;
	}

	private void validateDataDetailIsTrue(ContributionFeeExcelDetail detail, ContributionFeeExcelHead head, ContributionFeeImportCommonData commonData) {
		if (!BudgetAuthorfeesumService.TAX_TYPE_YES.equals(detail.getIsDecutionTax()) && !BudgetAuthorfeesumService.TAX_TYPE_NO.equals(detail.getIsDecutionTax()))
			throw new RuntimeException("是否扣税请填写【" + BudgetAuthorfeesumService.TAX_TYPE_YES + "】或【" + BudgetAuthorfeesumService.TAX_TYPE_NO + "】！");
		if (!BudgetAuthorfeesumService.TAX_TYPE_YES.equals(detail.getIsNeedTran()) && !BudgetAuthorfeesumService.TAX_TYPE_NO.equals(detail.getIsNeedTran()))
			throw new RuntimeException("是否转账请填写【" + BudgetAuthorfeesumService.TAX_TYPE_YES + "】或【" + BudgetAuthorfeesumService.TAX_TYPE_NO + "】！");
		/**
		 * 判断报销科目是否在提报部门和划拨部门下
		 */
		List<String> subjectNameList = new ArrayList<>();
		subjectNameList.add(detail.getSubjectName());
		List<Map<String, Object>> list1 = commonData.getUnitSubjects().stream().filter(e -> head.getUnit().getId().toString().equals(e.get("unitid").toString()) && detail.getSubjectName().equals(e.get("name").toString())).collect(Collectors.toList());
		if (list1 == null || list1.isEmpty())
			throw new RuntimeException("提报部门【" + head.getUnit().getName() + "】下无报销科目【" + detail.getSubjectName() + "】");

		/**
		 * 校验产品形态。必须是产品一级分类
		 */
		BudgetProductCategory productCategory = commonData.getProductCategories().stream().filter(e -> detail.getProductForm().equals(e.getName()) && "0".equals(e.getPid().toString())).findFirst().orElseThrow(() -> new RuntimeException("产品形态【" + detail.getProductForm() + "】不存在"));

		BudgetUnit hbUnit = getBudgetUnitByYearAndName(head.getYearPeriod().getId(), detail.getContributionFeeUnitName());
		Optional.ofNullable(hbUnit).orElseThrow(() -> new RuntimeException("届别【" + head.getYearPeriod().getPeriod() + "】" + "稿费所属部门【" + detail.getContributionFeeUnitName() + "】不存在！"));
		detail.setHbUnit(hbUnit);
		List<Map<String, Object>> list2 = commonData.getUnitSubjects().stream().filter(e -> hbUnit.getId().toString().equals(e.get("unitid").toString()) && detail.getSubjectName().equals(e.get("name").toString())).collect(Collectors.toList());
		if (list2 == null || list2.isEmpty())
			throw new RuntimeException("稿费所属部门【" + hbUnit.getName() + "】下无报销科目【" + detail.getSubjectName() + "】");
		String subjectId = list2.get(0).get("id").toString();
		detail.setSubjectId(subjectId);
		/**
		 * 校验产品预算II类。月度的产品动因
		 */
		//获取当前一级分类下所有的产品。
		List<BudgetProductCategory> categoryList = commonData.getProductCategories().stream().filter(e -> e.getPids().startsWith(productCategory.getPids())).collect(Collectors.toList());
		List<Long> categoryIds = categoryList.stream().map(e -> e.getId()).collect(Collectors.toList());
		List<BudgetProduct> productList = commonData.getProducts().stream().filter(e -> categoryIds.contains(e.getProcategoryid()) && e.getStopflag() == 0).collect(Collectors.toList());

		BudgetProduct product = productList.stream().filter(e -> e.getName().equals(detail.getMonthAgentName())).findFirst().orElse(null);
		if (Objects.isNull(product))
			throw new RuntimeException("产品形态【" + detail.getProductForm() + "】下不存在产品【" + detail.getMonthAgentName() + "】");

		BudgetMonthAgent monthAgent = monthAgentMapper.selectOne(new LambdaQueryWrapper<BudgetMonthAgent>()
				.eq(BudgetMonthAgent::getUnitid, hbUnit.getId())
				.eq(BudgetMonthAgent::getProductid, product.getId()).eq(BudgetMonthAgent::getName, product.getName())
				.eq(BudgetMonthAgent::getMonthid, head.getMonthPeriod().getId())
				.eq(BudgetMonthAgent::getSubjectid, subjectId));
		Optional.ofNullable(monthAgent).orElseThrow(() -> new RuntimeException(head.getMonthPeriod().getId() + "月度产品动因【" + product.getName() + "】不存在"));
		detail.setMonthAgent(monthAgent);
		if (!BudgetAuthorfeesumService.AUTHOR_TYPE_INNER.equals(detail.getAuthorType()) && !BudgetAuthorfeesumService.AUTHOR_TYPE_OUTER.equals(detail.getAuthorType()))
			throw new RuntimeException("作者类型请填写【" + BudgetAuthorfeesumService.AUTHOR_TYPE_INNER + "】或【" + BudgetAuthorfeesumService.AUTHOR_TYPE_OUTER + "】！");
		Boolean authorType = false;
		if (BudgetAuthorfeesumService.AUTHOR_TYPE_INNER.equals(detail.getAuthorType())) authorType = true;
		Boolean authorTypeTemp = authorType;
		String authorName = detail.getAuthorName().trim();
		String authorIdnumber = detail.getAuthorIdnumber().replace("x", "X").trim();
		BudgetAuthor author = commonData.getAuthors().stream().filter(e -> e.getAuthortype().toString().equals(authorTypeTemp.toString()) && e.getAuthor().equals(authorName) &&
				(authorIdnumber.equals(e.getIdnumber()) || authorIdnumber.equals(e.getTaxpayernumber())))
				.findFirst().orElseThrow(() -> new RuntimeException("找不到稿费作者【" + authorName + "、" + authorIdnumber + "】"));

		detail.setAuthor(author);
		WbUser user = UserCache.getUserByEmpNo(detail.getTeacherEmpno());
		Optional.ofNullable(user).orElseThrow(() -> new RuntimeException("找不到约稿教师【" + detail.getTeacherEmpname() + "(" + detail.getTeacherEmpno() + ")】"));
		detail.setTeacher(user);
		if (StringUtils.isNotBlank(detail.getAscriptionUnitName())) {
			Optional.ofNullable(getBudgetUnitByYearAndName(head.getYearPeriod().getId(), detail.getAscriptionUnitName())).orElseThrow(() -> new RuntimeException("届别【" + head.getYearPeriod().getPeriod() + "】" + "归属事业群【" + detail.getAscriptionUnitName() + "】不存在！"));
			;
		}
	}

	private BudgetYearPeriod getPeriodByName(String name) {
		BudgetYearPeriod yearPeriod = yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", name));
		return yearPeriod;
	}

	private BudgetUnit getBudgetUnitByYearAndName(Long yearId, String unitname) {
		BudgetUnit budgetUnit = unitMapper.selectOne(new LambdaQueryWrapper<BudgetUnit>().eq(BudgetUnit::getName, unitname).eq(BudgetUnit::getYearid, yearId));
		return budgetUnit;
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

	private ContributionFeeExcelHead validateImportTableHead(Map<Integer, String> data) {
		//表头
		String year = data.get(1); //届别
		String contributionFeeNo = data.get(3); //稿酬编号
		String unitname = data.get(5); //预算单位
		String contributionFeeMonth = data.get(7); //稿费月份
		String bxEmpno = data.get(9); //报销人的工号
		ContributionFeeExcelHead head = new ContributionFeeExcelHead(year, contributionFeeNo, unitname, contributionFeeMonth, bxEmpno);
		String errorInfo = BaseController.validate(head);
		if (StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
		BudgetYearPeriod yearPeriod = getPeriodByName(year);
		if (Objects.isNull(yearPeriod)) throw new RuntimeException("届别【" + year + "】不存在");
		BudgetUnit budgetUnit = getBudgetUnitByYearAndName(yearPeriod.getId(), unitname);
		if (Objects.isNull(budgetUnit)) throw new RuntimeException("届别【" + year + "】不存在预算单位【" + unitname + "】");
		head.setYearPeriod(yearPeriod);
		head.setUnit(budgetUnit);

		/**
		 * 校验稿费月份（格式为202105）
		 */

		if (contributionFeeMonth.length() != 6) throw new RuntimeException("稿费月份格式错误！例【202105】");
		String date = contributionFeeMonth.substring(0, 4) + "-" + contributionFeeMonth.substring(4, 6);
		try {
			Constants.FORMAT_10.parse(date + "-01");
		} catch (Exception e) {
			throw new RuntimeException("稿费月份格式错误！例【202105】");
		}
		/**
		 * 校验稿费月份要填在届别中
		 */
		Integer code = Integer.valueOf(yearPeriod.getCode());
		List<String> periodMonthList = getPeriodMonthList(code);
		if (!periodMonthList.contains(contributionFeeMonth))
			throw new RuntimeException("请根据届别【" + yearPeriod.getPeriod() + "】填写正确的月份。当前届别年为【" + code + "】");

		Integer month = Integer.valueOf(head.getContributionFeeMonth().substring(4, 6));

		BudgetMonthPeriod monthPeriod = monthMapper.selectOne(new LambdaQueryWrapper<BudgetMonthPeriod>().eq(BudgetMonthPeriod::getCode, month.toString()));
		if (Objects.isNull(monthPeriod)) throw new RuntimeException("不存在月【" + month + "】");
		head.setMonthPeriod(monthPeriod);

		WbUser user = UserCache.getUserByEmpNo(head.getBxEmpno());
		if (Objects.isNull(user)) throw new RuntimeException("找不到报销人【" + head.getBxEmpno() + "】");
		head.setBxUser(user);
		/**
		 * 校验报销人是否在此预算单位下
		 */
		validateReimbursementEmp(budgetUnit, head.getBxEmpno());
		/**
		 * 校验当前稿费月份是否能够导入
		 * 只要当前稿费月份存在计税了就不能导入。
		 */
		validateContributionFeeMonthIsCanImport(head.getContributionFeeMonth());
		return head;
	}

	private void validateContributionFeeMonthIsCanImport(String contributionFeeMonth) {
		long count = this.list(null).stream().filter(e -> e.getFeemonth().equals(contributionFeeMonth) && e.getStatus() > AuthorFeeStatusEnum.STATUS_AUDITED.getType()).count();
		if (count > 1) throw new RuntimeException("导入失败！稿费月份【" + contributionFeeMonth + "】中已存在计税后的数据！");
	}

	/**
	 * 校验报销人是否在此预算单位下
	 *
	 * @param unit
	 * @param empNo
	 */
	private void validateReimbursementEmp(BudgetUnit unit, String empNo) {
		String budgetdepts = unit.getBudgetdepts();
		String budgetusers = unit.getBudgetusers();
		boolean isExistUser = false;
		if (StringUtils.isNotBlank(budgetusers)) {
			List<String> budgetUserIdList = Arrays.asList(budgetusers.split(","));
			isExistUser = UserCache.EMPNO_USER_MAP.values().stream().filter(e -> budgetUserIdList.contains(e.getUserId()) && e.getUserName().equals(empNo)).findFirst().isPresent();
		}
		if (isExistUser) return; //人员中匹配上的话直接返回。不用校验部门。提升效率。
		boolean isExistDept = false;
		if (StringUtils.isNotBlank(budgetdepts)) {
			Map<String, WbDept> deptMap = DeptCache.DEPT_MAP.values().stream().collect(Collectors.toMap(WbDept::getDeptId, Function.identity()));
			//所有的子部门
			List<WbDept> allChildDeptList = new ArrayList<>();
			for (String deptid : budgetdepts.split(",")) {
				WbDept dept = deptMap.get(deptid);
				if (null == dept) {
					continue;
				}
				List<WbDept> childrenDepts = DeptCache.DEPT_MAP.values().stream().filter(e -> e.getParentIds().startsWith(dept.getParentIds())).collect(Collectors.toList());
				allChildDeptList.addAll(childrenDepts);
			}
			//子部门的id
			List<String> allChildDeptIdList = allChildDeptList.stream().map(e -> e.getDeptId()).distinct().collect(Collectors.toList());
			isExistDept = PersonCache.EMP_NO_USER_MAP.values().stream().filter(e -> allChildDeptIdList.contains(e.getDeptId()) && e.getPersonCode().equals(empNo)).findFirst().isPresent();
		}
		if (!isExistUser && !isExistDept)
			throw new RuntimeException("预算单位【" + unit.getName() + "】下不存在报销人【" + empNo + "】");
	}

	@Override
	public void saveData(Map<Integer, Map<Integer, String>> successMap, String importType, Map<Integer, Map<Integer, String>> errorMap, List<String> headErrorMsg, Object head, List<Object> importDetails, Object... params) {
		ContributionFeeExcelHead excelHead = (ContributionFeeExcelHead) head;
		List<ContributionFeeExcelDetail> details = importDetails.stream().map(e -> (ContributionFeeExcelDetail) e).collect(Collectors.toList());
		BudgetAuthorfeesum budgetAuthorFeeSum = this.saveAuthorFeeSum(excelHead);
		this.save(budgetAuthorFeeSum);
		/**
		 * 清除数据
		 */
		budgetAuthorfeedetailMapper.delete(new QueryWrapper<BudgetAuthorfeedetail>().eq("authorfeesumid", budgetAuthorFeeSum.getId()));
		if (!CollectionUtils.isEmpty(details)) {
			Map<String, WbBanks> bankMap = bankMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getSubBranchCode(), e -> e, (e1, e2) -> e1));
			List<BudgetAuthorfeedetail> feedetails = new ArrayList<>();
			for (ContributionFeeExcelDetail excelDetail : details) {
				BudgetAuthorfeedetail feeDetail = new BudgetAuthorfeedetail();
				feeDetail.setYearperiod(budgetAuthorFeeSum.getYearperiod());
				feeDetail.setMonthid(budgetAuthorFeeSum.getMonthid());
				feeDetail.setFeemonth(budgetAuthorFeeSum.getFeemonth());
				feeDetail.setAuthorfeesumid(budgetAuthorFeeSum.getId());
				feeDetail.setAuthortype(AUTHOR_TYPE_INNER.equals(excelDetail.getAuthorType()) ? true : false);
				feeDetail.setAuthorid(excelDetail.getAuthor().getId());
				feeDetail.setAuthorname(excelDetail.getAuthor().getAuthor());
				feeDetail.setAuthoridnumber(excelDetail.getAuthor().getIdnumber());
				feeDetail.setTaxpayeridnumber(excelDetail.getAuthor().getTaxpayernumber());
				feeDetail.setAuthorcompany(excelDetail.getAuthor().getCompany());
				WbBanks bank = bankMap.get(excelDetail.getAuthor().getBranchcode());
				if (bank == null)
					throw new RuntimeException("电子银联号【" + excelDetail.getAuthor().getBranchcode() + "】不存在！");
				feeDetail.setAuthorprovince(bank.getProvince());
				feeDetail.setAuthorcity(bank.getCity());
				feeDetail.setBankaccount(excelDetail.getAuthor().getBankaccount());
				feeDetail.setBankaccountbranchcode(excelDetail.getAuthor().getBranchcode());
				feeDetail.setBankaccountbranchname(bank.getSubBranchName());
				feeDetail.setTaxpayeridnumber(excelDetail.getAuthor().getTaxpayernumber());
				feeDetail.setFeestandard(new BigDecimal(excelDetail.getContributionFeeStandard()));
				feeDetail.setCopefee(new BigDecimal(excelDetail.getContributionFee()));
				feeDetail.setTaxtype(TAX_TYPE_YES.equals(excelDetail.getIsDecutionTax()) ? true : false);
				feeDetail.setAgentid(excelDetail.getMonthAgent().getId());
				feeDetail.setAgentname(excelDetail.getMonthAgent().getName());
				feeDetail.setSubjectid(Long.valueOf(excelDetail.getSubjectId()));
				feeDetail.setReimbursesubject(excelDetail.getSubjectName());
				feeDetail.setProducttype(excelDetail.getProductForm());
				feeDetail.setProductbgtcls(excelDetail.getMonthAgentName());
				feeDetail.setEmpid(excelDetail.getTeacher().getUserId());
				feeDetail.setEmpno(excelDetail.getTeacher().getUserName());
				feeDetail.setEmpname(excelDetail.getTeacher().getDisplayName());
				feeDetail.setCreatetime(new Date());
				feeDetail.setSubject(excelDetail.getSubject());
				feeDetail.setContext(excelDetail.getRemark());
				feeDetail.setPaperquality(excelDetail.getManuscriptQuality());
				feeDetail.setPageorcopy(excelDetail.getPageNumber());
				feeDetail.setFeebdgdeptid(excelDetail.getHbUnit().getId());
				feeDetail.setFeebdgdept(excelDetail.getHbUnit().getName());
				feeDetail.setBusinessgroup(excelDetail.getAscriptionUnitName());
				feeDetail.setNeedzz(TAX_TYPE_YES.equals(excelDetail.getIsNeedTran()) ? true : false);
				feedetails.add(feeDetail);
			}
			if (!feedetails.isEmpty()) this.detailService.saveBatch(feedetails);
			budgetAuthorFeeSum.setAuthorfeenum(details.size());
			/**
			 * 统计稿费总额、外审外包总额、待摊-稿费、待摊-外审外包
			 */
			BigDecimal gfTotal = details.stream().filter(e -> e.getSubjectName().equals(CONTRIBUTION_FEE)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setContributionfee(gfTotal);
			BigDecimal wswbTotal = details.stream().filter(e -> e.getSubjectName().equals(EXTERNAL_AUDIT_FEE)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setExternalauditfee(wswbTotal);
			BigDecimal dtgfTotal = details.stream().filter(e -> e.getSubjectName().equals(CONTRIBUTION_FEE_NEXT)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setContributionfeenext(dtgfTotal);
			BigDecimal dtwswbTotal = details.stream().filter(e -> e.getSubjectName().equals(EXTERNAL_AUDIT_FEE_NEXT)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setExternalauditfeenext(dtwswbTotal);

			BigDecimal jsTotal = details.stream().filter(e -> e.getIsDecutionTax().equals(TAX_TYPE_YES)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal bjsTotal = details.stream().filter(e -> e.getIsDecutionTax().equals(TAX_TYPE_NO)).map(e -> new BigDecimal(e.getContributionFee())).reduce(BigDecimal.ZERO, BigDecimal::add);
			budgetAuthorFeeSum.setNeedtaxtotal(jsTotal);
			budgetAuthorFeeSum.setNoneedtaxtotal(bjsTotal);
			budgetAuthorfeesumMapper.updateById(budgetAuthorFeeSum);
		}
	}


	public void resetCalculateTax(String feeMonth) {
		//获取当前月份的所有稿费（考虑一个月多批次稿费的情况）
		List<BudgetAuthorfeesum> feeSumList = this.budgetAuthorfeesumMapper.selectList(new QueryWrapper<BudgetAuthorfeesum>().likeRight("feemonth", feeMonth));

		long count = feeSumList.stream().filter(e -> e.getStatus() <= AuthorFeeStatusEnum.STATUS_AUDITED.getType()).count();
		if (count > 0) throw new RuntimeException("撤回失败！该批次存在计税前的稿费");
		//已经报销的数据
		long alreadyBxCount = feeSumList.stream().filter(e -> e.getStatus() == AuthorFeeStatusEnum.STATUS_REIMBURSED.getType()).count();
		if (alreadyBxCount > 0) {
			/**
			 * 计税时验证是否有报销单已经走完了审核
			 */
			List<BudgetReimbursementorder> bxOrderList = this.reimbursementMapper.selectList(new QueryWrapper<BudgetReimbursementorder>().likeRight("interimbatch", feeMonth));
			String bxOrder = bxOrderList.stream().filter(e -> e.getReuqeststatus() == StatusConstants.BX_PASS).map(e -> e.getReimcode()).collect(Collectors.joining(","));
			if (StringUtils.isNotBlank(bxOrder))
				throw new RuntimeException("撤回失败！稿费月份【" + feeMonth + "】中存在报销单【" + bxOrder + "】已被计入执行！");
			//TODO 删除报销单数据
			bxOrderList.forEach(e -> orderService.delete(e.getId()));
		}
		List<Long> sumIds = feeSumList.stream().map(e -> e.getId()).collect(Collectors.toList());
		//获取所有的稿费明细
		List<BudgetAuthorfeedetail> feeDetails = this.budgetAuthorfeedetailMapper.selectList(new QueryWrapper<BudgetAuthorfeedetail>().in("authorfeesumid", sumIds));
		//清空数据
		clearFeeTaxData(feeDetails);

		feeSumList.stream().forEach(e -> {
			e.setStatus(AuthorFeeStatusEnum.STATUS_UNAUDITED.type);
			this.budgetAuthorfeesumMapper.updateById(e);
		});
	}

	public List<ContributionFeeExportExcelDetail> getBatchContributionFee(String period, String feeMonth) {
		return this.budgetAuthorfeesumMapper.getBatchContributionFee(period,feeMonth);
	}
}
