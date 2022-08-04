package com.jtyjy.finance.manager.controller.authorfee;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.easyexcel.*;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.AuthorFeeReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Api(tags = {"稿费报表"})
@RestController
@RequestMapping("/api/authorfeeReport")
@CrossOrigin
@SuppressWarnings("all")
public class AuthorFeeReportController {

	private final static Logger LOGGER = LoggerFactory.getLogger(AuthorFeeReportController.class);

	@Autowired
	private BudgetAuthorfeesumService authorfeesumService;

	@Autowired
	private BudgetAuthorfeedtlMergeService mergeService;

	@Autowired
	private BudgetAuthorfeedetailService detailService;

	@Autowired
	private BudgetUnitService unitService;

	@Autowired
	private BudgetYearPeriodService yearService;

	@Autowired
	private BudgetYearSubjectService yearSubjectService;

	@Autowired
	private BudgetYearAgentService yearAgentService;

	@Autowired
	private TabDmService dmService;

	private final static String SPLIT_SYMBOL = "&%";

	@ApiOperation(value = "稿费报表首页", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearid", dataType = "Long", required = false)
	})
	@GetMapping("/getAuthorfeeReportList")
	public ResponseEntity<PageResult<AuthorFeeReportVO>> getAuthorfeeReportList(@RequestParam(name = "yearid", required = false) Long yearid,
	                                                                            @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows) {
		try {
			PageResult<AuthorFeeReportVO> pageList = authorfeesumService.getAuthorfeeReportList(yearid, page, rows);
			return ResponseEntity.ok(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "导出稿费入账明细表", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "稿费报表id", name = "reportid", dataType = "Long", required = true)
	})
	@GetMapping("/exportAuthorFeeEntyDetails")
	public void exportAuthorFeeEntyDetails(@RequestParam(name = "reportid", required = true) Long reportid, HttpServletResponse response) throws Exception {
		InputStream is = null;
		InputStream is1 = null;
		try {

			List<BudgetAuthorfeedtlMerge> authorFeeMergeList = mergeService.list(new QueryWrapper<BudgetAuthorfeedtlMerge>().eq("reportid", reportid));
			if (authorFeeMergeList.isEmpty()) throw new RuntimeException("无数据可导出！");
			List<Long> mergeIdList = authorFeeMergeList.stream().map(e -> e.getId()).collect(Collectors.toList());
			//所有的稿费明细
			List<BudgetAuthorfeedetail> feeDetailList = detailService.list(Wrappers.<BudgetAuthorfeedetail>lambdaQuery().in(BudgetAuthorfeedetail::getAuthormergeid, mergeIdList)
					.eq(BudgetAuthorfeedetail::getNeedzz, true));

			//入账汇总表
			List<AuthorFeeEntryDetailExcelData> excelDataSumList = new ArrayList<>();
			List<AuthorFeeEntryDetailExcelData> sum1 = new ArrayList<>();
			List<AuthorFeeEntryDetailExcelData> sum2 = new ArrayList<>();
			List<AuthorFeeEntryDetailExcelData> sum3 = new ArrayList<>();
			List<AuthorFeeEntryDetailExcelData> sum4 = new ArrayList<>();

			//填充sheet1的数据（稿费汇总）
			populateExcelSumData(authorFeeMergeList, excelDataSumList, feeDetailList, null);

			List<Map<String, Object>> feeMergeSplitGrupList = feeMergeSplitGrup(authorFeeMergeList, feeDetailList);
			for (int i = 0; i < feeMergeSplitGrupList.size(); i++) {
				List<String> sumIds = (List<String>) feeMergeSplitGrupList.get(i).get("sumId");
				List<BudgetAuthorfeedtlMerge> list = (List<BudgetAuthorfeedtlMerge>) feeMergeSplitGrupList.get(i).get("data");
				if (i == 0) populateExcelSumData(list, sum1, feeDetailList, sumIds);
				else if (i == 1) populateExcelSumData(list, sum2, feeDetailList, sumIds);
				else if (i == 2) populateExcelSumData(list, sum3, feeDetailList, sumIds);
				else if (i == 3) populateExcelSumData(list, sum4, feeDetailList, sumIds);
			}


			Map<String, Object> params = new HashMap<>();
			params.put("reportid", reportid);
			List<AuthorFeeCalTaxDetailExcelData> calTaxDetailList = this.authorfeesumService.getAuthorFeeCalTaxDetailListNoPage(params);

			FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			is = this.getClass().getClassLoader().getResourceAsStream("template/exportAuthorFeeReport.xlsx");
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("稿费入账明细表", response)).withTemplate(is).build();
			is1 = this.getClass().getClassLoader().getResourceAsStream("template/exportAuthorFeeReport.xlsx");
			List<ReadSheet> sheetList = EasyExcel.read(is1).build().excelExecutor().sheetList();
			for (int i = 0; i < sheetList.size(); i++) {
				ReadSheet readSheet = sheetList.get(i);
				WriteSheet writeSheet = EasyExcel.writerSheet(readSheet.getSheetName()).build();
				if (i == 0) {
					//模板中的表头
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					heads.put("preTaxMoneySum", excelDataSumList.stream().map(e -> e.getPreTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("cbssjTaxSum", excelDataSumList.stream().map(e -> e.getCbssjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("companySjSum", excelDataSumList.stream().map(e -> e.getCompanySjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("gslcSum", excelDataSumList.stream().map(e -> e.getGslc()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("afterTax", excelDataSumList.stream().map(e -> e.getAfterTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					workBook.fill(heads, writeSheet);
					workBook.fill(excelDataSumList, fillConfig, writeSheet);
				} else if (i == 1) {
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					heads.put("preTaxMoneySum", sum1.stream().map(e -> e.getPreTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("cbssjTaxSum", sum1.stream().map(e -> e.getCbssjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("companySjSum", sum1.stream().map(e -> e.getCompanySjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("gslcSum", sum1.stream().map(e -> e.getGslc()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("afterTax", sum1.stream().map(e -> e.getAfterTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					workBook.fill(heads, writeSheet);
					workBook.fill(sum1, fillConfig, writeSheet);
				} else if (i == 2) {
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					heads.put("preTaxMoneySum", sum2.stream().map(e -> e.getPreTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("cbssjTaxSum", sum2.stream().map(e -> e.getCbssjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("companySjSum", sum2.stream().map(e -> e.getCompanySjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("gslcSum", sum2.stream().map(e -> e.getGslc()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("afterTax", sum2.stream().map(e -> e.getAfterTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					workBook.fill(heads, writeSheet);
					workBook.fill(sum2, fillConfig, writeSheet);
				} else if (i == 3) {
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					heads.put("preTaxMoneySum", sum3.stream().map(e -> e.getPreTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("cbssjTaxSum", sum3.stream().map(e -> e.getCbssjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("companySjSum", sum3.stream().map(e -> e.getCompanySjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("gslcSum", sum3.stream().map(e -> e.getGslc()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("afterTax", sum3.stream().map(e -> e.getAfterTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					workBook.fill(heads, writeSheet);
					workBook.fill(sum3, fillConfig, writeSheet);
				} else if (i == 4) {
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					heads.put("preTaxMoneySum", sum4.stream().map(e -> e.getPreTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("cbssjTaxSum", sum4.stream().map(e -> e.getCbssjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("companySjSum", sum4.stream().map(e -> e.getCompanySjTax()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("gslcSum", sum4.stream().map(e -> e.getGslc()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					heads.put("afterTax", sum4.stream().map(e -> e.getAfterTaxMoney()).reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros().toPlainString());
					workBook.fill(heads, writeSheet);
					workBook.fill(sum4, fillConfig, writeSheet);
				} else if (i == sheetList.size() - 1) {
					Map<String, String> heads = new HashMap<>();
					heads.put("yearperiod", authorFeeMergeList.get(0).getYearperiod());
					heads.put("month", authorFeeMergeList.get(0).getMonthid().toString());
					workBook.fill(heads, writeSheet);
					workBook.fill(calTaxDetailList, writeSheet);
				}
			}
			workBook.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
			if (is1 != null) is1.close();
		}
	}

	/**
	 * 分组
	 *
	 * @param authorFeeMergeList
	 */
	private List<Map<String, Object>> feeMergeSplitGrup(List<BudgetAuthorfeedtlMerge> authorFeeMergeList, List<BudgetAuthorfeedetail> feeDetailList) {

		List<Long> feeSumIdList = feeDetailList.stream().map(e -> e.getAuthorfeesumid()).collect(Collectors.toList());
		List<BudgetAuthorfeesum> feeSumList = this.authorfeesumService.listByIds(feeSumIdList);
		//基于组的配置对feeSum通过baseunitid分组 
		List<List<BudgetAuthorfeesum>> feeSumGroupList = this.authorfeesumService.splitGroupFeeSum(feeSumList);
		List<Map<String, Object>> resultList = new ArrayList<>();
		for (int i = 0; i < feeSumGroupList.size(); i++) {
			// 默认排序[研究院,智慧,分销,名校]
			List<BudgetAuthorfeesum> list = feeSumGroupList.get(i);
			List<String> sumIds = list.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
			List<String> mergeIds = feeDetailList.stream().filter(e -> sumIds.contains(e.getAuthorfeesumid().toString())).map(e -> e.getAuthormergeid().toString()).collect(Collectors.toList());
			List<BudgetAuthorfeedtlMerge> resultMergeList = authorFeeMergeList.stream().filter(e -> mergeIds.contains(e.getId().toString())).collect(Collectors.toList());
			Map<String, Object> map = new HashMap<>();
			map.put("sumId", sumIds);
			map.put("data", resultMergeList);
			resultList.add(map);
		}
		return resultList;
	}

	/**
	 * 填充稿费入账报表汇总数据
	 *
	 * @param authorFeeMergeList
	 * @param excelDataSumList
	 */
	private void populateExcelSumData(List<BudgetAuthorfeedtlMerge> authorFeeMergeList, List<AuthorFeeEntryDetailExcelData> excelDataSumList, List<BudgetAuthorfeedetail> feeDetailList, List<String> sumIds) {
		//收款方为教育社的数据
		Map<String, List<BudgetAuthorfeedtlMerge>> pubMergeList = authorFeeMergeList.stream().filter(mm -> mm.getEdupubflag() == true && mm.getPrlautflag() != true).collect(Collectors.groupingBy(BudgetAuthorfeedtlMerge::getPaybankaccount));
		if (null != pubMergeList && pubMergeList.size() > 0) {
			Set<Entry<String, List<BudgetAuthorfeedtlMerge>>> entrySet = pubMergeList.entrySet();
			for (Entry<String, List<BudgetAuthorfeedtlMerge>> entry : entrySet) {
				AuthorFeeEntryDetailExcelData ed = new AuthorFeeEntryDetailExcelData();
				List<BudgetAuthorfeedtlMerge> details = entry.getValue();

				List<String> mergeIds = details.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
				List<BudgetAuthorfeedetail> feedetails = feeDetailList.stream().filter(e -> {
					Boolean flag = false;
					if (sumIds == null) {
						flag = true;
					} else {
						flag = sumIds.contains(e.getAuthorfeesumid().toString());
					}
					return flag && mergeIds.contains(e.getAuthormergeid().toString());
				}).collect(Collectors.toList());
				ed.setPayUnitName(details.get(0).getPayunit());
				ed.setPreTaxMoney(feedetails.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setCbssjTax(feedetails.stream().map(e -> e.getTax()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setCompanySjTax(BigDecimal.ZERO);
				ed.setGslc(BigDecimal.ZERO);
				ed.setAfterTaxMoney(ed.getPreTaxMoney().subtract(ed.getCbssjTax()));
				excelDataSumList.add(ed);
			}
		}

		//付款方为法人公司(过滤掉上面已经计算的收款方为教育社的数据)
		Map<String, List<BudgetAuthorfeedtlMerge>> accountMergeList = authorFeeMergeList.stream().filter(mm -> mm.getEdupubflag() != true && mm.getPrlautflag() != true).collect(Collectors.groupingBy(BudgetAuthorfeedtlMerge::getPaybankaccount));
		if (null != accountMergeList && accountMergeList.size() > 0) {
			Set<Entry<String, List<BudgetAuthorfeedtlMerge>>> entrySet = accountMergeList.entrySet();
			for (Entry<String, List<BudgetAuthorfeedtlMerge>> entry : entrySet) {
				AuthorFeeEntryDetailExcelData ed = new AuthorFeeEntryDetailExcelData();
				List<BudgetAuthorfeedtlMerge> details = entry.getValue();
				List<String> mergeIds = details.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
				List<BudgetAuthorfeedetail> feedetails = feeDetailList.stream().filter(e -> {
					Boolean flag = false;
					if (sumIds == null) {
						flag = true;
					} else {
						flag = sumIds.contains(e.getAuthorfeesumid().toString());
					}
					return flag && mergeIds.contains(e.getAuthormergeid().toString());
				}).collect(Collectors.toList());
				ed.setPayUnitName(details.get(0).getPayunit());
				ed.setPreTaxMoney(feedetails.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setCbssjTax(BigDecimal.ZERO);
				ed.setCompanySjTax(feedetails.stream().map(e -> e.getTax()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setGslc(BigDecimal.ZERO);
				ed.setAfterTaxMoney(ed.getPreTaxMoney().subtract(ed.getCompanySjTax()));
				excelDataSumList.add(ed);
			}
		}


		//付款方为个卡（陈彩莲：扣税）
		Map<String, List<BudgetAuthorfeedtlMerge>> personal_tax_merge = authorFeeMergeList.stream().filter(mm -> mm.getPrlautflag() == true && mm.getTaxtype() == true)
				.collect(Collectors.groupingBy(BudgetAuthorfeedtlMerge::getPaybankaccount));//陈彩莲：扣税
		if (null != personal_tax_merge && personal_tax_merge.size() > 0) {
			Set<Entry<String, List<BudgetAuthorfeedtlMerge>>> value3 = personal_tax_merge.entrySet();
			for (Entry<String, List<BudgetAuthorfeedtlMerge>> entry : value3) {
				AuthorFeeEntryDetailExcelData ed = new AuthorFeeEntryDetailExcelData();
				List<BudgetAuthorfeedtlMerge> details = entry.getValue();
				List<String> mergeIds = details.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
				List<BudgetAuthorfeedetail> feedetails = feeDetailList.stream().filter(e -> {
					Boolean flag = false;
					if (sumIds == null) {
						flag = true;
					} else {
						flag = sumIds.contains(e.getAuthorfeesumid().toString());
					}
					return flag && mergeIds.contains(e.getAuthormergeid().toString());
				}).collect(Collectors.toList());
				ed.setPayUnitName(details.get(0).getPayunit());
				ed.setPreTaxMoney(feedetails.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setCbssjTax(BigDecimal.ZERO);
				ed.setCompanySjTax(BigDecimal.ZERO);
				ed.setGslc(feedetails.stream().map(e -> e.getTax()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setAfterTaxMoney(ed.getPreTaxMoney().subtract(ed.getGslc()));
				excelDataSumList.add(ed);
			}
		}
		//付款方为个卡（陈彩莲：不扣税）
		Map<String, List<BudgetAuthorfeedtlMerge>> personal_notax_merge = authorFeeMergeList.stream().filter(mm -> mm.getPrlautflag() == true && mm.getTaxtype() != true)
				.collect(Collectors.groupingBy(BudgetAuthorfeedtlMerge::getPaybankaccount));//陈彩莲不扣税
		if (null != personal_notax_merge && personal_notax_merge.size() > 0) {
			Set<Entry<String, List<BudgetAuthorfeedtlMerge>>> value4 = personal_notax_merge.entrySet();
			for (Entry<String, List<BudgetAuthorfeedtlMerge>> entry : value4) {

				AuthorFeeEntryDetailExcelData ed = new AuthorFeeEntryDetailExcelData();
				List<BudgetAuthorfeedtlMerge> details = entry.getValue();
				List<String> mergeIds = details.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
				List<BudgetAuthorfeedetail> feedetails = feeDetailList.stream().filter(e -> {
					Boolean flag = false;
					if (sumIds == null) {
						flag = true;
					} else {
						flag = sumIds.contains(e.getAuthorfeesumid().toString());
					}
					return flag && mergeIds.contains(e.getAuthormergeid().toString());
				}).collect(Collectors.toList());
				ed.setPayUnitName(details.get(0).getPayunit());
				ed.setPreTaxMoney(feedetails.stream().map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
				ed.setCbssjTax(BigDecimal.ZERO);
				ed.setCompanySjTax(BigDecimal.ZERO);
				ed.setGslc(BigDecimal.ZERO);
				ed.setAfterTaxMoney(ed.getPreTaxMoney());
				excelDataSumList.add(ed);
			}
		}

	}


	@ApiOperation(value = "导出中心稿费发放明细表（这个导出写在稿费信息那个地方）", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "导航栏查询条件(必传。)", name = "query", dataType = "String", required = true)
	})
	@GetMapping("/exportDeptAuthorFeePayDetails")
	public void exportDeptAuthorFeePayDetails(@RequestParam(name = "query", required = true) String query, HttpServletResponse response) throws Exception {
		InputStream is = null;
		try {
			String[] queryArr = query.split("-");
			if (queryArr.length != 1 || StringUtils.isBlank(query)) throw new RuntimeException("请先选择导航栏中的一个届别！");
			BudgetYearPeriod yearPeriod = yearService.getOne(new QueryWrapper<BudgetYearPeriod>().eq("period", queryArr[0]));
			if (yearPeriod == null) throw new RuntimeException("无效的届别参数");
			List<BudgetAuthorfeedetail> feeDetails = detailService.list(new QueryWrapper<BudgetAuthorfeedetail>().eq("yearperiod", yearPeriod.getPeriod()).ne("id", 47));
			if (feeDetails.isEmpty()) throw new RuntimeException("无数据可导出");

			List<Long> unitIdList = feeDetails.stream().map(e -> e.getFeebdgdeptid()).collect(Collectors.toList());
			List<Long> subjectIdList = feeDetails.stream().map(e -> e.getSubjectid()).collect(Collectors.toList());

			List<BudgetYearSubject> yearSubjectList = yearSubjectService.list(new QueryWrapper<BudgetYearSubject>().in("unitid", unitIdList)
					.eq("yearid", yearPeriod.getId()).in("subjectid", subjectIdList));

			List<BudgetYearAgent> yearAgentList = yearAgentService.list(new QueryWrapper<BudgetYearAgent>().eq("yearid", yearPeriod.getId()).in("subjectid", subjectIdList).in("unitid", unitIdList));
			//根据预算单位+科目+动因分组
			Map<String, List<BudgetYearAgent>> yearAgentMap = yearAgentList.stream().collect(Collectors.groupingBy(e -> {
				return e.getUnitid() + SPLIT_SYMBOL + e.getSubjectid() + SPLIT_SYMBOL + e.getName();
			}));

			WriteSheet sumSheet = EasyExcel.writerSheet("稿费汇总数据").build();

			List<List<String>> oneSheetHeadList = getOneSheetHead(yearPeriod.getPeriod());
			Map<String, List<BudgetYearSubject>> yearSubjectMap = yearSubjectList.stream().collect(Collectors.groupingBy(e -> e.getUnitid() + SPLIT_SYMBOL + e.getSubjectid()));
			//第一个sheet中的数据
			List<AuthorFeeDeptSumExcelData> oneSheetDataList = new ArrayList<>();

			WriteTable writeTable1 = EasyExcel.writerTable(0).head(AuthorFeeDeptDetailGfExcelData.class).needHead(true).build();
			WriteTable writeTable2 = EasyExcel.writerTable(1).head(AuthorFeeDeptDetailWsWbExcelData.class).needHead(true).build();
			writeTable1.setUseDefaultStyle(true);
			writeTable2.setUseDefaultStyle(true);

			//导出明细的结果map
			Map<String, Map<String, List<AuthorFeeDeptDetailWsWbExcelData>>> detailMap = new HashMap<>();
			feeDetails.stream().collect(Collectors.groupingBy(e -> e.getFeebdgdeptid().toString()))
					.forEach((unitId, detailsByUnitId) -> {

						//以科目分组
						detailsByUnitId.stream().collect(Collectors.groupingBy(e -> e.getSubjectid())).forEach((subjectid, detailsBySubject) -> {
							//只汇总稿费和外审外包。
							if (!detailsBySubject.get(0).getReimbursesubject().equals(BudgetAuthorfeesumService.CONTRIBUTION_FEE)
									&& !detailsBySubject.get(0).getReimbursesubject().equals(BudgetAuthorfeesumService.EXTERNAL_AUDIT_FEE))
								return;

							AuthorFeeDeptSumExcelData ed = new AuthorFeeDeptSumExcelData();
							ed.setDeptName(detailsBySubject.get(0).getFeebdgdept());
							ed.setSubjectName(detailsBySubject.get(0).getReimbursesubject());
							List<BudgetYearSubject> curSubjectYearSubjectList = yearSubjectMap.get(unitId + SPLIT_SYMBOL + subjectid);
							ed.setYearMoney(BigDecimal.ZERO);
							ed.setYearExecuteMoney(BigDecimal.ZERO);
							if (curSubjectYearSubjectList != null) {
								ed.setYearMoney(curSubjectYearSubjectList.stream().map(e -> e.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add));
								ed.setYearExecuteMoney(curSubjectYearSubjectList.stream().map(e -> e.getExecutemoney()).reduce(BigDecimal.ZERO, BigDecimal::add));
							}
							oneSheetDataList.add(ed);
						});

						Map<String, List<AuthorFeeDeptDetailWsWbExcelData>> eddetailMap = detailMap.get(detailsByUnitId.get(0).getFeebdgdept());
						if (eddetailMap == null) {
							eddetailMap = new HashMap<>();
						}
						Map<String, List<AuthorFeeDeptDetailWsWbExcelData>> eddetailTempMap = eddetailMap;
						//以科目和动因分组
						detailsByUnitId.stream().collect(Collectors.groupingBy(e -> e.getSubjectid() + SPLIT_SYMBOL + e.getAgentname()))
								.forEach((key, detailsBySubjectAndAgent) -> {

									List<AuthorFeeDeptDetailWsWbExcelData> edDetailList = eddetailTempMap.get(detailsBySubjectAndAgent.get(0).getReimbursesubject());
									if (edDetailList == null) {
										edDetailList = new ArrayList<>();
									}
									Map<String, List<AuthorFeeDeptDetailWsWbExcelData>> map = new HashMap<>();
									List<BudgetYearAgent> agentList = yearAgentMap.get(unitId + SPLIT_SYMBOL + key);
									AuthorFeeDeptDetailWsWbExcelData detailEd = new AuthorFeeDeptDetailWsWbExcelData();
									detailEd.setTotal(agentList.get(0).getTotal());
									detailEd.setAgentName(agentList.get(0).getName());
									detailEd.setTotal6(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 6l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal7(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 7l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal8(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 8l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal9(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 9l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal10(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 10l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal11(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 11l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal12(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 12l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal1(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 1l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal2(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 2l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal3(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 3l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal4(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 4l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setTotal5(detailsBySubjectAndAgent.stream().filter(e -> e.getMonthid() == 5l).map(e -> e.getCopefee()).reduce(BigDecimal.ZERO, BigDecimal::add));
									detailEd.setPayTotal(detailEd.getTotal6()
											.add(detailEd.getTotal7())
											.add(detailEd.getTotal8())
											.add(detailEd.getTotal9())
											.add(detailEd.getTotal10())
											.add(detailEd.getTotal11())
											.add(detailEd.getTotal12())
											.add(detailEd.getTotal1())
											.add(detailEd.getTotal2())
											.add(detailEd.getTotal3())
											.add(detailEd.getTotal4())
											.add(detailEd.getTotal5()));
									edDetailList.add(detailEd);
									eddetailTempMap.put(detailsBySubjectAndAgent.get(0).getReimbursesubject(), edDetailList);
								});
						;

						detailMap.put(detailsByUnitId.get(0).getFeebdgdept(), eddetailTempMap);
					});
			;
			ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("中心稿费发放明细表", response)).build();
			WriteTable writeTable = EasyExcel.writerTable(0).head(oneSheetHeadList).needHead(true).build();
			excelWriter.write(oneSheetDataList, sumSheet, writeTable);

			if (!detailMap.isEmpty()) {
				detailMap.forEach((unitName, deptDetailMap) -> {
					WriteSheet detailSheet = EasyExcel.writerSheet(unitName).build();
					deptDetailMap.forEach((subjectName, edDetailList) -> {
						if (subjectName.equals(BudgetAuthorfeesumService.CONTRIBUTION_FEE)) {
							excelWriter.write(edDetailList, detailSheet, writeTable1);
						} else if (subjectName.equals(BudgetAuthorfeesumService.EXTERNAL_AUDIT_FEE)) {
							excelWriter.write(edDetailList, detailSheet, writeTable2);
						}
					});
				});
			}

			excelWriter.finish();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) is.close();
		}
	}

	/**
	 * 获取第一个sheet的表头
	 *
	 * @param period
	 * @return
	 */
	private List<List<String>> getOneSheetHead(String period) {
		List<List<String>> headList = new ArrayList<>();

		String title = period + "各部门稿费汇总";
		List<String> head0 = new ArrayList<>();
		head0.add(title);
		head0.add("部门");
		headList.add(head0);

		List<String> head1 = new ArrayList<>();
		head1.add(title);
		head1.add("科目");
		headList.add(head1);

		List<String> head2 = new ArrayList<>();
		head2.add(title);
		head2.add("年度预算");
		headList.add(head2);

		List<String> head3 = new ArrayList<>();
		head3.add(title);
		head3.add("年度执行");
		headList.add(head3);
		return headList;
	}

	public static List<List<String>> getDeptDetailHead(String subjectname, String yearPeriod) {
		List<List<String>> headList = new ArrayList<>();

		String title = yearPeriod + "稿费汇总";
		List<String> head0 = new ArrayList<>();
		head0.add(title);
		head0.add(subjectname + "年度预算");
		List<String> head1 = new ArrayList<>();
		head1.add(title);
		head1.add("动因名称");
		List<String> head2 = new ArrayList<>();
		head2.add(title);
		head2.add("实际已发放");
		List<String> head3 = new ArrayList<>();
		head3.add(title);
		head3.add("6月发放");
		List<String> head4 = new ArrayList<>();
		head4.add(title);
		head4.add("7月发放");
		List<String> head5 = new ArrayList<>();
		head5.add(title);
		head5.add("8月发放");
		List<String> head6 = new ArrayList<>();
		head6.add(title);
		head6.add("9月发放");
		List<String> head7 = new ArrayList<>();
		head7.add(title);
		head7.add("10月发放");
		List<String> head8 = new ArrayList<>();
		head8.add(title);
		head8.add("11月发放");
		List<String> head9 = new ArrayList<>();
		head9.add(title);
		head9.add("12月发放");
		List<String> head10 = new ArrayList<>();
		head10.add(title);
		head10.add("1月发放");
		List<String> head11 = new ArrayList<>();
		head11.add(title);
		head11.add("2月发放");
		List<String> head12 = new ArrayList<>();
		head12.add(title);
		head12.add("3月发放");
		List<String> head13 = new ArrayList<>();
		head13.add(title);
		head13.add("4月发放");
		List<String> head14 = new ArrayList<>();
		head14.add(title);
		head14.add("5月发放");
		headList.add(head0);
		headList.add(head1);
		headList.add(head2);
		headList.add(head3);
		headList.add(head4);
		headList.add(head5);
		headList.add(head6);
		headList.add(head7);
		headList.add(head8);
		headList.add(head9);
		headList.add(head10);
		headList.add(head11);
		headList.add(head12);
		headList.add(head13);
		headList.add(head14);

		return headList;
	}


	/**@ApiOperation(value = "导出稿费发放表",httpMethod="GET")
	 @ApiImplicitParams(value = {
	 @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
	 @ApiImplicitParam(value = "稿费报表id", name = "id", dataType = "Long", required = true)
	 })
	 @GetMapping("/exportDeptAuthorFeePayDetails") public void exportDeptAuthorFeePayDetails(@RequestParam(name="id",required = true)Long id,HttpServletResponse response) throws Exception{
	 String path = ClassUtils.getDefaultClassLoader().getResource("template").getPath();
	 String templateName = "/extractIncomeDetail.xlsx";
	 ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("导出稿费发放表", response),AuthorFeePayDetailExcelData.class).withTemplate(path+templateName).build();
	 WriteSheet sheet = EasyExcel.writerSheet(0).build();
	 WriteSheet sheet1 = EasyExcel.writerSheet(1).build();

	 List<BudgetAuthorfeedtlMerge> mergeList = this.mergeService.list(new QueryWrapper<BudgetAuthorfeedtlMerge>().eq("reportid", id));



	 workBook.fill(list, sheet);
	 workBook.fill(list, sheet1);
	 workBook.finish();
	 }**/

	/**public static void main(String[] args) throws IOException {
	 WriteSheet writeSheet1 = EasyExcel.writerSheet("1234").build();
	 WriteSheet writeSheet = EasyExcel.writerSheet("123").build();

	 List<List<String>> headList = new ArrayList<>();

	 List<String> head0 = new ArrayList<>();
	 head0.add("bbbb");
	 head0.add("aaaa");
	 headList.add(head0);

	 List<String> head1 = new ArrayList<>();
	 head1.add("bbbb");
	 head1.add("cbd");
	 headList.add(head1);

	 WriteTable writeTable = EasyExcel.writerTable(0).head(AuthorFeeDeptDetailWsWbExcelData.class).needHead(true).build();
	 WriteTable writeTable2 = EasyExcel.writerTable(1).head(AuthorFeeDeptDetailWsWbExcelData.class).needHead(true).build();
	 FileOutputStream os = new FileOutputStream(new File("D:\\exceltemplate\\a.xlsx"));
	 ExcelWriter excelWriter = EasyExcel.write(os).build();

	 AuthorFeeDeptSumExcelData a = new AuthorFeeDeptSumExcelData();
	 //a.setA("abc");
	 List<AuthorFeeDeptSumExcelData> alist = new ArrayList<>();
	 alist.add(a);
	 excelWriter.write(alist, writeSheet, writeTable);

	 AuthorFeeDeptDetailWsWbExcelData b = new AuthorFeeDeptDetailWsWbExcelData();
	 //b.setHaha("hehe");
	 List<AuthorFeeDeptDetailWsWbExcelData> blist = new ArrayList<>();
	 blist.add(b);
	 //excelWriter.write(blist, writeSheet, writeTable2);


	 excelWriter.write(blist, writeSheet1, writeTable);
	 excelWriter.write(blist, writeSheet1, writeTable2);
	 excelWriter.finish();
	 os.close();
	 }**/
}

