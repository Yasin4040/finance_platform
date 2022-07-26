package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 袁前兼
 * @Date 2021/6/21 13:47
 */
@Data
public class BudgetPayMoneyDetailVO {

    @ApiModelProperty(value = "合同Id")
    private Long contractId;

    @ApiModelProperty(value = "付款主键Id")
    private Long id;

    @ApiModelProperty(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款")
    private Integer payMoneyStatus;

    @ApiModelProperty(value = "付款单号")
    private String payMoneyCode;

    @ApiModelProperty(value = "批次号")
    private String payBatchCode;

    @ApiModelProperty(value = "支付类型：1:现金；2:转账；3:报销")
    private Integer payType;

    @ApiModelProperty(value = "付款金额")
    private BigDecimal payMoney;

    @ApiModelProperty(value = "单据类型")
    private Integer lendType;

    @ApiModelProperty(value = "单据类型描述")
    private String lendTypeDesc;

    @ApiModelProperty(value = "oa流程编号")
    private String requestCode;

    @ApiModelProperty(value = "付款对象id(报销单id,提成发放id,借款id,工资发放id,稿费id,资金挑拨id)")
    private Long payMoneyObjectId;

    @ApiModelProperty(value = "付款单类型：1：报销转账付款 2：提成发放付款 3：(日常)借款付款 4：资金调拨付款 5:项目现金付款 6:项目转账付款（借款）")
    private Integer payMoneyType;

    @ApiModelProperty(value = "付款单位Id")
    private Long bUnitId;

    @ApiModelProperty(value = "付款单位名称")
    private String bUnitName;

    @ApiModelProperty(value = "付款银行名称")
    private String bUnitAccountBranchName;

    @ApiModelProperty(value = "付款银行账号")
    private String bUnitBankAccount;

    @ApiModelProperty(value = "收款银行账号户名")
    private String bankAccountName;

    @ApiModelProperty(value = "收款银行账号")
    private String bankAccount;

    @ApiModelProperty(value = "收款银行名称")
    private String bankAccountBranchName;

    @ApiModelProperty(value = "开户行(收款)")
    private String openBank;

    @ApiModelProperty(value = "收款银行账户银联号")
    private String bankAccountBranchCode;

    @ApiModelProperty(value = "付款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

}
