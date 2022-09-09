package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.ExtractAccountDTO;
import com.jtyjy.finance.manager.enmus.ExtractPersonalityPayStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractTaskTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.*;
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
		List<ExtractAccountTaskResponseVO> resultList = accountTaskMapper.getExtractAccountTaskList(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), resultList);
	}

	/**
	 * <p>查看明细</p>
	 *
	 * @author minzhq
	 * @date 2022/9/8 10:10
	 */
	public PageResult<ExtractAccountTaskDetailVO> getExtractAccountTaskDetail(Long taskId, String unitName, String personalityName, Integer payStatus, Integer page, Integer rows) {

		BudgetExtractAccountTask budgetExtractAccountTask = accountTaskMapper.selectById(taskId);
		Map<String, Object> params = new HashMap<>(5);
		params.put("extractCode", budgetExtractAccountTask.getExtractCode());
		params.put("unitId", budgetExtractAccountTask.getBillingUnitId());
		params.put("unitName", unitName);
		params.put("personalityName", personalityName);
		params.put("payStatus", payStatus);
		if (budgetExtractAccountTask.getTaskType() == ExtractTaskTypeEnum.DELAY.type) {
			params.put("personalityIds", Arrays.asList(budgetExtractAccountTask.getPersonalityIds().split(",")));
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
		BudgetExtractAccountTask budgetExtractAccountTask = accountTaskMapper.selectOne(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getDelayExtractCode, delayPayApplyOrderNo));
		if (budgetExtractAccountTask.getTaskType() == ExtractTaskTypeEnum.DELAY.type) {
			ExtractDelayPayApplyVO result = new ExtractDelayPayApplyVO();
			BudgetExtractsum extractSum = budgetExtractsumMapper.selectOne(new LambdaQueryWrapper<BudgetExtractsum>().eq(BudgetExtractsum::getCode, budgetExtractAccountTask.getExtractCode()));
			result.setUnitName(extractSum.getDeptname());
			result.setOrderDate(budgetExtractAccountTask.getCreateTime());
			result.setPayReason("支付" + yearPeriodMapper.selectById(extractSum.getYearid()).getPeriod() + Integer.parseInt(extractSum.getExtractmonth().substring(4, 6)) + "月第" + Integer.parseInt(extractSum.getExtractmonth().substring(6, 8)) + "批提成");
			result.setExtractCode(extractSum.getCode());
			result.setBatch(budgetExtractAccountTask.getBatch());

			Map<String, Object> params = new HashMap<>(5);
			params.put("extractCode", budgetExtractAccountTask.getExtractCode());
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
	public List<ExtractBillingUnitVO> getExtractTaskBillingUnitList(Long taskId) {
		BudgetExtractAccountTask budgetExtractAccountTask = accountTaskMapper.selectById(taskId);
		List<BudgetExtractAccountTask> accountTasks = accountTaskMapper.selectList(new LambdaQueryWrapper<BudgetExtractAccountTask>().eq(BudgetExtractAccountTask::getExtractCode, budgetExtractAccountTask.getExtractCode()).eq(BudgetExtractAccountTask::getTaskType, budgetExtractAccountTask.getTaskType()).eq(BudgetExtractAccountTask::getIsShouldAccount, 1).select(BudgetExtractAccountTask::getBillingUnitId));
		return accountTasks.stream().map(e -> {
			BudgetBillingUnit budgetBillingUnit = billingUnitMapper.selectById(e.getBillingUnitId());
			return new ExtractBillingUnitVO(budgetBillingUnit.getId(), budgetBillingUnit.getName());
		}).collect(Collectors.toList());
	}

	/**
	 * <p>做账完成</p>
	 *
	 * @author minzhq
	 * @date 2022/9/9 14:22
	 */
	public void account(ExtractAccountDTO accountDTO) {
		BudgetExtractAccountTask budgetExtractAccountTask = accountTaskMapper.selectById(accountDTO.getTaskId());
		if (budgetExtractAccountTask.getAccountantStatus() == 1) {
			throw new RuntimeException("该任务已做账完成！");
		}

		LambdaUpdateWrapper<BudgetExtractAccountTask> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(BudgetExtractAccountTask::getId, budgetExtractAccountTask.getId());
		updateWrapper.eq(BudgetExtractAccountTask::getAccountantStatus, 0);
		updateWrapper.set(BudgetExtractAccountTask::getAccountantStatus, 1);
		int update = accountTaskMapper.update(new BudgetExtractAccountTask(), updateWrapper);
		if (update <= 0) {
			throw new RuntimeException("该任务已做账完成！");
		}


		Integer unCompleteTaskCount = accountTaskMapper.selectCount(new LambdaQueryWrapper<BudgetExtractAccountTask>()
				.eq(BudgetExtractAccountTask::getTaskType, budgetExtractAccountTask.getTaskType())
				.eq(BudgetExtractAccountTask::getExtractMonth, budgetExtractAccountTask.getExtractMonth())
				.eq(BudgetExtractAccountTask::getAccountantStatus, 0));
		if(unCompleteTaskCount == 0){
			//做账全部完成。

		}

		if (budgetExtractAccountTask.getTaskType() == ExtractTaskTypeEnum.COMMON.type) {
			//提成支付申请单

		} else if (budgetExtractAccountTask.getTaskType() == ExtractTaskTypeEnum.DELAY.type) {

		}
	}
}
