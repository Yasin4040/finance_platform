package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "提成签到日志首页")
@Data
public class BudgetExtractSignMain {

	@ApiModelProperty(value="提成批次")
	private String extractMonth;

	@ApiModelProperty(value="签到明细")
	private List<BudgetExtractSignDetail> signDetails;
}
