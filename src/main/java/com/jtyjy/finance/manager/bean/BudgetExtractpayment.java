package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractpayment")
@Data
public class BudgetExtractpayment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractdetailids")
    private String extractdetailids;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgetextractpaydetailid")
    private Long budgetextractpaydetailid;

    /**
     * 开票单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitid1")
    private Long bunitid1;

    /**
     * 开票单位名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname1")
    private String bunitname1;

    /**
     * 开票单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount1")
    private String bunitbankaccount1;

    /**
     * 开票单位账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode1")
    private String bunitaccountbranchcode1;

    /**
     * 开票单位账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname1")
    private String bunitaccountbranchname1;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoney1")
    private BigDecimal paymoney1 = BigDecimal.ZERO;

    /**
     * 发放费用(计算之后)
     */
    @ApiParam(hidden = true)
    @TableField(value = "payfee")
    private BigDecimal payfee = BigDecimal.ZERO;

    /**
     * 费用发放（计算之前）
     */
    @TableField(value = "before_cal_fee")
    private BigDecimal beforeCalFee = BigDecimal.ZERO;

    /**
     * 开票单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitid2")
    private Long bunitid2;

    /**
     * 开票单位名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname2")
    private String bunitname2;

    /**
     * 开票单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount2")
    private String bunitbankaccount2;

    /**
     * 开票单位账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode2")
    private String bunitaccountbranchcode2;

    /**
     * 开票单位账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname2")
    private String bunitaccountbranchname2;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoney2")
    private BigDecimal paymoney2 = BigDecimal.ZERO;

    /**
     * 开票单位名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname3")
    private String bunitname3;

    /**
     * 开票单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount3")
    private String bunitbankaccount3;

    /**
     * 开票单位账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode3")
    private String bunitaccountbranchcode3;

    /**
     * 开票单位账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname3")
    private String bunitaccountbranchname3;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoney3")
    private BigDecimal paymoney3;

    /**
     * 开票单位名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname4")
    private String bunitname4;

    /**
     * 开票单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount4")
    private String bunitbankaccount4;

    /**
     * 开票单位账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode4")
    private String bunitaccountbranchcode4;

    /**
     * 开票单位账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname4")
    private String bunitaccountbranchname4;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoney4")
    private BigDecimal paymoney4;

    /**
     * 开票单位名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname5")
    private String bunitname5;

    /**
     * 开票单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount5")
    private String bunitbankaccount5;

    /**
     * 开票单位账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode5")
    private String bunitaccountbranchcode5;

    /**
     * 开票单位账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname5")
    private String bunitaccountbranchname5;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoney5")
    private BigDecimal paymoney5;

    /**
     * 银行账户 名称(户名)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountname")
    private String bankaccountname;

    /**
     * 银行账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 银行账户 - 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchcode")
    private String bankaccountbranchcode;

    /**
     * 银行账户 - 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchname")
    private String bankaccountbranchname;

    /**
     * 应补贴个税
     */
    @ApiParam(hidden = true)
    @TableField(value = "subsidytax")
    private BigDecimal subsidytax;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    @ApiParam(hidden = true)
    @TableField(value = "bankaccountopenbank")
    private String bankaccountopenbank;

}
