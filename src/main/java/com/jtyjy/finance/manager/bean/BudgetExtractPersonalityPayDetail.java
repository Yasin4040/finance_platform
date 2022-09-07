package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailVO;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提成员工个体户发放明细
 *
 * @TableName budget_extract_personality_pay_detail
 */
@TableName(value = "budget_extract_personality_pay_detail")
@Data
public class BudgetExtractPersonalityPayDetail implements Serializable {
	/**
	 *
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 员工个体户id
	 */
	private Long personalityId;

	/**
	 * 提成批次
	 */
	private String extractMonth;

	/**
	 * 累计交票
	 */
	private BigDecimal receiptSum;

	/**
	 * 累计已发提成
	 */
	private BigDecimal extractSum;

	/**
	 * 当期待发放提成
	 */
	private BigDecimal curExtract;

	/**
	 * 当期提成发放金额
	 */
	private BigDecimal curRealExtract;

	/**
	 * 累计已发工资
	 */
	private BigDecimal salarySum;

	/**
	 * 当期工资发放金额
	 */
	private BigDecimal curSalary;

	/**
	 * 累计已发福利
	 */
	private BigDecimal welfareSum;

	/**
	 * 当期福利发放金额
	 */
	private BigDecimal curWelfare;

	/**
	 * 发放公司id
	 */
	private Long billingUnitId;

	/**
	 * 发放状态 1：正常 2：调账 3：延期
	 */
	private Integer payStatus;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;
	/**
	 * 确认完成/确认发放时间
	 */
	private Date operateTime;

	/**
	 * 是否发放 1：是
	 */
	private Boolean isSend;

	/**
	 * 是否是初始化数据 1：是
	 */
	private Boolean isInitData;
	/**
	 * 剩余票额
	 */
	private BigDecimal remainingInvoices;
	/**
	 * 剩余发放限额
	 */
	private BigDecimal remainingPayLimitMoney;


	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	public static BudgetExtractPersonalityPayDetail transfer(ExtractPersonalityPayDetailVO entity, BudgetExtractPersonalityPayDetail payDetail) {
		if (payDetail == null) {
			payDetail = new BudgetExtractPersonalityPayDetail();
			payDetail.setCreateTime(new Date());
			payDetail.setIsSend(false);
			payDetail.setIsInitData(false);
			payDetail.setUpdateTime(new Date());
		} else {
			payDetail.setId(entity.getId());
			payDetail.setUpdateTime(new Date());
		}
		payDetail.setPersonalityId(entity.getPersonalityId());
		payDetail.setCurRealExtract(entity.getCurExtract());
		payDetail.setCurSalary(entity.getCurSalary());
		payDetail.setCurWelfare(entity.getCurWelfare());
		payDetail.setBillingUnitId(entity.getBillingUnitId());
		payDetail.setPayStatus(entity.getPayStatus());
		payDetail.setCurExtract(entity.getExtract());
		return payDetail;
	}


	public void setCurRemainingInvoices(BudgetExtractPersonalityPayDetail payDetail, IndividualEmployeeFiles individualEmployeeFiles, BudgetBillingUnit budgetBillingUnit, List<IndividualEmployeeTicketReceiptInfo> individualEmployeeTicketReceiptInfoList) {

		// 若发放单位不是法人单位或者发放单位为法人单位但账户类型为个卡时，剩余票额、剩余发放限额不计算，默认为0；
		if ("0".equals(budgetBillingUnit.getBillingUnitType()) || ("1".equals(budgetBillingUnit.getBillingUnitType()) && individualEmployeeFiles.getAccountType() == 1)) {
			payDetail.setRemainingInvoices(BigDecimal.ZERO);
			payDetail.setRemainingPayLimitMoney(BigDecimal.ZERO);
		} else {
			payDetail.setRemainingInvoices(payDetail.getReceiptSum().subtract(payDetail.getExtractSum()).subtract(payDetail.getSalarySum()).subtract(payDetail.getWelfareSum()).subtract(payDetail.getCurRealExtract()).subtract(payDetail.getCurSalary()).subtract(payDetail.getCurWelfare()));

		}

	}


}