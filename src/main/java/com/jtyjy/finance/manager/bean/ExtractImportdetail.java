package com.jtyjy.finance.manager.bean;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import java.util.Date;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

/**
* 导入明细
* @TableName budget_extract_importdetail
*/
public class ExtractImportdetail implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    @ApiModelProperty("")
    private Long id;
    /**
    * 提成主表id
    */
    @NotNull(message="[提成主表id]不能为空")
    @ApiModelProperty("提成主表id")
    private Long extractsumid;
    /**
    * 提成明细id
    */
    @ApiModelProperty("提成明细id")
    private Long extractdetailid;
    /**
    * 届别
    */
    @ApiModelProperty("届别")
    private Long yearid;
    /**
    * 是否是公司员工
    */
    @ApiModelProperty("是否是公司员工")
    private Boolean iscompanyemp;
    /**
    * 是否是坏账
    */
    @ApiModelProperty("是否是坏账")
    private Boolean isbaddebt;
    /**
    * 
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("")
    @Length(max= 255,message="编码长度不能超过255")
    private String empid;
    /**
    * 
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("")
    @Length(max= 255,message="编码长度不能超过255")
    private String idnumber;
    /**
    * 工号
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("工号")
    @Length(max= 255,message="编码长度不能超过255")
    private String empno;
    /**
    * 名称
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("名称")
    @Length(max= 255,message="编码长度不能超过255")
    private String empname;
    /**
    * 
    */
    @ApiModelProperty("")
    private Date createtime;
    /**
    * 
    */
    @ApiModelProperty("")
    private Date updatetime;
    /**
    * 提成类型(2021-12月新增)
提成类型(2022-8月新增 两个状态)

   “1期间提成”、 “2扎账总提成”、“3扎帐后提成”、“4坏账明细”、“5绩效奖提成”、“6预提绩效奖”
    */
    @Size(max= 255,message="编码长度不能超过255")

    private String extractType;
    /**
    * 员工个体户id
    */
    @ApiModelProperty("员工个体户id")
    private Long individualEmployeeId;
    /**
    * 码洋
    */
    @ApiModelProperty("码洋")
    private BigDecimal totalPrice;
    /**
    * 实洋
    */
    @ApiModelProperty("实洋")
    private BigDecimal actualPrice;
    /**
    * 回款
    */
    @ApiModelProperty("回款")
    private BigDecimal collection;
    /**
    * 收入
    */
    @ApiModelProperty("收入")
    private BigDecimal income;
    /**
    * 在职帮离职回款成本
    */
    @ApiModelProperty("在职帮离职回款成本")
    private BigDecimal helpCollectionHost;
    /**
    * 到款剥离
    */
    @ApiModelProperty("到款剥离")
    private BigDecimal strippingReceivedFunds;
    /**
    * 常规提成
    */
    @ApiModelProperty("常规提成")
    private BigDecimal regularCommission;
    /**
    * 接手提成
    */
    @ApiModelProperty("接手提成")
    private BigDecimal takeOverTheCommission;
    /**
    * 特价提成
    */
    @ApiModelProperty("特价提成")
    private BigDecimal specialCommission;
    /**
    * 总提成
    */
    @ApiModelProperty("总提成")
    private BigDecimal totalRoyalty;
    /**
    * 已发提成
    */
    @ApiModelProperty("已发提成")
    private BigDecimal paidCommission;
    /**
    * 预留提成
    */
    @ApiModelProperty("预留提成")
    private BigDecimal reservedCommission;
    /**
    * 应发提成(2021-12月新增)
申请提成
    */

    @ApiModelProperty("应发提成(2021-12月新增")
    private BigDecimal shouldSendExtract;
    /**
    * 提成个税
    */
    @ApiModelProperty("提成个税")
    private BigDecimal tax;
    /**
    * 返提成个税
    */
    @ApiModelProperty("返提成个税")
    private BigDecimal taxReduction;
    /**
    * 综合税
    */
    @ApiModelProperty("综合税")
    private BigDecimal consotax;
    /**
    * 发票超额税金(2021-12月新增)
    */
    @ApiModelProperty("发票超额税金(2021-12月新增)")
    private BigDecimal invoiceExcessTax;
    /**
    * 返发票超额税金
    */
    @ApiModelProperty("返发票超额税金")
    private BigDecimal refundExcessTaxInvoice;
    /**
    * 往届发票超额税金
    */
    @ApiModelProperty("往届发票超额税金")
    private BigDecimal excessTaxPreviousInvoices;
    /**
    * 滞纳金
    */
    @ApiModelProperty("滞纳金")
    private BigDecimal lateFee;
    /**
    * 发货物流费
    */
    @ApiModelProperty("发货物流费")
    private BigDecimal deliveryLogisticsFee;
    /**
    * 发件费用
    */
    @ApiModelProperty("发件费用")
    private BigDecimal shippingCost;
    /**
    * 发样成本
    */
    @ApiModelProperty("发样成本")
    private BigDecimal sampleIssuingCost;
    /**
    * 退货物流费
    */
    @ApiModelProperty("退货物流费")
    private BigDecimal returnLogisticsFee;
    /**
    * 退货成本
    */
    @ApiModelProperty("退货成本")
    private BigDecimal returnCost;
    /**
    * 铺货成本

    */
    @ApiModelProperty("铺货成本 ")
    private BigDecimal distributionCost;
    /**
    * 分班打包费
    */
    @ApiModelProperty("分班打包费")
    private BigDecimal shiftPackingFee;
    /**
    * 礼品费
    */
    @ApiModelProperty("礼品费")
    private BigDecimal giftFee;
    /**
    * 坏账考核

    */
    @ApiModelProperty("坏账考核")
    private BigDecimal badDebtAssessment;
    /**
    * 未达标罚款
    */
    @ApiModelProperty("未达标罚款")
    private BigDecimal nonConformancePenalty;
    /**
    * 往来扣款
    */
    @ApiModelProperty("往来扣款")
    private BigDecimal currentDeduction;
    /**
    * 扣担保
    */
    @ApiModelProperty("扣担保")
    private BigDecimal deductionGuarantee;
    /**
    * 扣征信
    */
    @ApiModelProperty("扣征信")
    private BigDecimal deductCreditInformation;
    /**
    * 业务员垫支

    */
    @ApiModelProperty("业务员垫支 ")
    private BigDecimal salesmanAdvance;
    /**
    * 其他类型 扣款
    */
    @ApiModelProperty("其他类型 扣款")
    private BigDecimal otherTypesDeduction;
    /**
    * 扣款小计
    */
    @ApiModelProperty("扣款小计")
    private BigDecimal subtotalDeduction;
    /**
    * 实发提成
实发金额
    */
    @ApiModelProperty("实发提成 实发金额")
    private BigDecimal copeextract;
    /**
    * 更新人 工号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("更新人 工号")
    @Length(max= 20,message="编码长度不能超过20")
    private String updateBy;
    /**
    * 创建人 工号
    */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("创建人 工号")
    @Length(max= 20,message="编码长度不能超过20")
    private String createBy;


}
