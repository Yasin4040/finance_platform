package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Data
public class BudgetExtractPayQueryVO {

	@ApiModelProperty(value = "付款单位")
	private String billingUnitName;

	@ApiModelProperty(value = "部门")
	private String deptName;

	@ApiModelProperty(value = "银行类型")
	private String bankAccountBranchName;

	@ApiModelProperty(value = "批次")
	private String extractBatch;

	@ApiModelProperty(value = "提成单号")
	private String extractCode;

	@ApiModelProperty(value = "收款人名称")
	private String bankAccountName;
}
