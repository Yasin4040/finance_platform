package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author minzhq
 *
 */
@ApiModel
@Data
public class ExtractInfoVO {
	
	@ApiModelProperty(value = "id")
	private Long id;
	
	@ApiModelProperty(value = "提成单号")
	private String code;
	
	@ApiModelProperty(value = "审批状态")
	private Integer status;
	
	@ApiModelProperty(value = "审批状态名称")
	private String statusName;
	
	@ApiModelProperty(value = "届别")
	private String period;
	
	@ApiModelProperty(value = "审核人")
	private String verifyorname;
	
	@ApiModelProperty(value = "审核时间")
	private String verifytime;
	
	@ApiModelProperty(value = "应发提成")
	private BigDecimal totalCopeextract;
	
	@ApiModelProperty(value = "综合税")
	private BigDecimal totalConsotax;
	
	@ApiModelProperty(value = "预算单位")
	private String unitname;
	
	@ApiModelProperty(value = "提成批次")
	private String extractmonth;
	
	@ApiModelProperty(value = "发放人数")
	private Integer extractnum;
	
	@ApiModelProperty(value = "创建时间")
	private String createtime;
	
	@ApiModelProperty(value = "创建人")
	private String createorname;
	
	@ApiModelProperty(value = "备注")
	private String remark;
}
