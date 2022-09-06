package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.easyexcel.ExtractInitPersonlityDetailExcelData;
import com.jtyjy.finance.manager.easyexcel.ExtractPersonalityPayDetailExcelData;
import com.jtyjy.finance.manager.easyexcel.ExtractPersonlityDetailExcelData;
import com.jtyjy.finance.manager.enmus.ExtractPersonalityPayStatusEnum;
import com.jtyjy.finance.manager.enmus.ExtractStatusEnum;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.exception.MyException;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailQueryVO;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/2
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractPersonalityPayService extends ServiceImpl<BudgetExtractPersonalityPayDetailMapper, BudgetExtractPersonalityPayDetail> {

	@Autowired
	private BudgetExtractPersonalityPayDetailMapper personalityPayDetailMapper;

	@Autowired
	private BudgetExtractsumService extractsumService;

	@Autowired
	private IndividualEmployeeFilesMapper individualEmployeeFilesMapper;
	@Autowired
	private BudgetBillingUnitMapper billingUnitMapper;

	@Autowired
	private MessageSender sender;

	@Autowired
	private TabDmMapper dmMapper;

	@Autowired
	private BudgetExtractTaxHandleRecordMapper taxHandleRecordMapper;

	/**
	 * <p>添加员工个体户发放明细</p>
	 *
	 * @param entity
	 * @author minzhq
	 * @date 2022/9/2 13:44
	 */
	public void addExtractPersonalityPayDetail(ExtractPersonalityPayDetailVO entity) {
		this.extractsumService.validateIsCanOperatePersonalityPayDetail(entity.getExtractBatch());
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = validateData(entity);
		BudgetExtractPersonalityPayDetail payDetail = BudgetExtractPersonalityPayDetail.transfer(entity, null);
		payDetail.setExtractMonth(entity.getExtractBatch());

		Map<String, ExtractPersonlityDetailExcelData> individualEmployeeAgoPayDetailMap = extractsumService.getIndividualEmployeeAgoPayDetail(Lists.newArrayList(entity.getPersonalityId()), entity.getExtractBatch());
		ExtractPersonlityDetailExcelData agoExcelData = individualEmployeeAgoPayDetailMap.get(entity.getPersonalityId().toString() + "&&" + entity.getBillingUnitId().toString());
		extractsumService.setPayDetail(agoExcelData,payDetail,entity.getPersonalityId(),entity.getBillingUnitId());
		personalityPayDetailMapper.insert(payDetail);
		extractsumService.reCalculateInvoice(Lists.newArrayList(entity.getPersonalityId()),entity.getExtractBatch());

	}

	private List<BudgetExtractPersonalityPayDetail> validateData(ExtractPersonalityPayDetailVO entity){
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>()
				.eq(BudgetExtractPersonalityPayDetail::getExtractMonth, entity.getExtractBatch())
				.eq(BudgetExtractPersonalityPayDetail::getPersonalityId, entity.getPersonalityId())
				.ne(entity.getId()!=null,BudgetExtractPersonalityPayDetail::getId, entity.getId()));

		long count = extractPersonalityPayDetails.stream().filter(e -> e.getPersonalityId().equals(entity.getPersonalityId()) && e.getBillingUnitId().equals(entity.getBillingUnitId())).count();
		if (count > 0) {
			throw new RuntimeException("此员工个体户已有此发放单位的发放明细。");
		}
		long count1 = extractPersonalityPayDetails.stream().filter(e -> !e.getPayStatus().equals(entity.getPayStatus())).count();
		if (count1 > 0) {
			throw new RuntimeException("此员工个体户存在发放状态不是【" + ExtractPersonalityPayStatusEnum.getValue(entity.getPayStatus()) + "】的发放明细。");
		}

		List<BudgetExtractdetail> budgetExtractdetails = extractsumService.getCurBatchPersionalityExtract(entity.getExtractBatch(), entity.getPersonalityId());
		//待发提成
		BigDecimal curExtract = budgetExtractdetails.stream().map(BudgetExtractdetail::getCopeextract).reduce(BigDecimal.ZERO, BigDecimal::add);
		entity.setExtract(curExtract);
		BigDecimal dbMoney = extractPersonalityPayDetails.stream().map(e -> {
			return e.getCurRealExtract().add(e.getCurWelfare()).add(e.getCurSalary());
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		if(entity.getCurExtract().add(entity.getCurWelfare()).add(entity.getCurSalary()).add(dbMoney).subtract(curExtract).compareTo(BigDecimal.ZERO)>0){
			throw new RuntimeException("保存失败！此员工个体户当期提成发放金额合计超出当期待发提成金额！");
		}
		return extractPersonalityPayDetails;
	}

	public void updateExtractPersonalityPayDetail(ExtractPersonalityPayDetailVO entity) {
		this.extractsumService.validateIsCanOperatePersonalityPayDetail(entity.getExtractBatch());
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = validateData(entity);
		BudgetExtractPersonalityPayDetail agoPayDetail = personalityPayDetailMapper.selectById(entity.getId());
		BudgetExtractPersonalityPayDetail payDetail = BudgetExtractPersonalityPayDetail.transfer(entity, agoPayDetail);
		payDetail.setExtractMonth(entity.getExtractBatch());
		Map<String, ExtractPersonlityDetailExcelData> individualEmployeeAgoPayDetailMap = extractsumService.getIndividualEmployeeAgoPayDetail(Lists.newArrayList(entity.getPersonalityId()), entity.getExtractBatch());
		ExtractPersonlityDetailExcelData agoExcelData = individualEmployeeAgoPayDetailMap.get(entity.getPersonalityId().toString() + "&&" + entity.getBillingUnitId().toString());
		extractsumService.setPayDetail(agoExcelData,payDetail,entity.getPersonalityId(),entity.getBillingUnitId());
		personalityPayDetailMapper.updateById(payDetail);
		extractsumService.reCalculateInvoice(Lists.newArrayList(entity.getPersonalityId()),entity.getExtractBatch());
	}

	public void deleteExtractPersonalityPayDetail(String ids) {
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectBatchIds(Arrays.asList(ids.split(",")));
		this.extractsumService.validateIsCanOperatePersonalityPayDetail(extractPersonalityPayDetails.get(0).getExtractMonth());

		long count = extractPersonalityPayDetails.stream().filter(e -> e.getIsInitData()).count();
		if(count>0)throw new RuntimeException("存在不能删除的数据。");
		long count1 = extractPersonalityPayDetails.stream().filter(e -> e.getIsSend()).count();
		if(count1>0)throw new RuntimeException("存在已发放的数据。");
		personalityPayDetailMapper.deleteBatchIds(Arrays.asList(ids.split(",")));

		extractPersonalityPayDetails.stream().collect(Collectors.groupingBy(e->e.getPersonalityId())).forEach((personalityId,list)->{
			extractsumService.reCalculateInvoice(Lists.newArrayList(personalityId),extractPersonalityPayDetails.get(0).getExtractMonth());
		});
	}

	public List<ExtractPersonalityPayDetailExcelData> getExtractPersonalityPayDetailVO(ExtractPersonalityPayDetailQueryVO params, String extractBatch) {
		PageResult<ExtractPersonalityPayDetailVO> extractPersonalityPayDetailVO = extractsumService.getExtractPersonalityPayDetailVO(params, null, null, extractBatch);
		return extractPersonalityPayDetailVO.getList().stream().map(e -> {
			ExtractPersonalityPayDetailExcelData extractData = new ExtractPersonalityPayDetailExcelData();
			try {
				BeanUtils.copyProperties(extractData, e);
			} catch (IllegalAccessException illegalAccessException) {
				illegalAccessException.printStackTrace();
			} catch (InvocationTargetException invocationTargetException) {
				invocationTargetException.printStackTrace();
			}
			extractData.setIsSendStr(e.getIsSend() ? "是" : "否");
			return extractData;
		}).collect(Collectors.toList());

	}

	public void personalityNotice(String extractBatch) {
		this.extractsumService.validateIsCanOperatePersonalityPayDetail(extractBatch);
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getExtractMonth, extractBatch));
		refreshData(extractPersonalityPayDetails,extractBatch);
		boolean test = extractsumService.isTest();
		String testNotice = extractsumService.getTestNotice();
		extractPersonalityPayDetails.stream().filter(e->e.getRemainingInvoices().compareTo(BigDecimal.ZERO)<0).collect(Collectors.groupingBy(BudgetExtractPersonalityPayDetail::getPersonalityId)).forEach((e,list)->{
			try {
				IndividualEmployeeFiles individualEmployeeFiles = individualEmployeeFilesMapper.selectById(e);
				String empNo = individualEmployeeFiles.getEmployeeJobNum().toString();
				if (test) {
					empNo = testNotice;
				}
				sender.sendQywxMsgSyn(new QywxTextMsg(empNo, null, null, 0, "个体户【"+individualEmployeeFiles.getAccountName()+"】缺少【"+list.get(0).getRemainingInvoices().abs()+"】发票，请尽快提交发票！", null));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}



	private void refreshData(List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails,String extractBatch) {
		if(!CollectionUtils.isEmpty(extractPersonalityPayDetails)){
			Map<String, ExtractPersonlityDetailExcelData> individualEmployeeAgoPayDetailMap = extractsumService.getIndividualEmployeeAgoPayDetail(extractPersonalityPayDetails.stream().map(BudgetExtractPersonalityPayDetail::getPersonalityId).collect(Collectors.toList()), extractBatch);
			extractPersonalityPayDetails.forEach(e->{
				ExtractPersonlityDetailExcelData agoExcelData = individualEmployeeAgoPayDetailMap.get(e.getPersonalityId().toString() + "&&" + e.getBillingUnitId().toString());
				extractsumService.setPayDetail(agoExcelData,e,e.getPersonalityId(),e.getBillingUnitId());
				personalityPayDetailMapper.updateById(e);
			});
			extractsumService.reCalculateInvoice(extractPersonalityPayDetails.stream().map(e->e.getPersonalityId()).distinct().collect(Collectors.toList()), extractBatch);
		}

	}
	/**
	 * <p>撤回员工个体户发放的导入</p>
	 * @author minzhq
	 * @date 2022/9/3 13:37
	 * @param extractBatch
	 */
	public void resetPersonalityPayDetailImport(String extractBatch) {
		personalityPayDetailMapper.delete(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getExtractMonth,extractBatch));
	}

	/**
	 * <p>确认完成</p>
	 * @author minzhq
	 * @date 2022/9/3 13:40
	 * @param extractBatch
	 * @param isSecond 是否是二次确认
	 */
	public void ensureComplete(String extractBatch,boolean isSecond) {
		extractsumService.validateIsCanOperatePersonalityPayDetail(extractBatch);
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectList(new LambdaQueryWrapper<BudgetExtractPersonalityPayDetail>().eq(BudgetExtractPersonalityPayDetail::getExtractMonth, extractBatch));
		refreshData(extractPersonalityPayDetails,extractBatch);
		long count = extractPersonalityPayDetails.stream().filter(e -> e.getRemainingInvoices().compareTo(BigDecimal.ZERO) < 0 || e.getRemainingPayLimitMoney().compareTo(BigDecimal.ZERO) < 0).count();
		if(count > 0 && !isSecond){
			throw new MyException("存在剩余票额、剩余发放限额为负数的数据，是否确认");
		}else{
			doEnsureComplete(extractBatch,extractPersonalityPayDetails);
		}
	}

	public void doEnsureComplete(String extractBatch,List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails){

		BudgetExtractTaxHandleRecord extractTaxHandleRecord = extractsumService.getExtractTaxHandleRecord(extractBatch);
		if(extractTaxHandleRecord==null){
			extractTaxHandleRecord = new BudgetExtractTaxHandleRecord();
			extractTaxHandleRecord.setExtractMonth(extractBatch);
			extractTaxHandleRecord.setIsSetExcessComplete(false);
			extractTaxHandleRecord.setIsCalComplete(false);
			extractTaxHandleRecord.setIsPersonalityComplete(true);
			taxHandleRecordMapper.insert(extractTaxHandleRecord);
		}else{
			LambdaUpdateWrapper<BudgetExtractTaxHandleRecord> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(BudgetExtractTaxHandleRecord::getExtractMonth,extractBatch);
			updateWrapper.eq(BudgetExtractTaxHandleRecord::getIsPersonalityComplete,0);
			updateWrapper.set(BudgetExtractTaxHandleRecord::getIsPersonalityComplete,1);
			int updateCount = taxHandleRecordMapper.update(new BudgetExtractTaxHandleRecord(), updateWrapper);
			if(updateCount==0){
				return;
			}else{
				extractsumService.taxGroupSuccess(extractBatch);
			}
		}
		List<BudgetExtractsum> curBatchExtractSumList = extractsumService.getCurBatchExtractSum(extractBatch);
		extractsumService.generateExtractStepLog(curBatchExtractSumList.stream().map(BudgetExtractsum::getId).collect(Collectors.toList()), OperationNodeEnum.TAX_PREPARATION_CALCULATION_2,OperationNodeEnum.getValue(OperationNodeEnum.TAX_PREPARATION_CALCULATION_2.getType()) + "完成",1);

		List<BudgetExtractPersonalityPayDetail> payCommonDetails = extractPersonalityPayDetails.stream().filter(e -> e.getPayStatus() == ExtractPersonalityPayStatusEnum.COMMON.type).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(payCommonDetails)){
			payCommonDetails.forEach(e->{
				e.setOperateTime(new Date());
			});
			this.updateBatchById(payCommonDetails);
		}

	}
	/**
	 * <p>取消完成</p>
	 * @author minzhq
	 * @date 2022/9/3 14:51
	 * @param extractBatch
	 */
	public void cancelEnsureComplete(String extractBatch) {
		extractsumService.validateExtractIsAllPass(extractBatch);
		BudgetExtractTaxHandleRecord extractTaxHandleRecord = extractsumService.getExtractTaxHandleRecord(extractBatch);
		if (extractTaxHandleRecord!=null && !extractTaxHandleRecord.getIsPersonalityComplete()) {
			throw new RuntimeException("取消失败！您还未完成员工个体户发放。");
		}
		LambdaUpdateWrapper<BudgetExtractPersonalityPayDetail> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(BudgetExtractPersonalityPayDetail::getExtractMonth,extractBatch);
		updateWrapper.set(BudgetExtractPersonalityPayDetail::getOperateTime,null);
		//updateWrapper.set(BudgetExtractPersonalityPayDetail::getIsSend,0);
		this.update(updateWrapper);
	}
	/**
	 * <p>导入初始化数据</p>
	 * @author minzhq
	 * @date 2022/9/3 15:22
	 * @param inputStream
	 */
	public void importInitPersonalityPayDetail(InputStream inputStream) {
		List<ExtractInitPersonlityDetailExcelData> details = EasyExcelUtil.getExcelContent(inputStream, ExtractInitPersonlityDetailExcelData.class);
		List<BudgetExtractPersonalityPayDetail> payDetails = details.stream().map(e -> {
			BudgetExtractPersonalityPayDetail payDetail = new BudgetExtractPersonalityPayDetail();
			payDetail.setExtractMonth("20211299");
			payDetail.setIsInitData(true);
			payDetail.setIsSend(true);
			payDetail.setCreateTime(new Date());
			IndividualEmployeeFiles individualEmployeeFiles = extractsumService.getIndividualEmployeeFiles(e.getEmpNo(), e.getPersonlityName());
			BudgetBillingUnit budgetBillingUnit = this.billingUnitMapper.selectOne(new LambdaQueryWrapper<BudgetBillingUnit>().eq(BudgetBillingUnit::getName, e.getBillingUnitName()).eq(BudgetBillingUnit::getStopFlag, 0));
			payDetail.setBillingUnitId(budgetBillingUnit.getId());
			payDetail.setPersonalityId(individualEmployeeFiles.getId());
			payDetail.setReceiptSum(e.getReceiptSum() == null ? BigDecimal.ZERO : e.getReceiptSum());
			payDetail.setSalarySum(BigDecimal.ZERO);
			payDetail.setWelfareSum(BigDecimal.ZERO);
			payDetail.setExtractSum(BigDecimal.ZERO);
			payDetail.setCurExtract(e.getCurExtract() == null ? BigDecimal.ZERO : e.getCurExtract());
			payDetail.setCurRealExtract(payDetail.getCurExtract());
			payDetail.setCurSalary(e.getCurSalary() == null ? BigDecimal.ZERO : e.getCurSalary());
			payDetail.setCurWelfare(e.getCurWelfare() == null ? BigDecimal.ZERO : e.getCurWelfare());
			payDetail.setRemainingInvoices(BigDecimal.ZERO);
			payDetail.setRemainingPayLimitMoney(BigDecimal.ZERO);
			payDetail.setOperateTime(new Date());
			payDetail.setPayStatus(ExtractPersonalityPayStatusEnum.getEnumeByValue(e.getPayStatus()).type);
			return payDetail;
		}).collect(Collectors.toList());
		if(!payDetails.isEmpty()){
			this.saveBatch(payDetails);
		}
	}
	/**
	 * <p>延期发放</p>
	 * @author minzhq
	 * @date 2022/9/5 14:08
	 */
	public void ensureSend(String extractBatch, String ids) {
		List<BudgetExtractsum> curBatchExtractSum = extractsumService.getCurBatchExtractSum(extractBatch);
		if(curBatchExtractSum.get(0).getStatus()< ExtractStatusEnum.CALCULATION_COMPLETE.getType()){
			throw new RuntimeException("操作失败！该单据不支持此操作。");
		}
		List<BudgetExtractPersonalityPayDetail> extractPersonalityPayDetails = personalityPayDetailMapper.selectBatchIds(Arrays.asList(ids.split(",")));

		long count = extractPersonalityPayDetails.stream().filter(e -> e.getPayStatus() == ExtractPersonalityPayStatusEnum.COMMON.type || e.getPayStatus() == ExtractPersonalityPayStatusEnum.TRANSFER.type).count();
		if(count > 0){
			throw new RuntimeException("存在不允许延期发放的数据，请重新选择。");
		}
		long count1 = extractPersonalityPayDetails.stream().filter(e -> e.getIsSend() != null && e.getIsSend()).count();
		if(count1>0){
			throw new RuntimeException("存在已发放的数据，请重新选择。");
		}

		extractPersonalityPayDetails.forEach(e->{
			e.setOperateTime(new Date());
		});
		this.updateBatchById(extractPersonalityPayDetails);
	}
}
