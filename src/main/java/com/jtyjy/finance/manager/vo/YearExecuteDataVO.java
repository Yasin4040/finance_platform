package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description="年度执行VO")
@Data
public class YearExecuteDataVO {	
	
	@ApiModelProperty(value="科目")
	private String subjectName;
	
	@ApiModelProperty(value="年度科目累计执行")
	private BigDecimal yearSubjectExecuteMoney;
	
	@ApiModelProperty(value="报销年度科目累计执行")
	private BigDecimal bxYearSubjectExecuteMoney;
	
	@ApiModelProperty(value="动因名称")
	private String agentName;
	
	@ApiModelProperty(value="年度动因id")
	private Long yearAgentId;
	
	@ApiModelProperty(value="动因累计执行")
	private BigDecimal agentExecuteMoney;
	
	@ApiModelProperty(value="报销动因累计执行")
	private BigDecimal bxAgentExecuteMoney;
	
	@ApiModelProperty(value="是否显示处理按钮")
	private Boolean isShowHandleButton = false;
	
	
}
