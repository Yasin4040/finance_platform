package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.BudgetPaybatch;
import com.jtyjy.finance.manager.bean.BudgetPaymoney;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.cache.BankCache;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.ExtractPayCompleteDTO;
import com.jtyjy.finance.manager.dto.ExtractPreparePayDTO;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhBatchPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhDfPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetPayTotalExcelData;
import com.jtyjy.finance.manager.enmus.ExtractPayTemplateEnum;
import com.jtyjy.finance.manager.enmus.PayBatchTypeEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetPaybatchMapper;
import com.jtyjy.finance.manager.mapper.BudgetPaymoneyMapper;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractPayService {

	@Autowired
	private BudgetPaymoneyMapper paymoneyMapper;
	@Autowired
	private BudgetPaybatchMapper paybatchMapper;
	@Autowired
	private BankCache bankCache;
	@Autowired
	private BudgetPaymoneyService paymoneyService;

	/**
	 * <p>获取提成付款单</p>
	 * @author minzhq
	 * @date 2022/9/14 10:51
	 * @param params
	 * @param page
	 * @param rows
	 */
	public PageResult<BudgetExtractPayResponseVO> getExtractPayMoneyList(BudgetExtractPayQueryVO params, Integer page, Integer rows) {
		Page<BudgetExtractPayResponseVO> pageCond = new Page<>(page, rows);
		List<BudgetExtractPayResponseVO> resultList = paymoneyMapper.getExtractPayMoneyList(pageCond, params);
		resultList.forEach(e->{
			e.setIsDelay(e.getExtractCode().startsWith(Constants.EXTRACT_DELAY_ORDER_PREFIX));
		});
		return PageResult.apply(pageCond.getTotal(), resultList);
	}

	/**
	 * <p>提成准备付款</p>
	 * @author minzhq
	 * @date 2022/9/15 10:22
	 * @param extractPreparePayDTO
	 */
	public void extractPreparePay(ExtractPreparePayDTO extractPreparePayDTO) {
		List<BudgetPaymoney> budgetPaymonies = paymoneyMapper.selectBatchIds(extractPreparePayDTO.getPayMoneyIds());
		long count = budgetPaymonies.stream().filter(e -> e.getPaymoneystatus() != PaymoneyStatusEnum.RECEIVE_PAY.type).count();
		if(count > 0 ){
			throw new RuntimeException("请选择待支付的付款单！");
		}
		//创建付款批次
		BudgetPaybatch budgetPaybatch = new BudgetPaybatch();
		budgetPaybatch.setCreatetime(new Date());
		budgetPaybatch.setPaybatchtype(PayBatchTypeEnum.EXTRACT.type);
		budgetPaybatch.setPaymoneyids(budgetPaymonies.stream().map(e->e.getId().toString()).collect(Collectors.joining(",")));
		budgetPaybatch.setCreator(UserThreadLocal.getEmpNo());
		budgetPaybatch.setCreatorname(UserThreadLocal.getEmpName());
		budgetPaybatch.setPaytotalje(budgetPaymonies.stream().map(BudgetPaymoney::getPaymoney).reduce(BigDecimal.ZERO,BigDecimal::add));
		budgetPaybatch.setPaytotalnum(budgetPaymonies.size());
		budgetPaybatch.setRemark("提成付款");
		budgetPaybatch.setPaybatchcode(System.currentTimeMillis() + "");
		paybatchMapper.insert(budgetPaybatch);

		LambdaUpdateWrapper<BudgetPaymoney> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.in(BudgetPaymoney::getId,extractPreparePayDTO.getPayMoneyIds());
		updateWrapper.set(BudgetPaymoney::getPaymoneystatus,PaymoneyStatusEnum.PAYING.type);
		updateWrapper.set(BudgetPaymoney::getPaybatchid,budgetPaybatch.getId());
		paymoneyMapper.update(new BudgetPaymoney(),updateWrapper);

	}
	/**
	 * <p>提成付款明细</p>
	 * @author minzhq
	 * @date 2022/9/15 11:23
	 * @param payMoneyIds
	 */
	public List<Map<String, Object>> getExtractPayBatchDetailList(List<Long> payMoneyIds , int type) {

		List<Map<String,Object>> resultList = new ArrayList<>();
		List<BudgetPaymoney> budgetPaymonies = paymoneyMapper.selectBatchIds(payMoneyIds);
		List<BudgetPayTotalExcelData> excelDatas = new ArrayList<>();
		Map<String,Object> map = new HashMap<>();
		map.put("付款明细汇总表",excelDatas);
		resultList.add(map);
		if(type == ExtractPayTemplateEnum.ZS_BATCH.type){
			budgetPaymonies.stream().collect(Collectors.groupingBy(BudgetPaymoney::getBunitname)).forEach((bunitname,list)->{
				Map<String,Object> sheetMap = new LinkedHashMap<>();
				List<BudgetExtractZhBatchPayExcelData> bUnitNameList = list.stream().map(pm -> {
					return setBudgetExtractZhBatchPayExcelData(pm);
				}).collect(Collectors.toList());

				//合计行
				BudgetExtractZhBatchPayExcelData hj = new BudgetExtractZhBatchPayExcelData(false);
				hj.setYt("合计：");
				hj.setPayMoney(bUnitNameList.stream().map(e->e.getPayMoney()).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
				bUnitNameList.add(hj);
				sheetMap.put(bunitname,bUnitNameList);
				resultList.add(sheetMap);
				list.stream().collect(Collectors.groupingBy(e -> {
					return e.getBankaccountbranchname();
				})).forEach((key,list1)->{
					BudgetPayTotalExcelData excelData = new BudgetPayTotalExcelData(bunitname,key,list1.stream().map(BudgetPaymoney::getPaymoney).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
					excelDatas.add(excelData);
					List<BudgetExtractZhBatchPayExcelData> bunitBankList = list1.stream().map(pm -> {
						return setBudgetExtractZhBatchPayExcelData(pm);
					}).collect(Collectors.toList());

					//合计行
					BudgetExtractZhBatchPayExcelData hj1 = new BudgetExtractZhBatchPayExcelData(false);
					hj1.setYt("合计：");
					hj1.setPayMoney(bunitBankList.stream().map(e->e.getPayMoney()).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
					bunitBankList.add(hj1);
					Map<String,Object> sheetMap1 = new LinkedHashMap<>();
					sheetMap1.put(bunitname+"-"+key,bunitBankList);
					resultList.add(sheetMap1);
				});

			});

		}else if(type == ExtractPayTemplateEnum.ZS_DF.type){
			budgetPaymonies.stream().collect(Collectors.groupingBy(BudgetPaymoney::getBunitname)).forEach((bunitname,list)->{
				Map<String,Object> sheetMap = new LinkedHashMap<>();
				List<BudgetExtractZhDfPayExcelData> bUnitNameList = list.stream().map(pm -> {
					return setBudgetExtractZhDfPayExcelData(pm);
				}).collect(Collectors.toList());
				//合计行
				BudgetExtractZhDfPayExcelData hj = new BudgetExtractZhDfPayExcelData(false);
				hj.setBankAccountName("合计：");
				hj.setPayMoney(bUnitNameList.stream().map(e->e.getPayMoney()).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
				bUnitNameList.add(hj);
				sheetMap.put(bunitname,bUnitNameList);
				resultList.add(sheetMap);
				list.stream().collect(Collectors.groupingBy(e -> {
					return e.getBankaccountbranchname();
				})).forEach((key,list1)->{
					BudgetPayTotalExcelData excelData = new BudgetPayTotalExcelData(bunitname,key,list1.stream().map(BudgetPaymoney::getPaymoney).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
					excelDatas.add(excelData);
					List<BudgetExtractZhDfPayExcelData> bunitBankList = list1.stream().map(pm -> {
						return setBudgetExtractZhDfPayExcelData(pm);
					}).collect(Collectors.toList());

					//合计行
					BudgetExtractZhDfPayExcelData hj1 = new BudgetExtractZhDfPayExcelData(false);
					hj1.setBankAccountName("合计：");
					hj1.setPayMoney(bunitBankList.stream().map(e->e.getPayMoney()).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP));
					bunitBankList.add(hj1);
					Map<String,Object> sheetMap1 = new LinkedHashMap<>();
					sheetMap1.put(bunitname+"-"+key,bunitBankList);
					resultList.add(sheetMap1);
				});
			});
		}
		return resultList;
	}

	private BudgetExtractZhBatchPayExcelData setBudgetExtractZhBatchPayExcelData(BudgetPaymoney pm){
		BudgetExtractZhBatchPayExcelData excelData = new BudgetExtractZhBatchPayExcelData();
		excelData.setBankAccount(pm.getBankaccount());
		excelData.setBankAccountName(pm.getBankaccountname());
		excelData.setOpenBank(pm.getOpenbank());
		WbBanks bank = bankCache.getBankByBranchCode(pm.getBankaccountbranchcode());
		excelData.setProvince(bank.getProvince());
		excelData.setCity(bank.getCity());
		excelData.setBunitAccount(pm.getBunitbankaccount());
		excelData.setPayMoney(pm.getPaymoney().setScale(2,BigDecimal.ROUND_HALF_UP));
		excelData.setBranchCode(pm.getBankaccountbranchcode());
		excelData.setBankName(pm.getBankaccountbranchname());
		return excelData;
	}

	private BudgetExtractZhDfPayExcelData setBudgetExtractZhDfPayExcelData(BudgetPaymoney pm){
		BudgetExtractZhDfPayExcelData excelData = new BudgetExtractZhDfPayExcelData();
		excelData.setBankAccount(pm.getBankaccount());
		excelData.setBankAccountName(pm.getBankaccountname());
		excelData.setOpenBank(pm.getOpenbank());
		WbBanks bank = bankCache.getBankByBranchCode(pm.getBankaccountbranchcode());
		excelData.setCity(bank.getCity());
		excelData.setPayMoney(pm.getPaymoney().setScale(2,BigDecimal.ROUND_HALF_UP));
		return excelData;
	}

	/**
	 * <p>提成付款完成</p>
	 * @author minzhq
	 * @date 2022/9/16 9:58
	 * @param extractPayCompleteDTO
	 */
	public void paySuccess(ExtractPayCompleteDTO extractPayCompleteDTO) {
		List<BudgetPaymoney> budgetPaymonies = paymoneyMapper.selectBatchIds(extractPayCompleteDTO.getPayMoneyIds());
		long count = budgetPaymonies.stream().filter(e -> e.getPaymoneystatus() != PaymoneyStatusEnum.PAYING.type).count();
		if(count > 0 ){
			throw new RuntimeException("请选择支付中的付款单！");
		}

		budgetPaymonies.stream().peek(e->{
			e.setPaymoneystatus(PaymoneyStatusEnum.PAYED.type);
			e.setPaytime(new Date());

		});

	}
}
