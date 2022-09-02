package com.jtyjy.finance.manager.vo.application;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 提成导入明细
 * 
 * @author liziyao
 */
@ApiModel
@Data
public class ExtractImportDetailNewVO {
	/**
	 * 未知参数
	 */
	@ApiModelProperty("id")
	private Long id;

	/**
	 * 提成主表id
	 */
	@ApiModelProperty(value = "提成主表id")
	private Long extractsumid;

	/**
	 * 提成明细id
	 */
	@ApiModelProperty(value = "提成明细id")
	private Long extractdetailid;

	@ApiModelProperty(value = "提成届别不能为空")
	private Long yearid;

	@ApiModelProperty(value = "是否公司员工")
	private Boolean iscompanyemp;


	@ApiModelProperty(value = "是否公司员工")
	private Boolean isbaddebt;

	@ApiModelProperty(value = "提成人员")
	private String empid;

	@ApiModelProperty(value = "身份证号码")
	private String idnumber;

	@ApiModelProperty(value = "工号")
	private String empno;

	@ApiModelProperty(value = "姓名")
	private String empname;

	@ApiModelProperty(value = "实发提成")
	private BigDecimal copeextract;

	@ApiModelProperty(value = "提成个税")
	private BigDecimal consotax;

	@ApiModelProperty(value = "提成类型")
	private String extractType;

	@ApiModelProperty(value = "应发提成")
	private BigDecimal shouldSendExtract;

//	@ApiModelProperty(hidden = false, value = "个税(2021-12月新增)")
//	@ApiModelProperty(value = "个税(2021-12月新增)")
//	private BigDecimal tax = BigDecimal.ZERO;

//	@ApiModelProperty(hidden = false, value = "个税减免(2021-12月新增)")
//	@ApiModelProperty(value = "tax_reduction")
//	private BigDecimal taxReduction= BigDecimal.ZERO;
//
//	@ApiModelProperty(hidden = false, value = "发票超额税金(2021-12月新增)")
//	@ApiModelProperty(value = "invoice_excess_tax")
//	private BigDecimal invoiceExcessTax= BigDecimal.ZERO;
//
//	@ApiModelProperty(hidden = false, value = "发票超额税金减免(2021-12月新增)")
//	@ApiModelProperty(value = "invoice_excess_tax_reduction")
//	private BigDecimal invoiceExcessTaxReduction= BigDecimal.ZERO;

	@ApiModelProperty(value = "员工个体户id")
	private Long individualEmployeeId;

	@JsonFormat(pattern = "yyyy-MM-dd HH:hh:ss",timezone = "UTC+8")
	@ApiModelProperty(value = "创建时间")
	private Date createtime;

	//创建人 工号"
	@ApiModelProperty(value = "创建人")
	private String createBy;

	@JsonFormat(pattern = "yyyy-MM-dd HH:hh:ss",timezone = "UTC+8")
	@ApiModelProperty(value = "更新时间")
	private Date updatetime;

	@ApiModelProperty(value = "更新人")
	private String updateBy;
	//新增



	/**
	 * 码洋
	 */
	@ApiModelProperty(value = "码洋")
	private BigDecimal totalPrice;

	/**
	 * 本期回款
	 */
	@ApiModelProperty(value = "本期回款")
	private BigDecimal currentCollection;

	/**
	 * 底价
	 */
	@ApiModelProperty(value = "底价")
	private BigDecimal floorPrice;

	/**
	 * 结算提成
	 */
	@ApiModelProperty(value = "结算提成")
	private BigDecimal settlementCommission;

	/**
	 * 预留提成
	 */
	@ApiModelProperty(value = "预留提成")
	private BigDecimal reservedCommission;

	/**
	 * 返提成个税
	 */
	@ApiModelProperty(value = "返提成个税")
	private BigDecimal returnCommissionIncomeTax;

	/**
	 * 扣往届扎帐成本
	 */
	@ApiModelProperty(value = "扣往届扎帐成本")
	private BigDecimal deductCostPreviousAccounts;

	/**
	 * 扣发票超额税金
	 */
	@ApiModelProperty(value = "扣发票超额税金")
	private BigDecimal deductExcessTaxInvoice;

	/**
	 * 返发票超额税金
	 */
	@ApiModelProperty(value = "返发票超额税金")
	private BigDecimal refundExcessTaxInvoice;

	/**
	 * 扣退货品承担
	 */
	@ApiModelProperty(value = "扣退货品承担")
	private BigDecimal dutyholdingreturninggoods;

	/**
	 * 往来扣款
	 */
	@ApiModelProperty(value = "往来扣款")
	private BigDecimal currentDeduction;

	/**
	 * 扣担保
	 */
	@ApiModelProperty(value = "扣担保")
	private BigDecimal deductionGuarantee;

	/**
	 * 扣征信
	 */
	@ApiModelProperty(value = "扣征信")
	private BigDecimal deductCreditInformation;

	/**
	 * 扣款小计
	 */
	@ApiModelProperty(value = "扣款小计")
	private BigDecimal subtotalDeduction;
}
