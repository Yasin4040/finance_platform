package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class ExtractAvoidBankAccountVO {
	
	@ApiModelProperty(hidden = false,value = "开票单位id")
	private Long unitId;
	
	@ApiModelProperty(hidden = false,value = "避税发放单位账户id")
	private Long avoidUnitAccountId;
	
	@ApiModelProperty(hidden = false,value = "银行账号")
	private String bankaccount;
	
	@ApiModelProperty(hidden = false,value = "发放单位名称")
	private String billingUnitName;
}
