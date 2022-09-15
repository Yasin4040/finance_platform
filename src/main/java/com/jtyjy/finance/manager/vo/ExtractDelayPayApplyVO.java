package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 描述：<p>延期支付申请单</p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Data
public class ExtractDelayPayApplyVO {

	@ApiModelProperty("部门")
	private String unitName;

	@ApiModelProperty("单据日期")
	@JsonFormat(pattern = "yyyy年MM月dd日",timezone = "GMT+8")
	private Date orderDate;

	@ApiModelProperty("支付事由")
	private String payReason;

	@ApiModelProperty("提成单号")
	private String extractCode;

	@ApiModelProperty("待付批次")
	private Integer batch;

	@ApiModelProperty("发放明细")
	private List<ExtractDelayPayApplyPayDetail> payDetailList;

	@ApiModelProperty("付款明细")
	private List<ExtractDelayPayApplyPayMoneyDetail> payMoneyDetailList;

	@ApiModelProperty("合计发放金额")
	private BigDecimal payTotal;

	@Data
	public static class ExtractDelayPayApplyPayDetail{
		@ApiModelProperty("工号")
		private String empNo;

		@ApiModelProperty("姓名")
		private String empName;

		@ApiModelProperty("个体户名称/户名")
		private String accountName;

		@ApiModelProperty("账户类型")
		private String userType;

		@ApiModelProperty("当期发放金额")
		private BigDecimal money;
	}

	@Data
	public static class ExtractDelayPayApplyPayMoneyDetail{
		@ApiModelProperty("付款单位")
		private String billingUnitName;

		@ApiModelProperty("员工发放金额（提成发放）")
		private BigDecimal payMoney = BigDecimal.ZERO;

		@ApiModelProperty("员工发放金额（费用发放）")
		private BigDecimal fee = BigDecimal.ZERO;

		@ApiModelProperty("员工个体户发放金额（公户）")
		private BigDecimal personalityPayMoney1 = BigDecimal.ZERO;

		@ApiModelProperty("员工个体户发放金额（个卡）")
		private BigDecimal personalityPayMoney2 = BigDecimal.ZERO;
	}
}
