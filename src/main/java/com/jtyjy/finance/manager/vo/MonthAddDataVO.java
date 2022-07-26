package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description="月度追加VO")
@Data
public class MonthAddDataVO {	
	
	@ApiModelProperty(value="科目")
	private String subjectName;
	
	@ApiModelProperty(value="月度科目累计追加")
	private BigDecimal monthSubjectAddMoney;
	
	@ApiModelProperty(value="流程月度科目累计追加")
	private BigDecimal processMonthSubjectAddMoney;
	
	@ApiModelProperty(value="动因名称")
	private String agentName;
	
	@ApiModelProperty(value="月度动因id")
	private Long monthAgentId;
	
	@ApiModelProperty(value="动因累计追加")
	private BigDecimal agentAddMoney;
	
	@ApiModelProperty(value="流程动因累计追加")
	private BigDecimal processAgentAddMoney;
	
	@ApiModelProperty(value="是否显示处理按钮")
	private Boolean isShowHandleButton = false;
	
	
}
