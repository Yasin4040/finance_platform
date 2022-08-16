package com.jtyjy.finance.manager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtyjy.finance.manager.bean.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销单请求参数
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementRequest {

	/**
	 * 保存
	 */
	public static final String SAVE = "1";

	/**
	 * 修改
	 */
	public static final String MODIFY = "2";

	/**
	 * 提交
	 */
	public static final String SUBMIT = "3";

	//请求类型 1:保存 2：提交
	@ApiModelProperty(value = "请求类型 1：申请 2：修改",required = true)
	private String requestType;

	//请求类型 1:保存 2：提交
	@ApiModelProperty(value = "是否提交 1：是 其他：否",required = true)
	private String submit;

	//报销主表
	@ApiModelProperty(value = "报销信息",required = true)
	private BudgetReimbursementorder order;

	//明细表
	@ApiModelProperty(value = "明细",required = true)
	private List<BudgetReimbursementorderDetail> orderDetail = new ArrayList<>();

	//冲账表
	@ApiModelProperty(value = "冲账信息",required = true)
	private List<BudgetReimbursementorderPayment> orderPayment = new ArrayList<>();

	//转账表
	@ApiModelProperty(value = "转账信息",required = true)
	private List<BudgetReimbursementorderTrans> orderTrans = new ArrayList<>();

	//现金表
	@ApiModelProperty(value = "现金信息",required = true)
	private List<BudgetReimbursementorderCash> orderCash = new ArrayList<>();

	//划拨表
	@ApiModelProperty(value = "划拨信息",required = true)
	private List<BudgetReimbursementorderAllocated> orderAllocated = new ArrayList<>();

	//差旅表
	@ApiModelProperty(value = "差旅信息",required = true)
	private List<BudgetReimbursementorderTravel> orderTravel = new ArrayList<>();

	//招待表
	@ApiModelProperty(value = "招待信息",required = true)
	private List<BudgetReimbursementorderEntertain> orderEntertain = new ArrayList<>();

	//欠票信息
	@ApiModelProperty(value = "欠票信息",required = true)
	private List<BudgetReimbursementorderLackBill> lackBillList = new ArrayList<>();

	/**
	 * 是否入职半年
	 */
	private Boolean isHireHalfYear = false;

	/**
	 * 是否项目报销
	 */
	private Boolean isProjectBx;

	/**
	 * 是否是固定资产
	 */
	private Boolean isFixAsset;
	/**
	 * 固定资产锁定的报销科目数据
	 */
	private List<AssetSubjectMsg> assetLockedSubjectList;
	/**
	 * 是否仅验证(固定资产专属)  false 不做操作  true 保存
	 */
	private Boolean isOnlyValidate;
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AssetSubjectMsg{
		@ApiModelProperty(value = "科目id")
		private Long subjectId;
		@ApiModelProperty(value = "年度动因id")
		private Long yearAgentId;
		@ApiModelProperty(value = "月度动因id")
		private Long monthAgentId;
		@ApiModelProperty(value = "月id")
		private Long monthId;
		@ApiModelProperty(value = "部门id")
		private Long unitId;
		@ApiModelProperty(value = "金额")
		private BigDecimal lockedMoney;
	}


	/**
	 * 校验动因可执行金额
	 * @param orderDetail2
	 * @param string
	 * @param map
	 * @param errorTip
	 * @return
	 * @throws Exception
	 */
	public static <T> List<String> agentValidate(Class<T> clazz, List<T> list, String agentIdFieldName,String moneyFieldName, Map<String, Map<String,BigDecimal>> map, String errorTip,String rowFieldName) throws Exception {
		Field field = clazz.getDeclaredField(agentIdFieldName);
		if(field == null) {
			throw new Exception(clazz.getName() + "没有" + agentIdFieldName + "属性......");
		}
		Field moneyField= clazz.getDeclaredField(moneyFieldName);
		if(moneyField == null) {
			throw new Exception(clazz.getName() + "没有" + moneyFieldName + "属性......");
		}
		Field rowField= clazz.getDeclaredField(rowFieldName);
		if(rowField == null) {
			throw new Exception(clazz.getName() + "没有" + moneyFieldName + "属性......");
		}
		field.setAccessible(Boolean.TRUE);
		moneyField.setAccessible(Boolean.TRUE);
		rowField.setAccessible(Boolean.TRUE);
		BigDecimal requestMoney = null;
		BigDecimal bigMoney = null;
		Map<String,BigDecimal> requestMoneyMap = new HashMap<>();
		List<String> errmsg = new ArrayList<>();
		for (T ele : list) {
			//行号
			Integer row = (Integer)rowField.get(ele);
			String key = field.get(ele).toString();
			bigMoney = map.get(key).get("execMoney");
			if(bigMoney == null) {
				continue;
			}
			/**
			 * update by minzhq。存在重复动因的情况
			 */
			requestMoney = (BigDecimal) moneyField.get(ele);
			BigDecimal sameAgentMoney = BigDecimal.ZERO;
			if(requestMoneyMap.get(key)!=null) {
				sameAgentMoney = sameAgentMoney.add(requestMoneyMap.get(key));
			}
			requestMoney = requestMoney.add(sameAgentMoney);
			requestMoneyMap.put(key, requestMoney);
			BigDecimal lockedMoney = map.get(key).get("lockedMoney");
			BigDecimal assetLockedMoney = map.get(key).get("assetLockedMoney");
			if(assetLockedMoney == null) assetLockedMoney = BigDecimal.ZERO;
			if(bigMoney.add(assetLockedMoney).compareTo(requestMoney) < 0) {
				String lockedTip = "";
				String assetLockedTip = "";
				if(assetLockedMoney.compareTo(BigDecimal.ZERO)>0) assetLockedTip = assetLockedTip + "其中固定资产中被锁定的金额为【"+assetLockedMoney.stripTrailingZeros().toPlainString()+"】";
				if(lockedMoney.compareTo(BigDecimal.ZERO)>0) lockedTip = lockedTip+"其中被锁定的金额为【"+lockedMoney.stripTrailingZeros().toPlainString()+"】。";
				String curErrorMsg = errorTip + "，第" + row + "条数据可用金额【"+bigMoney.stripTrailingZeros().toPlainString()+"】不足以报销【"+requestMoney.stripTrailingZeros().toPlainString()+"】。"+lockedTip+assetLockedTip;
				errmsg.add(curErrorMsg);
			}
		}
		return errmsg;
	}


	public static void main(String[] args) throws Exception {
		ReimbursementRequest request = new ReimbursementRequest();
		request.setSubmit("0");
		request.setRequestType(ReimbursementRequest.SAVE);
		request.setOrder(BudgetReimbursementorder.getTestBean());
		request.setOrderDetail(BudgetReimbursementorderDetail.getTestBean());
		request.setOrderAllocated(BudgetReimbursementorderAllocated.getTestBean());
		request.setOrderCash(BudgetReimbursementorderCash.getTestBean());
//		request.setOrderPayment(BudgetReimbursementorderPayment.getBean());
		request.setOrderTrans(BudgetReimbursementorderTrans.getTestBean());
//		request.setOrderEntertain(BudgetReimbursementorderEntertain.getTestBean());
//		request.setOrderTravel(BudgetReimbursementorderTravel.getTestBean());
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		System.out.println(mapper.writeValueAsString(request));
	}
}
