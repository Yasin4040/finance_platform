package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/15
 */
@Data
public class ExtractPreparePayDTO {

	@ApiModelProperty(value = "支付模板类型（见通用接口）",required = true)
	@NotNull(message = "支付模板类型不能为空")
	private Integer payTemplateType;

	@ApiModelProperty(value = "付款单id列表",required = true)
	@NotEmpty(message = "请选择付款单")
	private List<Long> payMoneyIds;
}
