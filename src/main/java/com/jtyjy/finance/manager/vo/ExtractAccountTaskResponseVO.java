package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Data
public class ExtractAccountTaskResponseVO {

	@ApiModelProperty("提成主表id")
	private Long sumId;

	@ApiModelProperty("做账任务id")
	private Long id;

	@ApiModelProperty("单据状态")
	private Integer status;

	@ApiModelProperty("单据状态")
	private String statusName;

	@ApiModelProperty("提成单号")
	private String code;

	@ApiModelProperty("届别")
	private String yearPeriod;

	@ApiModelProperty("月份")
	private String monthPeriod;

	@ApiModelProperty("批次")
	private String extractBatch;

	@ApiModelProperty("部门")
	private String unitName;

	@ApiModelProperty("做账单位")
	private String billingUnitName;

	@ApiModelProperty("发放金额")
	private BigDecimal payMoney;

	@ApiModelProperty("任务接收时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date createTime;

	@ApiModelProperty("实际做账人")
	private String accountant;

	@ApiModelProperty("做账时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date accountantTime;

	@ApiModelProperty("任务类型（1：提成支付申请单 3：延期支付申请单）")
	private Integer taskType;
}
