package com.jtyjy.finance.manager.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "提成扣款明细")
@Data
public class ExtractDeductionDetailVO {
	@ApiModelProperty(value = "工号")
    private String empno;
	
	@ApiModelProperty(value = "姓名")
    private String empname;
	
	@ApiModelProperty(value = "届别")
	private String period;
	
	@ApiModelProperty(value = "预算单位")
	private String unitname;
	
	@ApiModelProperty(value = "提成批次")
	private String extractmonth;
	
	@ApiModelProperty(value = "提成单号")
	private String code;
	
	@ApiModelProperty(value = "扣款项目")
	private String projectname;
	
	@ApiModelProperty(value = "扣款金额")
	private String repaymoney;
}
