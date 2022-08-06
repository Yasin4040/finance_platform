package com.jtyjy.finance.manager.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jtyjy.core.anno.Select;
import com.jtyjy.core.format.StringDateFormat;
import com.jtyjy.core.format.anno.DatePatternAnno;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel
@Data
public class TabPayOrderVO {
	

	@ApiModelProperty(hidden = false,value="费用项目")
	private String feeItemId;


	@ApiModelProperty(hidden = false,value="缴费原因")
	private String payReason;

	//事件发生日期
	@ApiModelProperty(hidden = false,value="事件发生日期")
	private String occurTime;

	@ApiModelProperty(hidden = false,value="当事人类型 1：内部  0：外部")
	private Integer orderUserType;

	@ApiModelProperty(hidden = true,value = "工号")
	private String orderEmpNo;

	@ApiModelProperty(hidden = true,value = "次数")
	private Integer count;
}
