package com.jtyjy.finance.manager.mapper.response;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 月度动因预算和可执行信息
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonthAgentMoneyInfo {

	/**
	 * 界别主键
	 */
	@ApiModelProperty(value = "界别主键",hidden = false, required = true)
	@NotNull(message = "界别主键不能为空")
	private Long yearId;
	
	/**
	 * 月度主键
	 */
	@ApiModelProperty(value = "月度主键",hidden = false, required = true)
	@NotNull(message = "月度主键不能为空")
	private Long monthId;
	
	/**
	 * 预算单位主键
	 */
	@ApiModelProperty(value = "预算单位主键",hidden = false, required = true)
	@NotNull(message = "预算单位主键不能为空")
	private Long unitId;
	
	/**
	 * 预算单位名称
	 */
	@ApiModelProperty(value = "预算单位名称",hidden = false, required = false)
	private String unitName;
	
	/**
	 * 预算科目主键
	 */
	@ApiModelProperty(value = "预算科目主键",hidden = false, required = false)
	private String subjectId;
	
	/**
	 * 预算科目名称
	 */
	@ApiModelProperty(value = "预算科目名称",hidden = false, required = false)
	private String subjectName;
	
	/**
	 * 月度动因主键
	 */
	@ApiModelProperty(value = "月度动因主键",hidden = false, required = true)
	private Long monthAgentId;
	
	/**
	 * 月度动因名称
	 */
	@ApiModelProperty(value = "月度动因名称",hidden = false, required = false)
	private String monthAgentName;
	
	/**
	 * 科目月初预算
	 */
	@ApiModelProperty(value = "科目月初预算",hidden = false, required = false)
	private BigDecimal subjectMonthStartMoney;
	
	/**
	 * 科目本月可用
	 */
	@ApiModelProperty(value = "科目本月可用",hidden = false, required = false)
	private BigDecimal subjectMonthMoney;
	
	/**
	 * 动因年初预算
	 */
	@ApiModelProperty(value = "动因年初预算",hidden = false, required = false)
	private BigDecimal agentYearStartMoney;
	
	/**
	 * 动因年度可用
	 */
	@ApiModelProperty(value = "动因年度可用",hidden = false, required = false)
	private BigDecimal agentYearMoney;

	private Long yearAgentId;
}
