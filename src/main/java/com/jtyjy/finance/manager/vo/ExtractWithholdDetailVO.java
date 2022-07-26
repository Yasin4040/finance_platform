package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author minzhq
 * date 2021-4-25
 */
@ApiModel(description = "提成扣款明细")
@Data
public class ExtractWithholdDetailVO {
	
	@ApiModelProperty("还款单id")
	private Long repaymoneyid;
	
	@ApiModelProperty("还款时间")
	private String repaytime;
	
	@ApiModelProperty("还款金额")
	private BigDecimal repaymoney;
	
	@ApiModelProperty("编号")
	private String empno;
	
	@ApiModelProperty("姓名")
	private String empname;
	
	@ApiModelProperty("借款类型")
	private Integer lendtype;
	
	@ApiModelProperty("当时借款金额")
	private BigDecimal curlendmoney;
	
	@ApiModelProperty("借款单号")
	private String lendmoneycode;
	
	@ApiModelProperty("还剩借款金额")
	private BigDecimal nowlendmoney;
}
