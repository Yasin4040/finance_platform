package com.jtyjy.finance.manager.controller.reimbursement;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.ReimbursentTimeDetailConstant;
import com.jtyjy.finance.manager.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.constants.ReimbursementStepHelper;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.event.bx.BxCodeRequest;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import org.springframework.util.CollectionUtils;
import weaver.hrm.User;

import javax.validation.constraints.NotEmpty;

/**
 * 报销流程
 *
 * @author User
 */
@Component
public class ReimbursementFlowWorker {

	@Autowired
	private TabFlowConditionService conditionService;

	@Autowired
	private BudgetReimbursementorderService orderService;

	@Autowired
	private BudgetReimbursementorderFdtaskDetailService taskDetailService;

	@Autowired
	private BudgetPaymoneyService paymoneyService;

	@Autowired
	private BudgetBillingUnitService billingUnitService;

	@Autowired
	private BudgetReimburmentTimedetailService timeDetailService;

	@Autowired
	private WbUserService userService;

	@Autowired
	private TabProcedureService procedureService;

	/**
	 * 审核流程校验
	 * 1.用户是否打开页面：controller已经校验
	 * 2.报销单是否存在
	 * 3.报销单是否提交
	 * 4.扫描版本与当前版本是否一致
	 * 5.报销单是否已经审核通过
	 * 6.前置环节是否通过，出纳付款：有票需要校验是否分单确认
	 * 7.当前环节是否已经被审核：接收、预算、分单扫描、分单确认、出纳付款
	 * 8.做账环节是否是会计校验
	 * 9.做账会计是否是本公司的
	 * 10.结束内部流转校验
	 *
	 * @param codeRequest
	 * @param stepAndOpt
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public String flowValidate(BxCodeRequest codeRequest, String stepAndOpt, BudgetReimbursementorder order) throws Exception {
		String step = stepAndOpt.split(ReimbursementController.STEP_OPT_SPLIT)[0];
		String opt = stepAndOpt.split(ReimbursementController.STEP_OPT_SPLIT)[1];
		//流程基础校验
		String result = this.flowBaseValidate(codeRequest, order, step, opt);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		//当前环节是否已经接收或者审核通过
		result = this.hasFinished(order, step, opt);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		//上个流程必须通过校验
		result = this.intoStepConditionValidate(order, step);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		//做账会计校验
		result = this.billAccountingValidate(order, step);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		//结束内部流转校验
		result = this.finishedInnerCycleValidate(order, step, opt);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		return null;
	}

	/**
	 * 校验完成之后：设置报销单、设置付款单、设置扫描日志、设置节点时间信息
	 *
	 * @param codeRequest
	 * @param stepAndOpt
	 * @param order
	 * @throws Exception
	 */
	public void save(BxCodeRequest codeRequest, String stepAndOpt, BudgetReimbursementorder order) throws Exception {
		String step = stepAndOpt.split(ReimbursementController.STEP_OPT_SPLIT)[0];
		String opt = stepAndOpt.split(ReimbursementController.STEP_OPT_SPLIT)[1];
		this.dispatcherBusiness(codeRequest, order, step, opt);
		//更新报销单，创建扫描模式的日志
		this.orderService.updateAndSaveScanLog(codeRequest, order, step, opt);
	}

	/**
	 * 分发业务
	 *
	 * @param codeRequest
	 * @param order
	 * @param step
	 * @param opt
	 * @throws Exception
	 */
	private void dispatcherBusiness(BxCodeRequest codeRequest, BudgetReimbursementorder order, String step, String opt) throws Exception {
		/**
		 * 0、设置接收状态receiceStatus -->设置当前扫描状态curscanstatus和curscanstatusname
		 * 1、所有环节：设置接收状态、设置扫码信息（分为单据接收和审核两种模式）
		 * 2、分单确认：校验当前登录人是否是做账会计、校验报销单做账公司是不是当前登录人的公司（分发前已经校验）-->设置分单详情接收人为当前登录人和接收时间
		 * 3、出纳付款：设置报销单的所有未接收付款的付款单的付款状态为接收付款、接收时间和接收人-->设置出纳付款接收状态true
		 */
		//设置当前扫描信息 0
		Date nowDate = new Date();
		order.setCurscanstatus(Integer.parseInt(step));
		order.setCurscaner(codeRequest.getEmpNo());
		order.setCurscanstatusname(ReimbursementStepHelper.getName(step));
		order.setCurscanername(UserThreadLocal.get().getDisplayName());
		order.setCurscantime(Constants.FULL_FORMAT.format(nowDate));
		//设置流程接收状态
		order.setReceivestatus(Integer.parseInt(step));
		if (ReimbursementStepHelper.BILL_RECEIVE.equals(step)) {
			/**
			 * 单据接收环节，一接收默认接收和审核全部完成
			 */
			if (order.getParverifyreceivestatus() != null
					&& order.getParverifyreceivestatus()
					&& order.getParverifystatus() != null && order.getParverifystatus())
				throw new RuntimeException("报销单【" + order.getReimcode() + "】已完成单据接收");
			//单据提交新增
			this.timeDetailService.createBudgetReimbursentTimeDetail(order.getSubmittime(), nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.ONE);
			//票面接收新增
			this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.TWO);
			//票面审核接收新增
			this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.THREE);
			ReimbursementStepHelper.setReceivedStatusTrue(order, step);
			ReimbursementStepHelper.setCheckedStatusTrue(order, step);
			this.orderService.updateById(order);
			return;
		}
		//财务总监审核
		if (ReimbursementStepHelper.FINANCIAL_MANAGE_CHECK.equals(step)){
			//this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.SEVEN);
			//this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.EIGHT);
		}
		if(ReimbursementStepHelper.GENERAL_MANAGER_CHECK.equals(step)){
			//this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), ReimbursentTimeDetailConstant.EIGHT);
		}
		//分单确认特有设置
		if (ReimbursementStepHelper.SPLIT_BILL_CONFIRM.equals(step)) {
			//设置分单详情的接收人和接收时间
			List<BudgetReimbursementorderFdtaskDetail> list = this.taskDetailService.getTaskByUserIdAndOrder(order.getId(), 0);
			if (list != null && list.size() > 0) {
				for (BudgetReimbursementorderFdtaskDetail fdDetail : list) {
					fdDetail.setReceiver(UserThreadLocal.get().getUserName());
					fdDetail.setReceivername(UserThreadLocal.get().getDisplayName());
					fdDetail.setReceivetime(nowDate);
				}
				this.taskDetailService.updateBatchById(list);
			}
		}
		//出纳付款特有设置
		if (ReimbursementStepHelper.CASHIER_PAY.equals(step)) {
			//设置每一个付款单状态为接收付款、接收人和接收时间
			List<BudgetPaymoney> list = this.paymoneyService.getByOrderCode(order.getReimcode());
			if (list != null && list.size() > 0) {
				for (BudgetPaymoney bean : list) {
					bean.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.getType());
					bean.setReceiver(UserThreadLocal.get().getUserName());
					bean.setReceivername(UserThreadLocal.get().getDisplayName());
					bean.setReceivetime(nowDate);
				}
				this.paymoneyService.updateBatchById(list);
			}
			this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 6);//分单确认修改
			this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 7);//出纳接收新增
		}
		if (ReimbursementStepHelper.ACCOUNTING_DO_BILL.equals(step) && "2".equals(opt)) {
			//会计做账审核模式--结束流转
			step = ReimbursementStepHelper.END_PROCESS;
			ReimbursementStepHelper.setCheckedStatusTrue(order, step);
			this.orderService.updateById(order);
			return;
		}
		//设置环节接收状态 1
		ReimbursementStepHelper.setReceivedStatusTrue(order, step);
		this.orderService.updateById(order);
	}

	/**
	 * 完成内部流转校验
	 *
	 * @param order
	 * @param step
	 * @param opt
	 * @return
	 * @throws Exception
	 */
	private String finishedInnerCycleValidate(BudgetReimbursementorder order, String step, String opt) throws Exception {
		//分单确认节点
		if (ReimbursementStepHelper.ACCOUNTING_DO_BILL.equals(step) && !"1".equals(opt)) {
			boolean flag = ReimbursementStepHelper.getStepFieldValue(order, ReimbursementStepHelper.CORPORATION_DRAW_BILL, true);
			if (!flag) {
				return "当前扫描报销单【" + order.getReimcode() + "】还未法人公司抽单，不能结束内部流转";
			}
		}
		return null;
	}

	/**
	 * 做账会计校验：当前人是否是做账会计，当前报销单是否属于会计公司的
	 * 8.做账环节是否是会计校验
	 * 9.做账会计是否是本公司的
	 *
	 * @param order
	 * @param step
	 * @param opt
	 * @return
	 */
	private String billAccountingValidate(BudgetReimbursementorder order, String step) {
		//分单确认节点
		if (ReimbursementStepHelper.SPLIT_BILL_CONFIRM.equals(step)) {
			//List<BudgetReimbursementorderFdtaskDetail> list = this.taskDetailService.getTaskByUserIdAndOrder(order.getId(),0);

			List<BudgetReimbursementorderFdtaskDetail> details = taskDetailService.list(new QueryWrapper<BudgetReimbursementorderFdtaskDetail>().eq("accountstatus", 0).eq("reimbursementid", order.getId()));
			if (details.isEmpty()) return "很抱歉！没有找到您的分单任务！";
			List<Long> unitIdList = details.stream().map(e -> e.getPlanbunitid()).collect(Collectors.toList());
			Map<Long, BudgetBillingUnit> billUnitMap = this.billingUnitService.listByIds(unitIdList).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
			long count = billUnitMap.values().stream().filter(e -> ("," + e.getAccountants() + ",").contains("," + UserThreadLocal.get().getUserId() + ",")).count();
			if (count == 0) {
				return "您不是当前扫描报销单【" + order.getReimcode() + "】的做账会计！";
			}
			//刷新最新的做账会计
			details.forEach(e -> {
				if (StringUtils.isBlank(billUnitMap.get(e.getPlanbunitid()).getAccountants())) return;
				List<WbUser> users = userService.selectByIds(Arrays.asList(billUnitMap.get(e.getPlanbunitid()).getAccountants().split(",")));
				e.setPlanaccounters(users.stream().map(u -> u.getUserName()).collect(Collectors.joining(",")));
				e.setPlanaccounternames(users.stream().map(u -> u.getDisplayName()).collect(Collectors.joining(",")));
			});
			taskDetailService.updateBatchById(details);
		}
		return null;
	}

	/**
	 * 当前环节已完成
	 *
	 * @param order
	 * @param step
	 * @return
	 * @throws Exception
	 */
	private String hasFinished(BudgetReimbursementorder order, String step, String opt) throws Exception {
		//7.当前环节是否已经被审核：接收、预算、财务总监审核、总经理审核、分单扫描、分单确认、出纳付款
		if (order.getWorkFlowStep().contains(ReimbursementStepHelper.get(step))) {

			boolean finished = false;
			if (ReimbursementStepHelper.SPLIT_BILL_CONFIRM.equals(step)) {
				//分单环节作为前置条件:有票无票准备
				boolean hasTicket = this.orderService.hasTicket(order);
				if (!hasTicket) {
					return "当前扫描报销单【" + order.getReimcode() + "】开票单位全为无票，无需分单确认！";
				}
			}
			if (ReimbursementStepHelper.ACCOUNTING_DO_BILL.equals(step) && "2".equals(opt)) {
				//会计做账审核模式--结束流转
				step = ReimbursementStepHelper.END_PROCESS;
			}
			finished = ReimbursementStepHelper.getStepFieldValue(order, step, "1".equals(opt));
			if (finished) {
				String setOpt = "1".equals(opt) ? "-【接收】" : "";
				return "当前扫描报销单【" + order.getReimcode() + "】，已经完成【" + ReimbursementStepHelper.getName(step) + "】" + setOpt + "！";
			}
		} else {
			return "当前扫描报销单【" + order.getReimcode() + "】无需进行【" + ReimbursementStepHelper.getName(step) + "】！";
		}
		return null;
	}

	/**
	 * 进入环节的条件校验，满足条件则能进入环节
	 *
	 * @param order
	 * @param step
	 * @return
	 * @throws Exception
	 */
	private String intoStepConditionValidate(BudgetReimbursementorder order, String step) throws Exception {
		//获取条件版本
		Integer version = order.getWorkFlowVersion();
		if (version == null) {
			return null;
		}
		TabProcedure tabProcedure = procedureService.getById(Long.valueOf(version));
		if (Objects.isNull(tabProcedure)) return "服务器数据异常";
		List<TabFlowCondition> conditions = this.conditionService.getByColumn(TabFlowCondition.class, "tab_flow_condition", "the_version", version);
		if (conditions == null || conditions.size() == 0) {
			return null;
		}
		/*
		 * update by minzhq
		 * 由流程模板中维护的顺序为校验顺序
		 * 提示最早没有完成的流程
		 */
		//流程顺序
		String procedureLinkOrder = tabProcedure.getProcedureLinkOrder();
		Map<String, Integer> linkOrderMap = new HashMap<>(10);
		String[] linkOrderArr = new String[0];
		if (StringUtils.isNotBlank(procedureLinkOrder)) {
			linkOrderArr = procedureLinkOrder.split(",");
			for (int i = 0; i < linkOrderArr.length; i++) {
				linkOrderMap.put(linkOrderArr[i], i);
			}
		}
		String curDm = ReimbursementStepHelper.get(step);
		//获取前面的环节
		List<String> orderDmList = getBeforeStepDm(linkOrderMap,curDm,linkOrderArr);
		//条件分类：接收和审核
		Set<String> mustFinishReceiveStep = new TreeSet<>();
		Set<String> mustFinishCheckStep = new TreeSet<>();
		//获取必须要完成的步骤（循环遍历所有的）
		getMustFinishSteps(mustFinishReceiveStep,mustFinishCheckStep,conditions,order.getWorkFlowStep(),orderDmList);
		//排个序(按审批顺序倒序)
		mustFinishReceiveStep = mustFinishReceiveStep.stream().sorted(Comparator.comparingInt(linkOrderMap::get).reversed()).collect(Collectors.toCollection(LinkedHashSet::new));
		mustFinishCheckStep = mustFinishCheckStep.stream().sorted(Comparator.comparingInt(linkOrderMap::get).reversed()).collect(Collectors.toCollection(LinkedHashSet::new));
		//判断流程是否在流程总栈中
		String result = validateStepIsOn(mustFinishReceiveStep,mustFinishCheckStep);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		Map<String, String> receiveErrorMap = this.doValidate(mustFinishReceiveStep, order, true);
		Map<String, String> checkErrorMsgMap = this.doValidate(mustFinishCheckStep, order, false);
		/*
		 * 提示最早的校验数据信息
		 */
		if(receiveErrorMap.isEmpty()){
			if(!checkErrorMsgMap.isEmpty()){
				return String.join("<br>", checkErrorMsgMap.values());
			}
		}else{
			if(checkErrorMsgMap.isEmpty()){
				return String.join("<br>", receiveErrorMap.values());
			}else{
				/*
				 * 如果都不为空。提示最早的错误信息
				 */
				String receiveErrorStepDm = receiveErrorMap.keySet().stream().findFirst().orElse("");
				String checkErrorStepDm = checkErrorMsgMap.keySet().stream().findFirst().orElse("");
				Integer stemOrder = linkOrderMap.get(receiveErrorStepDm);
				Integer checkStemOrder = linkOrderMap.get(checkErrorStepDm);
				return stemOrder > checkStemOrder?String.join("<br>", checkErrorMsgMap.values()): String.join("<br>", receiveErrorMap.values());
			}
		}
		return null;
	}

	private String validateStepIsOn(Set<String> mustFinishReceiveStep, Set<String> mustFinishCheckStep) {
		String result = this.beforeConditionValidate(mustFinishReceiveStep);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		result = this.beforeConditionValidate(mustFinishCheckStep);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		return null;
	}

	/**
	 * 获取之前的步骤
	 */
	private List<String> getBeforeStepDm(Map<String, Integer> linkOrderMap,String curDm,String[] linkOrderArr){
		Integer curOrder = linkOrderMap.get(curDm);
		return new ArrayList<>(Arrays.asList(linkOrderArr).subList(0, curOrder + 1));
	}

	/**
	 * 获取一定要完成的步骤 （循环获取）
	 */
	private void getMustFinishSteps(Set<String> mustFinishReceiveStep, Set<String> mustFinishCheckStep, List<TabFlowCondition> conditions,String workFlowStep,List<String> orderDmList) {
		orderDmList.forEach(curDm->{
			for (TabFlowCondition condition : conditions) {
				if (!"1".equals(condition.getFlowType())) {
					continue;
				}
				if (curDm.equals(condition.getStepDm())) {
					if (StatusConstants.BX_RECEIVE_MODEL.toString().equals(condition.getTheCondition())) {
						for (String conditionStepDm : condition.getConditionStepDm().split(",")) {
							if (workFlowStep.contains(conditionStepDm)) {
								//设置的所有环节中包含条件环节
								mustFinishReceiveStep.add(conditionStepDm);
							}
						}
					} else if (StatusConstants.BX_CHECK_MODEL.toString().equals(condition.getTheCondition())) {
						for (String conditionStepDm : condition.getConditionStepDm().split(",")) {
							if (workFlowStep.contains(conditionStepDm)) {
								//设置的所有环节中包含条件环节
								mustFinishCheckStep.add(conditionStepDm);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * 流程基础校验
	 * 2.报销单是否存在
	 * 3.报销单是否提交
	 * 4.扫描版本与当前版本是否一致
	 * 5.报销单是否已经审核通过
	 *
	 * @param codeRequest
	 * @param order
	 * @param opt
	 * @param step
	 * @return
	 */
	private String flowBaseValidate(BxCodeRequest codeRequest, BudgetReimbursementorder order, String step, String opt) {
		if (order == null) {
			return "报销单不存在！";
		}
		if (order.getReuqeststatus() == null || (StatusConstants.BX_SUBMIT != order.getReuqeststatus() && StatusConstants.BX_PASS != order.getReuqeststatus())) {
			return "报销单【" + order.getReimcode() + "】还未提交！";
		}
		if (!order.getVersion().equals(codeRequest.getVersion())) {
			return "报销单【" + order.getReimcode() + "】版本与系统版本不一致！";
		}
		if ((order.getReuqeststatus() == null || StatusConstants.BX_PASS == order.getReuqeststatus()) && Integer.parseInt(step) < 4 && (Integer.parseInt(opt) == 2)) {
			return "报销单【" + order.getReimcode() + "】已经审核完成！";
		}
		return null;
	}

	/**
	 * 前置条件校验
	 * 返回最前步骤的错误信息
	 */
	private Map<String,String> doValidate(Set<String> mustFinishStep, BudgetReimbursementorder order, boolean isReceived) throws Exception {
		//环节：单据接收--预算审核--财务总监审核--总经理审核--分单扫描--分单确认--出纳付款--会计做账--凭证审核--法人公司抽单
		boolean flag = Boolean.FALSE;
		String result = "【审核】";
		//显示所有环节的验证错误
		Map<String,String> resultMap = new HashMap<>(1);
		for (String step : mustFinishStep) {
			if (ReimbursementStepHelper.SPLIT_BILL_CONFIRM.equals(ReimbursementStepHelper.getStep(step))) {
				//分单环节作为前置条件:有票无票准备
				boolean hasTicket = this.orderService.hasTicket(order);
				if (!hasTicket) {
					continue;
				}
			}
			flag = ReimbursementStepHelper.getStepFieldValue(order, ReimbursementStepHelper.getStep(step), isReceived);
			if (!flag) {
				//条件环节未完成
				if (isReceived) {
					result = "【接收】";
				}
				Map<String,String> errorMap = new HashMap<>(1);
				errorMap.put(step,"报销单【" + order.getReimcode() + "】，还未完成" + ReimbursementStepHelper.getNameByDm(step) + result);
				resultMap = errorMap;
			}
		}
		//返回最前端的校验错误
		return resultMap;
	}


	/**
	 * 前置条件校验之前校验
	 *
	 * @param mustFinishReceiveStep
	 * @return
	 */
	private String beforeConditionValidate(Set<String> mustFinishReceiveStep) {
		if (mustFinishReceiveStep == null || mustFinishReceiveStep.size() == 0) {
			return null;
		}
		for (String step : mustFinishReceiveStep) {
			//环节是否存在于环节栈
			if (!ReimbursementStepHelper.contains(step)) {
				return "您所处环节不属于环节总栈！";
			}
		}
		return null;
	}
}
