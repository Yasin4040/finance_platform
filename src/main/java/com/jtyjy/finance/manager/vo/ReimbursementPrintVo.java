package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jtyjy.finance.manager.bean.BudgetReimbursementorderAllocated;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderCash;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderPayment;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorderTrans;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 报销单打印详情
 * @author User
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReimbursementPrintVo {

	@ApiModelProperty(value = "流水号",hidden = false)
	private String serialNumber;
	@ApiModelProperty(value = "转账信息",hidden = false)
	private List<TransVo> transList = new ArrayList<>();
	@ApiModelProperty(value = "现金信息",hidden = false)
	private List<CashVo> cashList;
	@ApiModelProperty(value = "划拨信息",hidden = false)
	private List<AllocatedVo> allocatedList;
	@ApiModelProperty(value = "冲账信息",hidden = false)
	private List<PaymenyVo>paymentList;
	@ApiModelProperty(value = "转账小计",hidden = false)
	private BigDecimal transTotal;
	@ApiModelProperty(value = "现金小计",hidden = false)
	private BigDecimal cashTotal;
	@ApiModelProperty(value = "划拨小计",hidden = false)
	private BigDecimal allocatedTotal;
	@ApiModelProperty(value = "冲账小计",hidden = false)
	private BigDecimal paymentTotal;

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public List<TransVo> getTransList() {
		return transList;
	}

	public void setTransList(List<TransVo> transList) {
		this.transList = transList;
		BigDecimal total = BigDecimal.ZERO;
		for (TransVo ele : transList) {
			 total = total.add(ele.getMoney());
		}
		this.transTotal = total;
	}

	public List<CashVo> getCashList() {
		return cashList;
	}

	public void setCashList(List<CashVo> cashList) {
		this.cashList = cashList;
		BigDecimal total = BigDecimal.ZERO;
		for (CashVo ele : cashList) {
			 total = total.add(ele.getMoney());
		}
		this.cashTotal = total;
	}

	public List<AllocatedVo> getAllocatedList() {
		return allocatedList;
	}

	public void setAllocatedList(List<AllocatedVo> allocatedList) {
		this.allocatedList = allocatedList;
		BigDecimal total = BigDecimal.ZERO;
		for (AllocatedVo ele : allocatedList) {
			 total = total.add(ele.getMoney());
		}
		this.allocatedTotal = total;
	}
	
	public List<PaymenyVo> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<PaymenyVo> paymentList) {
		this.paymentList = paymentList;
		BigDecimal total = BigDecimal.ZERO;
		for (PaymenyVo ele : paymentList) {
			 total = total.add(ele.getMoney());
		}
		this.paymentTotal = total;
	}

	public BigDecimal getPaymentTotal() {
		return paymentTotal;
	}
	
	public BigDecimal getTransTotal() {
		return transTotal;
	}

	public BigDecimal getCashTotal() {
		return cashTotal;
	}

	public BigDecimal getAllocatedTotal() {
		return allocatedTotal;
	}
	
	public static ReimbursementPrintVo apply(ReimbursementRequest request) {
		ReimbursementPrintVo vo = new ReimbursementPrintVo();
		if(request != null) {
			String reimcode = request.getOrder().getReimcode();
			vo.setSerialNumber(reimcode);
			List<CashVo> cashList = null;
			List<AllocatedVo> allocatedList = null;
			List<PaymenyVo> paymentList = null;
			List<BudgetReimbursementorderTrans> tList = request.getOrderTrans();
			if(tList != null && tList.size() > 0){
				tList.stream().collect(Collectors.groupingBy(BudgetReimbursementorderTrans::getDraweeunitname)).forEach((billingunitname,list)->{
					List<BillingUnitTransVo> transVos = list.stream().map(e -> new BillingUnitTransVo(e.getPayeename(), e.getPayeebankaccount(), e.getPayeebankname(), e.getTransmoney(), e.getDraweeunitname())).collect(Collectors.toList());
					BigDecimal total = list.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
					vo.getTransList().add(new TransVo(total,transVos,transVos.isEmpty()?null:transVos.get(0).getBillingUnitName()));
				});
				vo.setTransList(vo.getTransList());
			}
			List<BudgetReimbursementorderCash> cList = request.getOrderCash();
			if(cList != null && cList.size() > 0){
				cashList = new ArrayList<CashVo>();
				for (BudgetReimbursementorderCash bean : cList) {
					cashList.add(new CashVo(bean.getPayeecode(), bean.getPayeename(), bean.getCashmoney()));
				}
				vo.setCashList(cashList);
			}
			List<BudgetReimbursementorderAllocated> aList = request.getOrderAllocated();
			if(aList != null && aList.size() > 0){
				allocatedList = new ArrayList<AllocatedVo>();
				for (BudgetReimbursementorderAllocated bean : aList) {
					allocatedList.add(new AllocatedVo(bean.getUnitname(), bean.getSubjectname(), bean.getAllocatedmoney()));
				}
				vo.setAllocatedList(allocatedList);
			}
			List<BudgetReimbursementorderPayment> pList = request.getOrderPayment();
			if(pList != null && pList.size() > 0){
				paymentList = new ArrayList<PaymenyVo>();
				for (BudgetReimbursementorderPayment bean : pList) {
					paymentList.add(new PaymenyVo(bean.getLendmoneyname(),bean.getLendType(), bean.getLendmoney(), bean.getPaymentmoney()));
				}
				vo.setPaymentList(paymentList);
			}
		}
		return vo;
	}

}

/**
 * 转账信息
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class TransVo{
	@ApiModelProperty(value = "小计",hidden = false)
	private BigDecimal money;
	@ApiModelProperty(value = "转账数据列表",hidden = false)
	private List<BillingUnitTransVo> voList = new ArrayList<>();
	@ApiModelProperty
	private String billingUnitName;
	
}
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class BillingUnitTransVo{
	@ApiModelProperty(value = "户名",hidden = false)
	private String accountName;
	@ApiModelProperty(value = "账号",hidden = false)
	private String account;
	@ApiModelProperty(value = "开户行",hidden = false)
	private String openBank;
	@ApiModelProperty(value = "转账金额",hidden = false)
	private BigDecimal money;
	@ApiModelProperty(value = "开票单位名称",hidden = false)
	private String billingUnitName;
}


/**
 * 现金
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class CashVo{
	@ApiModelProperty(value = "编号",hidden = false)
	private String serialNumber;
	@ApiModelProperty(value = "收款人",hidden = false)
	private String name;
	@ApiModelProperty(value = "现金金额",hidden = false)
	private BigDecimal money;
}

/**
 * 划拨
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class AllocatedVo{
	@ApiModelProperty(value = "预算单位",hidden = false)
	private String unitName;
	@ApiModelProperty(value = "科目",hidden = false)
	private String subjectName;
	@ApiModelProperty(value = "划拨金额",hidden = false)
	private BigDecimal money;
}

/**
 * 冲账
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class PaymenyVo{
	@ApiModelProperty(value = "预算单位",hidden = false)
	private String name;
	@ApiModelProperty(value = "借款类型",hidden = false)
	private String lendType;
	@ApiModelProperty(value = "欠款金额",hidden = false)
	private BigDecimal lendMoney;
	@ApiModelProperty(value = "冲账金额",hidden = false)
	private BigDecimal money;
}
