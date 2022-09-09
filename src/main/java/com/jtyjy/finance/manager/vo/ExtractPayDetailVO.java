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
public class ExtractPayDetailVO implements Cloneable{
	
	@ApiModelProperty(value="id",hidden = true)
	private Long id1;

	@ApiModelProperty(value="提成明细id",hidden = true)
	private String extractdetailids;

	@ApiModelProperty("是否公司员工")
	private Boolean isCompanyEmp;

	@ApiModelProperty("提成批次")
	private String extractmonth;
			
	@ApiModelProperty("编号")
	private String empno;
	
	@ApiModelProperty("姓名")
	private String empname;

	@ApiModelProperty("身份证号")
	private String idnumber;

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
	
	@ApiModelProperty("发放金额1")
	private BigDecimal billingPaymoney;

	@ApiModelProperty(value="提成发放单位2 id",hidden = true)
	private Long avoidBillingUnitId;

	@ApiModelProperty("提成发放单位2")
	private String avoidBillingNnitname;
	
	@ApiModelProperty("发放金额2")
	private BigDecimal avoidBillingPaymoney;

	@ApiModelProperty("费用发放")
	private BigDecimal beforeCalFee;

	@ApiModelProperty(value="提成发放单位3 id",hidden = true)
	private Long outUnitId;

	@ApiModelProperty("提成发放单位3")
	private String outUnitName;

	@ApiModelProperty("发放金额3")
	private BigDecimal outUnitPayMoney;

	@ApiModelProperty("超额费用发放")
	private BigDecimal payFee;

	@ApiModelProperty(hidden = true)
	private Boolean isSelf = true;


	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
