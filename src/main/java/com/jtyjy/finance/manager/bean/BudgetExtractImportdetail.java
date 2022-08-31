package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "budget_extract_importdetail")
@Data
public class BudgetExtractImportdetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成主表id
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractsumid")
    private Long extractsumid;

    /**
     * 提成明细id
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractdetailid")
    private Long extractdetailid;

    /**
     * 届别
     */
    @NotNull(message = "提成届别不能为空")
    @ApiModelProperty(hidden = false, value = "提成届别")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 是否是公司员工
     */
    @NotNull(message = "是否公司员工不能为空")
    @ApiModelProperty(hidden = false, value = "是否公司员工")
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 是否是坏账
     */
    @NotNull(message = "是否坏账不能为空")
    @ApiModelProperty(hidden = false, value = "是否坏账")
    @TableField(value = "isbaddebt")
    private Boolean isbaddebt;

    /**
     * 员工id
     */
    @NotBlank(message = "提成人员不能为空")
    @ApiModelProperty(hidden = false, value = "提成人员")
    @TableField(value = "empid")
    private String empid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 实发提成
     */
    @NotNull(message = "应发提成不能为空")
    @ApiModelProperty(hidden = false, value = "应发提成")
    @TableField(value = "copeextract")
    private BigDecimal copeextract;

    /**
     * 综合税
     */
    @NotNull(message = "综合税不能为空")
    @ApiModelProperty(hidden = false, value = "综合税")
    @TableField(value = "consotax")
    private BigDecimal consotax;


    @ApiModelProperty(hidden = false, value = "提成类型(2021-12月新增)")
    @TableField(value = "extract_type")
    private String extractType;

    @ApiModelProperty(hidden = false, value = "应发提成(2021-12月新增)")
    @TableField(value = "should_send_extract")
    private BigDecimal shouldSendExtract = BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "个税(2021-12月新增)")
    @TableField(value = "tax")
    private BigDecimal tax = BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "个税减免(2021-12月新增)")
    @TableField(value = "tax_reduction")
    private BigDecimal taxReduction= BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "发票超额税金(2021-12月新增)")
    @TableField(value = "invoice_excess_tax")
    private BigDecimal invoiceExcessTax= BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "发票超额税金减免(2021-12月新增)")
    @TableField(value = "invoice_excess_tax_reduction")
    private BigDecimal invoiceExcessTaxReduction= BigDecimal.ZERO;
    /**
     * 员工个体户id
     */
    @ApiModelProperty(value = "员工个体户id", hidden = false)
    @TableField(value = "individual_employee_id")
    private Long individualEmployeeId;
    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    //创建人 工号"
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;


    @TableField(value = "update_by")
    private String updateBy;
    //新增



    /**
     * 码洋
     */
    @TableField(value = "total_price")
    private BigDecimal totalPrice;

    /**
     * 本期回款
     */
    @TableField(value = "current_collection")
    private BigDecimal currentCollection;

    /**
     * 底价
     */
    @TableField(value = "floor_price")
    private BigDecimal floorPrice;

    /**
     * 结算提成
     */
    @TableField(value = "settlement_commission")
    private BigDecimal settlementCommission;

    /**
     * 预留提成
     */
    @TableField(value = "reserved_commission")
    private BigDecimal reservedCommission;

    /**
     * 返提成个税
     */
    @TableField(value = "return_commission_income_tax")
    private BigDecimal returnCommissionIncomeTax;

    /**
     * 扣往届扎帐成本
     */
    @TableField(value = "deduct_cost_previous_accounts")
    private BigDecimal deductCostPreviousAccounts;

    /**
     * 扣发票超额税金
     */
    @TableField(value = "deduct_excess_tax_invoice")
    private BigDecimal deductExcessTaxInvoice;

    /**
     * 返发票超额税金
     */
    @TableField(value = "refund_excess_tax_invoice")
    private BigDecimal refundExcessTaxInvoice;

    /**
     * 扣退货品承担
     */
    @TableField(value = "dutyHoldingReturningGoods")
    private BigDecimal dutyholdingreturninggoods;

    /**
     * 往来扣款
     */
    @TableField(value = "current_deduction")
    private BigDecimal currentDeduction;

    /**
     * 扣担保
     */
    @TableField(value = "deduction_guarantee")
    private BigDecimal deductionGuarantee;

    /**
     * 扣征信
     */
    @TableField(value = "deduct_credit_information")
    private BigDecimal deductCreditInformation;

    /**
     * 扣款小计
     */
    @TableField(value = "subtotal_deduction")
    private BigDecimal subtotalDeduction;
}
