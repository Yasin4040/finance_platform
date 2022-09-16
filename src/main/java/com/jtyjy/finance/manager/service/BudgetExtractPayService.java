package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.*;
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
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetExtractDelayApplicationMapper;
import com.jtyjy.finance.manager.mapper.BudgetPaybatchMapper;
import com.jtyjy.finance.manager.mapper.BudgetPaymoneyMapper;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
	@Autowired
	private BudgetExtractPerPayDetailService perPayDetailService;
	@Autowired
	private ExtractAccountEntryTaskService accountEntryTaskService;
	@Autowired
	private BudgetExtractPersonalityPayService extractPersonalityPayService;
	@Autowired
	private BudgetExtractDelayApplicationMapper delayApplicationMapper;
	@Value("${tc.redis.key}")
	private String TC_REDIS_KEY;

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
		budgetPaymonies.forEach(e->{
			e.setPaymoneystatus(PaymoneyStatusEnum.PAYED.type);
			e.setPaytime(new Date());
		});
		paymoneyService.updateBatchById(budgetPaymonies);

		List<Long> perPayDetailIds = budgetPaymonies.stream().map(e -> e.getPaymoneyobjectid()).collect(Collectors.toList());
		List<BudgetExtractPerPayDetail> perPayDetails = perPayDetailService.listByIds(perPayDetailIds);

		List<Long> personalityPayIds = perPayDetails.stream().filter(e -> e.getExtractCode().startsWith(Constants.EXTRACT_DELAY_ORDER_PREFIX) && e.getSourceId() != null).map(e -> e.getSourceId()).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(personalityPayIds)){
			LambdaUpdateWrapper<BudgetExtractPersonalityPayDetail> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.set(BudgetExtractPersonalityPayDetail::getIsSend,1);
			updateWrapper.in(BudgetExtractPersonalityPayDetail::getId,personalityPayIds);
			extractPersonalityPayService.update(updateWrapper);
		}
		perPayDetails.stream().collect(Collectors.groupingBy(e->e.getExtractCode().substring(0,2))).forEach((orderPrefix,orderDetailList)->{

			if(orderPrefix.equals(Constants.EXTRACT_DELAY_ORDER_PREFIX)){
				//延期
				orderDetailList.stream().collect(Collectors.groupingBy(BudgetExtractPerPayDetail::getExtractMonth)).forEach((extractBatch,batchDetailList)->{
					/**
					 * 一个提成批次下，一批的延期全部付款
					 */
					batchDetailList.stream().collect(Collectors.groupingBy(e->{
						BudgetExtractDelayApplication delayApplication = delayApplicationMapper.selectOne(new LambdaQueryWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getDelayCode, e.getExtractCode()));
						return delayApplication.getBatch()==null?1:delayApplication.getBatch();
					})).forEach((batch,delayBatchList)->{

						List<Long> delayPayDetails = delayBatchList.stream().map(e -> e.getId()).collect(Collectors.toList());
						List<String> codeList = delayBatchList.stream().map(e -> e.getExtractCode()).collect(Collectors.toList());
						int unPaySuccessCount = paymoneyService.count(new LambdaQueryWrapper<BudgetPaymoney>().eq(BudgetPaymoney::getPaymoneytype, PaymoneyTypeEnum.EXTRACT_PAY.type).in(BudgetPaymoney::getPaymoneyobjectcode,codeList).in(BudgetPaymoney::getPaymoneyobjectid, delayPayDetails).ne(BudgetPaymoney::getPaymoneystatus, PaymoneyStatusEnum.PAYED.type));
						if(unPaySuccessCount == 0){
							//该批次全部支付成功
							accountEntryTaskService.addEntryTask(true,codeList,extractBatch);
						}
					});
				});

			}else if(orderPrefix.equals(TC_REDIS_KEY)){
				orderDetailList.stream().collect(Collectors.groupingBy(BudgetExtractPerPayDetail::getExtractMonth)).forEach((extractBatch,batchDetailList)->{
					List<BudgetExtractPerPayDetail> list = perPayDetailService.list(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().eq(BudgetExtractPerPayDetail::getExtractMonth, extractBatch));
					if(!CollectionUtils.isEmpty(list)){
						List<String> codeList = list.stream().map(e -> e.getExtractCode()).collect(Collectors.toList());
						int unPaySuccessCount = paymoneyService.count(new LambdaQueryWrapper<BudgetPaymoney>().eq(BudgetPaymoney::getPaymoneytype, PaymoneyTypeEnum.EXTRACT_PAY.type).in(BudgetPaymoney::getPaymoneyobjectcode,codeList).in(BudgetPaymoney::getPaymoneyobjectid, list.stream().map(e -> e.getId()).collect(Collectors.toList())).ne(BudgetPaymoney::getPaymoneystatus, PaymoneyStatusEnum.PAYED.type));
						if(unPaySuccessCount == 0){
							//该批次全部支付成功
							accountEntryTaskService.addEntryTask(false,null,extractBatch);
						}
					}

				});
			}
		});

	}
}
