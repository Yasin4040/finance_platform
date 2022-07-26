package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 提成导入明细
 * 
 * @author minzhq
 */
@ApiModel
@Data
public class ExtractImportDetailVO {
	@ApiModelProperty(value = "id")
	private Long id;
	@ApiModelProperty(value = "届别id")
	private Long yearid;
	@ApiModelProperty(value = "届别")
	private String period;
	@ApiModelProperty(value = "是否公司员工 0否1是")
	private Boolean iscompanyemp;
	@ApiModelProperty(value = "是否坏账 0否1是")
	private Boolean isbaddebt;
	@ApiModelProperty(value = "人员id")
	private String empid;
	@ApiModelProperty(value = "人员编号")
	private String empno;
	@ApiModelProperty(value = "人员姓名")
	private String empname;
	@ApiModelProperty(value = "身份证号")
	private String idnumber;
	@ApiModelProperty(value = "应发提成")
	private BigDecimal copeextract;
	@ApiModelProperty(value = "综合税")
	private BigDecimal consotax;

	@ApiModelProperty(hidden = false, value = "提成类型")
	private String extractType;

	@ApiModelProperty(hidden = false, value = "应发提成")
	private BigDecimal shouldSendExtract;

	@ApiModelProperty(hidden = false, value = "个税")
	private BigDecimal tax;

	@ApiModelProperty(hidden = false, value = "个税减免")
	private BigDecimal taxReduction;

	@ApiModelProperty(hidden = false, value = "发票超额税金")
	private BigDecimal invoiceExcessTax;

	@ApiModelProperty(hidden = false, value = "发票超额税金减免")
	private BigDecimal invoiceExcessTaxReduction;
}
