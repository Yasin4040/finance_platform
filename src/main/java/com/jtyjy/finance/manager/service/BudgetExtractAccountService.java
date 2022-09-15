package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.ExtractAccountDTO;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetExtractAccountService extends DefaultBaseService<BudgetExtractAccountTaskMapper, BudgetExtractAccountTask> {
	private final TabChangeLogMapper loggerMapper;
	private final BudgetExtractAccountTaskMapper accountTaskMapper;
	private final BudgetExtractsumMapper budgetExtractsumMapper;
	private final BudgetYearPeriodMapper yearPeriodMapper;
	private final BudgetExtractPerPayDetailMapper perPayDetailMapper;
	private final BudgetBillingUnitMapper billingUnitMapper;
	private final IndividualEmployeeFilesMapper individualEmployeeFilesMapper;
	private final BudgetExtractsumService extractsumService;
	private final BudgetExtractDelayApplicationMapper delayApplicationMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
	}

	/**
	 * <p>获取做账列表</p>
	 *
	 * @author minzhq
	 * @date 2022/9/8 9:08
	 */
	public PageResult<ExtractAccountTaskResponseVO> getExtractAccountTaskList(ExtractAccountTaskQueryVO params, Integer page, Integer rows) {
		params.setEmpNo(UserThreadLocal.get().getUserId());
		Page<ExtractAccountTaskResponseVO> pageCond = new Page<>(page, rows);
		List<ExtractAccountTaskResponseVO> resultList = null;
		if(!params.getIsHistory()){
			//不是历史数据
			resultList = accountTaskMapper.getExtractAccountTaskList(pageCond, params);
		}else{
			resultList = accountTaskMapper.getExtractAccountTaskHistoryList(pageCond, params);
			resultList.forEach(e->{
				if(e.getTaskType() == ExtractTaskTypeEnum.DELAY.type){
					BudgetExtractDelayApplication delayApplication = delayApplicationMapper.selectOne(new LambdaQueryWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getDelayCode, e.getCode()));
					e.setStatusName(ExtractDelayStatusEnum.getValue(delayApplication.getStatus()));
				}else{
					e.setStatusName(ExtractStatusEnum.getValue(e.getStatus()));
				}
			});
		}
		return PageResult.apply(pageCond.getTotal(), resultList);
	}

	/**
	 * <p>查看明细</p>
	 *
	 * @author minzhq
	 * @date 2022/9/8 10:10
	 */
	public PageResult<ExtractAccountTaskDetailVO> getExtractAccountTaskDetail(String code, String unitName, String personalityName,String empNo, Integer payStatus, Integer page, Integer rows) {

		List<BudgetExtractAccountTask> accountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode, code));
		Map<String, Object> params = new HashMap<>(5);
		params.put("extractCode", code);
		params.put("unitName", unitName);
		params.put("personalityName", personalityName);
		params.put("payStatus", payStatus);
		params.put("empNo", empNo);
		if (accountTasks.get(0).getTaskType() == ExtractTaskTypeEnum.DELAY.type) {
			params.put("personalityIds", Arrays.asList(accountTasks.get(0).getPersonalityIds().split(",")));
		}
		Page<ExtractAccountTaskDetailVO> pageCond = new Page<>(page, rows);
		List<ExtractAccountTaskDetailVO> resultList = accountTaskMapper.getExtractAccountTaskDetail(pageCond, params);
		resultList.forEach(result -> result.setPayStatusName(ExtractPersonalityPayStatusEnum.getValue(result.getPayStatus())));
		return PageResult.apply(pageCond.getTotal(), resultList);
	}

	/**
	 * <p>获取延期支付申请单</p>
	 *
	 * @author minzhq
	 * @date 2022/9/8 10:26
	 */
	public ExtractDelayPayApplyVO getExtractDelayPayApplyDetail(String delayPayApplyOrderNo) {
		List<BudgetExtractAccountTask> budgetExtractAccountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode, delayPayApplyOrderNo));
		BudgetExtractAccountTask budgetExtractAccountTask = budgetExtractAccountTasks.get(0);
		if (budgetExtractAccountTask.getTaskType() == ExtractTaskTypeEnum.DELAY.type) {
			ExtractDelayPayApplyVO result = new ExtractDelayPayApplyVO();
			BudgetExtractsum extractSum = budgetExtractsumMapper.selectOne(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getCode, budgetExtractAccountTask.getRelationExtractCode()));
			result.setUnitName(extractSum.getDeptname());
			result.setOrderDate(budgetExtractAccountTask.getCreateTime());
			result.setPayReason("支付" + yearPeriodMapper.selectById(extractSum.getYearid()).getPeriod() + Integer.parseInt(extractSum.getExtractmonth().substring(4, 6)) + "月第" + Integer.parseInt(extractSum.getExtractmonth().substring(6, 8)) + "批提成");
			result.setExtractCode(extractSum.getCode());
			result.setBatch(budgetExtractAccountTask.getBatch());

			Map<String, Object> params = new HashMap<>(5);
			params.put("extractCode", extractSum.getCode());
			params.put("unitId", budgetExtractAccountTask.getBillingUnitId());
			params.put("personalityIds", Arrays.asList(budgetExtractAccountTask.getPersonalityIds().split(",")));
			List<ExtractAccountTaskDetailVO> resultList = accountTaskMapper.getExtractAccountTaskDetail(null, params);

			List<ExtractDelayPayApplyVO.ExtractDelayPayApplyPayDetail> payApplyPayDetails = resultList.stream().map(e -> {
				ExtractDelayPayApplyVO.ExtractDelayPayApplyPayDetail payDetail = new ExtractDelayPayApplyVO.ExtractDelayPayApplyPayDetail();
				payDetail.setEmpName(e.getEmpName());
				payDetail.setEmpNo(e.getEmpNo());
				payDetail.setAccountName(e.getAccountName());
				payDetail.setUserType(e.getAccountType() == 1 ? "个卡" : "公户");
				payDetail.setMoney(e.getMoney());
				return payDetail;
			}).collect(Collectors.toList());
			result.setPayDetailList(payApplyPayDetails);

			List<BudgetExtractPerPayDetail> perPayDetails = perPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPerPayDetail>().eq(BudgetExtractPerPayDetail::getExtractCode, extractSum.getCode()).in(BudgetExtractPerPayDetail::getPersonalityId, Arrays.asList(budgetExtractAccountTask.getPersonalityIds().split(","))));

			List<ExtractDelayPayApplyVO.ExtractDelayPayApplyPayMoneyDetail> payMoneyPayDetails = new ArrayList<>();
			perPayDetails.stream().collect(Collectors.groupingBy(BudgetExtractPerPayDetail::getBillingUnitId)).forEach((billingUnitId, list) -> {
				ExtractDelayPayApplyVO.ExtractDelayPayApplyPayMoneyDetail detail = new ExtractDelayPayApplyVO.ExtractDelayPayApplyPayMoneyDetail();
				detail.setBillingUnitName(billingUnitMapper.selectById(billingUnitId).getName());

				BigDecimal total = list.stream().map(BudgetExtractPerPayDetail::getPayMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal personalityPayMoney2 = list.stream().filter(e -> {
					IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMapper.selectById(e.getPersonalityId());
					return individualEmployeeFiles.getAccountType() == 1;
				}).map(BudgetExtractPerPayDetail::getPayMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
				detail.setPersonalityPayMoney2(personalityPayMoney2);
				detail.setPersonalityPayMoney1(total.subtract(personalityPayMoney2));
				payMoneyPayDetails.add(detail);
			});
			result.setPayMoneyDetailList(payMoneyPayDetails);


			BigDecimal payTotal = payMoneyPayDetails.stream().map(e -> {
				return e.getPersonalityPayMoney1().add(e.getPersonalityPayMoney2()).add(e.getFee()).add(e.getPayMoney());
			}).reduce(BigDecimal.ZERO, BigDecimal::add);
			result.setPayTotal(payTotal);

			return result;
		}
		return null;
	}

	/**
	 * <p>获取做账单位列表</p>
	 *
	 * @author minzhq
	 * @date 2022/9/9 13:55
	 */
	public List<ExtractBillingUnitVO> getExtractTaskBillingUnitList(String extractCode) {
		List<BudgetExtractAccountTask> accountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode,extractCode).eq(BudgetExtractAccountTask::getIsShouldAccount, 1).select(BudgetExtractAccountTask::getBillingUnitId));
		return accountTasks.stream().map(e -> {
			BudgetBillingUnit budgetBillingUnit = billingUnitMapper.selectById(e.getBillingUnitId());
			return new ExtractBillingUnitVO(budgetBillingUnit.getId(), budgetBillingUnit.getName());
		}).filter(distinct(ExtractBillingUnitVO::getId)).collect(Collectors.toList());
	}

	private <T> Predicate<T> distinct(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * <p>做账完成</p>
	 *
	 * @author minzhq
	 * @date 2022/9/9 14:22
	 */
	public void account(ExtractAccountDTO accountDTO) {

		billingUnitMapper.selectBatchIds(accountDTO.getBillingUnitIdList()).forEach(budgetBillingUnit -> {
			boolean isCanAccount = Arrays.stream(budgetBillingUnit.getAccountants().split(",")).anyMatch(e -> e.equals(UserThreadLocal.get().getUserId()));
			if(!isCanAccount){
				throw new RuntimeException("您不是【"+budgetBillingUnit.getName()+"】的会计！");
			}

		});
		LambdaUpdateWrapper<BudgetExtractAccountTask> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(BudgetExtractAccountTask::getExtractCode, accountDTO.getExtractCode());
		updateWrapper.eq(BudgetExtractAccountTask::getAccountantStatus, 0);
		updateWrapper.eq(BudgetExtractAccountTask::getIsShouldAccount, 1);
		updateWrapper.in(BudgetExtractAccountTask::getBillingUnitId, accountDTO.getBillingUnitIdList());
		updateWrapper.set(BudgetExtractAccountTask::getAccountantStatus, 1);
		updateWrapper.set(BudgetExtractAccountTask::getAccountantEmpNo, UserThreadLocal.get().getUserName());
		updateWrapper.set(BudgetExtractAccountTask::getAccountantTime, new Date());
		int update = accountTaskMapper.update(new BudgetExtractAccountTask(), updateWrapper);
		if (update != accountDTO.getBillingUnitIdList().size()) {
			throw new RuntimeException("存在已经做账完成的开票单位！");
		}

		List<BudgetExtractAccountTask> accountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode, accountDTO.getExtractCode()));


		boolean isDelay = false;
		if (accountTasks.get(0).getTaskType() == ExtractTaskTypeEnum.COMMON.type) {
			//提成支付申请单
			BudgetExtractsum extractSum = budgetExtractsumMapper.selectOne(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getCode, accountDTO.getExtractCode()));
			extractsumService.generateExtractStepLog(Lists.newArrayList(extractSum.getId()), OperationNodeEnum.ACCOUNTING,"【"+OperationNodeEnum.getValue(OperationNodeEnum.ACCOUNTING.getType()) + "】完成",1);
			Integer orderUnCompleteTaskCount = accountTaskMapper.selectCount(new LambdaQueryWrapper<BudgetExtractAccountTask>()
					.eq(BudgetExtractAccountTask::getTaskType, accountTasks.get(0).getTaskType())
					.eq(BudgetExtractAccountTask::getExtractCode, accountDTO.getExtractCode())
					.eq(BudgetExtractAccountTask::getAccountantStatus, 0)
					.eq(BudgetExtractAccountTask::getIsShouldAccount, 1));
			if(orderUnCompleteTaskCount == 0){
				//该单号完成做账
				extractSum.setStatus(ExtractStatusEnum.ACCOUNT.getType());
				budgetExtractsumMapper.updateById(extractSum);
			}
		} else if (accountTasks.get(0).getTaskType() == ExtractTaskTypeEnum.DELAY.type) {
			isDelay = true;
			BudgetExtractDelayApplication delayApplication = delayApplicationMapper.selectOne(new LambdaQueryWrapper<BudgetExtractDelayApplication>().eq(BudgetExtractDelayApplication::getDelayCode, accountDTO.getExtractCode()));
			delayApplication.setStatus(ExtractDelayStatusEnum.ACCOUNT.type);
			delayApplicationMapper.updateById(delayApplication);
		}

		/*
		 * 延期支付申请单一个单号完成做账就生成付款。
		 * 提成支付申请单一个批次完成做账就生成付款。
		 */
		Integer batchUnCompleteTaskCount = 100;
		List<String> delayExtractCodeList = null;
		if(isDelay){
			BudgetExtractAccountTask budgetExtractAccountTask = accountTaskMapper.selectOne(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode, accountDTO.getExtractCode()));
			batchUnCompleteTaskCount = accountTaskMapper.selectCount(new LambdaQueryWrapper<BudgetExtractAccountTask>()
					.eq(BudgetExtractAccountTask::getTaskType, ExtractTaskTypeEnum.DELAY.type)
					.eq(BudgetExtractAccountTask::getExtractMonth, budgetExtractAccountTask.getExtractMonth())
					.eq(BudgetExtractAccountTask::getBatch,budgetExtractAccountTask.getBatch())
					.eq(BudgetExtractAccountTask::getAccountantStatus, 0)
					.eq(BudgetExtractAccountTask::getIsShouldAccount, 1));

			if(batchUnCompleteTaskCount == 0) delayExtractCodeList = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>()
					.eq(BudgetExtractAccountTask::getTaskType, ExtractTaskTypeEnum.DELAY.type)
					.eq(BudgetExtractAccountTask::getExtractMonth, budgetExtractAccountTask.getExtractMonth())
					.eq(BudgetExtractAccountTask::getBatch,budgetExtractAccountTask.getBatch())
					.eq(BudgetExtractAccountTask::getAccountantStatus, 1)
					.eq(BudgetExtractAccountTask::getIsShouldAccount, 1)).stream().map(BudgetExtractAccountTask::getExtractCode).collect(Collectors.toList());
		}else{
			batchUnCompleteTaskCount = accountTaskMapper.selectCount(new LambdaQueryWrapper<BudgetExtractAccountTask>()
					.eq(BudgetExtractAccountTask::getTaskType, accountTasks.get(0).getTaskType())
					.eq(BudgetExtractAccountTask::getExtractMonth, accountTasks.get(0).getExtractMonth())
					.eq(BudgetExtractAccountTask::getAccountantStatus, 0)
					.eq(BudgetExtractAccountTask::getIsShouldAccount, 1));
		}

		if(batchUnCompleteTaskCount == 0){
			//做账全部完成。
			Map<Long, BudgetBillingUnit> unitMap = this.billingUnitMapper.selectList(null).stream().collect(Collectors.toMap(BudgetBillingUnit::getId, Function.identity()));
			extractsumService.finishAccount(isDelay,delayExtractCodeList,accountTasks.get(0).getExtractMonth(),unitMap);
		}
	}
}
