package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 提成批次的二维码
 */
@TableName(value = "budget_extract_qrcode")
@Data
public class BudgetExtractQrcode implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(value = "id")
	private Long id;

	@TableField(value = "extract_month")
	@ApiModelProperty(value = "提成批次")
	private String extractMonth;

	@TableField(value = "qrcode")
	@ApiModelProperty(value = "二维码 base64字符")
	private String qrcode;

	@TableField(value = "create_time")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
}
