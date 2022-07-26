package com.jtyjy.finance.manager.controller.authorfee.calpay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jtyjy.finance.manager.bean.BudgetAuthorfeedetail;
import com.jtyjy.finance.manager.bean.BudgetAuthorfeedtlMerge;
import com.jtyjy.finance.manager.bean.BudgetBaseUnit;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderAllocated;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderDetail;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTrans;
import com.jtyjy.finance.manager.bean.BudgetUnit;

import lombok.Data;

/**
 * 稿费报销通用数据
 * @author minzhq
 *
 */
@Data
public class AuthorFeeReimCommonData {
	
	private String feeMonth;
	
	private String sumIds; //稿费主表的id  从小到大排序 例1-2-3
	
	private BudgetBaseUnit bxBaseUnit; //报销的基础单位
	
	private BudgetUnit bxUnit; //报销预算单位
	
	List<BudgetAuthorfeedetail> feeMonthDetails; //当前稿费月份的稿费明细
	List<BudgetAuthorfeedetail> curCheckFeeDetailList; //当前勾选的所有稿费明细
	
	List<BudgetAuthorfeedtlMerge> curCheckMergeFeeDetailList;  //当前勾选的所有稿费合并明细
	
	private BigDecimal serviceFee = BigDecimal.ZERO;
	
	private BudgetReimbursementorder curCreatedBxOrder; //当前创建的报销单主表
	
	private List<BudgetReimbursementorderTrans> transList = new ArrayList<>(); //转账
	
	private List<BudgetReimbursementorderAllocated> allocatedList = new ArrayList<>();//划拨的list
	
	private List<BudgetReimbursementorderDetail> bxDetailList = new ArrayList<>();//报销明细
	
	private BigDecimal othermoney = BigDecimal.ZERO; //其它金额
	
	private List<Map<String,Object>> executeDataList; //执行数据
}
