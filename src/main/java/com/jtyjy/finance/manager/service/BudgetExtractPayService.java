package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.BankCache;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.ExtractPayCompleteDTO;
import com.jtyjy.finance.manager.dto.ExtractPreparePayDTO;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhBatchPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetExtractZhDfPayExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetPayTotalExcelData;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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

	private final BudgetPaymoneyMapper paymoneyMapper;
	private final BudgetPaybatchMapper paybatchMapper;
	private final BankCache bankCache;
	private final BudgetPaymoneyService paymoneyService;
	private final BudgetExtractPerPayDetailService perPayDetailService;
	private final ExtractAccountEntryTaskService accountEntryTaskService;
	private final BudgetExtractPersonalityPayService extractPersonalityPayService;
	private final BudgetExtractDelayApplicationMapper delayApplicationMapper;
	private final BudgetExtractsumMapper extractsumMapper;
	private final BudgetExtractsumService extractsumService;
	private final BudgetExtractAccountTaskMapper accountTaskMapper;
	private final BudgetExtractTaxHandleRecordMapper taxHandleRecordMapper;
	private final BudgetExtractPersonalityPayDetailMapper personalityPayDetailMapper;
	private final BudgetYearPeriodMapper yearPeriodMapper;
	private final MessageSender sender;
	private final CommonService commonService;
	private final BudgetUnitMapper unitMapper;
	private final BudgetBillingUnitMapper billingUnitMapper;
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
		boolean test = extractsumService.isTest();
		String testNotice = extractsumService.getTestNotice();
		Map<String,Boolean> extratBatchPayMap = new HashMap<>();
		perPayDetails.stream().collect(Collectors.groupingBy(e->e.getExtractCode().substring(0,2))).forEach((orderPrefix,orderDetailList)->{

			if(orderPrefix.equals(Constants.EXTRACT_DELAY_ORDER_PREFIX)){
				//延期
				orderDetailList.stream().collect(Collectors.groupingBy(BudgetExtractPerPayDetail::getExtractMonth)).forEach((extractBatch,batchDetailList)->{
					setIsPayFlag(extractBatch,extratBatchPayMap);
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
							LambdaUpdateWrapper<BudgetExtractDelayApplication> updateWrapper = new LambdaUpdateWrapper<>();
							updateWrapper.in(BudgetExtractDelayApplication::getDelayCode,codeList);
							updateWrapper.set(BudgetExtractDelayApplication::getStatus,ExtractDelayStatusEnum.PAY.type);
							delayApplicationMapper.update(new BudgetExtractDelayApplication(),updateWrapper);


							try{
								List<String> extractSumCodeList = delayApplicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractDelayApplication>().in(BudgetExtractDelayApplication::getDelayCode, codeList)).stream().map(e -> e.getRelationExtractCode()).collect(Collectors.toList());
								List<BudgetExtractsum> sumList = extractsumService.list(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getCode, extractSumCodeList));
								String accounts = sumList.stream().map(e -> {
									String deptid = e.getDeptid();
									BudgetUnit budgetUnit = unitMapper.selectById(deptid);
									String accounting = budgetUnit.getAccounting();
									if (StringUtils.isNotBlank(accounting)) {
										return UserCache.getUserByUserId(accounting).getUserName();
									}
									return null;
								}).filter(e -> StringUtils.isNotBlank(e)).collect(Collectors.joining("|"));
								BudgetYearPeriod budgetYearPeriod = yearPeriodMapper.selectById(sumList.get(0).getYearid());
								if(test){
									accounts = testNotice;
								}
								if(StringUtils.isNotBlank(accounts)){
									sender.sendQywxMsg(new QywxTextMsg(accounts, null, null, 0, "【延期】"+budgetYearPeriod.getPeriod()+Integer.parseInt(extractBatch.substring(4,6))+"月"+Integer.parseInt(extractBatch.substring(6,8))+"批提成已支付完成，可进行入账操作！", null));
								};
							}catch (Exception ignored){

							}
						}
					});
				});

			}else if(orderPrefix.equals(TC_REDIS_KEY)){
				orderDetailList.stream().collect(Collectors.groupingBy(BudgetExtractPerPayDetail::getExtractMonth)).forEach((extractBatch,batchDetailList)->{
					setIsPayFlag(extractBatch,extratBatchPayMap);
					List<BudgetExtractPerPayDetail> list = perPayDetailService.list(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().eq(BudgetExtractPerPayDetail::getExtractMonth, extractBatch));
					if(!CollectionUtils.isEmpty(list)){
						List<String> codeList = list.stream().map(e -> e.getExtractCode()).collect(Collectors.toList());
						int unPaySuccessCount = paymoneyService.count(new LambdaQueryWrapper<BudgetPaymoney>().eq(BudgetPaymoney::getPaymoneytype, PaymoneyTypeEnum.EXTRACT_PAY.type).in(BudgetPaymoney::getPaymoneyobjectcode,codeList).in(BudgetPaymoney::getPaymoneyobjectid, list.stream().map(e -> e.getId()).collect(Collectors.toList())).ne(BudgetPaymoney::getPaymoneystatus, PaymoneyStatusEnum.PAYED.type));
						if(unPaySuccessCount == 0){
							//该批次全部支付成功
							LambdaUpdateWrapper<BudgetExtractsum> updateWrapper = new LambdaUpdateWrapper<>();
							updateWrapper.set(BudgetExtractsum::getStatus, ExtractStatusEnum.PAY.type);
							updateWrapper.eq(BudgetExtractsum::getExtractmonth,extractBatch);
							updateWrapper.ne(BudgetExtractsum::getStatus,ExtractStatusEnum.REJECT.type);
							extractsumMapper.update(new BudgetExtractsum(),updateWrapper);
							extractsumService.generateExtractStepLog(extractsumService.getCurBatchExtractSum(extractBatch).stream().map(e->e.getId()).collect(Collectors.toList()), OperationNodeEnum.CASHIER_PAYMENT, "【" + OperationNodeEnum.CASHIER_PAYMENT.getValue(OperationNodeEnum.CASHIER_PAYMENT.getType()) + "】完成", LogStatusEnum.COMPLETE.getCode());
							accountEntryTaskService.addEntryTask(false,null,extractBatch);
						}
					}

					try{
						List<BudgetExtractsum> sumList = extractsumService.list(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch));
						String accounts = sumList.stream().map(e -> {
							String deptid = e.getDeptid();
							BudgetUnit budgetUnit = unitMapper.selectById(deptid);
							String accounting = budgetUnit.getAccounting();
							if (StringUtils.isNotBlank(accounting)) {
								return UserCache.getUserByUserId(accounting).getUserName();
							}
							return null;
						}).filter(e -> StringUtils.isNotBlank(e)).collect(Collectors.joining("|"));
						BudgetYearPeriod budgetYearPeriod = yearPeriodMapper.selectById(sumList.get(0).getYearid());
						if(test){
							accounts = testNotice;
						}
						if(StringUtils.isNotBlank(accounts)){
							sender.sendQywxMsg(new QywxTextMsg(accounts, null, null, 0, budgetYearPeriod.getPeriod()+Integer.parseInt(extractBatch.substring(4,6))+"月"+Integer.parseInt(extractBatch.substring(6,8))+"批提成已支付完成，可进行入账操作！", null));
						};
					}catch (Exception ignored){

					}

				});
			}
		});
		extratBatchPayMap.forEach((extractBatch,isEverPay)->{
			if(!isEverPay){
				//从来没有付款成功过
				try{
					List<BudgetExtractsum> list = extractsumService.getCurBatchExtractSum(extractBatch);
					BudgetYearPeriod budgetYearPeriod = yearPeriodMapper.selectById(list.get(0).getYearid());
					//所有的大区经理
					List<String> empNoList = commonService.getEmpNoListByRoleNames(RoleNameEnum.BIG_MANAGER.value);
					if(!CollectionUtils.isEmpty(empNoList)){
						sender.sendQywxMsg(new QywxTextMsg(String.join("|",empNoList), null, null, 0, "提成明细发布通知：<br>"+budgetYearPeriod.getPeriod()+Integer.parseInt(extractBatch.substring(4,6))+"月第"+Integer.parseInt(extractBatch.substring(6,8))+"批提成明细数据已发布，请登录http://ys.jtyjy.com查看。", null));
					}
					List<BudgetExtractdetail> extractDetails = extractsumService.getExtractDetailBySumIds(list.stream().map(e -> e.getId()).collect(Collectors.toList()), null, null);

				}catch (Exception e){

				}
			}
		});
	}

	/**
	 * <p>获取当前批次有没有付款成功</p>
	 * @author minzhq
	 * @date 2022/9/19 15:04
	 * @param extractBatch
	 * @param extratBatchPayMap
	 */
	private void setIsPayFlag(String extractBatch, Map<String, Boolean> extratBatchPayMap) {
		List<String> codeList = extractsumService.getCurBatchExtractSum(extractBatch).stream().map(e -> e.getCode()).collect(Collectors.toList());
		List<String> delayCodeList = delayApplicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractDelayApplication>().in(BudgetExtractDelayApplication::getRelationExtractCode, codeList)).stream().map(e -> e.getDelayCode()).collect(Collectors.toList());
		codeList.addAll(delayCodeList);
		if(!CollectionUtils.isEmpty(codeList)){
			int count = paymoneyService.count(new LambdaQueryWrapper<BudgetPaymoney>().in(BudgetPaymoney::getPaymoneyobjectcode, codeList).eq(BudgetPaymoney::getPaymoneytype, PaymoneyTypeEnum.EXTRACT_PAY.type).eq(BudgetPaymoney::getPaymoneystatus, PaymoneyStatusEnum.PAYED.type));
			if(count > 0 ){
				extratBatchPayMap.put(extractBatch,true);
			}else{
				extratBatchPayMap.put(extractBatch,false);
			}
		}
	}

	/**
	 * <p>出纳退回</p>
	 * @author minzhq
	 * @date 2022/9/17 14:19
	 * @param extractBatch
	 */
	public void payReject(String extractBatch) {
		List<BudgetExtractsum> batchExtractSums = extractsumService.getFutureBatchExtractSumContainSelf(extractBatch);
		batchExtractSums.stream().collect(Collectors.groupingBy(BudgetExtractsum::getExtractmonth)).forEach((batch,curBatchExtractSums)->{
			if(extractBatch.equals(batch)){
				long payFinishCount = curBatchExtractSums.stream().filter(e -> e.getStatus() >= ExtractStatusEnum.PAY.type).count();
				if(payFinishCount>0){
					throw new RuntimeException("提成批次"+batch+"已有提成支付申请单流转至后续环节！");
				}
				//当前批次存在延期支付申请单到了后续环节，不允许退回
				Integer delayPayFinishCount = delayApplicationMapper.selectCount(new LambdaQueryWrapper<BudgetExtractDelayApplication>().in(BudgetExtractDelayApplication::getExtractMonth, extractBatch).ge(BudgetExtractDelayApplication::getStatus, ExtractDelayStatusEnum.PAY.type));
				if(delayPayFinishCount > 0 ){
					throw new RuntimeException("提成批次"+batch+"已有延期支付申请单流转至后续环节！");
				}
			}else{
//				BudgetExtractTaxHandleRecord extractTaxHandleRecord = extractsumService.getExtractTaxHandleRecord(batch);
//				if(extractTaxHandleRecord!=null && (extractTaxHandleRecord.getIsCalComplete() || extractTaxHandleRecord.getIsPersonalityComplete())){
//					throw new RuntimeException("已有后续批次"+batch+"已被处理！");
//				}
			}
		});
		List<Long> sumIds = batchExtractSums.stream().filter(e -> e.getExtractmonth().equals(extractBatch)).map(BudgetExtractsum::getId).collect(Collectors.toList());
		List<String> extractCodeList = batchExtractSums.stream().filter(e -> e.getExtractmonth().equals(extractBatch)).map(BudgetExtractsum::getCode).collect(Collectors.toList());
		List<String> delayCodeList = delayApplicationMapper.selectList(new LambdaQueryWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getExtractMonth, extractBatch)).stream().map(e -> e.getDelayCode()).collect(Collectors.toList());
		extractCodeList.addAll(delayCodeList);
		LambdaQueryWrapper<BudgetPaymoney> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(BudgetPaymoney::getPaymoneytype,PaymoneyTypeEnum.EXTRACT_PAY.type);
		queryWrapper.eq(BudgetPaymoney::getPaymoneystatus,PaymoneyStatusEnum.PAYED.type);
		queryWrapper.in(BudgetPaymoney::getPaymoneyobjectcode,extractCodeList);
		int count = paymoneyService.count(queryWrapper);
		if(count >0){
			throw new RuntimeException("该批次已有付款单已支付完成！");
		}
		LambdaQueryWrapper<BudgetPaymoney> qw = new LambdaQueryWrapper<>();
		qw.eq(BudgetPaymoney::getPaymoneytype,PaymoneyTypeEnum.EXTRACT_PAY.type);
		qw.in(BudgetPaymoney::getPaymoneyobjectcode,extractCodeList);
		List<BudgetExtractAccountTask> accountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractMonth, extractBatch));
		paymoneyService.remove(qw);
		extractsumService.update(new LambdaUpdateWrapper<BudgetExtractsum>().set(BudgetExtractsum::getStatus,ExtractStatusEnum.APPROVED.type).in(BudgetExtractsum::getId,sumIds));
		delayApplicationMapper.delete(new LambdaUpdateWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getExtractMonth,extractBatch));
		accountTaskMapper.delete(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractMonth,extractBatch));
		extractsumService.generateExtractStepLog(sumIds, OperationNodeEnum.CASHIER_PAYMENT,"【"+OperationNodeEnum.getValue(OperationNodeEnum.CASHIER_PAYMENT.getType()) + "】退回",LogStatusEnum.REJECT.getCode());
		taxHandleRecordMapper.update(new BudgetExtractTaxHandleRecord(),new LambdaUpdateWrapper<BudgetExtractTaxHandleRecord>().eq(BudgetExtractTaxHandleRecord::getExtractMonth,extractBatch).set(BudgetExtractTaxHandleRecord::getIsPersonalityComplete,0));
		perPayDetailService.remove(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().eq(BudgetExtractPerPayDetail::getExtractMonth,extractBatch));
		personalityPayDetailMapper.update(new BudgetExtractPersonalityPayDetail(),new LambdaUpdateWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getExtractMonth,extractBatch).set(BudgetExtractPersonalityPayDetail::getOperateTime,null));

		try{
			Map<Long, BudgetBillingUnit> unitMap = this.billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(BudgetBillingUnit::getId, Function.identity()));
			String accountants = accountTasks.stream().flatMap(e -> {
				BudgetBillingUnit budgetBillingUnit = unitMap.get(e.getBillingUnitId());
				if(StringUtils.isNotBlank(budgetBillingUnit.getAccountants())){
					return Arrays.stream(budgetBillingUnit.getAccountants().split(","));
				}
				return null;
			}).filter(StringUtils::isNotBlank).map(e-> UserCache.getUserByUserId(e).getUserName()).distinct().collect(Collectors.joining("|"));

			if(extractsumService.isTest()){
				accountants = extractsumService.getTestNotice();
			}
			List<BudgetExtractsum> list = extractsumService.list(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getExtractmonth, extractBatch));
			BudgetYearPeriod budgetYearPeriod = yearPeriodMapper.selectById(list.get(0).getYearid());
			if(StringUtils.isNotBlank(accountants))sender.sendQywxMsg(new QywxTextMsg(accountants, null, null, 0, budgetYearPeriod.getPeriod()+Integer.parseInt(extractBatch.substring(4,6))+"月"+Integer.parseInt(extractBatch.substring(6,8))+"批提成已出纳退回，请尽快删除对应凭证！", null));
		}catch (Exception e){}
	}
}
