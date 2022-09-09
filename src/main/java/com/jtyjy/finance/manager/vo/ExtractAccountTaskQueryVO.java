package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/8
 */
@Data
public class ExtractAccountTaskQueryVO {

	@ApiModelProperty(value = "单号", dataType = "String", required = false)
	private String orderNo;
	@ApiModelProperty("届别id")
	private Long yearPeriodId;
	@ApiModelProperty("月id,用申请报销里面的查询月份那个接口")
	private Long monthPeriodId;
	@ApiModelProperty(value = "提成批次", name = "extractBatch", dataType = "String", required = false)
	private String extractBatch;
	@ApiModelProperty(value = "部门", name = "deptName", dataType = "String", required = false)
	private String deptName;
	@ApiModelProperty(value = "做账单位", name = "unitName", dataType = "String", required = false)
	private String billingUnitName;
	@ApiModelProperty(value = "是否看历史数据(false:否 true:是)", name = "isHistory", dataType = "Boolean", required = true)
	private Boolean isHistory;
	@ApiModelProperty(value = "单据状态（详见Swagger通用接口【获取提成状态列表】）")
	private Integer status;
	@ApiModelProperty(value = "当前登录人工号",hidden = false)
	private String empNo;
}
