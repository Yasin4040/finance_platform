package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 非员工个体户的外部单位发放明细
 * @TableName budget_extractpayment_outer_unit
 */
@TableName(value ="budget_extractpayment_outer_unit")
@Data
public class BudgetExtractpaymentOuterUnit implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成批次
     */
    private String extractMonth;

    /**
     * 提成发放id
     */
    private Long extractPaymentId;

    /**
     * 发放单位id
     */
    private Long billingUnitId;

    private String billingUnitName;

    /**
     * 发放单位账户
     */
    private String unitBankAccount;

    /**
     * 电子联行号
     */
    private String branchcode;

    /**
     * 银行类型
     */
    private String bankName;

    /**
     * 开户行
     */
    private String openBank;

    /**
     * 发放金额
     */
    private BigDecimal payMoney;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}