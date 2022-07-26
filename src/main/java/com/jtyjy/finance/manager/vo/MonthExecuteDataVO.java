package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description="月度执行VO")
@Data
public class MonthExecuteDataVO {	
	
	@ApiModelProperty(value="科目")
	private String subjectName;
	
	@ApiModelProperty(value="月度科目累计执行")
	private BigDecimal monthSubjectExecuteMoney;
	
	@ApiModelProperty(value="报销月度科目累计执行")
	private BigDecimal bxMonthSubjectExecuteMoney;
	
	@ApiModelProperty(value="动因名称")
	private String agentName;
	
	@ApiModelProperty(value="月度动因id")
	private Long monthAgentId;
	
	@ApiModelProperty(value="动因累计执行")
	private BigDecimal agentExecuteMoney;
	
	@ApiModelProperty(value="报销动因累计执行")
	private BigDecimal bxAgentExecuteMoney;
	
	@ApiModelProperty(value="是否显示处理按钮")
	private Boolean isShowHandleButton = false;
	
	
}
