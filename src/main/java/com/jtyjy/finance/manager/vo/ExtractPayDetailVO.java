package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author minzhq
 * date 2021-4-25
 */
@ApiModel(description = "提成发放明细")
@Data
public class ExtractPayDetailVO {
	
	@ApiModelProperty(value="还款单id")
	private Long id;

	@ApiModelProperty(value="提成明细id",hidden = true)
	private String extractdetailids;

	@ApiModelProperty("提成批次")
	private String extractmonth;
			
	@ApiModelProperty("编号")
	private String empno;
	
	@ApiModelProperty("姓名")
	private String empname;		
	
	@ApiModelProperty("实发提成")
	private BigDecimal realextract;
	
	@ApiModelProperty("综合税")
	private BigDecimal consotax;
	
	@ApiModelProperty("还款金额")
	private BigDecimal repaymoney;

	@ApiModelProperty(value="提成发放单位1 id",hidden = true)
	private Long billingUnitId;

	@ApiModelProperty("提成发放单位1")
	private String billingUnitname;
	
	@ApiModelProperty("发放金额")
	private BigDecimal billingPaymoney;
	
	@ApiModelProperty("提成发放单位2")
	private String avoidBillingNnitname;
	
	@ApiModelProperty("发放金额")
	private BigDecimal avoidBillingPaymoney;

	@ApiModelProperty("费用发放")
	private BigDecimal beforeCalFee;

	@ApiModelProperty("超额费用发放")
	private BigDecimal payFee;
}
