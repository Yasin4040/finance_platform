package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description="年度拆借VO")
@Data
public class YearLendDataVO {	
	
	@ApiModelProperty(value="科目")
	private String subjectName;
	
	@ApiModelProperty(value="动因名称")
	private String agentName;
	
	@ApiModelProperty(value="年度动因id")
	private Long yearAgentId;
	
	@ApiModelProperty(value="年度科目累计拆进")
	private BigDecimal yearSubjectLendinMoney;
	
	@ApiModelProperty(value="流程年度科目累计拆进")
	private BigDecimal processYearSubjectLendinMoney;
		
	@ApiModelProperty(value="动因累计拆进")
	private BigDecimal agentLendinMoney;
	
	@ApiModelProperty(value="流程动因累计拆进")
	private BigDecimal processAgentLendinMoney;
	
	
	@ApiModelProperty(value="年度科目累计拆出")
	private BigDecimal yearSubjectLendoutMoney;
	
	@ApiModelProperty(value="流程年度科目累计拆出")
	private BigDecimal processYearSubjectLendoutMoney;
		
	@ApiModelProperty(value="动因累计拆出")
	private BigDecimal agentLendoutMoney;
	
	@ApiModelProperty(value="流程动因累计拆出")
	private BigDecimal processAgentLendoutMoney;
	
	@ApiModelProperty(value="是否显示处理按钮")
	private Boolean isShowHandleButton = false;
	
}
