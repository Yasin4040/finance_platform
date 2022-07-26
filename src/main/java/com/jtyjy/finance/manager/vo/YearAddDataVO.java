package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description="年度追加VO")
@Data
public class YearAddDataVO {	
	
	@ApiModelProperty(value="科目")
	private String subjectName;
	
	@ApiModelProperty(value="年度科目累计追加")
	private BigDecimal yearSubjectAddMoney;
	
	@ApiModelProperty(value="流程年度科目累计追加")
	private BigDecimal processYearSubjectAddMoney;
	
	@ApiModelProperty(value="动因名称")
	private String agentName;
	
	@ApiModelProperty(value="年度动因id")
	private Long yearAgentId;
	
	@ApiModelProperty(value="动因累计追加")
	private BigDecimal agentAddMoney;
	
	@ApiModelProperty(value="流程动因累计追加")
	private BigDecimal processAgentAddMoney;
	
	@ApiModelProperty(value="是否显示处理按钮")
	private Boolean isShowHandleButton = false;
	
	
}
