package com.jtyjy.finance.manager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/13
 */
@Data
public class ExtractPersonalityQueryVO {

	@ApiModelProperty("员工个体户id")
	@NotNull(message = "员工个体户id不能为空")
	private Long personalityId;

	@ApiModelProperty("提成批次(导航栏批次)")
	@NotBlank(message = "请选择批次")
	private String query;

	@ApiModelProperty("发放单位id")
	@NotNull(message = "发放单位不能为空")
	private Long billingUnitId;

	@ApiModelProperty("当期提成发放金额")
	private BigDecimal curExtract = BigDecimal.ZERO;

	@ApiModelProperty("当期工资发放金额")
	private BigDecimal curSalary = BigDecimal.ZERO;

	@ApiModelProperty("当期福利费发放金额")
	private BigDecimal curWelfare = BigDecimal.ZERO;

}
