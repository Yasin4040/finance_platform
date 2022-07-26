package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 稿费明细VO
 * @author minzhq
 *
 */
@Data
public class AuthorFeeDetailVO {
	@ApiModelProperty(value = "id")
	private Long id;
	
	@ApiModelProperty(value = "是否扣税（1：是0： 否）")
	private Integer taxType;
	
	@ApiModelProperty(value = "报销科目")
	private String reimburseSubject;
	
	@ApiModelProperty(value = "邀稿内容及去向")
	private String context;
	
	@ApiModelProperty(value = "产品预算II类")
	private String monthAgentName;
	
	@ApiModelProperty(value = "作者")
	private String authorName;
	
	@ApiModelProperty(value = "身份证号")
	private String authorIdnumber;
	
	@ApiModelProperty(value = "纳税人识别号")
	private String taxpayerIdnumber;
	
	@ApiModelProperty(value = "作者单位")
	private String authorCompany;
	
	@ApiModelProperty(value = "作者类型")
	private String authorType;
	
	@ApiModelProperty(value = "收款银行省")
	private String authorProvince;
	
	@ApiModelProperty(value = "收款银行市")
	private String authorCity;
	
	@ApiModelProperty(value = "开户行")
	private String openBank;
	
	@ApiModelProperty(value = "银行账号")
	private String bankAccount;
	
	@ApiModelProperty(value = "稿件质量")
	private String paperQuality;
	
	@ApiModelProperty(value = "页码或份数")
	private String pageOrCopy;
	
	@ApiModelProperty(value = "稿酬标准")
	private BigDecimal feeStandard;
	
	@ApiModelProperty(value = "应发稿酬")
	private BigDecimal copeFee;
		
	@ApiModelProperty(value = "约稿老师")
	private String empname;
	
	@ApiModelProperty(value = "稿费所属部门")
	private String feebdgDept;
	
	@ApiModelProperty(value = "归属事业群")
	private String businessGroup;
	
	@ApiModelProperty(value = "是否需要转账(0否 1 是)")
	private Integer needzz;
	
}
