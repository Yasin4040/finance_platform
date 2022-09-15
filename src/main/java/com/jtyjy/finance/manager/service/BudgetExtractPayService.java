package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.BudgetPaybatch;
import com.jtyjy.finance.manager.bean.BudgetPaymoney;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.cache.BankCache;
import com.jtyjy.finance.manager.dto.ExtractPreparePayDTO;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhBatchPayExcelData;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
		updateWrapper.in(BudgetPaymoney::getId,budgetPaybatch.getPaymoneyids());
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

		if(type == ExtractPayTemplateEnum.ZS_BATCH.type){



			budgetPaymonies.stream().collect(Collectors.groupingBy(BudgetPaymoney::getBunitname)).forEach((bunitname,list)->{

				List<BudgetExtractZhBatchPayExcelData> bUnitNameList = list.stream().map(pm -> {
					BudgetExtractZhBatchPayExcelData excelData = new BudgetExtractZhBatchPayExcelData();
					excelData.setBankAccount(pm.getBankaccount());
					excelData.setBankAccountName(pm.getBankaccountname());
					excelData.setOpenBank(pm.getOpenbank());
					WbBanks bank = bankCache.getBankByBranchCode(pm.getBankaccountbranchcode());
					excelData.setProvince(bank.getProvince());
					excelData.setCity(bank.getCity());
					excelData.setBunitAccount(pm.getBunitbankaccount());
					excelData.setPayMoney(pm.getPaymoney().stripTrailingZeros().toPlainString());
					excelData.setBranchCode(pm.getBankaccountbranchcode());
					excelData.setBankName(pm.getBankaccountbranchname());
					return excelData;
				}).collect(Collectors.toList());

				Map<String, List<BudgetExtractZhBatchPayExcelData>> bUnitNameBankMap = bUnitNameList.stream().collect(Collectors.groupingBy(e -> {
					return bunitname + "-" + e.getBankName();
				}));

			});
		}

		return resultList;
	}
}
