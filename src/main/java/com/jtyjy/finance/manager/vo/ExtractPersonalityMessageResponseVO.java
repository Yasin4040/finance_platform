package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/13
 */
@Data
public class ExtractPersonalityMessageResponseVO {

	@ApiModelProperty("累计已发")
	private BigDecimal moneySum;

	@ApiModelProperty("剩余票额")
	private BigDecimal remainingInvoices;

	@ApiModelProperty("当期待发提成")
	private BigDecimal curExtract;

	@ApiModelProperty("剩余发放限额")
	private BigDecimal remainingPayLimitMoney;

	@ApiModelProperty("累计交票")
	private BigDecimal receiptSum;

	@ApiModelProperty("累计已发提成")
	private BigDecimal extractSum;

	@ApiModelProperty("累计已发工资")
	private BigDecimal salarySum;

	@ApiModelProperty("累计已发福利")
	private BigDecimal welfareSum;

}
