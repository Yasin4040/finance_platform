package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/1
 */
@Data
public class ExtractPersonalityPayDetailQueryVO {

	@ApiModelProperty("批次")
	private String batch;

	@ApiModelProperty("部门")
	private String firstDept;

	@ApiModelProperty("省区/大区")
	private String secondDept;

	@ApiModelProperty("工号")
	private String empNo;

	@ApiModelProperty("姓名")
	private String empName;

	@ApiModelProperty("个体户名称/户名")
	private String personalityName;

	@ApiModelProperty("账户类型(1个卡 2 公户)")
	private Integer userType;

	@ApiModelProperty("发放状态。1：正常 2：调账 3：延期")
	private Integer payStatus;

	@ApiModelProperty("发放单位")
	private String unitName;

	@ApiModelProperty("剩余票额")
	private String remainingInvoices;

	@ApiModelProperty("剩余发放限额")
	private String remainingPayLimitMoney;

	@ApiModelProperty("提成Id")
	private Long sumId;

	@ApiModelProperty("导航栏查询条件")
	private String query;

}
