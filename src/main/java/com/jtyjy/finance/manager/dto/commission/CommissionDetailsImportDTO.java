package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/30.
 * Time: 15:32
 */
@Data
public class CommissionDetailsImportDTO {

//    /**
//     * 未知参数
//     */
//    @ApiParam(hidden = true)
//    @TableId(type = IdType.AUTO)
//    private Long id;
//
//    /**
//     * 提成主表id
//     */
//    @ApiParam(hidden = true)
//    @TableField(value = "extractsumid")
//    private Long extractsumid;
//
//    /**
//     * 提成明细id
//     */
//    @ApiParam(hidden = true)
//    @TableField(value = "extractdetailid")
//    private Long extractdetailid;

    @ExcelProperty(value = "*业务类型")
    private String businessType;
    @ExcelProperty(value = "*工号")
    private String empno;
    @ExcelProperty(value = "*姓名")
    private String empname;

    @ExcelProperty(value = "*坏账(是/否)")
    private String  ifBadDebt;

    @ExcelProperty(value = "*提成类型")
    private String extractType;
//    @ExcelProperty(value = "*提成届别")
//    private Long yearid;
    @ExcelProperty(value = "*提成届别")
    private String yearName;

    @ExcelProperty(value = "码洋")
    private BigDecimal totalPrice;
    @ExcelProperty(value = "实洋")
    private BigDecimal actualPrice;
    @ExcelProperty(value = "回款")
    private BigDecimal collection;
    @ExcelProperty(value = "收入")
    private BigDecimal income;


    @ExcelProperty(value = "在职帮离职回款成本")
    private  BigDecimal helpCollectionHost;
    @ExcelProperty(value = "到款剥离")
    private  BigDecimal strippingReceivedFunds;
    @ExcelProperty(value = "常规提成")
    private BigDecimal regularCommission;
    @ExcelProperty(value = "接手提成")
    private BigDecimal takeOverTheCommission;
    @ExcelProperty(value = "特价提成")
    private BigDecimal specialCommission;



    @ExcelProperty(value = "总提成")
    private  BigDecimal totalRoyalty;

    @ExcelProperty(value = "已发提成")
    private BigDecimal paidCommission;
    @ExcelProperty(value = "预留提成")
    private BigDecimal reservedCommission;
    @ExcelProperty(value = "应发提成")
    private BigDecimal shouldSendExtract;

    //代收代缴款
    @ExcelProperty(value = "提成个税")
    private  BigDecimal tax;
    @ExcelProperty(value = "返提成个税")
    private BigDecimal taxReduction;
    @ExcelProperty(value = "综合税")
    private BigDecimal consotax;
    @ExcelProperty(value = "发票超额税金")
    private BigDecimal invoiceExcessTax;
    @ExcelProperty(value = "返发票超额税金")
    private BigDecimal invoiceExcessTaxReduction;
    @ExcelProperty(value = "往届发票超额税金")
    private BigDecimal excessTaxPreviousInvoices;

    //业务扣款
    @ExcelProperty(value = "滞纳金")
    private  BigDecimal lateFee;
    @ExcelProperty(value = "发货物流费")
    private BigDecimal deliveryLogisticsFee;
    @ExcelProperty(value = "发件费用")
    private BigDecimal shippingCost;
    @ExcelProperty(value = "发样成本")
    private BigDecimal sampleIssuingCost;
    @ExcelProperty(value = "退货物流费")
    private BigDecimal returnLogisticsFee;
    //业务扣款--费用
    @ExcelProperty(value = "退货成本")
    private  BigDecimal returnCost;
    @ExcelProperty(value = "铺货成本")
    private BigDecimal distributionCost;
    @ExcelProperty(value = "分班打包费")
    private BigDecimal shiftPackingFee;
    @ExcelProperty(value = "礼品费")
    private BigDecimal giftFee;
    @ExcelProperty(value = "坏账考核")
    private BigDecimal badDebtAssessment;
    @ExcelProperty(value = "未达标罚款")
    private BigDecimal nonConformancePenalty;


    //其他罚款

    @ExcelProperty(value = "往来扣款")
    private BigDecimal currentDeduction;
    @ExcelProperty(value = "扣担保")
    private BigDecimal deductionGuarantee;
    @ExcelProperty(value = "扣征信")
    private BigDecimal deductCreditInformation;
    @ExcelProperty(value = "业务员垫支")
    private BigDecimal salesmanAdvance;
    @ExcelProperty(value = "其他类型扣款")
    private BigDecimal otherTypesDeduction;
    @ExcelProperty(value = "扣款小计")
    private BigDecimal subtotalOfDeduction;
    @ExcelProperty(value = "实发金额")
    private BigDecimal copeextract;

    @ExcelProperty(value="错误明细")
    private String errMsg;
}
