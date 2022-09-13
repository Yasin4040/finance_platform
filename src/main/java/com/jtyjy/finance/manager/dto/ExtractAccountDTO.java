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
 * @since 2022/9/9
 */
@Data
public class ExtractAccountDTO {

	@ApiModelProperty(value = "提成单号")
	@NotNull(message = "提成单号不能为空")
	private String extractCode;

	@ApiModelProperty(value = "开票单位id列表")
	@NotEmpty(message = "开票单位不能为空")
	private List<Long> billingUnitIdList;
}
