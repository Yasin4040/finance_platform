package com.jtyjy.finance.manager.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 导入明细
 * @TableName budget_extract_importdetail
 */
@TableName(value ="budget_extract_importdetail")
@Data
public class BudgetExtractImportdetail implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提成主表id
     */
    @TableField(value = "extractsumid")

    private Long extractsumid;

    /**
     * 提成明细id
     */
    @TableField(value = "extractdetailid")
    private Long extractdetailid;

    /**
     * 届别
     */
    @TableField(value = "yearid")
//    @ExcelProperty(value = "届别")
    private Long yearid;

    /**
     * 是否是公司员工
     */
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 业务类型
     */
    @TableField(value = "business_type")
    private Integer businessType;

    /**
     * 是否是坏账
     */
    @TableField(value = "isbaddebt")
    private Boolean isbaddebt;

    /**
     * 
     */
    @TableField(value = "empid")
    private String empid;

    /**
     * 
     */
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 工号
     */
    @TableField(value = "empno")
    private String empno;

    /**
     * 名称
     */
    @TableField(value = "empname")
    private String empname;

    /**
     * 
     */
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 
     */
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 提成类型(2021-12月新增)
提成类型(2022-8月新增 两个状态)

   “1期间提成”、 “2扎账总提成”、“3扎帐后提成”、“4坏账明细”、“5绩效奖提成”、“6预提绩效奖”
     */
    @TableField(value = "extract_type")
    private String extractType;

    /**
     * 员工个体户id
     */
    @TableField(value = "individual_employee_id")
    private Long individualEmployeeId;

    /**
     * 码洋
     */
    @TableField(value = "total_price")
    private BigDecimal totalPrice;

    /**
     * 实洋
     */
    @TableField(value = "actual_price")
    private BigDecimal actualPrice;

    /**
     * 回款
     */
    @TableField(value = "collection")
    private BigDecimal collection;

    /**
     * 收入
     */
    @TableField(value = "income")
    private BigDecimal income;

    /**
     * 在职帮离职回款成本
     */
    @TableField(value = "help_collection_host")
    private BigDecimal helpCollectionHost;

    /**
     * 到款剥离
     */
    @TableField(value = "stripping_received_funds")
    private BigDecimal strippingReceivedFunds;

    /**
     * 常规提成
     */
    @TableField(value = "regular_commission")
    private BigDecimal regularCommission;

    /**
     * 接手提成
     */
    @TableField(value = "take_over_the_commission")
    private BigDecimal takeOverTheCommission;

    /**
     * 特价提成
     */
    @TableField(value = "special_commission")
    private BigDecimal specialCommission;

    /**
     * 总提成
     */
    @TableField(value = "total_royalty")
    private BigDecimal totalRoyalty;

    /**
     * 已发提成
     */
    @TableField(value = "paid_commission")
    private BigDecimal paidCommission;

    /**
     * 预留提成
     */
    @TableField(value = "reserved_commission")
    private BigDecimal reservedCommission;

    /**
     * 应发提成(2021-12月新增)
申请提成
     */
    @TableField(value = "should_send_extract")
    private BigDecimal shouldSendExtract;

    /**
     * 提成个税
     */
    @TableField(value = "tax")
    private BigDecimal tax;

    /**
     * 返提成个税
     */
    @TableField(value = "tax_reduction")
    private BigDecimal taxReduction;

    /**
     * 综合税
     */
    @TableField(value = "consotax")
    private BigDecimal consotax;

    /**
     * 发票超额税金(2021-12月新增)
     */
    @TableField(value = "invoice_excess_tax")
    private BigDecimal invoiceExcessTax;

    /**
     * 返发票超额税金
     */
    @TableField(value = "invoice_excess_tax_reduction")
    private BigDecimal invoiceExcessTaxReduction;

    /**
     * 往届发票超额税金
     */
    @TableField(value = "excess_tax_previous_invoices")
    private BigDecimal excessTaxPreviousInvoices;

    /**
     * 滞纳金
     */
    @TableField(value = "late_fee")
    private BigDecimal lateFee;

    /**
     * 发货物流费
     */
    @TableField(value = "delivery_logistics_fee")
    private BigDecimal deliveryLogisticsFee;

    /**
     * 发件费用
     */
    @TableField(value = "shipping_cost")
    private BigDecimal shippingCost;

    /**
     * 发样成本
     */
    @TableField(value = "sample_issuing_cost")
    private BigDecimal sampleIssuingCost;

    /**
     * 退货物流费
     */
    @TableField(value = "return_logistics_fee")
    private BigDecimal returnLogisticsFee;

    /**
     * 退货成本
     */
    @TableField(value = "return_cost")
    private BigDecimal returnCost;

    /**
     * 铺货成本

     */
    @TableField(value = "distribution_cost")
    private BigDecimal distributionCost;

    /**
     * 分班打包费
     */
    @TableField(value = "shift_packing_fee")
    private BigDecimal shiftPackingFee;

    /**
     * 礼品费
     */
    @TableField(value = "gift_fee")
    private BigDecimal giftFee;

    /**
     * 坏账考核

     */
    @TableField(value = "bad_debt_assessment")
    private BigDecimal badDebtAssessment;

    /**
     * 未达标罚款
     */
    @TableField(value = "non_conformance_penalty")
    private BigDecimal nonConformancePenalty;

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
     * 业务员垫支

     */
    @TableField(value = "salesman_advance")
    private BigDecimal salesmanAdvance;

    /**
     * 其他类型 扣款
     */
    @TableField(value = "other_types_deduction")
    private BigDecimal otherTypesDeduction;

    /**
     * 扣款小计
     */
    @TableField(value = "subtotal_deduction")
    private BigDecimal subtotalDeduction;

    /**
     *实发金额
     */
    @TableField(value = "copeextract")
    private BigDecimal copeextract;

    /**
     * 更新人 工号
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 创建人 工号
     */
    @TableField(value = "create_by")
    private String createBy;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}