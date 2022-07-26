package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-17 11:31
 */
@Data
public class BudgetContractLendExcelVO {

    @ApiModelProperty(value = "是否付清")
    private String repaymentStatusName;


    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款")
    private Integer payMoneyStatus;

    @ApiModelProperty(value = "员工工号")
    private String empNo;

    @ApiModelProperty(value = "借款人")
    private String empName;

    @ApiModelProperty(value = "借款金额")
    private BigDecimal lendMoney;

    @ApiModelProperty(value = "已还")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未还")
    private BigDecimal unRepaidMoney;

    @ApiModelProperty(value = "借款日期")
    private String lendDate;

    @ApiModelProperty(value = "计划还款日期")
    private String payPlanDate;


    @ApiModelProperty(value = "合同名称")
    private String contractName;

    @ApiModelProperty(value = "约定结算方式")
    private String agreeSumType;

    @ApiModelProperty(value = "借款说明")
    private String remark;



}
