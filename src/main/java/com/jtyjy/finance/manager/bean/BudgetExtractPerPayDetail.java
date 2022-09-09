package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提成每笔发放明细表
 * @TableName budget_extract_per_pay_detail
 */
@TableName(value ="budget_extract_per_pay_detail")
@Data
public class BudgetExtractPerPayDetail implements Serializable {
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
     * 提成单号
     */
    private String extractCode;

    /**
     * 付款单位
     */
    private Long billingUnitId;

    /**
     * 付款单位账户
     */
    private String billingUnitAccount;

    /**
     * 付款单位账户电子银联号
     */
    private String billingUnitBranchCode;

    /**
     * 付款单位银行类型
     */
    private String billingUnitBankName;

    /**
     * 付款单位开户行
     */
    private String billingUnitOpenBank;

    /**
     * 付款单位名称
     */
    private String billingUnitName;

    /**
     * 发放金额
     */
    private BigDecimal payMoney;

    /**
     * 是否是公司员工
     */
    private Boolean isCompanyEmp;

    /**
     * 员工个体户id
     */
    private Long personalityId;

    /**
     * 收款人标识
     */
    private String receiverCode;

    /**
     * 收款人姓名
     */
    private String receiverName;

    /**
     * 收款人户名
     */
    private String receiverAccountName;

    /**
     * 收款人银行账号
     */
    private String receiverBankAccount;

    /**
     * 收款人银行账号电子银联号
     */
    private String receiverBankAccountBranchCode;

    /**
     * 收款人银行类型
     */
    private String receiveBankAccountBankName;

    /**
     * 收款人开户行
     */
    private String receiverOpenBank;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 发放状态 1：正常 2：调账 3：延期
     */
    private Integer payStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}