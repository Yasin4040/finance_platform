package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提成费用发放明细(计算分公司之前)
 * @author minzhq
 * @since 2021-12-18
 */
@TableName(value = "budget_extract_fee_pay_detail")
@Data
public class BudgetExtractFeePayDetailBeforeCal {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "id")
	private Long id;

	@TableField(value = "extract_month")
	@ApiModelProperty(value = "提成批次")
	private String extractMonth;

	@TableField(value = "empno")
	@ApiModelProperty(value = "工号")
	private String empNo;

	@TableField(value = "empname")
	@ApiModelProperty(value = "姓名")
	private String empName;

	@TableField(value = "fee_pay")
	@ApiModelProperty(value = "费用发放")
	private BigDecimal feePay;

	@TableField(value = "creatorname")
	@ApiModelProperty(value = "创建人")
	private String creatorName;

	@TableField(value = "create_time")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
}
