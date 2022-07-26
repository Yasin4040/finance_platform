package com.jtyjy.finance.manager.constants;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;

/**
 * 报销单流程环节【1：单据接收  2：预算审核 3：分单扫描 4：分单确认 5：出纳付款 6：会计做账 7：凭证审核 8：法人公司抽单 9：财务总监审核 10：总经理审核】
 * @author User
 *
 */
public class ReimbursementStepHelper {
	
	/**
	 * 环节代码映射
	 */
	private static final Map<String, String> STEP_DM_MAP = new HashMap<String, String>();
	/**
	 * 代码环节映射
	 */
	private static final Map<String, String> DM_STEP_MAP = new HashMap<String, String>();
	/**
	 * 环节名称映射
	 */
	private static final Map<String, String> STEP_NAME_MAP = new HashMap<String, String>();
	/**
	 * 环节代码
	 */
	private static final Set<String> STEPS = new HashSet<String>();
	
	/**
	 * 可以初始化的属性名
	 */
	private static final Map<String,String[]> CAN_INIT_FIELD_NAME = new HashMap<String,String[]>();
	
	/**
	 * 接收环节字段
	 */
	private static final Map<String,String> RECEIVED_STEP_FIELD = new HashMap<String,String>();
	
	/**
	 * 审核环节字段
	 */
	private static final Map<String,String> CHECKED_STEP_FIELD = new HashMap<String,String>();
	   
    /**
     * 报销审核（单据接收+预算审核）
     */
    public static final String BXSH = "1&2";
    
	/**
	 * 单据接收
	 */
	public static final String BILL_RECEIVE = "1";
	/**
	 * 预算审核
	 */
	public static final String BUDGET_CHECK = "2";
	/**
	 * 分单扫描
	 */
	public static final String SPLIT_BILL_SCAN = "3";
	/**
	 * 分单确认
	 */
	public static final String SPLIT_BILL_CONFIRM = "4";
	/**
	 * 出纳付款
	 */
	public static final String CASHIER_PAY = "5";
	/**
	 * 会计做账
	 */
	public static final String ACCOUNTING_DO_BILL = "6";
	/**
	 * 凭证审核
	 */
	public static final String VOUCHER_CHECK = "7";
	/**
	 * 法人公司抽单
	 */
	public static final String CORPORATION_DRAW_BILL = "8";
	
	
	/**
	 * 会计做账--结束流转
	 */
	public static final String END_PROCESS = "9";
	
	/**
	 * 财务总监审核
	 */
	public static final String FINANCIAL_MANAGE_CHECK = "10";
	/**
	 * 总经理审核
	 */
	public static final String GENERAL_MANAGER_CHECK = "11";
	
	/**
	 * 接收模式
	 */
	public static final String RECEIVED = "1";
	
	static {
		try {
			//设置环节代码映射
			STEP_DM_MAP.put(BILL_RECEIVE,"bill_receive");
			STEP_DM_MAP.put(BUDGET_CHECK,"budget_check");
			STEP_DM_MAP.put(SPLIT_BILL_SCAN,"split_bill_scan");
			STEP_DM_MAP.put(SPLIT_BILL_CONFIRM,"split_bill_confirm");
			STEP_DM_MAP.put(CASHIER_PAY,"cashier_pay");
			STEP_DM_MAP.put(ACCOUNTING_DO_BILL,"accounting_do_bill");
			STEP_DM_MAP.put(VOUCHER_CHECK,"voucher_check");
			STEP_DM_MAP.put(CORPORATION_DRAW_BILL,"corporation_draw_bill");
			STEP_DM_MAP.put(FINANCIAL_MANAGE_CHECK,"financial_manage_check");
			STEP_DM_MAP.put(GENERAL_MANAGER_CHECK,"general_manager_check");
			//设置环节代码名映射
			STEP_NAME_MAP.put(BILL_RECEIVE, "单据接收");
			STEP_NAME_MAP.put(BUDGET_CHECK, "预算审核");
			STEP_NAME_MAP.put(SPLIT_BILL_SCAN, "分单扫描");
			STEP_NAME_MAP.put(SPLIT_BILL_CONFIRM, "分单确认");
			STEP_NAME_MAP.put(CASHIER_PAY, "出纳付款");
			STEP_NAME_MAP.put(ACCOUNTING_DO_BILL, "会计做账");
			STEP_NAME_MAP.put(VOUCHER_CHECK, "凭证审核");
			STEP_NAME_MAP.put(CORPORATION_DRAW_BILL, "法人公司抽单");
			STEP_NAME_MAP.put(FINANCIAL_MANAGE_CHECK, "财务总监审核");
			STEP_NAME_MAP.put(GENERAL_MANAGER_CHECK, "总经理审核");
            STEP_NAME_MAP.put(END_PROCESS, "会计做账--结束流转");
			//设置代码环节代码映射
			for (Entry<String, String> entry : STEP_DM_MAP.entrySet()) {
				DM_STEP_MAP.put(entry.getValue(), entry.getKey());
			}
			//设置环节代码
			STEPS.addAll(STEP_DM_MAP.values());
			//单据接收和审核
			RECEIVED_STEP_FIELD.put(BILL_RECEIVE,"parverifyreceivestatus");
			CHECKED_STEP_FIELD.put(BILL_RECEIVE,"parverifystatus");
			//预算接收和审核
			RECEIVED_STEP_FIELD.put(BUDGET_CHECK,"budgetverifyreceivestatus");
			CHECKED_STEP_FIELD.put(BUDGET_CHECK,"budgetverifystatus");
			//分单接收和审核
			RECEIVED_STEP_FIELD.put(SPLIT_BILL_SCAN,"fdreceivestatus");
			CHECKED_STEP_FIELD.put(SPLIT_BILL_SCAN,"fdstatus");
			//做账接收和审核（分单确认）
			RECEIVED_STEP_FIELD.put(SPLIT_BILL_CONFIRM,"accountreceivestatus");
			CHECKED_STEP_FIELD.put(SPLIT_BILL_CONFIRM,"accountstatus");
			//出纳付款是否接收
			RECEIVED_STEP_FIELD.put(CASHIER_PAY,"cashierpaymentreceivestatus");
			//会计做账是否接收
			RECEIVED_STEP_FIELD.put(ACCOUNTING_DO_BILL,"account1receivestatus");
			CHECKED_STEP_FIELD.put(END_PROCESS,"endreceivestatus");
			//凭证审核是否接收
			RECEIVED_STEP_FIELD.put(VOUCHER_CHECK,"voucherauditreceivestatus");
			//法人公司抽单是否接收
			RECEIVED_STEP_FIELD.put(CORPORATION_DRAW_BILL,"drawbillreceivestatus");
			//财务总监审核是否接收和审核
			RECEIVED_STEP_FIELD.put(FINANCIAL_MANAGE_CHECK,"financialmanagereceivestatus");
            CHECKED_STEP_FIELD.put(FINANCIAL_MANAGE_CHECK,"financialmanagestatus");
			//总经理审核是否接收和审核
            RECEIVED_STEP_FIELD.put(GENERAL_MANAGER_CHECK,"generalmanagereceivestatus");
            CHECKED_STEP_FIELD.put(GENERAL_MANAGER_CHECK,"generalmanagestatus");
            
			//设置环节可初始化属性
			CAN_INIT_FIELD_NAME.put(BILL_RECEIVE,new String[]{"parverifyreceivestatus","parverifytime","parverifystatus","parverifyer"} );
			CAN_INIT_FIELD_NAME.put(BUDGET_CHECK, new String[]{"budgetverifyreceivestatus","budgetverifytime","budgetverifystatus","budgetverifyer"});
			CAN_INIT_FIELD_NAME.put(SPLIT_BILL_SCAN, new String[]{"fdreceivestatus","fdtime","fdstatus","fder"});
			CAN_INIT_FIELD_NAME.put(SPLIT_BILL_CONFIRM, new String[]{"accountreceivestatus","accounttime","accountstatus","accounter"});
            CAN_INIT_FIELD_NAME.put(FINANCIAL_MANAGE_CHECK, new String[]{"financialmanagereceivestatus","financialmanagetime","financialmanagestatus","financialmanager"});
			CAN_INIT_FIELD_NAME.put(GENERAL_MANAGER_CHECK, new String[]{"generalmanagereceivestatus","generalmanagetime","generalmanagestatus","generalmanager"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据请求参数获取环节代码
	 * @param key
	 * @return
	 */
	public static final String get(String key) {
		return STEP_DM_MAP.get(key);
	}
	
	/**
	 * 根据环节代码获取数字环节
	 * @param key
	 * @return
	 */
	public static final String getStep(String dm) {
		return DM_STEP_MAP.get(dm);
	}
	
	/**
	 * 环节栈是否包含某个环节
	 * @param step
	 * @return
	 */
	public static final boolean contains(String step){
	    for (String _step : step.split(",")) {
	        if (!STEPS.contains(_step)) {
	            return false;
	        }
	    }
		return true;
	}
	
	/**
	 * 根据请求参数获取环节名称代码
	 * @param key
	 * @return
	 */
	public static final String getName(String key) {
		return STEP_NAME_MAP.get(key);
	}
	
	/**
	 * 根据环节代码获取环节名称
	 * @param key
	 * @return
	 */
	public static final String getNameByDm(String dm) {
		return STEP_NAME_MAP.get(DM_STEP_MAP.get(dm));
	}
	
	/**
	 * 环节信息：【报销单号】：【环节】-【操作】-【状态】
	 * @param orderCode 报销单号
	 * @param step
	 * @param opt
	 * @return
	 */
	public static final String getStepMessage(String orderCode, String step, String opt, String resultMessage) {
		if(RECEIVED.equals(opt)) {
			return "报销单【"+orderCode+"】：【"+getName(step)+"】-【接收】-【扫描"+resultMessage+"】";
		}
		return "报销单【"+orderCode+"】：【"+getName(step)+"】-【扫描"+resultMessage+"】";
	}
	
	/**
	 * 获取环节属性值
	 * @param order 报销单
	 * @param step 环节代码
	 * @param isReceived 是否接收
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public static final boolean getStepFieldValue(BudgetReimbursementorder order, String step,boolean isReceived) throws Exception {
		
		Field field = null;
		String fieldName = CHECKED_STEP_FIELD.get(step);
		if(isReceived) {
			fieldName = RECEIVED_STEP_FIELD.get(step);
		}
		if (StringUtils.isBlank(fieldName)) {//无此属性
		    return true;
		}
		field = BudgetReimbursementorder.class.getDeclaredField(fieldName);
		if(field == null) {
			throw new Exception("环节对应的属性在报销单对象中不存在！");
		}
		field.setAccessible(true);
		return (Boolean)field.get(order);
	}
	
	/**
	 * 设置环节接收状态为true
	 * @param order
	 * @param step
	 * @throws Exception 
	 */
	public static final void setReceivedStatusTrue(BudgetReimbursementorder order, String step) throws Exception {
		Field field = BudgetReimbursementorder.class.getDeclaredField(RECEIVED_STEP_FIELD.get(step));
		field.setAccessible(true);
		field.set(order, true);
	}
	
	/**
	 * 设置环节审核状态为true
	 * @param order
	 * @param step
	 * @throws Exception 
	 */
	public static final void setCheckedStatusTrue(BudgetReimbursementorder order, String step) throws Exception {
	    String nowDate = Constants.FULL_FORMAT.format(new Date());
	    order.setCurscanername(UserThreadLocal.get().getDisplayName());
        order.setCurscaner(UserThreadLocal.get().getUserName());
        order.setCurscanstatusname(ReimbursementStepHelper.getName(step));
        order.setCurscantime(nowDate);
	    Field field = BudgetReimbursementorder.class.getDeclaredField(CHECKED_STEP_FIELD.get(step));
		field.setAccessible(true);
		field.set(order, true);
		String userName = UserThreadLocal.get().getUserName();
		if(BILL_RECEIVE.equals(step)) {
			order.setParverifyer(userName);
			order.setParverifytime(nowDate);
		}else if(BUDGET_CHECK.equals(step)) {
			order.setBudgetverifyer(userName);
			order.setBudgetverifytime(nowDate);
		}else if(SPLIT_BILL_CONFIRM.equals(step)) {
            order.setAccounter(userName);
            order.setAccounttime(nowDate);
        }else if(FINANCIAL_MANAGE_CHECK.equals(step)) {
	        order.setFinancialmanager(userName);
	        order.setFinancialmanagetime(nowDate);
	    }else if(GENERAL_MANAGER_CHECK.equals(step)) {
            order.setGeneralmanager(userName);
            order.setGeneralmanagetime(nowDate);
        }else if(END_PROCESS.equals(step)){
            order.setEndreceivestatus(true);
        }
	}

	/**
	 * 初始化流程
	 * @param order
	 * @param steps 初始化传递的流程
	 * @throws Exception 
	 */
	public static void init(BudgetReimbursementorder order, String... steps) throws Exception {
		String[] values = null;
		for (Entry<String, String[]> entry : CAN_INIT_FIELD_NAME.entrySet()) {
			for (String step : steps) {
			    String stepCode = ReimbursementStepHelper.get(entry.getKey());//环节代码
				if(stepCode.equals(step)) {
					values = entry.getValue();
					if(values != null && values.length > 0) {
						Field field = null;
						for (String value : values) {
							field = BudgetReimbursementorder.class.getDeclaredField(value);
							if(field != null) {
								field.setAccessible(true);
								if(field.getType() == Boolean.class) {
									field.set(order, Boolean.FALSE);
								}else {
									field.set(order,null);
								}
							}
						}
					}
				}
			}
				 
		}
	}
	
	/**
	 * 获取后置环节
	 * @param order 报销单
	 * @param step  当前环节
	 * @param containMe 是否包含当前环节
	 * @return
	 */
	public static final String[] afterSteps(BudgetReimbursementorder order, String step, boolean containMe) {
		String[] steps = order.getWorkFlowStep().split(",");
		StringJoiner sj = new StringJoiner(",");
		boolean flag = Boolean.FALSE;
		for (int i = steps.length -1 ; i >= 0 ; i--) {
		    String _step = steps[i];
		    if(flag) {
                sj.add(_step);
            }
            if(!flag && step.equals(_step)) {
                flag = Boolean.TRUE;
                if(containMe) {
                    sj.add(_step);
                }
            }
		}
		return sj.toString().split(",");
	}
}
