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
public class BudgetContractLendVO {

    @ApiModelProperty(value = "借款Id")
    private Long id;

    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "员工工号")
    private String empNo;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

    @ApiModelProperty(value = "借款类型")
    private Integer lendType;

    @ApiModelProperty(value = "借款类型描述")
    private String lendTypeDesc;

    @ApiModelProperty(value = "借款金额")
    private BigDecimal lendMoney;

    @ApiModelProperty(value = "已还")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未还")
    private BigDecimal unRepaidMoney;

    @ApiModelProperty(value = "借款日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lendDate;

    @ApiModelProperty(value = "计划还款日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date payPlanDate;

    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(value = "说明")
    private String remark;

    @ApiModelProperty(value = "是否付清")
    private Boolean repaymentStatus;

    @ApiModelProperty(value = "合同名称")
    private String contractName;

    @ApiModelProperty(value = "约定结算方式")
    private String agreeSumType;

    @ApiModelProperty(value = "支付类型：1:现金; 2:转账; 3:报销")
    private Integer payType;

    @ApiModelProperty(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款")
    private Integer payMoneyStatus;

}
