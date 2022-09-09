package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractBillingUnitVO {

	@ApiModelProperty(value = "开票单位ID")
	private Long id;

	@ApiModelProperty(value = "名称")
	private String name;
}
