package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelProperty;
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
    @ExcelProperty(value = "*提成届别")
    private Long yearid;

    @ExcelProperty(value = "*提成类型")
    private String extractType;

    @ExcelProperty(value = "码洋")
    private BigDecimal totalPrice;

    @ExcelProperty(value = "本期回款")
    private BigDecimal currentCollection;
    @ExcelProperty(value = "低价")
    private BigDecimal floorPrice;
    @ExcelProperty(value = "结算提成")
    private BigDecimal settlementCommission;
    @ExcelProperty(value = "预留提成")
    private BigDecimal reservedCommission;

    @ExcelProperty(value = "申请提成")
    //申请提成 = 应发提成
    private BigDecimal shouldSendExtract;
    @ExcelProperty(value = "提成个税")
    private BigDecimal tax;

    @ExcelProperty(value = "返提成个税")
    private BigDecimal returnCommissionIncomeTax;

    @ExcelProperty(value = "扣往届扎帐成本")
    private BigDecimal deductTheCostPreviousAccounts;

    @ExcelProperty(value = "扣发票超额税金")
    private BigDecimal deductExcessTaxOnInvoice;

    @ExcelProperty(value = "返发票超额税金")
    private BigDecimal refundExcessTaxInvoice;


    @ExcelProperty(value = "扣退货品承担")
    private BigDecimal dutyWithholdingReturningGoods;
    @ExcelProperty(value = "往来扣款")
    private BigDecimal currentDeduction;
    @ExcelProperty(value = "扣担保")
    private BigDecimal deductionGuarantee;
    @ExcelProperty(value = "扣征信")
    private BigDecimal deductCreditInformation;
    @ExcelProperty(value = "扣款小计")
    private BigDecimal subtotalOfDeduction;
    @ExcelProperty(value = "实发金额")
    //实发金额 = 实发提成
    private BigDecimal copeextract;

//
//    private BigDecimal consotax;
//
//    @ApiModelProperty(hidden = false, value = "个税(2021-12月新增)")
//    @TableField(value = "tax")
//    private BigDecimal tax = BigDecimal.ZERO;


}
