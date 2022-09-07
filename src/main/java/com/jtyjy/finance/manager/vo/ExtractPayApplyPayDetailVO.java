package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：<p>提成支付申请单发放明细</p>
 *
 * @author minzhq
 * @since 2022/9/3
 */
@Data
public class ExtractPayApplyPayDetailVO {

	@ApiModelProperty("内部发放金额")
	private BigDecimal innerPayMoney;

	@ApiModelProperty("未发放金额")
	private BigDecimal unPayMoney;

	@ApiModelProperty("外部户发放金额")
	private BigDecimal outUnitPayMoney;

	@ApiModelProperty("发放明细列表")
	private List<ExtractUnitPayDetail> payDetails;

	@Data
	public static class ExtractUnitPayDetail{

		@ApiModelProperty("付款单位")
		private String billingUnitName;

		@ApiModelProperty("员工发放金额（提成发放）")
		private BigDecimal payMoney;

		@ApiModelProperty("员工发放金额（费用发放）")
		private BigDecimal fee;

		@ApiModelProperty("员工个体户发放金额（公户）")
		private BigDecimal personalityPayMoney1;

		@ApiModelProperty("员工个体户发放金额（个卡）")
		private BigDecimal personalityPayMoney2;

		@ApiModelProperty("合计")
		private BigDecimal total;
	}
}
