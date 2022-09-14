package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：<p>提成支付界面</p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Data
public class BudgetExtractPayResponseVO {

	@ApiModelProperty("付款单id")
	private Long id;
	@ApiModelProperty(value = "支付状态",hidden = true)
	private Integer payStatus;
	@ApiModelProperty(value = "付款状态名称")
	private String payStatusName;
	@ApiModelProperty(value = "任务接收时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date createTime;
	@ApiModelProperty(value = "付款单号")
	private String payMoneyCode;
	@ApiModelProperty(value = "提成单号")
	private String extractCode;
	@ApiModelProperty(value = "批次")
	private String extractBatch;
	@ApiModelProperty(value = "部门")
	private String deptName;
	@ApiModelProperty(value = "付款单位")
	private String billingUnitName;
	@ApiModelProperty(value = "付款金额")
	private BigDecimal payMoney;
	@ApiModelProperty(value = "收款人账号")
	private String bankAccount;
	@ApiModelProperty(value = "收款人名称")
	private String bankAccountName;
	@ApiModelProperty(value = "收方开户行")
	private String openBank;
	@ApiModelProperty(value = "电子联行号")
	private String bankAccountBranchCode;
	@ApiModelProperty(value = "省份")
	private String province;
	@ApiModelProperty(value = "城市")
	private String city;
	@ApiModelProperty(value = "银行类型")
	private String bankAccountBranchName;
	@ApiModelProperty(value = "支付人员")
	private String payer;
	@ApiModelProperty(value = "支付时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date payTime;
}
