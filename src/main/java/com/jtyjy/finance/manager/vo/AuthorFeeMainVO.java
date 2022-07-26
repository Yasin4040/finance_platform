package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 稿费主表VO
 * @author User
 *
 */
@Data
@NoArgsConstructor
public class AuthorFeeMainVO {
	
	@ApiModelProperty(value = "id")
	private Long id;
	
	@ApiModelProperty(value = "稿费单号")
	private String code;
	
	@ApiModelProperty(value = "届别")
	private String yearPeriod;
	
	@ApiModelProperty(value = "单据状态")
	private Integer status;
	
	@ApiModelProperty(value = "稿费月份")
	private String feeMonth;
	
	@ApiModelProperty(value = "提报部门")
	private String feeDeptName;
	
	@ApiModelProperty(value = "记录条数")
	private Integer authorFeeNum;
	
	@ApiModelProperty(value = "总额")
	private BigDecimal totalAuthorFee;
	
	@ApiModelProperty(value = "稿费总额")
	private BigDecimal contributionFee;
	
	@ApiModelProperty(value = "外审外包总额")
	private BigDecimal externalauditFee;
	
	@ApiModelProperty(value = "待摊-稿费总额")
	private BigDecimal contributionFeeNext;
	
	@ApiModelProperty(value = "待摊-外审外包总额")
	private BigDecimal externalauditFeeNext;
	
	@ApiModelProperty(value = "计税总额")
	private BigDecimal needTaxTotal;
	
	@ApiModelProperty(value = "不计税总额")
	private BigDecimal noneedTaxTotal;
	
	@ApiModelProperty(value = "创建人")
	private String creatorName;
	
	@ApiModelProperty(value = "创建时间")
	private String createTime;
	
	@ApiModelProperty(value = "审批意见")
	private String remark;
}
