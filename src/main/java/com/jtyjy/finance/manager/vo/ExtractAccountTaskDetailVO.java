package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Data
public class ExtractAccountTaskDetailVO {

	@ApiModelProperty("工号")
	private String empNo;

	@ApiModelProperty("姓名")
	private String empName;

	@ApiModelProperty("个体户名称/户名")
	private String accountName;

	@ApiModelProperty("实发金额")
	private BigDecimal money;

	@ApiModelProperty("收款人")
	private String receiver;

	@ApiModelProperty("收款银行")
	private String receiverBank;

	@ApiModelProperty("收款账号")
	private String bankAccount;

	@ApiModelProperty("付款单位")
	private String unitName;

	@ApiModelProperty(value="发放状态",hidden = true)
	private Integer payStatus;

	@ApiModelProperty("发放状态")
	private String payStatusName;

	@ApiModelProperty(value="个体户id",hidden = true)
	private Long personalityId;

	@ApiModelProperty(value="账户类型",hidden = true)
	private Integer accountType;
}
