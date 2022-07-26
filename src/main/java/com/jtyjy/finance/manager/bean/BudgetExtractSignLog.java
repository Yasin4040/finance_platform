package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 提成签收日志
 */
@TableName(value = "budget_extract_sign_log")
@Data
public class BudgetExtractSignLog {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "id")
	private Long id;

	@TableField(value = "extract_month")
	@ApiModelProperty(value = "提成批次")
	private String extractMonth;

	@TableField(value = "empno")
	@ApiModelProperty(value = "接收人工号")
	private String empNo;

	@TableField(value = "empname")
	@ApiModelProperty(value = "姓名")
	private String empName;

	@TableField(value = "create_time")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
}
