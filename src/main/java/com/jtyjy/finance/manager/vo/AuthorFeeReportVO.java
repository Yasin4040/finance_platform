package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 稿费报表VO
 * @author minzhq
 */
@Data
@ApiModel("稿费报表VO")
public class AuthorFeeReportVO {
	@ApiModelProperty(value="id")
	private Long id;
	
	@ApiModelProperty(value="届别")
	private String yearperiod;
	
	@ApiModelProperty(value="稿费月份")
	private String feeMonth;
	
	@ApiModelProperty(value="税前合计")
	private BigDecimal copefeesum;
	
	@ApiModelProperty(value="个税合计")
	private BigDecimal taxsum;
	
	@ApiModelProperty(value="税后合计")
	private BigDecimal realfeesum;
	
	@ApiModelProperty(value="创建时间")
	private String createtime;
}
