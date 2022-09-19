package com.jtyjy.finance.manager.vo.application;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 11:08
 */
@Data
public class CommissionImportDetailPowerVO {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "*业务类型 提成类型")
    private String businessType;
    @ApiModelProperty(value = "*工号")
    private String empno;
    @ApiModelProperty(value = "*姓名")
    private String empname;

    @ApiModelProperty(value = "*坏账(是/否)")
    private String  ifBadDebt;

    @ApiModelProperty(value = "*提成类型")
    private String extractType;
    //    @ApiModelProperty(desc = "*提成届别")
//    private Long yearid;
    @ApiModelProperty(value = "*提成届别")
    private String yearName;
    @ApiModelProperty(value = "*提成届别Id")
    private String yearId;

    @ApiModelProperty(value = "码洋")
    private BigDecimal totalPrice;
    @ApiModelProperty(value = "实洋")
    private BigDecimal actualPrice;
    @ApiModelProperty(value = "回款")
    private BigDecimal collection;
    @ApiModelProperty(value = "收入")
    private BigDecimal income;


    @ApiModelProperty(value = "在职帮离职回款成本")
    private  BigDecimal helpCollectionHost;
    @ApiModelProperty(value = "到款剥离")
    private  BigDecimal strippingReceivedFunds;
    @ApiModelProperty(value = "常规提成")
    private BigDecimal regularCommission;
    @ApiModelProperty(value = "接手提成")
    private BigDecimal takeOverTheCommission;
    @ApiModelProperty(value = "特价提成")
    private BigDecimal specialCommission;



    @ApiModelProperty(value = "总提成")
    private  BigDecimal totalRoyalty;

    @ApiModelProperty(value = "已发提成")
    private BigDecimal paidCommission;
    @ApiModelProperty(value = "预留提成")
    private BigDecimal reservedCommission;
    @ApiModelProperty(value = "应发提成")
    private BigDecimal shouldSendExtract;

    //代收代缴款
    @ApiModelProperty(value = "提成个税")
    private  BigDecimal tax;
    @ApiModelProperty(value = "返提成个税")
    private BigDecimal taxReduction;
    @ApiModelProperty(value = "综合税")
    private BigDecimal consotax;
    @ApiModelProperty(value = "发票超额税金")
    private BigDecimal invoiceExcessTax;
    @ApiModelProperty(value = "返发票超额税金")
    private BigDecimal invoiceExcessTaxReduction;
    @ApiModelProperty(value = "往届发票超额税金")
    private BigDecimal excessTaxPreviousInvoices;

    //业务扣款
    @ApiModelProperty(value = "滞纳金")
    private  BigDecimal lateFee;
    @ApiModelProperty(value = "发货物流费")
    private BigDecimal deliveryLogisticsFee;
    @ApiModelProperty(value = "发件费用")
    private BigDecimal shippingCost;
    @ApiModelProperty(value = "发样成本")
    private BigDecimal sampleIssuingCost;
    @ApiModelProperty(value = "退货物流费")
    private BigDecimal returnLogisticsFee;
    //业务扣款--费用
    @ApiModelProperty(value = "退货成本")
    private  BigDecimal returnCost;
    @ApiModelProperty(value = "铺货成本")
    private BigDecimal distributionCost;
    @ApiModelProperty(value = "分班打包费")
    private BigDecimal shiftPackingFee;
    @ApiModelProperty(value = "礼品费")
    private BigDecimal giftFee;
    @ApiModelProperty(value = "坏账考核")
    private BigDecimal badDebtAssessment;
    @ApiModelProperty(value = "未达标罚款")
    private BigDecimal nonConformancePenalty;


    //其他罚款
    @ExcelProperty(value = "往来成本")
    private BigDecimal previousCost;
    @ApiModelProperty(value = "往来扣款")
    private BigDecimal currentDeduction;
    @ApiModelProperty(value = "扣担保")
    private BigDecimal deductionGuarantee;
    @ApiModelProperty(value = "扣征信")
    private BigDecimal deductCreditInformation;
    @ApiModelProperty(value = "业务员垫支")
    private BigDecimal salesmanAdvance;
    @ApiModelProperty(value = "其他类型扣款")
    private BigDecimal otherTypesDeduction;
    @ApiModelProperty(value = "扣款小计")
    private BigDecimal subtotalOfDeduction;
    @ApiModelProperty(value = "实发金额")
    private BigDecimal copeextract;

    @ApiModelProperty(value = "是否允许大区经理查看。")
    private Integer ifBigManager;
    @ApiModelProperty(value = "是否允许经理查看。")
    private Integer ifManager;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    @ApiModelProperty(value = "提成单号")
    private String extractCode;
    @ApiModelProperty(value = "是否允许大区经理查看。")
    private String ifBigManagerView;
    @ApiModelProperty(value = "是否允许经理查看。")
    private String ifManagerView;
    @ApiModelProperty(value = "月份")
    private String monthName;
    @ApiModelProperty(value = "客户类型--员工二级部门 完整名称")
    private String empDeptFullName;
    @ApiModelProperty(value = "提成批次")
    private String extractMonth;


}
